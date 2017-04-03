package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/1/17.
 */
public class VarRef extends OutputModelObject {
    public String name;

    public VarRef(String varname){
        this.name = varname;
    }
}
