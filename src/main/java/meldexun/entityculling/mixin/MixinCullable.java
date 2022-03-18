package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.util.ICullable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

@Mixin(value = { Entity.class, TileEntity.class })
public class MixinCullable implements ICullable {

	@Unique
	private int culling_lastFrameUpdated = Integer.MIN_VALUE;
	@Unique
	private int culling_id;
	@Unique
	private boolean culled;
	@Unique
	private boolean shadowCulled;
	@Unique
	private boolean canBeOcclusionCulled;

	@Unique
	@Override
	public int culling_getLastTimeUpdated() {
		return culling_lastFrameUpdated;
	}

	@Unique
	@Override
	public void culling_setLastTimeUpdated(int lastFrameUpdated) {
		culling_lastFrameUpdated = lastFrameUpdated;
	}

	@Unique
	@Override
	public int culling_getId() {
		return culling_id;
	}

	@Unique
	@Override
	public void culling_setId(int id) {
		culling_id = id;
	}

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
