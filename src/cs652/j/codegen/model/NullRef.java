package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/6/17.
 */
public class NullRef extends Expr {
    public String name;
    public TypeSpec type;

    public NullRef(String name, TypeSpec type){
        this.name = name;
        this.type = type;
    }
}
