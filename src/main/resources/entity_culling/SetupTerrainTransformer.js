function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	return {
		"SetupTerrain Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.WorldRenderer",
				"methodName": "func_228437_a_",
				"methodDesc": "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V"
			},
			"transformer": function(methodNode) {
				/*
				var l = methodNode.instructions;
				for (var i = 0; i < l.size(); i++) {
					var ins = l.get(i);
					if (ins.getOpcode() != -1) {
						ASMAPI.log("INFO", "{} {}", i, ins.getOpcode());
					}
				}
				*/
				
				var targetNode1 = methodNode.instructions.get(515);
				var popNode1 = methodNode.instructions.get(520);
				
				if (targetNode1.getOpcode() == 25 &&
					popNode1 instanceof LabelNode) {
					methodNode.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 25));
					methodNode.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderChunkShadow", "(Ljava/lang/Object;)Z", false));
					methodNode.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFEQ, popNode1));
				}
				
				return methodNode;
			}
		}
	}
}