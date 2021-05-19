function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	return {
		"ClippingHelper Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.culling.ClippingHelper",
				"methodName": "cubeInFrustum",
				"methodDesc": "(FFFFFF)Z"
			},
			"transformer": function(methodNode) {
				ASMAPI.log("INFO", "Transforming method: cubeInFrustum net.minecraft.client.renderer.culling.ClippingHelper");
				
				/*
				var l = methodNode.instructions;
				for (var i = 0; i < l.size(); i++) {
					var ins = l.get(i);
					if (ins.getOpcode() != -1) {
						ASMAPI.log("INFO", "{} {}", i, ins.getOpcode());
					}
				}
				*/
				
				var targetNode = methodNode.instructions.get(0);
				
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(targetNode, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/culling/ClippingHelper", "frustumData", "[Lnet/minecraft/util/math/vector/Vector4f;"));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 1));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 2));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 3));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 4));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 5));
				methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.FLOAD, 6));
				methodNode.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "cubeInFrustum", "([Lnet/minecraft/util/math/vector/Vector4f;FFFFFF)Z", false));
				methodNode.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IRETURN));
				
				return methodNode;
			}
		}
	}
}