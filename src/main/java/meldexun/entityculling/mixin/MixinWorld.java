package meldexun.entityculling.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.util.ILoadable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Mixin(World.class)
public class MixinWorld {

	@Inject(method = "markTileEntityForRemoval", at = @At("HEAD"))
	public void markTileEntityForRemoval(TileEntity tileEntity, CallbackInfo info) {
		((ILoadable) tileEntity).setChunkLoaded(false);
	}

	@Inject(method = "unloadEntities", at = @At("HEAD"))
	public void unloadEntities(Collection<Entity> entityCollection, CallbackInfo info) {
		entityCollection.forEach(e -> ((ILoadable) e).setChunkLoaded(false));
	}

}
