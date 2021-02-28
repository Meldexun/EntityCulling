package meldexun.entityculling.plugin.transformer;

import java.util.function.Consumer;

import org.objectweb.asm.tree.MethodNode;

public class MethodTransformer {

	public final String className;
	public final String transformedClassName;
	public final String methodName;
	public final String transformedMethodName;
	public final String methodDesc;
	public final String transformedMethodDesc;
	public final Consumer<MethodNode> transformer;

	public MethodTransformer(String className, String transformedClassName, String methodName, String transformedMethodName, String methodDesc, String transformedMethodDesc, Consumer<MethodNode> transformer) {
		this.className = className;
		this.transformedClassName = transformedClassName;
		this.methodName = methodName;
		this.transformedMethodName = transformedMethodName;
		this.methodDesc = methodDesc;
		this.transformedMethodDesc = transformedMethodDesc;
		this.transformer = transformer;
	}

	public boolean canApplyTransform(MethodNode methodNode) {
		return (methodNode.name.equals(this.methodName) && methodNode.desc.equals(this.methodDesc)) || (methodNode.name.equals(this.transformedMethodName) && methodNode.desc.equals(this.transformedMethodDesc));
	}

}
