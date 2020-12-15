package meldexun.entityculling.plugin;

import org.objectweb.asm.tree.MethodNode;

public class MethodTransformer {

	@FunctionalInterface
	public interface IMethodTransform {
		void transformMethod(MethodNode method);
	}

	public final String className;
	public final String transformedClassName;
	public final String methodName;
	public final String transformedMethodName;
	public final String methodDesc;
	public final String transformedMethodDesc;
	private final IMethodTransform methodTransform;

	public MethodTransformer(String className, String transformedClassName, String methodName, String transformedMethodName, String methodDesc, String transformedMethodDesc, IMethodTransform methodTransform) {
		this.className = className;
		this.transformedClassName = transformedClassName;
		this.methodName = methodName;
		this.transformedMethodName = transformedMethodName;
		this.methodDesc = methodDesc;
		this.transformedMethodDesc = transformedMethodDesc;
		this.methodTransform = methodTransform;
	}

	public boolean canApplyMethodTransform(MethodNode method) {
		return (method.name.equals(this.methodName) && method.desc.equals(this.methodDesc)) || (method.name.equals(this.transformedMethodName) && method.desc.equals(this.transformedMethodDesc));
	}

	public void applyMethodTransform(MethodNode method) {
		this.methodTransform.transformMethod(method);
	}

}
