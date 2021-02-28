package meldexun.entityculling.plugin;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("meldexun.entityculling.plugin")
public class EntityCullingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "meldexun.entityculling.plugin.EntityCullingTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return "meldexun.entityculling.EntityCullingContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
