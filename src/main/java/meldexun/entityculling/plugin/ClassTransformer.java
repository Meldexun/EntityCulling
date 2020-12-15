package meldexun.entityculling.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {

	private static final Map<String, Map<String, List<MethodTransformer>>> METHOD_TRANSFORMERS = new HashMap<>();

	public static boolean isOptifineDetected = false;

	private static void registerMethodTransformer(MethodTransformer methodTransformer) {
		Map<String, List<MethodTransformer>> map = METHOD_TRANSFORMERS.computeIfAbsent(methodTransformer.transformedClassName, key -> new HashMap<>());
		List<MethodTransformer> list1 = map.computeIfAbsent(methodTransformer.methodName, key -> new ArrayList<>());
		List<MethodTransformer> list2 = map.computeIfAbsent(methodTransformer.transformedMethodName, key -> new ArrayList<>());
		list1.add(methodTransformer);
		list2.add(methodTransformer);
	}

	static {
		try {
			File modsFolder = new File(EntityCullingPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			for (File mod : FileUtils.listFiles(modsFolder, new String[] { "jar" }, false)) {
				if (mod.getName().contains("OptiFine")) {
					isOptifineDetected = true;
					break;
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (!isOptifineDetected) {
			registerMethodTransformer(new MethodTransformer("buo", "net.minecraft.client.renderer.EntityRenderer", "a", "renderWorldPass", "(IFJ)V", "(IFJ)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(465);
				AbstractInsnNode targetNode2 = method.instructions.get(808);

				method.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 8));
				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "updateEntityLists", "(Lnet/minecraft/client/renderer/culling/ICamera;)V", false));
				method.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook", "clearEntityLists", "()V", false));
			}));
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
			registerMethodTransformer(new MethodTransformer("buo", "net.minecraft.client.renderer.EntityRenderer", "a", "renderWorldPass", "(IFJ)V", "(IFJ)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(724);
				AbstractInsnNode targetNode2 = method.instructions.get(1042);

				method.instructions.insertBefore(targetNode1, new VarInsnNode(Opcodes.ALOAD, 10));
				method.instructions.insertBefore(targetNode1, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook$Optifine", "updateEntityLists", "(Lnet/minecraft/client/renderer/culling/ICamera;)V", false));
				method.instructions.insertBefore(targetNode2, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/entityculling/plugin/Hook$Optifine", "clearEntityLists", "()V", false));
			}));
			registerMethodTransformer(new MethodTransformer("buw", "net.minecraft.client.renderer.RenderGlobal", "a", "renderEntities", "(Lvg;Lbxy;F)V", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", method -> {
				// printMethodInstructions(method);

				AbstractInsnNode targetNode1 = method.instructions.get(388);
				AbstractInsnNode popNode1 = method.instructions.get(1034);
				AbstractInsnNode targetNode2 = method.instructions.get(1076);
				AbstractInsnNode popNode2 = method.instructions.get(1271);

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
		Map<String, List<MethodTransformer>> map = METHOD_TRANSFORMERS.get(transformedName);

		if (map == null || map.isEmpty()) {
			return basicClass;
		}

		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);

			for (MethodNode method : classNode.methods) {
				List<MethodTransformer> list = map.get(method.name);

				if (list == null || list.isEmpty()) {
					continue;
				}

				for (MethodTransformer methodTransformer : list) {
					if (methodTransformer.canApplyMethodTransform(method)) {
						methodTransformer.applyMethodTransform(method);
					}
				}
			}

			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			try {
				classNode.accept(classWriter);
			} catch (Exception e) {
				e.printStackTrace();
				return basicClass;
			}
			return classWriter.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return basicClass;
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
		System.out.println(sb.toString());
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
