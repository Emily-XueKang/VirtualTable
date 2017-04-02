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
import org.antlr.symtab.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.List;

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

	public OutputModelObject visitFile(JParser.FileContext ctx){
		CFile file = new CFile(fileName);
		file.main = (MainMethod)visit(ctx.main());
		return file;
	}

	public OutputModelObject visitMain(JParser.MainContext ctx){
		MainMethod mainMethod = new MainMethod();
		mainMethod.body = (Block)visit(ctx.block());
		return mainMethod;
	}
	public OutputModelObject visitBlock(JParser.BlockContext ctx){
		Block block = new Block();
		for(JParser.StatementContext stat : ctx.statement()){
			OutputModelObject omo = visit(stat);
			block.locals.add(omo);
		}
		return block;
	}


	//visitLocalVarStat call the visitLocalVariable to visit this alternative's child/children
	public OutputModelObject visitLocalVarStat(JParser.LocalVarStatContext ctx){
		return visit(ctx.localVariableDeclaration());
	}
	public OutputModelObject visitLocalVariableDeclaration(JParser.LocalVariableDeclarationContext ctx){
		String varname = ctx.ID().getText();
		System.out.println(ctx.ID().getText());
		PrimitiveTypeSpec type = (PrimitiveTypeSpec)visit(ctx.jType());
		return new VarDef(varname,type);
	}

	@Override
	public OutputModelObject visitJType(JParser.JTypeContext ctx) {
		String typename;
		if(ctx.ID() != null){
			typename = ctx.ID().getText();
		}
		else if(ctx.getText().equals("int")){
			typename = "int";
		}
		else{
			typename = "float";
		}
		return new PrimitiveTypeSpec(typename);
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
		return new VarRef(varname);
	}

	@Override
	public OutputModelObject visitLiteralRef(JParser.LiteralRefContext ctx) {
		if(ctx.INT() != null){
			System.out.println(ctx.INT().getText());
			return new LiteralRef(ctx.INT().getText());
		}
		else{
			System.out.println(ctx.FLOAT().getText());
			return new LiteralRef(ctx.FLOAT().getText());
		}
	}

	@Override
	public OutputModelObject visitCtorCall(JParser.CtorCallContext ctx) {
		String ctorid = ctx.ID().getText();
		return new CtorCall(ctorid);
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

//	@Override
//	public OutputModelObject visitExpressionList(JParser.ExpressionListContext ctx) {
//		ArrayList<OutputModelObject> exprlist = new ArrayList<>();
//		for(JParser.ExpressionContext expr : ctx.expression()){
//			exprlist.add(visit(expr));
//		}
//		return ;
//	}
}
