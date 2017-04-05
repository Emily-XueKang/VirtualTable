package cs652.j.codegen.model;


/**
 * Created by xuekang on 3/29/17.
 */
public class LiteralRef extends Expr {
    public String literal;
    public TypeSpec type;

    public LiteralRef(String literal, TypeSpec literaltype){
        this.literal = literal;
        this.type = literaltype;
    }

}
