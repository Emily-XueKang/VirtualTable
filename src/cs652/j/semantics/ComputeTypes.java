package cs652.j.semantics;

import cs652.j.codegen.model.ClassDef;
import cs652.j.parser.JBaseListener;
import cs652.j.parser.JParser;
import org.antlr.symtab.*;

public class ComputeTypes extends JBaseListener {
	protected StringBuilder buf = new StringBuilder();
	Scope currentScope;

	public static final Type JINT_TYPE = new JPrimitiveType("int");
	public static final Type JFLOAT_TYPE = new JPrimitiveType("float");
	public static final Type JSTRING_TYPE = new JPrimitiveType("string");
	public static final Type JVOID_TYPE = new JPrimitiveType("void");


	public Scope CurrentScope;
	public ClassDef currentClass;

	public ComputeTypes(GlobalScope globals)
	{
		//this.currentScope = globals;
	}

	/**
	 * Append name and type of this symbol to output.
	 * @param buf StringBuilder
	 * @param name of symbol
	 * @param type of symbol
	 */
	public void appendOutput(StringBuilder buf, String name, String type){
		buf.append(name + " is " + type + "\n");
	}

	@Override
	public void enterFile(JParser.FileContext ctx) {
		currentScope = ctx.scope;
	}

	@Override
	public void enterMain(JParser.MainContext ctx) {
		currentScope = ctx.scope;
	}

	@Override
	public void enterBlock(JParser.BlockContext ctx) {
		currentScope = ctx.scope;
	}

	@Override
	public void enterClassDeclaration(JParser.ClassDeclarationContext ctx) {
		currentScope = ctx.scope;
	}

	@Override
	public void enterMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		currentScope = ctx.scope;
	}


	@Override
	public void exitFieldRef(JParser.FieldRefContext ctx) {
		String id = ctx.ID().getText();
		TypedSymbol var;
		JClass cs = (JClass) ctx.expression().type;
		if(cs != null){
			var = (TypedSymbol) cs.resolveMember(id);
			if(var != null){
				ctx.type = var.getType();
			}
		}
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}
	@Override
	public void exitCtorCall(JParser.CtorCallContext ctx) {
		Symbol var = currentScope.resolve(ctx.ID().getText());
		ctx.type = (Type) var;
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	@Override
	public void enterLiteralRef(JParser.LiteralRefContext ctx) {
		if(ctx.FLOAT()!=null){
			ctx.type = JFLOAT_TYPE;
		}
		else if(ctx.INT()!=null){
			ctx.type = JINT_TYPE;
		}
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	@Override
	public void exitIdRef(JParser.IdRefContext ctx) {
		TypedSymbol var = (TypedSymbol)currentScope.resolve(ctx.ID().getText());
		if(var != null){
			ctx.type = var.getType();
		}
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	@Override
	public void exitThisRef(JParser.ThisRefContext ctx) {
		JMethod ms = (JMethod) currentScope.getEnclosingScope();
		JClass cs = (JClass) ms.getEnclosingScope();
		JClass cType = (JClass) cs.resolve(cs.getName());
		ctx.type = cType;
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	@Override
	public void exitNullRef(JParser.NullRefContext ctx) {
		ctx.type = JVOID_TYPE;
	}

	@Override
	public void exitMethodCall(JParser.MethodCallContext ctx) {
		TypedSymbol mvar = (TypedSymbol)currentScope.resolve(ctx.ID().getText());
		if(mvar !=null){
			ctx.type = mvar.getType();
		}
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	@Override
	public void exitQMethodCall(JParser.QMethodCallContext ctx) {
		String id = ctx.ID().getText();
		TypedSymbol mvar;
		JClass cs = (JClass) ctx.expression().type;
		if(cs != null){
			mvar = (TypedSymbol) cs.resolveMember(id);
			if(mvar != null){
				ctx.type = mvar.getType();
			}
		}
		appendOutput(buf,ctx.getText(),ctx.type.getName());
	}

	public String getRefOutput() {
		return buf.toString();
	}
}


