package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/5/17.
 */
public class ThisRef extends Expr{
    public String name;
    public TypeSpec type;
    public ThisRef(String name, TypeSpec type){
        this.name = name;
        this.type = type;
    }
}
