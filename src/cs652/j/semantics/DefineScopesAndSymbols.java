package cs652.j.semantics;

import cs652.j.parser.JBaseListener;
import cs652.j.parser.JParser;
import org.antlr.symtab.*;

public class DefineScopesAndSymbols extends JBaseListener {
	public Scope currentScope;
	public GlobalScope globalScope;
	public static final Type JINT_TYPE = new JPrimitiveType("int");
	public static final Type JFLOAT_TYPE = new JPrimitiveType("float");
	public static final Type JSTRING_TYPE = new JPrimitiveType("string");
	public static final Type JVOID_TYPE = new JPrimitiveType("void");

	public DefineScopesAndSymbols(GlobalScope globals) {
		currentScope = globals;
		globalScope = globals;
		currentScope.define((JPrimitiveType)JINT_TYPE);
		currentScope.define((JPrimitiveType)JFLOAT_TYPE);
		currentScope.define((JPrimitiveType)JSTRING_TYPE);
		currentScope.define((JPrimitiveType)JVOID_TYPE);
	}

	@Override
	public void enterFile(JParser.FileContext ctx) {
		ctx.scope = (GlobalScope) currentScope;
	}
	@Override
	public void exitFile(JParser.FileContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterMain(JParser.MainContext ctx) {
		Type type = ComputeTypes.JVOID_TYPE;
		JMethod main = new JMethod("main",ctx);
		main.setEnclosingScope(currentScope);
		main.setType(type);
		currentScope.define(main);
		currentScope = main;
		ctx.scope = main;
	}
	@Override
	public void exitMain(JParser.MainContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterBlock(JParser.BlockContext ctx) {
		LocalScope ls = new LocalScope(currentScope);
		ctx.scope = ls;
		currentScope.nest(ls);
		currentScope = ls;
	}
	@Override
	public void exitBlock(JParser.BlockContext ctx)
	{
		currentScope = currentScope.getEnclosingScope();
	}

	public void enterClassDeclaration (JParser.ClassDeclarationContext ctx){
		String className = ctx.name.getText();
		String superclassName;
		JClass cs = new JClass(className,ctx);
		cs.setEnclosingScope(currentScope);
		if(ctx.superClass!=null){
			superclassName = ctx.superClass.getText();
			cs.setSuperClass(superclassName);
		}
		currentScope.define(cs);
		currentScope = cs;
		ctx.scope = cs;
	}
	public void exitClassDeclaration(JParser.ClassDeclarationContext ctx)
	{
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		String id = ctx.ID().getText();
		JMethod m= new JMethod(id,ctx);
		m.setEnclosingScope(currentScope);
		JArg thisArg = new JArg("this");
		if(ctx.jType()!=null){
			Symbol thisType = currentScope.resolve(ctx.jType().getText());
			m.setType((Type)thisType);
		}
		else{
			m.setType(JVOID_TYPE);
		}
		thisArg.setType((Type)currentScope.resolve(currentScope.getName()));
		currentScope.define(m);
		currentScope = m;
		currentScope.define(thisArg);
		ctx.scope = m;
//		String typeName = "void";
//		if(ctx.jType()!=null)
//			typeName = ctx.jType().getText();
//		JMethod jmethod = new JMethod(ctx.ID().getText(), ctx);
//		jmethod.setEnclosingScope(currentScope);
//		jmethod.setType((Type)globalScope.resolve(typeName));
//		currentScope.define(jmethod);
//		ctx.scope = jmethod;
//		JArg jArg = new JArg("this");
//		jArg.setType((Type)currentScope.resolve(currentScope.getName()));
//		currentScope = jmethod;
//		currentScope.define(jArg);
	}
	@Override
	public void exitMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterLocalVariableDeclaration(JParser.LocalVariableDeclarationContext ctx) {
		Symbol type = currentScope.resolve(ctx.jType().getText());
		String id = ctx.ID().getText();
		JVar var = new JVar(id);
		var.setType((Type) type);
		var.setScope(currentScope);
		currentScope.define(var);
	}

	@Override
	public void enterFieldDeclaration(JParser.FieldDeclarationContext ctx) {
		Symbol type = currentScope.resolve(ctx.jType().getText());
		String id = ctx.ID().getText();
		JField f = new JField(id);
		f.setScope(currentScope);
		currentScope.define(f);
		f.setType((Type)type);
	}

	@Override
	public void enterFormalParameter(JParser.FormalParameterContext ctx) {
		Symbol type = currentScope.resolve(ctx.jType().getText());
		String id = ctx.ID().getText();
		JArg para = new JArg(id);
		para.setScope(currentScope);
		para.setType((Type)type);
		currentScope.define(para);
	}
}

//package cs652.j.semantics;
//
//import cs652.j.parser.JBaseListener;
//import cs652.j.parser.JParser;
//import org.antlr.symtab.*;
//
//public class DefineScopesAndSymbols extends JBaseListener
//{
//	public Scope currentScope;
//	public GlobalScope globalScope;
//	public DefineScopesAndSymbols(GlobalScope globals)
//	{
//		currentScope = globals;
//		globalScope = globals;
//		currentScope.define((Symbol)ComputeTypes.JINT_TYPE);
//		currentScope.define((Symbol)ComputeTypes.JFLOAT_TYPE);
//		currentScope.define((Symbol)ComputeTypes.JSTRING_TYPE);
//		currentScope.define((Symbol)ComputeTypes.JVOID_TYPE);
//	}
//
//	@Override
//	public void enterFile(JParser.FileContext ctx) {
//		ctx.scope = (GlobalScope)currentScope;
//	}
//
//	@Override
//	public void enterClassDeclaration(JParser.ClassDeclarationContext ctx)
//	{
//		JClass jClass = new JClass(ctx.name.getText(),ctx);
//		jClass.setEnclosingScope(currentScope);
//		if (ctx.superClass != null)
//		{
//			jClass.setSuperClass(ctx.superClass.getText());
//		}
//		currentScope.define(jClass);
//		ctx.scope = jClass;
//		pushScope(jClass);
//	}
//
//	@Override
//	public void enterFieldDeclaration(JParser.FieldDeclarationContext ctx)
//	{
//		JField jField = new JField(ctx.ID().getText());
//		jField.setScope(currentScope);
//		jField.setType((Type) currentScope.resolve(ctx.jType().getText()));
//		currentScope.define(jField);
//	}
//
//	@Override
//	public void enterMethodDeclaration(JParser.MethodDeclarationContext ctx)
//	{
//		String typeName = "void";
//		if(ctx.jType()!=null)
//			typeName = ctx.jType().getText();
//		JMethod jmethod = new JMethod(ctx.ID().getText(), ctx);
//		jmethod.setEnclosingScope(currentScope);
//		jmethod.setType((Type)globalScope.resolve(typeName));
//		currentScope.define(jmethod);
//		ctx.scope = jmethod;
//		JArg jArg = new JArg("this");
//		jArg.setType((Type)currentScope.resolve(currentScope.getName()));
//		pushScope(jmethod);
//		currentScope.define(jArg);
//	}
//
//	@Override
//	public void enterFormalParameter(JParser.FormalParameterContext ctx)
//	{
//		JArg jArg = new JArg(ctx.ID().getText());
//		jArg.setScope(currentScope);
//		jArg.setType((Type) currentScope.resolve(ctx.jType().getText()));
//		currentScope.define(jArg);
//	}
//
//	@Override
//	public void enterBlock(JParser.BlockContext ctx)
//	{
//		LocalScope lScope = new LocalScope(currentScope);
//		ctx.scope = lScope;
//		currentScope.nest(lScope);
//		pushScope(lScope);
//	}
//
//	@Override
//	public void enterMain(JParser.MainContext ctx)
//	{
//		JMethod jMethod = new JMethod("main",ctx);
//		jMethod.setEnclosingScope(currentScope);
//		currentScope.define(jMethod);
//		ctx.scope = jMethod;
//		jMethod.setType(ComputeTypes.JVOID_TYPE);
//		pushScope(jMethod);
//	}
//
//	@Override
//	public void enterLocalVariableDeclaration(JParser.LocalVariableDeclarationContext ctx)
//	{
//		JVar jVar = new JVar(ctx.ID().getText());
//		jVar.setScope(currentScope);
//		jVar.setType((Type) currentScope.resolve(ctx.jType().getText()));
//		currentScope.define(jVar);
//	}
//
//	@Override
//	public void exitFile(JParser.FileContext ctx) {
//		popScope();
//	}
//
//	@Override
//	public void exitClassDeclaration(JParser.ClassDeclarationContext ctx) {
//		popScope();
//	}
//
//	@Override
//	public void exitMethodDeclaration(JParser.MethodDeclarationContext ctx) {
//		popScope();
//	}
//
//	@Override
//	public void exitBlock(JParser.BlockContext ctx) {
//		popScope();
//	}
//
//	@Override
//	public void exitMain(JParser.MainContext ctx)
//	{
//		popScope();
//	}
//
//	private void pushScope(Scope s)
//	{
//		currentScope = s;
//	}
//
//	private void popScope()
//	{
//		currentScope = currentScope.getEnclosingScope();
//	}
//}
