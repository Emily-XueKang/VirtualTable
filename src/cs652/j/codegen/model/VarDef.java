package cs652.j.codegen.model;

/**
 * Created by xuekang on 3/31/17.
 */
public class VarDef extends OutputModelObject {
    public String id;

    @ModelElement
    public TypeSpec type;

    public VarDef(String id, TypeSpec type){
        this.id = id;
        this.type = type;
    }
}
