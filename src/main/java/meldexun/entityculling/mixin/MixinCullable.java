package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.util.ICullable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

@Mixin(value = { Entity.class, TileEntity.class })
public class MixinCullable implements ICullable {

	// ----- opengl culling ----- //

	@Unique
	private final CullInfo cullInfo = new CullInfo();
	@Unique
	private final CullInfo shadowCullInfo = new CullInfo();

	@Unique
	@Override
	public CullInfo getCullInfo() {
		return cullInfo;
	}

	@Unique
	@Override
	public CullInfo getShadowCullInfo() {
		return shadowCullInfo;
	}

	// ----- raytraced culling ----- //

	@Unique
	private boolean culled;
	@Unique
	private boolean shadowCulled;
	@Unique
	private boolean canBeOcclusionCulled;

	@Unique
	@Override
	public boolean isCulled() {
		return culled;
	}

	@Unique
	@Override
	public void setCulled(boolean culled) {
		this.culled = culled;
	}

	@Unique
	@Override
	public boolean isShadowCulled() {
		return shadowCulled;
	}

	@Unique
	@Override
	public void setShadowCulled(boolean shadowCulled) {
		this.shadowCulled = shadowCulled;
	}

	@Unique
	@Override
	public boolean canBeOcclusionCulled() {
		return canBeOcclusionCulled;
	}

	@Unique
	@Override
	public void setCanBeOcclusionCulled(boolean canBeOcclusionCulled) {
		this.canBeOcclusionCulled = canBeOcclusionCulled;
	}

}
