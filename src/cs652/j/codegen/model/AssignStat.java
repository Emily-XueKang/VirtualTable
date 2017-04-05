package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/1/17.
 */
public class AssignStat extends Stat{
    @ModelElement
    public Expr left;
    @ModelElement
    public OutputModelObject right;

    public AssignStat(Expr leftvar, OutputModelObject rightvar){
        this.left = leftvar;
        this.right = rightvar;
    }
}
