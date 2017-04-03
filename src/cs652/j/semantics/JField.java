package cs652.j.semantics;

import org.antlr.symtab.MemberSymbol;
import org.antlr.symtab.VariableSymbol;

/**
 * Created by xuekang on 3/1/17.
 */
public class JField extends VariableSymbol implements MemberSymbol {
    protected int slot;

    public JField(String name) {
        super(name);
    }

    @Override
    public int getSlotNumber() { return slot; }

}

