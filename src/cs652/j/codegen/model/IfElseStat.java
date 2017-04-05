package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/5/17.
 */
public class IfElseStat extends IfStat {
    @ModelElement
    public Stat elseStat;

    public IfElseStat(String cond,Expr condition){
        super(cond,condition);
    }
}
