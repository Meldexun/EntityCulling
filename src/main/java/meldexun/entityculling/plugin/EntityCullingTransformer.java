package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import meldexun.entityculling.EntityCullingContainer;
import meldexun.entityculling.plugin.transformer.ClassTransformer;
import meldexun.entityculling.plugin.transformer.FieldTransformer;
import meldexun.entityculling.plugin.transformer.MethodTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class EntityCullingTransformer implements IClassTransformer {

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

				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook$Optifine", "renderEntities", "()Z", false));
				method.instructions.insertBefore(targetNode1, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode1));
				method.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook$Optifine", "renderTileEntities", "()Z", false));
				method.instructions.insertBefore(targetNode2, new JumpInsnNode(Opcodes.IFNE, (LabelNode) popNode2));
			}));
		}
		registerMethodTransformer(new MethodTransformer("bze", "net.minecraft.client.renderer.entity.Render", "a", "shouldRender", "(Lvg;Lbxy;DDD)Z", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", method -> {
			// printMethodInstructions(method);

			AbstractInsnNode targetNode = method.instructions.get(0);

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
		EntityCullingContainer.LOGGER.info(sb);
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
