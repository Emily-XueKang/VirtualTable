package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/2/17.
 */
public class MethodDef extends OutputModelObject {
    public String name;

    public MethodDef(String name){
        this.name = name;
    };
    @ModelElement
    public String funcName;

    public void setfunName(FuncPtrType fpt){
        this.funcName = fpt.typename + "_" + name;
    }

    @ModelElement
    public TypeSpec returnType;

    public void setreturnType(TypeSpec rt){
        this.returnType = rt;
    }

//    @ModelElement
//    List<OutputModelObject> formalparamters = new ArrayList<>();

    @ModelElement
    public OutputModelObject body;


}
