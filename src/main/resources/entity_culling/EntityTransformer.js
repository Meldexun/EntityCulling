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
				"name": "net.minecraft.entity.Entity"
			},
			"transformer": function(classNode) {
				// ASMAPI.log("INFO", "Transforming class: net.minecraft.entity.Entity");
				
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledFast", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledSlow", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledShadowPass", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "query", "I", null, -1));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryInitialized", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryResultDirty", "Z", null, false));
				
				classNode.interfaces.add("meldexun/entityculling/ICullable");
				
				var methodIsCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledFast", "()Z", null, null);
				methodIsCulledFast.instructions.clear();
				methodIsCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCulledFast.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledFast", "Z"));
				methodIsCulledFast.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCulledFast);
				
				var methodSetCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledFast", "(Z)V", null, null);
				methodSetCulledFast.instructions.clear();
				methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCulledFast.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledFast", "Z"));
				methodSetCulledFast.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCulledFast);
				
				var methodIsCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledSlow", "()Z", null, null);
				methodIsCulledSlow.instructions.clear();
				methodIsCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCulledSlow.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledSlow", "Z"));
				methodIsCulledSlow.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCulledSlow);
				
				var methodSetCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledSlow", "(Z)V", null, null);
				methodSetCulledSlow.instructions.clear();
				methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCulledSlow.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledSlow", "Z"));
				methodSetCulledSlow.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCulledSlow);
				
				var methodIsCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledShadowPass", "()Z", null, null);
				methodIsCulledShadowPass.instructions.clear();
				methodIsCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledShadowPass", "Z"));
				methodIsCulledShadowPass.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCulledShadowPass);
				
				var methodSetCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledShadowPass", "(Z)V", null, null);
				methodSetCulledShadowPass.instructions.clear();
				methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledShadowPass", "Z"));
				methodSetCulledShadowPass.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCulledShadowPass);
				
				var methodGetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "getQuery", "()I", null, null);
				methodGetQuery.instructions.clear();
				methodGetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodGetQuery.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "query", "I"));
				methodGetQuery.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodGetQuery);
				
				var methodSetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "setQuery", "(I)V", null, null);
				methodSetQuery.instructions.clear();
				methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetQuery.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "query", "I"));
				methodSetQuery.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetQuery);
				
				var methodIsQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryInitialized", "()Z", null, null);
				methodIsQueryInitialized.instructions.clear();
				methodIsQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "queryInitialized", "Z"));
				methodIsQueryInitialized.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsQueryInitialized);
				
				var methodSetQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryInitialized", "(Z)V", null, null);
				methodSetQueryInitialized.instructions.clear();
				methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "queryInitialized", "Z"));
				methodSetQueryInitialized.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetQueryInitialized);
				
				var methodIsQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryResultDirty", "()Z", null, null);
				methodIsQueryResultUpToDate.instructions.clear();
				methodIsQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "queryResultDirty", "Z"));
				methodIsQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsQueryResultUpToDate);
				
				var methodSetQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryResultDirty", "(Z)V", null, null);
				methodSetQueryResultUpToDate.instructions.clear();
				methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "queryResultDirty", "Z"));
				methodSetQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetQueryResultUpToDate);
				
				return classNode;
			}
		}
	}
}