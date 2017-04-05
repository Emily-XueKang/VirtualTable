package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/5/17.
 */
public class IfStat extends Stat {
    public String cond;
    @ModelElement
    public Expr condition;

    @ModelElement
    public Stat stat;

    public IfStat(String cond,Expr condition){
        this.cond = cond;
        this.condition = condition;
    }
}
