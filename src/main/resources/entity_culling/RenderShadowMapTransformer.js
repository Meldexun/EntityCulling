function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
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
				ASMAPI.log("INFO", "Transforming method: renderShadowMap net.optifine.shaders.ShadersRender");
				//ASMAPI.log("INFO", "{}", ASMAPI.methodNodeToString(methodNode));
				
				var targetNode1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/shaders/Shaders", "nextEntity", "(Lnet/minecraft/entity/Entity;)V");
				//targetNode1 = ASMAPI.findFirstInstructionBefore(methodNode, Opcodes.PUTFIELD, methodNode.instructions.indexOf(targetNode1));
				{
					for (var i = methodNode.instructions.indexOf(targetNode1); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
							targetNode1 = insnNode;
							break;
						}
					}
				}
				//targetNode1 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode1));
				{
					for (var i = methodNode.instructions.indexOf(targetNode1); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getOpcode() == -1) {
							targetNode1 = insnNode;
							break;
						}
					}
				}
				var popNode1 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode1));
				//popNode1 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode1));
				//popNode1 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode1) - 1);
				{
					for (var i = methodNode.instructions.indexOf(popNode1); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getType() == AbstractInsnNode.LABEL) {
							popNode1 = insnNode;
							break;
						}
					}
				}
				
				var targetNode2 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/shaders/Shaders", "nextBlockEntity", "(Lnet/minecraft/tileentity/TileEntity;)V");
				//targetNode2 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode2));
				{
					for (var i = methodNode.instructions.indexOf(targetNode2); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getOpcode() == -1) {
							targetNode2 = insnNode;
							break;
						}
					}
				}
				var popNode2 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode2));
				//popNode2 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode2));
				//popNode2 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode2) - 1);
				{
					for (var i = methodNode.instructions.indexOf(popNode2); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getType() == AbstractInsnNode.LABEL) {
							popNode2 = insnNode;
							break;
						}
					}
				}
				
				methodNode.instructions.insert(targetNode1, ASMAPI.listOf(
						new VarInsnNode(Opcodes.ALOAD, 29),
						new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntityShadow", "(Lnet/minecraft/entity/Entity;)Z", false),
						new JumpInsnNode(Opcodes.IFEQ, popNode1)
				));
				
				methodNode.instructions.insert(targetNode2, ASMAPI.listOf(
						new VarInsnNode(Opcodes.ALOAD, 30),
						new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntityShadow", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
						new JumpInsnNode(Opcodes.IFEQ, popNode2)
				));
				
				return methodNode;
			}
		}
	}
}