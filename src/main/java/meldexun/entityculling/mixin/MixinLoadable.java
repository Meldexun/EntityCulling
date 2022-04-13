package meldexun.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import meldexun.entityculling.util.ILoadable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

@Mixin(value = { Entity.class, TileEntity.class })
public class MixinLoadable implements ILoadable {

	@Unique
	private boolean isChunkLoaded = true;

	@Override
	public boolean isChunkLoaded() {
		return isChunkLoaded;
	}

	@Override
	public void setChunkLoaded(boolean isChunkLoaded) {
		this.isChunkLoaded = isChunkLoaded;
	}

}
