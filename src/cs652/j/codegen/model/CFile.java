package cs652.j.codegen.model;

import java.util.ArrayList;
import java.util.List;

public class CFile extends OutputModelObject {
	public final String fileName;
	@ModelElement public List<ClassDef> classes = new ArrayList<>();
	@ModelElement public MainMethod main;

	public void addClass(ClassDef classDef){
		classes.add(classDef);
	}

	public CFile(String fileName) {
		this.fileName = fileName;
	}
}
