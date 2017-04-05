package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/4/17.
 */
public class TypeCast extends Expr {
    @ModelElement
    public TypeSpec type;

    @ModelElement
    public Expr expr;

    public TypeCast(Expr expr, TypeSpec type){
        this.expr = expr;
        this.type = type;
    }
}
