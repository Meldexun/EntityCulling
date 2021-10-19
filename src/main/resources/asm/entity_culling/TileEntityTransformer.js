function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	return {
		"TileEntity Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.level.block.entity.BlockEntity"
			},
			"transformer": function(classNode) {
				ASMAPI.log("INFO", "Transforming class: net.minecraft.world.level.block.entity.BlockEntity");
				
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulled", "Z", null, false));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isShadowCulled", "Z", null, false));
				
				classNode.interfaces.add("meldexun/entityculling/util/ICullable");
				
				var methodIsCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isCulled", "()Z", null, null);
				methodIsCulled.instructions.clear();
				methodIsCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isCulled", "Z"));
				methodIsCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCulled);
				
				var methodSetCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setCulled", "(Z)V", null, null);
				methodSetCulled.instructions.clear();
				methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isCulled", "Z"));
				methodSetCulled.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCulled);
				
				var methodIsShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isShadowCulled", "()Z", null, null);
				methodIsShadowCulled.instructions.clear();
				methodIsShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsShadowCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isShadowCulled", "Z"));
				methodIsShadowCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsShadowCulled);
				
				var methodSetShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setShadowCulled", "(Z)V", null, null);
				methodSetShadowCulled.instructions.clear();
				methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetShadowCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isShadowCulled", "Z"));
				methodSetShadowCulled.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetShadowCulled);
				
				
				
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCacheable", "I", null, 0));
				classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;", null, null));
				
				classNode.interfaces.add("meldexun/entityculling/util/IBoundingBoxCache");
				
				var methodIsCacheable = new MethodNode(Opcodes.ACC_PUBLIC, "isCacheable", "()I", null, null);
				methodIsCacheable.instructions.clear();
				methodIsCacheable.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodIsCacheable.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isCacheable", "I"));
				methodIsCacheable.instructions.add(new InsnNode(Opcodes.IRETURN));
				classNode.methods.add(methodIsCacheable);
				
				var methodSetCacheable = new MethodNode(Opcodes.ACC_PUBLIC, "setCacheable", "(I)V", null, null);
				methodSetCacheable.instructions.clear();
				methodSetCacheable.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCacheable.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
				methodSetCacheable.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "isCacheable", "I"));
				methodSetCacheable.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCacheable);
				
				var methodGetCachedBoundingBox = new MethodNode(Opcodes.ACC_PUBLIC, "getCachedBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", null, null);
				methodGetCachedBoundingBox.instructions.clear();
				methodGetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodGetCachedBoundingBox.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;"));
				methodGetCachedBoundingBox.instructions.add(new InsnNode(Opcodes.ARETURN));
				classNode.methods.add(methodGetCachedBoundingBox);
				
				var methodSetCachedBoundingBox = new MethodNode(Opcodes.ACC_PUBLIC, "setCachedBoundingBox", "(Lnet/minecraft/util/math/AxisAlignedBB;)V", null, null);
				methodSetCachedBoundingBox.instructions.clear();
				methodSetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				methodSetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				methodSetCachedBoundingBox.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/level/block/entity/BlockEntity", "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;"));
				methodSetCachedBoundingBox.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(methodSetCachedBoundingBox);
				
				return classNode;
			}
		}
	}
}