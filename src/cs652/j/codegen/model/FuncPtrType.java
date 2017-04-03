package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/2/17.
 */
public class FuncPtrType extends OutputModelObject {
    @ModelElement
    public String typename;

    public FuncPtrType(String typename){
        this.typename = typename;
    }
}
