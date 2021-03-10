function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	return {
		"UpdateCameraAndRender Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.WorldRenderer",
				"methodName": "func_228426_a_",
				"methodDesc": "(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V"
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
				
				var targetNode1 = methodNode.instructions.get(719);
				var popNode1 = methodNode.instructions.get(554);
				
				var targetNode2 = methodNode.instructions.get(824);
				var popNode2 = methodNode.instructions.get(804);
				
				var targetNode3 = methodNode.instructions.get(964);
				var popNode3 = methodNode.instructions.get(944);
				
				var targetNode4 = methodNode.instructions.get(471);
				
				if (targetNode1.getOpcode() == 25 &&
					popNode1 instanceof LabelNode &&
					targetNode2.getOpcode() == 25 &&
					popNode2 instanceof LabelNode &&
					targetNode3.getOpcode() == 25 &&
					popNode3 instanceof LabelNode &&
					targetNode4.getOpcode() == 25) {
					// Vanilla
					methodNode.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 40));
					methodNode.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false));
					methodNode.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFEQ, popNode1));
					
					methodNode.instructions.insertBefore(targetNode2, new VarInsnNode(Opcodes.ALOAD, 43));
					methodNode.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false));
					methodNode.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFEQ, popNode2));
					
					methodNode.instructions.insertBefore(targetNode3, new VarInsnNode(Opcodes.ALOAD, 41));
					methodNode.instructions.insertBefore(targetNode3, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false));
					methodNode.instructions.insertBefore(targetNode3, new JumpInsnNode(Opcodes.IFEQ, popNode3));
					
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 6));
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 1));
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 9));
					methodNode.instructions.insertBefore(targetNode4, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false));
				} else {
					// Optifine
					targetNode1 = methodNode.instructions.get(1042);
					popNode1 = methodNode.instructions.get(932);
					
					targetNode2 = methodNode.instructions.get(1212);
					popNode2 = methodNode.instructions.get(1176);
					
					targetNode3 = methodNode.instructions.get(1385);
					popNode3 = methodNode.instructions.get(1349);
					
					targetNode4 = methodNode.instructions.get(647);
					
					methodNode.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 44));
					methodNode.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false));
					methodNode.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFEQ, popNode1));
					
					methodNode.instructions.insertBefore(targetNode2, new VarInsnNode(Opcodes.ALOAD, 47));
					methodNode.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false));
					methodNode.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFEQ, popNode2));
					
					methodNode.instructions.insertBefore(targetNode3, new VarInsnNode(Opcodes.ALOAD, 45));
					methodNode.instructions.insertBefore(targetNode3, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false));
					methodNode.instructions.insertBefore(targetNode3, new JumpInsnNode(Opcodes.IFEQ, popNode3));
					
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 6));
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 1));
					methodNode.instructions.insertBefore(targetNode4, new VarInsnNode(Opcodes.ALOAD, 9));
					methodNode.instructions.insertBefore(targetNode4, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false));
				}
				
				return methodNode;
			}
		}
	}
}