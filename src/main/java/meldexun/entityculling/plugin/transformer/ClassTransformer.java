package meldexun.entityculling.plugin.transformer;

import java.util.function.Consumer;

import org.objectweb.asm.tree.ClassNode;

public class ClassTransformer {

	public final String className;
	public final String transformedClassName;
	public final Consumer<ClassNode> transformer;

	public ClassTransformer(String className, String transformedClassName, Consumer<ClassNode> transformer) {
		this.className = className;
		this.transformedClassName = transformedClassName;
		this.transformer = transformer;
	}

}
