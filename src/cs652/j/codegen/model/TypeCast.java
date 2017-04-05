package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/4/17.
 */
public class TypeCast extends OutputModelObject {
    @ModelElement
    public TypeSpec type;

    @ModelElement
    public String expr;

    public TypeCast(String expr, TypeSpec type){
        this.expr = expr;
        this.type = type;
    }
}
