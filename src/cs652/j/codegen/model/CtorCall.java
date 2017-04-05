package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/2/17.
 */
public class CtorCall extends OutputModelObject{
    public String id;
    public ObjectTypeSpec type;
    public CtorCall(String id, ObjectTypeSpec type ){
        this.id = id;
        this.type = type;

    }
}
