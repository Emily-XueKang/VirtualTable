package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 4/2/17.
 */
public class PrintStat extends Stat {
    public String format;

    @ModelElement
    public List<OutputModelObject> args = new ArrayList<>();

    public PrintStat(String format){
        this.format = format;
    }
    public void addArg(OutputModelObject arg){
        args.add(arg);
    }
}
