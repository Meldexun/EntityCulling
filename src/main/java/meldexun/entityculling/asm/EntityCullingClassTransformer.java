package meldexun.entityculling.asm;

import net.minecraft.launchwrapper.IClassTransformer;

public class EntityCullingClassTransformer implements IClassTransformer {

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
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

}
