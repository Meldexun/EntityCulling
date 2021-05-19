package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.entityculling.plugin.transformer.ClassTransformer;
import meldexun.entityculling.plugin.transformer.FieldTransformer;
import meldexun.entityculling.plugin.transformer.MethodTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class EntityCullingTransformer implements IClassTransformer {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, List<ClassTransformer>> CLASS_TRANSFORMERS = new HashMap<>();
	private static final Map<String, Map<String, List<FieldTransformer>>> FIELD_TRANSFORMERS = new HashMap<>();
	private static final Map<String, Map<String, List<MethodTransformer>>> METHOD_TRANSFORMERS = new HashMap<>();

	public static final boolean IS_OPTIFINE_DETECTED;

	public static void registerClassTransformer(ClassTransformer classTransformer) {
		CLASS_TRANSFORMERS.computeIfAbsent(classTransformer.transformedClassName, key -> new ArrayList<>()).add(classTransformer);
	}

	public static void registerFieldTransformer(FieldTransformer fieldTransformer) {
		Map<String, List<FieldTransformer>> map = FIELD_TRANSFORMERS.computeIfAbsent(fieldTransformer.transformedClassName, key -> new HashMap<>());
		map.computeIfAbsent(fieldTransformer.fieldName, key -> new ArrayList<>()).add(fieldTransformer);
		map.computeIfAbsent(fieldTransformer.transformedFieldName, key -> new ArrayList<>()).add(fieldTransformer);
	}

	public static void registerMethodTransformer(MethodTransformer methodTransformer) {
		Map<String, List<MethodTransformer>> map = METHOD_TRANSFORMERS.computeIfAbsent(methodTransformer.transformedClassName, key -> new HashMap<>());
		map.computeIfAbsent(methodTransformer.methodName, key -> new ArrayList<>()).add(methodTransformer);
		map.computeIfAbsent(methodTransformer.transformedMethodName, key -> new ArrayList<>()).add(methodTransformer);
	}

	static {
		boolean flag = false;
		try {
			Class.forName("optifine.OptiFineClassTransformer", false, EntityCullingTransformer.class.getClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		IS_OPTIFINE_DETECTED = flag;

		registerClassTransformer(new ClassTransformer("ve", "net.minecraft.entity.Entity", classNode -> {
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledFast", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledSlow", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledShadowPass", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "query", "I", null, -1));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryInitialized", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryResultDirty", "Z", null, false));

			classNode.interfaces.add("meldexun/entityculling/ICullable");

			MethodNode methodIsCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledFast", "()Z", null, null);
			methodIsCulledFast.instructions.clear();
			methodIsCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledFast.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledFast", "Z"));
			methodIsCulledFast.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledFast);

			MethodNode methodSetCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledFast", "(Z)V", null, null);
			methodSetCulledFast.instructions.clear();
			methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledFast.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledFast", "Z"));
			methodSetCulledFast.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledFast);

			MethodNode methodIsCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledSlow", "()Z", null, null);
			methodIsCulledSlow.instructions.clear();
			methodIsCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledSlow.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledSlow", "Z"));
			methodIsCulledSlow.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledSlow);

			MethodNode methodSetCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledSlow", "(Z)V", null, null);
			methodSetCulledSlow.instructions.clear();
			methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledSlow.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledSlow", "Z"));
			methodSetCulledSlow.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledSlow);

			MethodNode methodIsCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledShadowPass", "()Z", null, null);
			methodIsCulledShadowPass.instructions.clear();
			methodIsCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "isCulledShadowPass", "Z"));
			methodIsCulledShadowPass.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledShadowPass);

			MethodNode methodSetCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledShadowPass", "(Z)V", null, null);
			methodSetCulledShadowPass.instructions.clear();
			methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "isCulledShadowPass", "Z"));
			methodSetCulledShadowPass.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledShadowPass);

			MethodNode methodGetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "getQuery", "()I", null, null);
			methodGetQuery.instructions.clear();
			methodGetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetQuery.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "query", "I"));
			methodGetQuery.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodGetQuery);

			MethodNode methodSetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "setQuery", "(I)V", null, null);
			methodSetQuery.instructions.clear();
			methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQuery.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "query", "I"));
			methodSetQuery.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQuery);

			MethodNode methodIsQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryInitialized", "()Z", null, null);
			methodIsQueryInitialized.instructions.clear();
			methodIsQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "queryInitialized", "Z"));
			methodIsQueryInitialized.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsQueryInitialized);

			MethodNode methodSetQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryInitialized", "(Z)V", null, null);
			methodSetQueryInitialized.instructions.clear();
			methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "queryInitialized", "Z"));
			methodSetQueryInitialized.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQueryInitialized);

			MethodNode methodIsQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryResultDirty", "()Z", null, null);
			methodIsQueryResultUpToDate.instructions.clear();
			methodIsQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/Entity", "queryResultDirty", "Z"));
			methodIsQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsQueryResultUpToDate);

			MethodNode methodSetQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryResultDirty", "(Z)V", null, null);
			methodSetQueryResultUpToDate.instructions.clear();
			methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/Entity", "queryResultDirty", "Z"));
			methodSetQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQueryResultUpToDate);
		}));
		registerClassTransformer(new ClassTransformer("avh", "net.minecraft.tileentity.TileEntity", classNode -> {
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledFast", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledSlow", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "isCulledShadowPass", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "query", "I", null, -1));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryInitialized", "Z", null, false));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "queryResultDirty", "Z", null, false));

			classNode.interfaces.add("meldexun/entityculling/ICullable");

			MethodNode methodIsCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledFast", "()Z", null, null);
			methodIsCulledFast.instructions.clear();
			methodIsCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledFast.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isCulledFast", "Z"));
			methodIsCulledFast.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledFast);

			MethodNode methodSetCulledFast = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledFast", "(Z)V", null, null);
			methodSetCulledFast.instructions.clear();
			methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledFast.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledFast.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isCulledFast", "Z"));
			methodSetCulledFast.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledFast);

			MethodNode methodIsCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledSlow", "()Z", null, null);
			methodIsCulledSlow.instructions.clear();
			methodIsCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledSlow.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isCulledSlow", "Z"));
			methodIsCulledSlow.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledSlow);

			MethodNode methodSetCulledSlow = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledSlow", "(Z)V", null, null);
			methodSetCulledSlow.instructions.clear();
			methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledSlow.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledSlow.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isCulledSlow", "Z"));
			methodSetCulledSlow.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledSlow);

			MethodNode methodIsCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "isCulledShadowPass", "()Z", null, null);
			methodIsCulledShadowPass.instructions.clear();
			methodIsCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "isCulledShadowPass", "Z"));
			methodIsCulledShadowPass.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsCulledShadowPass);

			MethodNode methodSetCulledShadowPass = new MethodNode(Opcodes.ACC_PUBLIC, "setCulledShadowPass", "(Z)V", null, null);
			methodSetCulledShadowPass.instructions.clear();
			methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetCulledShadowPass.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetCulledShadowPass.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "isCulledShadowPass", "Z"));
			methodSetCulledShadowPass.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetCulledShadowPass);

			MethodNode methodGetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "getQuery", "()I", null, null);
			methodGetQuery.instructions.clear();
			methodGetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetQuery.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "query", "I"));
			methodGetQuery.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodGetQuery);

			MethodNode methodSetQuery = new MethodNode(Opcodes.ACC_PUBLIC, "setQuery", "(I)V", null, null);
			methodSetQuery.instructions.clear();
			methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQuery.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQuery.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "query", "I"));
			methodSetQuery.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQuery);

			MethodNode methodIsQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryInitialized", "()Z", null, null);
			methodIsQueryInitialized.instructions.clear();
			methodIsQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "queryInitialized", "Z"));
			methodIsQueryInitialized.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsQueryInitialized);

			MethodNode methodSetQueryInitialized = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryInitialized", "(Z)V", null, null);
			methodSetQueryInitialized.instructions.clear();
			methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQueryInitialized.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQueryInitialized.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "queryInitialized", "Z"));
			methodSetQueryInitialized.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQueryInitialized);

			MethodNode methodIsQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "isQueryResultDirty", "()Z", null, null);
			methodIsQueryResultUpToDate.instructions.clear();
			methodIsQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodIsQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "queryResultDirty", "Z"));
			methodIsQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(methodIsQueryResultUpToDate);

			MethodNode methodSetQueryResultUpToDate = new MethodNode(Opcodes.ACC_PUBLIC, "setQueryResultDirty", "(Z)V", null, null);
			methodSetQueryResultUpToDate.instructions.clear();
			methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetQueryResultUpToDate.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
			methodSetQueryResultUpToDate.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "queryResultDirty", "Z"));
			methodSetQueryResultUpToDate.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetQueryResultUpToDate);

			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "prevAABB", "Lnet/minecraft/util/math/AxisAlignedBB;", null, null));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "prevPos", "Lnet/minecraft/util/math/BlockPos;", null, null));
			classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "prevState", "Lnet/minecraft/block/Block;", null, null));

			classNode.interfaces.add("meldexun/entityculling/ITileEntityBBCache");

			MethodNode methodGetPrevAABB = new MethodNode(Opcodes.ACC_PUBLIC, "getPrevAABB", "()Lnet/minecraft/util/math/AxisAlignedBB;", null, null);
			methodGetPrevAABB.instructions.clear();
			methodGetPrevAABB.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetPrevAABB.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "prevAABB", "Lnet/minecraft/util/math/AxisAlignedBB;"));
			methodGetPrevAABB.instructions.add(new InsnNode(Opcodes.ARETURN));
			classNode.methods.add(methodGetPrevAABB);

			MethodNode methodSetPrevAABB = new MethodNode(Opcodes.ACC_PUBLIC, "setPrevAABB", "(Lnet/minecraft/util/math/AxisAlignedBB;)V", null, null);
			methodSetPrevAABB.instructions.clear();
			methodSetPrevAABB.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetPrevAABB.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
			methodSetPrevAABB.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "prevAABB", "Lnet/minecraft/util/math/AxisAlignedBB;"));
			methodSetPrevAABB.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetPrevAABB);

			MethodNode methodGetPrevPos = new MethodNode(Opcodes.ACC_PUBLIC, "getPrevPos", "()Lnet/minecraft/util/math/BlockPos;", null, null);
			methodGetPrevPos.instructions.clear();
			methodGetPrevPos.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetPrevPos.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "prevPos", "Lnet/minecraft/util/math/BlockPos;"));
			methodGetPrevPos.instructions.add(new InsnNode(Opcodes.ARETURN));
			classNode.methods.add(methodGetPrevPos);

			MethodNode methodSetPrevPos = new MethodNode(Opcodes.ACC_PUBLIC, "setPrevPos", "(Lnet/minecraft/util/math/BlockPos;)V", null, null);
			methodSetPrevPos.instructions.clear();
			methodSetPrevPos.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetPrevPos.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
			methodSetPrevPos.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "prevPos", "Lnet/minecraft/util/math/BlockPos;"));
			methodSetPrevPos.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetPrevPos);

			MethodNode methodGetPrevState = new MethodNode(Opcodes.ACC_PUBLIC, "getPrevState", "()Lnet/minecraft/block/Block;", null, null);
			methodGetPrevState.instructions.clear();
			methodGetPrevState.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodGetPrevState.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "prevState", "Lnet/minecraft/block/Block;"));
			methodGetPrevState.instructions.add(new InsnNode(Opcodes.ARETURN));
			classNode.methods.add(methodGetPrevState);

			MethodNode methodSetPrevState = new MethodNode(Opcodes.ACC_PUBLIC, "setPrevState", "(Lnet/minecraft/block/Block;)V", null, null);
			methodSetPrevState.instructions.clear();
			methodSetPrevState.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			methodSetPrevState.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
			methodSetPrevState.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "prevState", "Lnet/minecraft/block/Block;"));
			methodSetPrevState.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(methodSetPrevState);
		}));
		if (!IS_OPTIFINE_DETECTED) {
			registerMethodTransformer(new MethodTransformer("buw", "net.minecraft.client.renderer.RenderGlobal", "a", "renderEntities", "(Lvg;Lbxy;F)V", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(297);
				AbstractInsnNode popNode1 = method.instructions.get(691);
				AbstractInsnNode targetNode2 = method.instructions.get(706);
				AbstractInsnNode popNode2 = method.instructions.get(835);

				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "renderEntities", "()Z", false));
				method.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode1));
				method.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "renderTileEntities", "()Z", false));
				method.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode2));
			}));
		} else {
			registerMethodTransformer(new MethodTransformer("buw", "net.minecraft.client.renderer.RenderGlobal", "a", "renderEntities", "(Lvg;Lbxy;F)V", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(374);
				AbstractInsnNode popNode1 = method.instructions.get(1039);
				AbstractInsnNode targetNode2 = method.instructions.get(1074);
				AbstractInsnNode popNode2 = method.instructions.get(1269);

				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "renderEntities", "()Z", false));
				method.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode1));
				method.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "renderTileEntities", "()Z", false));
				method.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode2));
			}));
			registerMethodTransformer(new MethodTransformer("buw", "net.minecraft.client.renderer.RenderGlobal", "a", "setupTerrain", "(Lvg;DLbxy;IZ)V", "(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(558);
				AbstractInsnNode popNode1 = method.instructions.get(565);

				method.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 28));
				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRenderChunkShadow", "(Ljava/lang/Object;)Z", false));
				method.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFEQ, (LabelNode) popNode1));
			}));
		}
		registerMethodTransformer(new MethodTransformer("bze", "net.minecraft.client.renderer.entity.Render", "a", "shouldRender", "(Lvg;Lbxy;DDD)Z", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", method -> {
			// printMethodInstructions(method);

			AbstractInsnNode targetNode = method.instructions.get(0);

			method.instructions.insertBefore(targetNode, new FieldInsnNode(Opcodes.GETSTATIC, "meldexun/entityculling/EntityCullingConfig", "enabled", "Z"));
			method.instructions.insertBefore(targetNode, new JumpInsnNode(Opcodes.IFEQ, (LabelNode) targetNode));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 1));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 2));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 3));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 5));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 7));
			method.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "shouldRender", "(Lnet/minecraft/client/renderer/entity/Render;Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", false));
			method.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IRETURN));
		}));
		registerMethodTransformer(new MethodTransformer("bwv", "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher", "a", "render", "(Lavj;FI)V", "(Lnet/minecraft/tileentity/TileEntity;FI)V", method -> {
			// printMethodInstructions(method);

			AbstractInsnNode targetNode = method.instructions.get(0);

			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 1));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 3));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
			method.instructions.insertBefore(targetNode, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "drawingBatch", "Z"));
			method.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "render", "(Lnet/minecraft/tileentity/TileEntity;IZ)Z", false));
			method.instructions.insertBefore(targetNode, new JumpInsnNode(Opcodes.IFEQ, (LabelNode) targetNode));
			method.instructions.insertBefore(targetNode, new InsnNode(Opcodes.RETURN));
		}));
		registerMethodTransformer(new MethodTransformer("bhz", "net.minecraft.client.Minecraft", "k", "getLimitFramerate", "()I", "()I", method -> {
			// printMethodInstructions(method);

			AbstractInsnNode targetNode1 = method.instructions.get(0);
			AbstractInsnNode popNode1 = method.instructions.get(15);

			method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "getLimitFramerate", "()I", false));
			method.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1));
		}));
		registerMethodTransformer(new MethodTransformer("bxz", "net.minecraft.client.renderer.culling.ClippingHelper", "b", "isBoxInFrustum", "(DDDDDD)Z", "(DDDDDD)Z", method -> {
			AbstractInsnNode targetNode = method.instructions.get(0);

			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
			method.instructions.insertBefore(targetNode, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/culling/ClippingHelper", "frustum", "[[F"));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 1));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 3));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 5));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 7));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 9));
			method.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.DLOAD, 11));
			method.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "isBoxInFrustum", "([[FDDDDDD)Z", false));
			method.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IRETURN));
		}));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		List<ClassTransformer> classTransformers = CLASS_TRANSFORMERS.get(transformedName);
		Map<String, List<FieldTransformer>> fieldTransformerMap = FIELD_TRANSFORMERS.get(transformedName);
		Map<String, List<MethodTransformer>> methodTransformerMap = METHOD_TRANSFORMERS.get(transformedName);

		if ((classTransformers == null || classTransformers.isEmpty()) && (fieldTransformerMap == null || fieldTransformerMap.isEmpty()) && (methodTransformerMap == null || methodTransformerMap.isEmpty())) {
			return basicClass;
		}

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);

		if (classTransformers != null) {
			for (ClassTransformer classTransformer : classTransformers) {
				classTransformer.transformer.accept(classNode);
			}
		}

		if (fieldTransformerMap != null) {
			for (FieldNode fieldNode : classNode.fields) {
				List<FieldTransformer> fieldTransformers = fieldTransformerMap.get(fieldNode.name);

				if (fieldTransformers == null || fieldTransformers.isEmpty()) {
					continue;
				}

				for (FieldTransformer fieldTransformer : fieldTransformers) {
					if (fieldTransformer.canApplyTransform(fieldNode)) {
						fieldTransformer.transformer.accept(fieldNode);
					}
				}
			}
		}

		if (methodTransformerMap != null) {
			for (MethodNode methodNode : classNode.methods) {
				List<MethodTransformer> methodTransformers = methodTransformerMap.get(methodNode.name);

				if (methodTransformers == null || methodTransformers.isEmpty()) {
					continue;
				}

				for (MethodTransformer methodTransformer : methodTransformers) {
					if (methodTransformer.canApplyTransform(methodNode)) {
						methodTransformer.transformer.accept(methodNode);
					}
				}
			}
		}

		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}

	public static void printMethodInstructions(MethodNode method) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (AbstractInsnNode instruction : method.instructions.toArray()) {
			if (i >= 0) {
				sb.append("\n" + i + " " + insnToString(instruction));
			}
			i++;
		}
		LOGGER.info(sb);
	}

	public static String insnToString(AbstractInsnNode insn) {
		StringBuilder sb = new StringBuilder(insn.getOpcode() + " " + insn.getClass().getSimpleName() + "\t");
		if (insn instanceof MethodInsnNode) {
			sb.append(" " + ((MethodInsnNode) insn).owner);
			sb.append(" " + ((MethodInsnNode) insn).name);
			sb.append(" " + ((MethodInsnNode) insn).desc);
		} else if (insn instanceof VarInsnNode) {
			sb.append(" " + ((VarInsnNode) insn).var);
		} else if (insn instanceof FieldInsnNode) {
			sb.append(" " + ((FieldInsnNode) insn).owner);
			sb.append(" " + ((FieldInsnNode) insn).name);
			sb.append(" " + ((FieldInsnNode) insn).desc);
		} else if (insn instanceof JumpInsnNode) {
			sb.append(" " + ((JumpInsnNode) insn).label.getLabel());
		} else if (insn instanceof LabelNode) {
			sb.append(" " + ((LabelNode) insn).getLabel());
		} else if (insn instanceof FrameNode) {
			sb.append(" " + ((FrameNode) insn).local);
			sb.append(" " + ((FrameNode) insn).stack);
		}
		return sb.toString();
	}

}
