package meldexun.entityculling.plugin.transformer;

import java.util.function.Consumer;

import org.objectweb.asm.tree.FieldNode;

public class FieldTransformer {

	public final String className;
	public final String transformedClassName;
	public final String fieldName;
	public final String transformedFieldName;
	public final String fieldDesc;
	public final String transformedFieldDesc;
	public final Consumer<FieldNode> transformer;

	public FieldTransformer(String className, String transformedClassName, String fieldName, String transformedFieldName, String fieldDesc, String transformedFieldDesc, Consumer<FieldNode> transformer) {
		this.className = className;
		this.transformedClassName = transformedClassName;
		this.fieldName = fieldName;
		this.transformedFieldName = transformedFieldName;
		this.fieldDesc = fieldDesc;
		this.transformedFieldDesc = transformedFieldDesc;
		this.transformer = transformer;
	}

	public boolean canApplyTransform(FieldNode fieldNode) {
		return (fieldNode.name.equals(this.fieldName) && fieldNode.desc.equals(this.fieldDesc)) || (fieldNode.name.equals(this.transformedFieldName) && fieldNode.desc.equals(this.transformedFieldDesc));
	}

}
