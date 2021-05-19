function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	return {
		"RenderShadowMap Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.optifine.shaders.ShadersRender",
				"methodName": "renderShadowMap",
				"methodDesc": "(Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/ActiveRenderInfo;IFJ)V"
			},
			"transformer": function(methodNode) {
				//ASMAPI.log("INFO", "Transforming method: renderShadowMap net.optifine.shaders.ShadersRender");
				
				/*
				var l = methodNode.instructions;
				for (var i = 0; i < l.size(); i++) {
					var ins = l.get(i);
					if (ins.getOpcode() != -1) {
						ASMAPI.log("INFO", "{} {}", i, ins.getOpcode());
					}
				}
				*/
				
				var targetNode1 = methodNode.instructions.get(569);
				var popNode1 = methodNode.instructions.get(513);
				
				var targetNode2 = methodNode.instructions.get(716);
				var popNode2 = methodNode.instructions.get(680);
				
				methodNode.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 28));
				methodNode.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntityShadow", "(Lnet/minecraft/entity/Entity;)Z", false));
				methodNode.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFEQ, popNode1));
				
				methodNode.instructions.insertBefore(targetNode2, new VarInsnNode(Opcodes.ALOAD, 28));
				methodNode.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntityShadow", "(Lnet/minecraft/tileentity/TileEntity;)Z", false));
				methodNode.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFEQ, popNode2));
				
				return methodNode;
			}
		}
	}
}