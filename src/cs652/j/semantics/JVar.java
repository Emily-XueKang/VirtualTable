package cs652.j.semantics;

import org.antlr.symtab.BaseSymbol;
import org.antlr.symtab.Type;
import org.antlr.symtab.TypedSymbol;

/**
 * Created by xuekang on 3/1/17.
 */
public class JVar extends BaseSymbol implements TypedSymbol {
    public JVar(String name) { super(name);}

    @Override
    public void setType(Type type) {
        super.setType(type);
    }
}

