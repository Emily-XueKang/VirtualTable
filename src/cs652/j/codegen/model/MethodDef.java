package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/2/17.
 */
public class MethodDef extends OutputModelObject {
    @ModelElement
    public FuncName funcName;

    public MethodDef(FuncName name){
        this.funcName = name;
    };

    @ModelElement
    public TypeSpec returnType;


//    @ModelElement
//    List<VarDef> formalparamters = new ArrayList<>();

    @ModelElement
    public OutputModelObject body;


}
