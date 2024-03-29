function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	return {
		"Entity Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.entity.Entity"
			},
			"transformer": function(classNode) {
				ASMAPI.log("INFO", "Transforming class: net.minecraft.world.entity.Entity");
				
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulled", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isShadowCulled", "Z", null, false));
				
				classNode.interfaces.add("meldexun/entityculling/util/ICullable");
				
				var methodIsCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isCulled", "()Z", null, null);
				methodIsCulled.instructions.clear();
				methodIsCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/entity/Entity", "isCulled", "Z"));
				methodIsCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCulled);
				
				var methodSetCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setCulled", "(Z)V", null, null);
				methodSetCulled.instructions.clear();
				methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/entity/Entity", "isCulled", "Z"));
				methodSetCulled.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCulled);
				
				var methodIsShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isShadowCulled", "()Z", null, null);
				methodIsShadowCulled.instructions.clear();
				methodIsShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsShadowCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/entity/Entity", "isShadowCulled", "Z"));
				methodIsShadowCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsShadowCulled);
				
				var methodSetShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setShadowCulled", "(Z)V", null, null);
				methodSetShadowCulled.instructions.clear();
				methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetShadowCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/entity/Entity", "isShadowCulled", "Z"));
				methodSetShadowCulled.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetShadowCulled);
				
				return classNode;
			}
		}
	}
}