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
				//*
				ASMAPI.log("INFO", "Transforming method: updateCameraAndRender net.minecraft.client.renderer.WorldRenderer");
				ASMAPI.log("INFO", "{}", ASMAPI.methodNodeToString(methodNode));
				//*/
				
				var targetNode1 = methodNode.instructions.get(719);
				var popNode1 = methodNode.instructions.get(554);
				
				var targetNode2 = methodNode.instructions.get(824);
				var popNode2 = methodNode.instructions.get(804);
				
				var targetNode3 = methodNode.instructions.get(964);
				var popNode3 = methodNode.instructions.get(944);
				
				var targetNode4 = methodNode.instructions.get(471);
				
				var targetNode5 = methodNode.instructions.get(817);
				
				var targetNode6 = methodNode.instructions.get(957);
				
				if (false) {
				/*
				if (targetNode1.getOpcode() == 25 &&
					popNode1 instanceof LabelNode &&
					targetNode2.getOpcode() == 25 &&
					popNode2 instanceof LabelNode &&
					targetNode3.getOpcode() == 25 &&
					popNode3 instanceof LabelNode &&
					targetNode4.getOpcode() == 25) {
				*/
					// Vanilla
					
					
					
					var nodeRenderEntity = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.SPECIAL, "net/minecraft/client/renderer/WorldRenderer", ASMAPI.mapMethod("func_228418_a_"), "(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V");
					// ASMAPI.findFirstInstructionBefore is bugged, do it manually instead
					// var targetNode1 = ASMAPI.findFirstInstructionBefore(methodNode, Opcodes.ASTORE, methodNode.instructions.indexOf(nodeRenderEntity));
					var targetNode1 = null;
					for (var i = methodNode.instructions.indexOf(nodeRenderEntity); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getOpcode() == Opcodes.ASTORE) {
							targetNode1 = insnNode;
							break;
						}
					}
					var nodeHasNext1 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode1));
					var popNode1 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext1) - 3);
					methodNode.instructions.insert(targetNode1, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 40),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode1)
					));
					
					var nodeIsVisible1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z");
					var targetNode2 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeIsVisible1) - 3);
					var nodeHasNext2 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode2));
					var popNode2 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext2) - 3);
					methodNode.instructions.insertBefore(targetNode2, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 43),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode2)
					));
					
					var nodeIsVisible2 = ASMAPI.findFirstMethodCallAfter(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z", methodNode.instructions.indexOf(nodeIsVisible1) + 1);
					var targetNode3 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeIsVisible2) - 3);
					var nodeHasNext3 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode3));
					var popNode3 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext3) - 3);
					methodNode.instructions.insertBefore(targetNode3, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 41),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode3)
					));
					
					var nodeEntitiesForRendering = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/world/ClientWorld", ASMAPI.mapMethod("func_217416_b"), "()Ljava/lang/Iterable;");
					var nodePopPush = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "net/minecraft/profiler/IProfiler", ASMAPI.mapMethod("func_219895_b"), "(Ljava/lang/String;)V", methodNode.instructions.indexOf(nodeEntitiesForRendering));
					methodNode.instructions.insert(nodePopPush, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 6),
							new VarInsnNode(Opcodes.ALOAD, 1),
							new VarInsnNode(Opcodes.ALOAD, 9),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false)
					));
					
					var popNode5 = new LabelNode();
					var nodeGetRenderBoundingBox1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/tileentity/TileEntity", "getRenderBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;");
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox1, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox1, new JumpInsnNode(Opcodes.GOTO, popNode5));
					methodNode.instructions.insert(nodeGetRenderBoundingBox1, popNode5);
					
					var popNode6 = new LabelNode();
					var nodeGetRenderBoundingBox2 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/tileentity/TileEntity", "getRenderBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;");
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox2, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox2, new JumpInsnNode(Opcodes.GOTO, popNode6));
					methodNode.instructions.insert(nodeGetRenderBoundingBox2, popNode6);
					
					
					
					/*
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
					
					var popNode5 = new LabelNode();
					methodNode.instructions.insertBefore(targetNode5, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(targetNode5, new JumpInsnNode(Opcodes.GOTO, popNode5));
					methodNode.instructions.insert(targetNode5, popNode5);
					
					var popNode6 = new LabelNode();
					methodNode.instructions.insertBefore(targetNode6, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(targetNode6, new JumpInsnNode(Opcodes.GOTO, popNode6));
					methodNode.instructions.insert(targetNode6, popNode6);
					*/
				} else {
					// Optifine
					
					
					
					
					var nodeRenderEntity = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.SPECIAL, "net/minecraft/client/renderer/WorldRenderer", ASMAPI.mapMethod("func_228418_a_"), "(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V");
					// ASMAPI.findFirstInstructionBefore is bugged, do it manually instead
					// var targetNode1 = ASMAPI.findFirstInstructionBefore(methodNode, Opcodes.ASTORE, methodNode.instructions.indexOf(nodeRenderEntity));
					var targetNode1 = null;
					for (var i = methodNode.instructions.indexOf(nodeRenderEntity); i >= 0; i--) {
						var insnNode = methodNode.instructions.get(i);
						if (insnNode.getOpcode() == Opcodes.ASTORE) {
							targetNode1 = insnNode;
							break;
						}
					}
					var nodeHasNext1 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode1));
					var popNode1 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext1) - 3);
					methodNode.instructions.insert(targetNode1, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 40),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode1)
					));
					
					var node_Reflector_call_1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/reflect/Reflector", "call", "(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;");
					var targetNode2 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(node_Reflector_call_1));
					var nodeHasNext2 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode2));
					var popNode2 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext2) - 3);
					methodNode.instructions.insertBefore(targetNode2, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 43),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode2)
					));
					
					var node_Reflector_call_2 = ASMAPI.findFirstMethodCallAfter(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/reflect/Reflector", "call", "(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;", methodNode.instructions.indexOf(node_Reflector_call_1) + 1);
					var targetNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(node_Reflector_call_2));
					var nodeHasNext3 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode3));
					var popNode3 = methodNode.instructions.get(methodNode.instructions.indexOf(nodeHasNext3) - 3);
					methodNode.instructions.insertBefore(targetNode3, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 41),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode3)
					));
					
					var nodeEntitiesForRendering = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/world/ClientWorld", ASMAPI.mapMethod("func_217416_b"), "()Ljava/lang/Iterable;");
					var nodePopPush = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "net/minecraft/profiler/IProfiler", ASMAPI.mapMethod("func_219895_b"), "(Ljava/lang/String;)V", methodNode.instructions.indexOf(nodeEntitiesForRendering));
					methodNode.instructions.insert(nodePopPush, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 6),
							new VarInsnNode(Opcodes.ALOAD, 1),
							new VarInsnNode(Opcodes.ALOAD, 9),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false)
					));
					
					var popNode5 = new LabelNode();
					var nodeGetRenderBoundingBox1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/tileentity/TileEntity", "getRenderBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;");
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox1, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox1, new JumpInsnNode(Opcodes.GOTO, popNode5));
					methodNode.instructions.insert(nodeGetRenderBoundingBox1, popNode5);
					
					var popNode6 = new LabelNode();
					var nodeGetRenderBoundingBox2 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/tileentity/TileEntity", "getRenderBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;");
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox2, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(nodeGetRenderBoundingBox2, new JumpInsnNode(Opcodes.GOTO, popNode6));
					methodNode.instructions.insert(nodeGetRenderBoundingBox2, popNode6);
					
					
					
					
					
					
					
					
					targetNode1 = methodNode.instructions.get(1040);
					popNode1 = methodNode.instructions.get(930);
					
					targetNode2 = methodNode.instructions.get(1210);
					popNode2 = methodNode.instructions.get(1174);
					
					targetNode3 = methodNode.instructions.get(1383);
					popNode3 = methodNode.instructions.get(1347);
					
					targetNode4 = methodNode.instructions.get(645);
					
					targetNode5 = methodNode.instructions.get(1190);
					var targetNode5_1 = methodNode.instructions.get(1195);
					
					targetNode6 = methodNode.instructions.get(1363);
					var targetNode6_1 = methodNode.instructions.get(1368);
					
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
					
					var popNode5 = new LabelNode();
					methodNode.instructions.insertBefore(targetNode5, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(targetNode5, new JumpInsnNode(Opcodes.GOTO, popNode5));
					methodNode.instructions.insertBefore(targetNode5_1, popNode5);
					
					var popNode6 = new LabelNode();
					methodNode.instructions.insertBefore(targetNode6, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/ITileEntityBBCache", "getCachedAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", true));
					methodNode.instructions.insertBefore(targetNode6, new JumpInsnNode(Opcodes.GOTO, popNode6));
					methodNode.instructions.insertBefore(targetNode6_1, popNode6);
				}
				
				return methodNode;
			}
		}
	}
}