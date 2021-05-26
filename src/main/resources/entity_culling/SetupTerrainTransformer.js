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
				ASMAPI.log("INFO", "Transforming method: setupTerrain net.minecraft.client.renderer.WorldRenderer");
				//ASMAPI.log("INFO", "{}", ASMAPI.methodNodeToString(methodNode));
				
				var targetNode = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/util/ChunkUtils", "hasEntities", "(Lnet/minecraft/world/chunk/Chunk;)Z");
				
				if (targetNode != null) {
					targetNode = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "it/unimi/dsi/fastutil/objects/ObjectList", "add", "(Ljava/lang/Object;)Z", methodNode.instructions.indexOf(targetNode));
					//targetNode = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode));
					{
						for (var i = methodNode.instructions.indexOf(targetNode); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == -1) {
								targetNode = insnNode;
								break;
							}
						}
					}
					var popNode = ASMAPI.findFirstInstructionAfter(methodNode, -1, methodNode.instructions.indexOf(targetNode) + 1);
					
					methodNode.instructions.insert(targetNode, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 25),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderChunkShadow", "(Ljava/lang/Object;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode)
					));
				}
				
				return methodNode;
			}
		}
	}
}