package cs652.j.codegen;

//import cs652.j.codegen.model.AssignStat;
import cs652.j.codegen.model.*;
//import cs652.j.codegen.model.CallStat;
//import cs652.j.codegen.model.CtorCall;
//import cs652.j.codegen.model.Expr;
//import cs652.j.codegen.model.FieldRef;
//import cs652.j.codegen.model.FuncName;
//import cs652.j.codegen.model.IfElseStat;
//import cs652.j.codegen.model.IfStat;
//import cs652.j.codegen.model.LiteralRef;
//import cs652.j.codegen.model.MethodCall;
//import cs652.j.codegen.model.MethodDef;
//import cs652.j.codegen.model.NullRef;
//import cs652.j.codegen.model.ObjectTypeSpec;
//import cs652.j.codegen.model.PrintStat;
//import cs652.j.codegen.model.PrintStringStat;
//import cs652.j.codegen.model.ReturnStat;
//import cs652.j.codegen.model.Stat;
//import cs652.j.codegen.model.ThisRef;
//import cs652.j.codegen.model.TypeCast;
//import cs652.j.codegen.model.TypeSpec;
//import cs652.j.codegen.model.VarRef;
//import cs652.j.codegen.model.WhileStat;
import cs652.j.parser.JBaseVisitor;
import cs652.j.parser.JParser;
import cs652.j.semantics.JClass;
//import cs652.j.semantics.JField;
import cs652.j.semantics.JMethod;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.symtab.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import sun.reflect.generics.tree.TypeSignature;

import javax.management.JMException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CodeGenerator extends JBaseVisitor<OutputModelObject> {
	public STGroup templates;
	public String fileName;

	public Scope currentScope;
	public JClass currentClass;

	public CodeGenerator(String fileName) {
		this.fileName = fileName;
		templates = new STGroupFile("cs652/j/templates/C.stg");
	}

	public CFile generate(ParserRuleContext tree) {
		CFile file = (CFile)visit(tree);
		return file;
	}

	@Override
	public OutputModelObject visitFile(JParser.FileContext ctx){
		CFile file = new CFile(fileName);
		file.main = (MainMethod)visit(ctx.main());
		for(JParser.ClassDeclarationContext classDef : ctx.classDeclaration()){
			OutputModelObject a = visit(classDef);
			file.addClass((ClassDef) a);
		}
		return file;
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
	public OutputModelObject visitBlock(JParser.BlockContext ctx){
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
		System.out.println("lvdtype: "+type.name);
		return new VarDef(varname,type);
	}

	@Override
	public OutputModelObject visitCallStat(JParser.CallStatContext ctx) {
		return visit(ctx.expression());
	}

	@Override
	public OutputModelObject visitQMethodCall(JParser.QMethodCallContext ctx) {
		VarRef receiver = (VarRef) visit(ctx.expression());
		TypeSpec returnType = new PrimitiveTypeSpec(ctx.type.getName());
		FuncPtrType fpt = new FuncPtrType(returnType);

		MethodCall methodCall = new MethodCall();
		methodCall.name = ctx.ID().getText();

		TypeSpec receiverType = receiver.vartype;
		String receiverName = receiver.name;

		TypeCast implicit = new TypeCast(receiverName,receiverType);
		methodCall.agrs.add(implicit);
		for(ParseTree a : ctx.expressionList().expression()){
			OutputModelObject vr = visit(a);
			TypeCast tc;
			if(vr instanceof LiteralRef){
				tc = new TypeCast(((LiteralRef) vr).literal,null);
				fpt.argTypes.add(((LiteralRef) vr).type);
				methodCall.agrs.add(tc);
			}
			else if(vr instanceof VarRef){
				tc = new TypeCast(((VarRef) vr).name,((VarRef) vr).vartype);
				fpt.argTypes.add(((VarRef) vr).vartype);
				methodCall.agrs.add(tc);
			}
			else if(vr instanceof CtorCall){
				TypeSpec ctorType = new ObjectTypeSpec(((CtorCall) vr).id);
				tc = new TypeCast(((CtorCall) vr).id, ctorType);
				fpt.argTypes.add(((CtorCall) vr).type);
				methodCall.agrs.add(tc);
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
		//return new VarRef("this", thisType);
		return thisType;
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
		VarRef leftvar = (VarRef) visit(ctx.expression(0));
		OutputModelObject rightvar = visit(ctx.expression(1));
		AssignStat assignStat = new AssignStat(leftvar,rightvar);
		return assignStat;
	}

	@Override
	public OutputModelObject visitIdRef(JParser.IdRefContext ctx) {
		String varname = ctx.ID().getText();
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
	public OutputModelObject visitCtorCall(JParser.CtorCallContext ctx) {
		String ctorid = ctx.ID().getText();
		ObjectTypeSpec ctortype = new ObjectTypeSpec(ctx.type.getName());
		return new CtorCall(ctorid, ctortype);
	}

	@Override
	public OutputModelObject visitPrintStat(JParser.PrintStatContext ctx) {
		PrintStat printStat = new PrintStat(ctx.STRING().getText());
		for(JParser.ExpressionContext arg : ctx.expressionList().expression()){
			OutputModelObject a = visit(arg);
			printStat.addArg(a);
		}
		return printStat;
	}

	@Override
	public OutputModelObject visitPrintStringStat(JParser.PrintStringStatContext ctx) {
		return new PrintStringStat(ctx.STRING().getText());
	}

	@Override
	public OutputModelObject visitClassDeclaration(JParser.ClassDeclarationContext ctx) {
		currentClass = ctx.scope;
		ClassDef classDef = new ClassDef(currentClass);

		Set<MethodSymbol> jMethods = currentClass.getMethods();
		for(MethodSymbol jMethod : jMethods){
			FuncName fm = new FuncName((JMethod)jMethod);
			fm.slotNumber = fm.method.getSlotNumber();
			classDef.vtable.add(fm);
		}

		for(ParseTree child : ctx.classBody().children){
			OutputModelObject omo = visit(child);
			if(omo instanceof VarDef){
				classDef.fields.add((VarDef) omo);
			}
			else if(omo instanceof MethodDef){
				// omo instance of MethodDef
				classDef.methods.add((MethodDef) omo);
			}
		}
		return classDef;
	}

	@Override
	public OutputModelObject visitMethodDeclaration(JParser.MethodDeclarationContext ctx) {
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


	/*fileds decl in classdeclaration*/
	@Override
	public OutputModelObject visitFieldDeclaration(JParser.FieldDeclarationContext ctx) {
		return new VarDef(ctx.ID().getText(), (TypeSpec) visit(ctx.jType()));
	}

	//	@Override
//	public OutputModelObject visitExpressionList(JParser.ExpressionListContext ctx) {
//		ArrayList<OutputModelObject> exprlist = new ArrayList<>();
//		for(JParser.ExpressionContext expr : ctx.expression()){
//			exprlist.add(visit(expr));
//		}
//		return ;
//	}
}
