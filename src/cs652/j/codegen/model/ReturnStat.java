package cs652.j.codegen.model;

/**
 * Created by xuekang on 4/5/17.
 */
public class ReturnStat extends Stat {
    @ModelElement
    public Expr expr;

    public ReturnStat(Expr expr){
        this.expr = expr;
    }
}
