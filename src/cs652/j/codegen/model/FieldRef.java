package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/5/17.
 */
public class FieldRef extends Expr {
    @ModelElement
    public Expr object;

    public String name;

    public FieldRef(String name, Expr object){
        this.name = name;
        this.object = object;
    }
}
