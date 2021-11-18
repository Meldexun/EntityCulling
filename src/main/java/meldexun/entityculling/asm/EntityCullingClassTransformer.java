package meldexun.entityculling.asm;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
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
		this.registerClassTransformer("vg", "net/minecraft/entity/Entity", classNode -> {
			ASMUtil.LOGGER.info("Transforming class: Entity");

			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulled", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isShadowCulled", "Z", null, false));

			classNode.interfaces.add("meldexun/entityculling/util/ICullable");

			MethodNode methodIsCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isCulled", "()Z", null, null);
			methodIsCulled.instructions.clear();
			methodIsCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulled", "Z"));
			methodIsCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulled);

			MethodNode methodSetCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setCulled", "(Z)V", null, null);
			methodSetCulled.instructions.clear();
			methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulled", "Z"));
			methodSetCulled.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulled);

			MethodNode methodIsShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isShadowCulled", "()Z", null, null);
			methodIsShadowCulled.instructions.clear();
			methodIsShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsShadowCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isShadowCulled", "Z"));
			methodIsShadowCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsShadowCulled);

			MethodNode methodSetShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setShadowCulled", "(Z)V", null, null);
			methodSetShadowCulled.instructions.clear();
			methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetShadowCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isShadowCulled", "Z"));
			methodSetShadowCulled.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetShadowCulled);
		});

		this.registerClassTransformer("avj", "net/minecraft/tileentity/TileEntity", classNode -> {
			ASMUtil.LOGGER.info("Transforming class: TileEntity");

			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulled", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isShadowCulled", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCacheable", "I", null, 0));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;", null, null));

			classNode.interfaces.add("meldexun/entityculling/util/ICullable");
			classNode.interfaces.add("meldexun/entityculling/util/IBoundingBoxCache");

			MethodNode methodIsCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isCulled", "()Z", null, null);
			methodIsCulled.instructions.clear();
			methodIsCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isCulled", "Z"));
			methodIsCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulled);

			MethodNode methodSetCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setCulled", "(Z)V", null, null);
			methodSetCulled.instructions.clear();
			methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isCulled", "Z"));
			methodSetCulled.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulled);

			MethodNode methodIsShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "isShadowCulled", "()Z", null, null);
			methodIsShadowCulled.instructions.clear();
			methodIsShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsShadowCulled.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isShadowCulled", "Z"));
			methodIsShadowCulled.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsShadowCulled);

			MethodNode methodSetShadowCulled = new MethodNode(Opcodes.ACC_PUBLIC, "setShadowCulled", "(Z)V", null, null);
			methodSetShadowCulled.instructions.clear();
			methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetShadowCulled.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetShadowCulled.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isShadowCulled", "Z"));
			methodSetShadowCulled.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetShadowCulled);

			MethodNode methodIsCacheable = new MethodNode(Opcodes.ACC_PUBLIC, "isCacheable", "()I", null, null);
			methodIsCacheable.instructions.clear();
			methodIsCacheable.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCacheable.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isCacheable", "I"));
			methodIsCacheable.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCacheable);

			MethodNode methodSetCacheable = new MethodNode(Opcodes.ACC_PUBLIC, "setCacheable", "(I)V", null, null);
			methodSetCacheable.instructions.clear();
			methodSetCacheable.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCacheable.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCacheable.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isCacheable", "I"));
			methodSetCacheable.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCacheable);

			MethodNode methodGetCachedBoundingBox = new MethodNode(Opcodes.ACC_PUBLIC, "getCachedBoundingBox", "()Lnet/minecraft/util/math/AxisAlignedBB;", null, null);
			methodGetCachedBoundingBox.instructions.clear();
			methodGetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetCachedBoundingBox.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;"));
			methodGetCachedBoundingBox.instructions.add(new InsnNode(Opcodes.ARETURN));
			classNode.methods.add(methodGetCachedBoundingBox);

			MethodNode methodSetCachedBoundingBox = new MethodNode(Opcodes.ACC_PUBLIC, "setCachedBoundingBox", "(Lnet/minecraft/util/math/AxisAlignedBB;)V", null, null);
			methodSetCachedBoundingBox.instructions.clear();
			methodSetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCachedBoundingBox.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
			methodSetCachedBoundingBox.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "cachedBoundingBox", "Lnet/minecraft/util/math/AxisAlignedBB;"));
			methodSetCachedBoundingBox.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCachedBoundingBox);
		});

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

			methodNode.instructions.insert(ASMUtil.listOf(
				new VarInsnNode(Opcodes.ALOAD, 4),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/RenderGlobalHook", "setup", "(Lnet/minecraft/client/renderer/culling/ICamera;)V", false)
			));
		});

		this.registerMethodTransformer("buy", "a", "(Lvg;Lbxy;F)V", "net/minecraft/client/renderer/RenderGlobal", "renderEntities", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: RenderGlobal#renderEntities(Entity, ICamera, float)");

			AbstractInsnNode targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;", "com/google/common/collect/Lists", "newArrayList", "()Ljava/util/ArrayList;");
			targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode1);
			AbstractInsnNode popNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "preDrawBatch", "()V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "preDrawBatch", "()V", targetNode1);
			popNode1 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "rl", "c", "(Ljava/lang/String;)V", "net/minecraft/profiler/Profiler", "endStartSection", "(Ljava/lang/String;)V", popNode1);
			try {
				// optifine compatibility
				popNode1 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKESTATIC, "net/optifine/shaders/Shaders", "endEntities", "()V", "net/optifine/shaders/Shaders", "endEntities", "()V", popNode1);
				popNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.JUMP_INSN, popNode1);
			} catch (NoSuchElementException e) {
				// ignore
			}
			popNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, popNode1);

			AbstractInsnNode targetNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "preDrawBatch", "()V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "preDrawBatch", "()V", popNode1);
			try {
				// optifine compatibility
				targetNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESTATIC, "bxf", "updateTextRenderDistance", "()V", "net/minecraft/client/renderer/tileentity/TileEntitySignRenderer", "updateTextRenderDistance", "()V", targetNode2);
			} catch (NoSuchElementException e) {
				// ignore
			}
			targetNode2 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode2);
			AbstractInsnNode popNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEVIRTUAL, "bwx", "drawBatch", "(I)V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "drawBatch", "(I)V", targetNode2);
			try {
				// optifine compatibility
				popNode2 = ASMUtil.findLastMethodCall(methodNode, Opcodes.INVOKESTATIC, "net/optifine/reflect/ReflectorField", "exists", "()Z", "net/optifine/reflect/ReflectorField", "exists", "()Z", popNode2);
			} catch (NoSuchElementException e) {
				// ignore
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

		this.registerMethodTransformer("buy", "a", "(Ljava/util/Collection;Ljava/util/Collection;)V", "net/minecraft/client/renderer/RenderGlobal", "updateTileEntities", "(Ljava/util/Collection;Ljava/util/Collection;)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: RenderGlobal#updateTileEntities(Collection, Collection)");

			LabelNode popNode1 = new LabelNode();

			methodNode.instructions.insert(ASMUtil.listOf(
				new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "enabled", "Z"),
				new JumpInsnNode(Opcodes.IFEQ, popNode1),
				new InsnNode(Opcodes.RETURN),
				popNode1
			));
		});

		this.registerMethodTransformer("bzg", "a", "(Lvg;Lbxy;DDD)Z", "net/minecraft/client/renderer/entity/Render", "shouldRender", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: Render#shouldRender(Entity, ICamera, double, double, double)");

			LabelNode popNode1 = new LabelNode();

			methodNode.instructions.insert(ASMUtil.listOf(
				new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "enabled", "Z"),
				new JumpInsnNode(Opcodes.IFEQ, popNode1),
				new VarInsnNode(Opcodes.ALOAD, 0),
				new VarInsnNode(Opcodes.ALOAD, 1),
				new VarInsnNode(Opcodes.ALOAD, 2),
				new VarInsnNode(Opcodes.DLOAD, 3),
				new VarInsnNode(Opcodes.DLOAD, 5),
				new VarInsnNode(Opcodes.DLOAD, 7),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/RenderHook", "shouldRender", "(Lnet/minecraft/client/renderer/entity/Render;Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", false),
				new InsnNode(Opcodes.IRETURN),
				popNode1
			));
		});

		this.registerMethodTransformer("bwx", "a", "(Lavj;FI)V", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "render", "(Lnet/minecraft/tileentity/TileEntity;FI)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: TileEntityRendererDispatcher#render(TileEntity, float, int)");

			// TODO just skip unwanted checks instead of overwriting the complete method?
			LabelNode popNode1 = new LabelNode();

			methodNode.instructions.insert(ASMUtil.listOf(
				new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "enabled", "Z"),
				new JumpInsnNode(Opcodes.IFEQ, popNode1),
				new VarInsnNode(Opcodes.ALOAD, 1),
				new VarInsnNode(Opcodes.ILOAD, 3),
				new VarInsnNode(Opcodes.ALOAD, 0),
				new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "drawingBatch", "Z"),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/TileEntityRendererDispatcherHook", "render", "(Lnet/minecraft/tileentity/TileEntity;IZ)Z", false),
				new InsnNode(Opcodes.RETURN),
				popNode1
			));
		});

		this.registerMethodTransformer("bib", "k", "()I", "net/minecraft/client/Minecraft", "getLimitFramerate", "()I", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: Minecraft#getLimitFramerate()");

			LabelNode popNode1 = new LabelNode();

			methodNode.instructions.insert(ASMUtil.listOf(
				new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "enabled", "Z"),
				new JumpInsnNode(Opcodes.IFEQ, popNode1),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/MinecraftHook", "getLimitFramerate", "()I", false),
				new InsnNode(Opcodes.IRETURN),
				popNode1
			));
		});

		this.registerMethodTransformer("byb", "b", "(DDDDDD)Z", "net/minecraft/client/renderer/culling/ClippingHelper", "isBoxInFrustum", "(DDDDDD)Z", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: ClippingHelper#isBoxInFrustum(double, double, double, double, double, double)");

			LabelNode popNode1 = new LabelNode();

			methodNode.instructions.insert(ASMUtil.listOf(
				new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/config/EntityCullingConfig", "enabled", "Z"),
				new JumpInsnNode(Opcodes.IFEQ, popNode1),
				new VarInsnNode(Opcodes.ALOAD, 0),
				new VarInsnNode(Opcodes.DLOAD, 1),
				new VarInsnNode(Opcodes.DLOAD, 3),
				new VarInsnNode(Opcodes.DLOAD, 5),
				new VarInsnNode(Opcodes.DLOAD, 7),
				new VarInsnNode(Opcodes.DLOAD, 9),
				new VarInsnNode(Opcodes.DLOAD, 11),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/asm/hook/ClippingHelperHook", "isBoxInFrustum", "(Lnet/minecraft/client/renderer/culling/ClippingHelper;DDDDDD)Z", false),
				new InsnNode(Opcodes.IRETURN),
				popNode1
			));
		});
		// @formatter:on
	}

}
