package cs652.j.codegen;

import cs652.j.codegen.model.*;
import cs652.j.parser.JBaseVisitor;
import cs652.j.parser.JParser;
import cs652.j.semantics.JClass;
//import cs652.j.semantics.JField;
import cs652.j.semantics.JField;
import cs652.j.semantics.JMethod;
import cs652.j.semantics.JPrimitiveType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.symtab.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.management.JMException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CodeGenerator extends JBaseVisitor<OutputModelObject> {
	public STGroup templates;
	public String fileName;

	public Scope currentScope;
	public JClass currentClass;

	public CFile cFile;

	public CodeGenerator(String fileName) {
		this.fileName = fileName;
		templates = new STGroupFile("cs652/j/templates/C.stg");
	}

	public CFile generate(ParserRuleContext tree) {
		cFile = (CFile)visit(tree);
		return cFile;
	}

	@Override
	public OutputModelObject visitFile(JParser.FileContext ctx){
		currentScope = ctx.scope;
		cFile = new CFile(fileName);
		cFile.main = (MainMethod)visit(ctx.main());
		for(JParser.ClassDeclarationContext classDef : ctx.classDeclaration()){
			OutputModelObject a = visit(classDef);
			cFile.addClass((ClassDef) a);
		}
		currentScope = currentScope.getEnclosingScope();
		return cFile;
	}

	@Override
	public OutputModelObject visitClassDeclaration(JParser.ClassDeclarationContext ctx) {
		currentScope = ctx.scope;
		currentClass = ctx.scope;
		ClassDef classDef = new ClassDef(currentClass);

		for(FieldSymbol sc : ctx.scope.getFields()){
			String varname = sc.getName();
			TypeSpec vartype;
			if(sc.getType() instanceof JPrimitiveType){
				vartype = new PrimitiveTypeSpec(sc.getType().getName());
			}
			else{
				vartype = new ObjectTypeSpec(sc.getType().getName());
			}
			classDef.fields.add((new VarDef(varname,vartype)));
		}

		Set<MethodSymbol> jMethods = currentClass.getMethods();
		for(MethodSymbol jMethod : jMethods){
			FuncName fm = new FuncName((JMethod)jMethod);
			fm.slotNumber = fm.method.getSlotNumber();
			//System.out.println("class:" + currentClass + "//fm.classname:" + fm.getClassName()+ "//fm.mathodname:" + fm.getMethodName());
			classDef.vtable.add(fm);
		}

		for(ParseTree child : ctx.classBody().children){
			OutputModelObject omo = visit(child);
//			if(omo instanceof VarDef){
//				classDef.fields.add((VarDef) omo);
//			}
			if(omo instanceof MethodDef){
				// omo instance of MethodDef
				classDef.methods.add((MethodDef) omo);
			}

		}
		currentScope = currentScope.getEnclosingScope();
		return classDef;
	}

	/*fileds decl in classdeclaration*/
	@Override
	public OutputModelObject visitFieldDeclaration(JParser.FieldDeclarationContext ctx) {
		return new VarDef(ctx.ID().getText(), (TypeSpec) visit(ctx.jType()));
	}

	@Override
	public OutputModelObject visitMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		currentScope = ctx.scope;
		FuncName funcName = new FuncName(ctx.scope);
		MethodDef methodDef = new MethodDef(funcName);
		if(ctx.jType()!=null){
			methodDef.returnType = (TypeSpec) visit(ctx.jType());
		}
		else{
			methodDef.returnType = new PrimitiveTypeSpec(ctx.scope.getType().getName());
		}

		if(ctx.formalParameters().formalParameterList()!=null){
			for(ParseTree fp : ctx.formalParameters().formalParameterList().formalParameter()){
				OutputModelObject omo = visit(fp);
				methodDef.args.add((VarDef) omo);
			}
		}
		methodDef.body = (Block) visit(ctx.methodBody());
		currentScope = currentScope.getEnclosingScope();
		return methodDef;
	}

	@Override
	public OutputModelObject visitMethodBody(JParser.MethodBodyContext ctx) {
		return visit(ctx.block());
	}

	@Override
	public OutputModelObject visitFormalParameter(JParser.FormalParameterContext ctx) {
		return new VarDef(ctx.ID().getText(), (TypeSpec) visit(ctx.jType()));
	}

	@Override
	public OutputModelObject visitMain(JParser.MainContext ctx){
		FuncName funcName = new FuncName(ctx.scope);
		MainMethod mainMethod = new MainMethod(funcName);
		mainMethod.returnType = new PrimitiveTypeSpec("int");
		mainMethod.args.add(new VarDef("argc", new PrimitiveTypeSpec("int")));
		mainMethod.args.add(new VarDef("*argv[]",new PrimitiveTypeSpec("char")));
		mainMethod.body = (Block)visit(ctx.block());
		return mainMethod;
	}

	@Override
	public OutputModelObject visitBlockStat(JParser.BlockStatContext ctx) {
		return visit(ctx.block());
	}

	@Override
	public OutputModelObject visitBlock(JParser.BlockContext ctx){
		currentScope = ctx.scope;
		Block block = new Block();
		for(JParser.StatementContext stat : ctx.statement()){
			OutputModelObject omo = visit(stat);
			if(omo instanceof VarDef){
				block.locals.add(omo);
			}
			else{
				block.instrs.add(omo);
			}
		}
		currentScope = currentScope.getEnclosingScope();
		return block;
	}

	//visitLocalVarStat call the visitLocalVariable to visit this alternative's child/children
	@Override
	public OutputModelObject visitLocalVarStat(JParser.LocalVarStatContext ctx){
		return visit(ctx.localVariableDeclaration());
	}

	@Override
	public OutputModelObject visitLocalVariableDeclaration(JParser.LocalVariableDeclarationContext ctx){
		String varname = ctx.ID().getText();
		TypeSpec type = (TypeSpec) visit(ctx.jType());
		return new VarDef(varname,type);
	}

	@Override
	public OutputModelObject visitCallStat(JParser.CallStatContext ctx) {
		return new CallStat(visit(ctx.expression()));
	}

	@Override
	public OutputModelObject visitMethodCall(JParser.MethodCallContext ctx) {
		String methodname = ctx.ID().getText();
		MethodCall methodCall = new MethodCall(methodname);
		Expr receiver;

		System.out.println("currentclass: "+ currentClass.getName());
		JClass jClass = (JClass) currentScope.resolve(currentClass.getName());

		TypeSpec returnType = new PrimitiveTypeSpec(ctx.type.getName());
		JMethod jMethod = (JMethod) jClass.resolveMethod(methodname);
		FuncName funcName = new FuncName(jMethod);
		String receiverclass = funcName.getClassName();
		ObjectTypeSpec receiverType= new ObjectTypeSpec(receiverclass);

		receiver = new VarRef("this", receiverType);

		TypeCast implicit = new TypeCast(receiver,receiverType);

		FuncPtrType fpt = new FuncPtrType(returnType);
		methodCall.fptrType = fpt;
		methodCall.args.add(implicit);
		fpt.argTypes.add(implicit.type);
		methodCall.receiver = receiver;
		methodCall.receiverType = receiverType;
		return methodCall;
	}

	@Override
	public OutputModelObject visitQMethodCall(JParser.QMethodCallContext ctx) {
		//VarRef receiver = (VarRef) visit(ctx.expression());
		Expr receiver = (Expr) visit(ctx.expression());
		TypeSpec receiverType;

		if(receiver instanceof FieldRef){
			System.out.println("receiver is field");
			receiverType = ((FieldRef) receiver).object.type;
		}
		else if(receiver instanceof ThisRef){
			System.out.println("receiver is this");
			receiverType = ((ThisRef) receiver).type;
		}
		else /*if(receiver instanceof VarRef)*/{
			System.out.println("receiver is var");
			receiverType = ((VarRef) receiver).vartype;
		}

		String className = ctx.expression().type.getName();
		String methodname = ctx.ID().getText();
		JClass jClass = (JClass) currentScope.resolve(className);
		JMethod jMethod = (JMethod) jClass.resolveMethod(methodname);
		FuncName funcName = new FuncName(jMethod);
		String receiverclass = funcName.getClassName();
		ObjectTypeSpec receiveclassType = new ObjectTypeSpec(receiverclass);

		TypeSpec returnType = new PrimitiveTypeSpec(ctx.type.getName());

		FuncPtrType fpt = new FuncPtrType(returnType);

		MethodCall methodCall = new MethodCall(methodname);

		//receiverType = receiveclassType;

		//TypeCast implicit = new TypeCast(receiver,receiverType);
		TypeCast implicit = new TypeCast(receiver,receiveclassType);
		methodCall.args.add(implicit);
		fpt.argTypes.add(implicit.type);
		if(ctx.expressionList()!=null){
			for(ParseTree a : ctx.expressionList().expression()){
				OutputModelObject vr = visit(a);
				TypeCast tc;
				if(vr instanceof LiteralRef){
					//tc = new TypeCast(((LiteralRef) vr).literal,null);
					tc = new TypeCast((Expr) vr,null);
					fpt.argTypes.add(((LiteralRef) vr).type);
					methodCall.args.add(tc);
				}
				else if(vr instanceof VarRef){
					tc = new TypeCast(((VarRef) vr),((VarRef) vr).vartype);
					fpt.argTypes.add(((VarRef) vr).vartype);
					methodCall.args.add(tc);
				}
				else if(vr instanceof CtorCall){
					TypeSpec ctorType = new ObjectTypeSpec(((CtorCall) vr).id);
					tc = new TypeCast((CtorCall) vr, ctorType);
					fpt.argTypes.add(((CtorCall) vr).type);
					methodCall.args.add(tc);
				}
			}
		}
		methodCall.fptrType = fpt;
		methodCall.receiver = receiver;
		methodCall.receiverType = receiverType;
		return methodCall;
	}

	@Override
	public OutputModelObject visitThisRef(JParser.ThisRefContext ctx) {
		TypeSpec thisType = new ObjectTypeSpec(ctx.type.getName());
		return new ThisRef("this", thisType);
	}

	@Override
	public OutputModelObject visitReturnStat(JParser.ReturnStatContext ctx) {
		return new ReturnStat((Expr) visit(ctx.expression()));
	}

	@Override
	public OutputModelObject visitIfStat(JParser.IfStatContext ctx) {
		Expr condition = (Expr) visit(ctx.parExpression());
		String cond = ctx.parExpression().getText();
		if(ctx.statement(1)==null){
			IfStat ifStat = new IfStat(cond,condition);
			ifStat.stat = (Stat) visit(ctx.statement(0));
			return ifStat;
		}
		else if(ctx.statement(1)!=null){
			IfElseStat ifElseStat = new IfElseStat(cond, condition);
			ifElseStat.stat = (Stat) visit(ctx.statement(0));
			ifElseStat.elseStat = (Stat) visit(ctx.statement(1));
			return ifElseStat;
		}
		else return null;
	}

	@Override
	public OutputModelObject visitWhileStat(JParser.WhileStatContext ctx) {
		Expr condition = (Expr) visit(ctx.parExpression());
		String cond = ctx.parExpression().getText();
		WhileStat whileStat = new WhileStat(cond,condition);
		whileStat.stat = (Block) visit(ctx.statement());
		return whileStat;
	}

	@Override
	public OutputModelObject visitJType(JParser.JTypeContext ctx) {
		String typename;
		if(ctx.ID()!= null){
			typename = ctx.ID().getText();
			return new ObjectTypeSpec(typename);
		}
		else if(ctx.getText().equals("int")){
			typename = "int";
			return new PrimitiveTypeSpec(typename);
		}
		else if(ctx.getText().equals("float")){
			typename = "float";
			return new PrimitiveTypeSpec(typename);
		}
		else{
			typename = "void";
			return new PrimitiveTypeSpec(typename);
		}
	}

	@Override
	public OutputModelObject visitAssignStat(JParser.AssignStatContext ctx) {
		Expr leftvar = (Expr) visit(ctx.expression(0));
		OutputModelObject rightvar = visit(ctx.expression(1));
		AssignStat assignStat = new AssignStat(leftvar,rightvar);
		return assignStat;
	}

	@Override
	public OutputModelObject visitIdRef(JParser.IdRefContext ctx) {
		String varname = ctx.ID().getText();
		Symbol var = currentScope.resolve(varname);
		if(var instanceof JField){
			varname = "this->" + varname;
		}
		TypeSpec vartype;
		if(ctx.type.getName().equals("int") ||ctx.type.getName().equals("float")|| ctx.type.getName().equals("float")){
			vartype = new PrimitiveTypeSpec(ctx.type.getName());
		}
		else{
			vartype = new ObjectTypeSpec(ctx.type.getName());
		}

		return new VarRef(varname,vartype);
	}

	@Override
	public OutputModelObject visitLiteralRef(JParser.LiteralRefContext ctx) {
		PrimitiveTypeSpec lt = new PrimitiveTypeSpec(ctx.type.getName());
		if(ctx.INT() != null){
			return new LiteralRef(ctx.INT().getText(),lt);
		}
		else{
			return new LiteralRef(ctx.FLOAT().getText(),lt);
		}
	}

	@Override
	public OutputModelObject visitFieldRef(JParser.FieldRefContext ctx) {
		String name = ctx.ID().getText();
		FieldRef fieldRef = new FieldRef(name);
		if(visit(ctx.expression())instanceof VarRef){
			fieldRef.object = (VarRef)visit(ctx.expression());
		}
		else if(visit(ctx.expression()) instanceof ThisRef){
			fieldRef.object = new ThisRef("this",new ObjectTypeSpec(ctx.expression().type.getName()));
		}
		else if(visit(ctx.expression()) instanceof MethodCall){
			fieldRef.object = (MethodCall) visit(ctx.expression());
		}
		else{
			fieldRef.object = (FieldRef) visit(ctx.expression());
		}

		return fieldRef;
	}

	@Override
	public OutputModelObject visitCtorCall(JParser.CtorCallContext ctx) {
		String ctorid = ctx.ID().getText();
		ObjectTypeSpec ctortype = new ObjectTypeSpec(ctx.type.getName());
		return new CtorCall(ctorid, ctortype);
	}

	@Override
	public OutputModelObject visitPrintStat(JParser.PrintStatContext ctx) {
		PrintStat printStat = new PrintStat(ctx.STRING().getText());
		for(JParser.ExpressionContext arg : ctx.expressionList().expression()){
			Expr a = (Expr) visit(arg);
			printStat.addArg(a);
		}
		return printStat;
	}

	@Override
	public OutputModelObject visitPrintStringStat(JParser.PrintStringStatContext ctx) {
		return new PrintStringStat(ctx.STRING().getText());
	}


}
