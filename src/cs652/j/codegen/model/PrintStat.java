package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/2/17.
 */
public class PrintStat extends Stat {
    public String format;

    @ModelElement
    public List<Expr> args = new ArrayList<>();

    public PrintStat(String format){
        this.format = format;
    }
    public void addArg(Expr arg){
        args.add(arg);
    }
}
