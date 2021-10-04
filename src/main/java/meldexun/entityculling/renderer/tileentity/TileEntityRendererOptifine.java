package meldexun.entityculling.renderer.tileentity;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.ICullable;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRendererOptifine extends TileEntityRenderer {

	private static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	private static final ReflectionMethod<Void> NEXT_BLOCK_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextBlockEntity", "nextBlockEntity",
			TileEntity.class);
	private boolean isShaders = false;

	@Override
	public void setup(ICamera camera, double camX, double camY, double camZ) {
		this.isShaders = IS_SHADERS.invoke(null);
		super.setup(camera, camX, camY, camZ);
	}

	@Override
	protected void fillTileEntityLists(ICamera camera, double camX, double camY, double camZ) {
		if (IS_SHADOW_PASS.getBoolean(null) && !EntityCullingConfig.optifineShaderOptions.tileEntityShadowsEnabled) {
			return;
		}
		int r = this.renderedTileEntities;
		int o = this.occludedTileEntities;
		int t = this.totalTileEntities;
		super.fillTileEntityLists(camera, camX, camY, camZ);
		if (IS_SHADOW_PASS.getBoolean(null)) {
			this.renderedTileEntities = r;
			this.occludedTileEntities = o;
			this.totalTileEntities = t;
		}
	}

	@Override
	protected void addToRenderLists(TileEntity tileEntity, ICamera camera, double camX, double camY, double camZ) {
		if (IS_SHADOW_PASS.getBoolean(null) && EntityCullingConfig.optifineShaderOptions.tileEntityShadowsDistanceLimited) {
			double d = EntityCullingConfig.optifineShaderOptions.tileEntityShadowsMaxDistance * 16.0D;
			if (tileEntity.getDistanceSq(camX, camY, camZ) > d * d) {
				return;
			}
		}
		super.addToRenderLists(tileEntity, camera, camX, camY, camZ);
	}

	@Override
	protected void drawBox(TileEntity tileEntity, double camX, double camY, double camZ) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return;
		}
		super.drawBox(tileEntity, camX, camY, camZ);
	}

	@Override
	protected boolean isOcclusionCulled(TileEntity tileEntity) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return ((ICullable) tileEntity).isShadowCulled();
		}
		return super.isOcclusionCulled(tileEntity);
	}

	@Override
	protected void preRenderTileEntity(TileEntity tileEntity) {
		if (this.isShaders) {
			NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
		}
		super.preRenderTileEntity(tileEntity);
	}

	@Override
	protected void drawPoints() {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return;
		}
		super.drawPoints();
	}

}
