package cs652.j.codegen.model;

import cs652.j.semantics.JMethod;

/**
 * Created by xuekang on 4/3/17.
 */
public class FuncName extends OutputModelObject {
    public JMethod method;
    public int slotNumber;

    public String getClassName(){
            return method.getEnclosingScope().getName();
    }
    public String getMethodName()
    {
        return method.getName();
    }
    public FuncName(JMethod method){
        this.method = method;
    }
}
