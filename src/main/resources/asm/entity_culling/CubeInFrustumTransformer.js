function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
	return {
		"ClippingHelper Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.culling.ClippingHelper",
				"methodName": "func_228954_a_",
				"methodDesc": "(FFFFFF)Z"
			},
			"transformer": function(methodNode) {
				ASMAPI.log("INFO", "Transforming method: cubeInFrustum net.minecraft.client.renderer.culling.ClippingHelper");
				//ASMAPI.log("INFO", "{}", ASMAPI.methodNodeToString(methodNode));
				
				var targetNode = methodNode.instructions.getFirst();
				var skipNode = new LabelNode();
				
				methodNode.instructions.insertBefore(targetNode, ASMAPI.listOf(
						new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "CLIENT_CONFIG", "Lmeldexun/entityculling/config/EntityCullingConfig$ClientConfig;"),
						new FieldInsnNode(Opcodes.GETFIELD, "meldexun/entityculling/config/EntityCullingConfig$ClientConfig", "enabled", "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"),
						new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/ForgeConfigSpec$BooleanValue", "get", "()Ljava/lang/Object;", false),
						new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"),
						new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false),
						new JumpInsnNode(Opcodes.IFEQ, skipNode),
						
						new VarInsnNode(Opcodes.ALOAD, 0),
						new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapField("field_228948_a_"), "[Lnet/minecraft/util/math/vector/Vector4f;"),
						new VarInsnNode(Opcodes.FLOAD, 1),
						new VarInsnNode(Opcodes.FLOAD, 2),
						new VarInsnNode(Opcodes.FLOAD, 3),
						new VarInsnNode(Opcodes.FLOAD, 4),
						new VarInsnNode(Opcodes.FLOAD, 5),
						new VarInsnNode(Opcodes.FLOAD, 6),
						new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/ClippingHelperHook", "cubeInFrustum", "([Lnet/minecraft/util/math/vector/Vector4f;FFFFFF)Z", false),
						new InsnNode(Opcodes.IRETURN),
						
						skipNode
				));
				
				return methodNode;
			}
		}
	}
}