package cs652.j.codegen.model;

/**
 * Created by xuekang on 3/31/17.
 */
public class VarDef extends OutputModelObject {
    public String id;

    @ModelElement
    public OutputModelObject type;

    public VarDef(String id, OutputModelObject type){
        this.id = id;
        this.type = type;
    }
}
