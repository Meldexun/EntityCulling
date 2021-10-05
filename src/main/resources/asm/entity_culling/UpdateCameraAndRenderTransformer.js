function initializeCoreMod() {
	ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
	return {
		"UpdateCameraAndRender Transformer": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.WorldRenderer",
				"methodName": "func_228426_a_",
				"methodDesc": "(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V"
			},
			"transformer": function(methodNode) {
				ASMAPI.log("INFO", "Transforming method: updateCameraAndRender net.minecraft.client.renderer.WorldRenderer");
				//ASMAPI.log("INFO", "{}", ASMAPI.methodNodeToString(methodNode));
				
				if (ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/shaders/Shaders", "beginEntities", "()V") == null) {
					// ---------- Vanilla ---------- //
					var targetNode1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/world/ClientWorld", ASMAPI.mapMethod("func_217416_b"), "()Ljava/lang/Iterable;");
					targetNode1 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "net/minecraft/profiler/IProfiler", ASMAPI.mapMethod("func_219895_b"), "(Ljava/lang/String;)V", methodNode.instructions.indexOf(targetNode1));
					
					var targetNode2 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.SPECIAL, "net/minecraft/client/renderer/WorldRenderer", ASMAPI.mapMethod("func_228418_a_"), "(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V");
					//targetNode2 = ASMAPI.findFirstInstructionBefore(methodNode, Opcodes.ASTORE, methodNode.instructions.indexOf(targetNode2));
					{
						for (var i = methodNode.instructions.indexOf(targetNode2); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == Opcodes.ASTORE) {
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
					
					var targetNode3 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z");
					//targetNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode3));
					{
						for (var i = methodNode.instructions.indexOf(targetNode3); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == -1) {
								targetNode3 = insnNode;
								break;
							}
						}
					}
					var popNode3 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode3));
					//popNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode3));
					//popNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode3) - 1);
					{
						for (var i = methodNode.instructions.indexOf(popNode3); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getType() == AbstractInsnNode.LABEL) {
								popNode3 = insnNode;
								break;
							}
						}
					}
					var popNode3_1 = ASMAPI.findFirstInstructionAfter(methodNode, -1, methodNode.instructions.indexOf(targetNode3) + 1);
					var skipNode3 = new LabelNode();
					
					var targetNode4 = ASMAPI.findFirstMethodCallAfter(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z", methodNode.instructions.indexOf(popNode3_1));
					//targetNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode4));
					{
						for (var i = methodNode.instructions.indexOf(targetNode4); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == -1) {
								targetNode4 = insnNode;
								break;
							}
						}
					}
					var popNode4 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode4));
					//popNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode4));
					//popNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode4) - 1);
					{
						for (var i = methodNode.instructions.indexOf(popNode4); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getType() == AbstractInsnNode.LABEL) {
								popNode4 = insnNode;
								break;
							}
						}
					}
					var popNode4_1 = ASMAPI.findFirstInstructionAfter(methodNode, -1, methodNode.instructions.indexOf(targetNode4) + 1);
					var skipNode4 = new LabelNode();
					
					methodNode.instructions.insert(targetNode1, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 6),
							new VarInsnNode(Opcodes.ALOAD, 1),
							new VarInsnNode(Opcodes.ALOAD, 9),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false)
					));
					
					methodNode.instructions.insert(targetNode2, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 40),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode2)
					));
					
					methodNode.instructions.insert(targetNode3, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 43),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode3),
							
							new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "CLIENT_CONFIG", "Lmeldexun/entityculling/config/EntityCullingConfig$ClientConfig;"),
							new FieldInsnNode(Opcodes.GETFIELD, "meldexun/entityculling/config/EntityCullingConfig$ClientConfig", "enabled", "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/ForgeConfigSpec$BooleanValue", "get", "()Ljava/lang/Object;", false),
							new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false),
							new JumpInsnNode(Opcodes.IFEQ, skipNode3),
							
							new VarInsnNode(Opcodes.ALOAD, 20),
							new VarInsnNode(Opcodes.ALOAD, 43),
							new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/util/IBoundingBoxCache", "getOrCacheBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", true),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode3),
							new JumpInsnNode(Opcodes.GOTO, popNode3_1),
							
							skipNode3
					));
					
					methodNode.instructions.insert(targetNode4, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 41),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode4),
							
							new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "CLIENT_CONFIG", "Lmeldexun/entityculling/config/EntityCullingConfig$ClientConfig;"),
							new FieldInsnNode(Opcodes.GETFIELD, "meldexun/entityculling/config/EntityCullingConfig$ClientConfig", "enabled", "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/ForgeConfigSpec$BooleanValue", "get", "()Ljava/lang/Object;", false),
							new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false),
							new JumpInsnNode(Opcodes.IFEQ, skipNode4),
							
							new VarInsnNode(Opcodes.ALOAD, 20),
							new VarInsnNode(Opcodes.ALOAD, 41),
							new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/util/IBoundingBoxCache", "getOrCacheBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", true),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelper", ASMAPI.mapMethod("func_228957_a_"), "(Lnet/minecraft/util/math/AxisAlignedBB;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode4),
							new JumpInsnNode(Opcodes.GOTO, popNode4_1),
							
							skipNode4
					));
				} else {
					// ---------- Optifine ---------- //
					var targetNode1 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/shaders/Shaders", "beginEntities", "()V");
					targetNode1 = ASMAPI.findFirstMethodCallAfter(methodNode, ASMAPI.MethodType.INTERFACE, "net/minecraft/profiler/IProfiler", ASMAPI.mapMethod("func_219895_b"), "(Ljava/lang/String;)V", methodNode.instructions.indexOf(targetNode1));
					
					var targetNode2 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL, "net/minecraft/client/renderer/WorldRenderer", ASMAPI.mapMethod("func_228418_a_"), "(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V");
					//targetNode2 = ASMAPI.findFirstInstructionBefore(methodNode, Opcodes.ASTORE, methodNode.instructions.indexOf(targetNode2));
					{
						for (var i = methodNode.instructions.indexOf(targetNode2); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == Opcodes.ASTORE) {
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
					
					var targetNode3 = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/reflect/Reflector", "call", "(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;");
					//targetNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode3));
					{
						for (var i = methodNode.instructions.indexOf(targetNode3); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == -1) {
								targetNode3 = insnNode;
								break;
							}
						}
					}
					var popNode3 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode3));
					//popNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode3));
					//popNode3 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode3) - 1);
					{
						for (var i = methodNode.instructions.indexOf(popNode3); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getType() == AbstractInsnNode.LABEL) {
								popNode3 = insnNode;
								break;
							}
						}
					}
					var popNode3_1 = ASMAPI.findFirstInstructionAfter(methodNode, -1, methodNode.instructions.indexOf(targetNode3) + 1);
					var skipNode3 = new LabelNode();
					
					var targetNode4 = ASMAPI.findFirstMethodCallAfter(methodNode, ASMAPI.MethodType.STATIC, "net/optifine/reflect/Reflector", "call", "(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;", methodNode.instructions.indexOf(popNode3_1));
					//targetNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(targetNode4));
					{
						for (var i = methodNode.instructions.indexOf(targetNode4); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getOpcode() == -1) {
								targetNode4 = insnNode;
								break;
							}
						}
					}
					var popNode4 = ASMAPI.findFirstMethodCallBefore(methodNode, ASMAPI.MethodType.INTERFACE, "java/util/Iterator", "hasNext", "()Z", methodNode.instructions.indexOf(targetNode4));
					//popNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode4));
					//popNode4 = ASMAPI.findFirstInstructionBefore(methodNode, -1, methodNode.instructions.indexOf(popNode4) - 1);
					{
						for (var i = methodNode.instructions.indexOf(popNode4); i >= 0; i--) {
							var insnNode = methodNode.instructions.get(i);
							if (insnNode.getType() == AbstractInsnNode.LABEL) {
								popNode4 = insnNode;
								break;
							}
						}
					}
					var popNode4_1 = ASMAPI.findFirstInstructionAfter(methodNode, -1, methodNode.instructions.indexOf(targetNode4) + 1);
					var skipNode4 = new LabelNode();
					
					methodNode.instructions.insert(targetNode1, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 6),
							new VarInsnNode(Opcodes.ALOAD, 1),
							new VarInsnNode(Opcodes.ALOAD, 9),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "preRenderEntities", "(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/math/vector/Matrix4f;)V", false)
					));
					
					methodNode.instructions.insert(targetNode2, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 44),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderEntity", "(Lnet/minecraft/entity/Entity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode2)
					));
					
					methodNode.instructions.insert(targetNode3, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 47),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode3),
							
							new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "CLIENT_CONFIG", "Lmeldexun/entityculling/config/EntityCullingConfig$ClientConfig;"),
							new FieldInsnNode(Opcodes.GETFIELD, "meldexun/entityculling/config/EntityCullingConfig$ClientConfig", "enabled", "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/ForgeConfigSpec$BooleanValue", "get", "()Ljava/lang/Object;", false),
							new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false),
							new JumpInsnNode(Opcodes.IFEQ, skipNode3),
							
							new VarInsnNode(Opcodes.ALOAD, 47),
							new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/util/IBoundingBoxCache", "getOrCacheBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", true),
							new VarInsnNode(Opcodes.ASTORE, 48),
							new JumpInsnNode(Opcodes.GOTO, popNode3_1),
							
							skipNode3
					));
					
					methodNode.instructions.insert(targetNode4, ASMAPI.listOf(
							new VarInsnNode(Opcodes.ALOAD, 45),
							new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/WorldRendererHook", "shouldRenderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z", false),
							new JumpInsnNode(Opcodes.IFEQ, popNode4),
							
							new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "CLIENT_CONFIG", "Lmeldexun/entityculling/config/EntityCullingConfig$ClientConfig;"),
							new FieldInsnNode(Opcodes.GETFIELD, "meldexun/entityculling/config/EntityCullingConfig$ClientConfig", "enabled", "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/ForgeConfigSpec$BooleanValue", "get", "()Ljava/lang/Object;", false),
							new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"),
							new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false),
							new JumpInsnNode(Opcodes.IFEQ, skipNode4),
							
							new VarInsnNode(Opcodes.ALOAD, 45),
							new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meldexun/entityculling/util/IBoundingBoxCache", "getOrCacheBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", true),
							new VarInsnNode(Opcodes.ASTORE, 46),
							new JumpInsnNode(Opcodes.GOTO, popNode4_1),
							
							skipNode4
					));
				}
				
				return methodNode;
			}
		}
	}
}