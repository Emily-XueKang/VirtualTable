package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuekang on 3/29/17.
 */
public class ClassDef extends OutputModelObject {
    public String name;

    @ModelElement
    public List<OutputModelObject> fields = new ArrayList<>();

    @ModelElement
    public List<OutputModelObject> methods = new ArrayList<>();

    public ClassDef(String classname){
        this.name = classname;
    }

    public void addFiled(OutputModelObject field){
        fields.add(field);
    }

    public void addMethod(OutputModelObject method){
        methods.add(method);
    }

}
