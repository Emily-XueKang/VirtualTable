package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/1/17.
 */
public class AssignStat extends OutputModelObject{
    @ModelElement
    public VarRef left;
    @ModelElement
    public OutputModelObject right;

    public AssignStat(VarRef leftvar, OutputModelObject rightvar){
        this.left = leftvar;
        this.right = rightvar;
    }
}
