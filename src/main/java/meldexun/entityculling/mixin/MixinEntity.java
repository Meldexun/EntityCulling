package meldexun.entityculling.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.IBoundingBoxCache;
import meldexun.entityculling.util.IEntityRendererCache;
import meldexun.entityculling.util.MutableAABB;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public class MixinEntity implements IBoundingBoxCache, IEntityRendererCache {

	@Unique
	private final MutableAABB cachedBoundingBox = new MutableAABB();
	@Unique
	private Render<Entity> renderer;

	@Unique
	@Override
	public void updateCachedBoundingBox(double partialTicks) {
		cachedBoundingBox.set(((Entity) (Object) this).getRenderBoundingBox());
		cachedBoundingBox.grow(0.5D);
		cachedBoundingBox.offset(
				-(((Entity) (Object) this).posX - ((Entity) (Object) this).lastTickPosX) * (1.0D - partialTicks),
				-(((Entity) (Object) this).posY - ((Entity) (Object) this).lastTickPosY) * (1.0D - partialTicks),
				-(((Entity) (Object) this).posZ - ((Entity) (Object) this).lastTickPosZ) * (1.0D - partialTicks));
		Vec3d v = EntityCullingConfig.entity.entityBoundingBoxGrowthListImpl.get((Entity) (Object) this);
		if (v != null) {
			cachedBoundingBox.grow(v);
		}
	}

	@Unique
	@Override
	public MutableAABB getCachedBoundingBox() {
		return this.cachedBoundingBox;
	}

	@Inject(method = "onAddedToWorld", at = @At("HEAD"), remap = false)
	public void onAddedToWorld(CallbackInfo info) {
		if (!((Entity) (Object) this).world.isRemote)
			return;
		renderer = loadRenderer((Entity) (Object) this);
	}

	@SuppressWarnings("unchecked")
	@Unique
	@Override
	@Nullable
	public <T extends Entity> Render<T> getRenderer() {
		return (Render<T>) renderer;
	}

}
