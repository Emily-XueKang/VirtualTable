package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/5/17.
 */
public class WhileStat extends OutputModelObject {
    public String cond;

    @ModelElement
    public Expr condition;
    @ModelElement
    public Block stat;

    public WhileStat(String cond, Expr condition){
        this.condition = condition;
        this.cond = cond;
    }

}
