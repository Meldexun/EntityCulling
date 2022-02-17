package meldexun.entityculling.asm;

import java.util.Collection;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil.ASMUtil;
import meldexun.asmutil.transformer.clazz.AbstractClassTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.tileentity.TileEntity;

/**
 * Transformed methods:<br>
 * - {@linkplain Entity}<br>
 * - {@linkplain TileEntity}<br>
 * - {@linkplain RenderGlobal#setupTerrain(Entity, double, ICamera, int, boolean)}<br>
 * - {@linkplain RenderGlobal#renderEntities(Entity, ICamera, float)}<br>
 * - {@linkplain RenderGlobal#updateTileEntities(Collection, Collection)}<br>
 * - {@linkplain Render#shouldRender(Entity, ICamera, double, double, double)}<br>
 * - {@linkplain TileEntityRendererDispatcher#render(TileEntity, float, int)}<br>
 * - {@linkplain Minecraft#getLimitFramerate()}<br>
 * - {@linkplain ClippingHelper#isBoxInFrustum(double, double, double, double, double, double)}<br>
 */
public class EntityCullingClassTransformer extends AbstractClassTransformer implements IClassTransformer {

	public static final boolean OPTIFINE_DETECTED;
	static {
		boolean flag = false;
		try {
			Class.forName("optifine.OptiFineClassTransformer", false, EntityCullingPlugin.class.getClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		OPTIFINE_DETECTED = flag;
	}

	@Override
	protected void registerTransformers() {
		// @formatter:off
		this.registerMethodTransformer("buy", "a", "(Lvg;DLbxy;IZ)V", "net/minecraft/client/renderer/RenderGlobal", "setupTerrain", "(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: RenderGlobal#setupTerrain(Entity, double, ICamera, int, boolean)");

			if (OPTIFINE_DETECTED) {
				AbstractInsnNode targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", "java/util/List", "add", "(Ljava/lang/Object;)Z");
				targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", "java/util/List", "add", "(Ljava/lang/Object;)Z", targetNode1);
				targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode1);
				AbstractInsnNode popNode1 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.JUMP_INSN, targetNode1);
				popNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, popNode1);

				methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, 28),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/RenderGlobalHook", "shouldRenderChunkShadow", "(Lnet/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation;)Z", false),
					new JumpInsnNode(Opcodes.IFEQ, (LabelNode) popNode1)
				));
			}
		});

		this.registerMethodTransformer("buy", "a", "(Lvg;Lbxy;F)V", "net/minecraft/client/renderer/RenderGlobal", "renderEntities", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: RenderGlobal#renderEntities(Entity, ICamera, float)");

			AbstractInsnNode targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;", "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;");
			targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode1);
			AbstractInsnNode popNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "preDrawBatch", "()V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "preDrawBatch", "()V", targetNode1);
			popNode1 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "rl", "c", "(Ljava/lang/String;)V", "net/minecraft/profiler/Profiler", "endStartSection", "(Ljava/lang/String;)V", popNode1);
			if (OPTIFINE_DETECTED) {
				popNode1 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKESTATIC, "net/optifine/shaders/Shaders", "endEntities", "()V", "net/optifine/shaders/Shaders", "endEntities", "()V", popNode1);
				popNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.JUMP_INSN, popNode1);
			}
			popNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, popNode1);

			AbstractInsnNode targetNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "preDrawBatch", "()V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "preDrawBatch", "()V", popNode1);
			if (OPTIFINE_DETECTED) {
				targetNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESTATIC, "bxf", "updateTextRenderDistance", "()V", "net/minecraft/client/renderer/tileentity/TileEntitySignRenderer", "updateTextRenderDistance", "()V", targetNode2);
			}
			targetNode2 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode2);
			AbstractInsnNode popNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "drawBatch", "(I)V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "drawBatch", "(I)V", targetNode2);
			if (OPTIFINE_DETECTED) {
				popNode2 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "net/optifine/reflect/ReflectorMethod", "exists", "()Z", "net/optifine/reflect/ReflectorMethod", "exists", "()Z", popNode2);
			}
			popNode2 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, popNode2);

			methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
				new VarInsnNode(Opcodes.FLOAD, 3),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/RenderGlobalHook", "renderEntities", "(F)Z", false),
				new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode1)
			));

			methodNode.instructions.insert(targetNode2, ASMUtil.listOf(
				new VarInsnNode(Opcodes.FLOAD, 3),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/RenderGlobalHook", "renderTileEntities", "(F)Z", false),
				new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode2)
			));
		});
		// @formatter:on
	}

}
