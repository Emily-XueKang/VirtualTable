package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/2/17.
 */
public class FuncPtrType extends OutputModelObject {
    @ModelElement
    public TypeSpec returnType;

    @ModelElement
    public List<TypeSpec> argTypes = new ArrayList<>();

    public FuncPtrType(TypeSpec returnType){
        this.returnType = returnType;
    }
}
