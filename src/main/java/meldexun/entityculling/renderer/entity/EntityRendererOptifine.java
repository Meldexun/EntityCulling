package meldexun.entityculling.renderer.entity;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.ICullable;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;

public class EntityRendererOptifine extends EntityRenderer {

	private static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	private static final ReflectionMethod<Boolean> NEXT_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextEntity", "nextEntity",
			Entity.class);
	private static final ReflectionField<Entity> RENDERED_ENTITY = new ReflectionField<>(RenderGlobal.class, "renderedEntity", "renderedEntity");
	private static final ReflectionMethod<Boolean> IS_FAST_RENDER = new ReflectionMethod<>("Config", "isFastRender", "isFastRender");
	private static final ReflectionMethod<Boolean> IS_ANTIALIASING = new ReflectionMethod<>("Config", "isAntialiasing", "isAntialiasing");
	private static final ReflectionMethod<Void> BEGIN_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "beginEntitiesGlowing",
			"beginEntitiesGlowing");
	private static final ReflectionMethod<Void> END_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "endEntitiesGlowing",
			"endEntitiesGlowing");
	protected final Queue<Entity> entityListOutlinePass1 = new ArrayDeque<>();
	private boolean isShaders = false;

	@Override
	public void setup(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		this.isShaders = IS_SHADERS.invoke(null);
		super.setup(camera, camX, camY, camZ, partialTicks);
	}

	@Override
	protected void clearEntityLists() {
		super.clearEntityLists();
		this.entityListOutlinePass1.clear();
	}

	@Override
	protected void fillEntityLists(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		if (IS_SHADOW_PASS.getBoolean(null) && !EntityCullingConfig.optifineShaderOptions.entityShadowsEnabled) {
			return;
		}
		int r = this.renderedEntities;
		int o = this.occludedEntities;
		int t = this.totalEntities;
		super.fillEntityLists(camera, camX, camY, camZ, partialTicks);
		if (IS_SHADOW_PASS.getBoolean(null)) {
			this.renderedEntities = r;
			this.occludedEntities = o;
			this.totalEntities = t;
		}
	}

	@Override
	protected <T extends Entity> boolean addToRenderLists(T entity, ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		if (!super.addToRenderLists(entity, camera, camX, camY, camZ, partialTicks)) {
			return false;
		}
		if (entity.shouldRenderInPass(1) && this.shouldRenderOutlines(entity)) {
			this.entityListOutlinePass1.add(entity);
		}
		return true;
	}

	@Override
	protected boolean shouldRenderEntity(Entity entity, double camX, double camY, double camZ) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			if (EntityCullingConfig.optifineShaderOptions.entityShadowsDistanceLimited) {
				double d = EntityCullingConfig.optifineShaderOptions.entityShadowsMaxDistance * 16.0D;
				if (entity.getDistanceSq(camX, camY, camZ) > d * d) {
					return false;
				}
			}
			return true;
		}
		return super.shouldRenderEntity(entity, camX, camY, camZ);
	}

	@Override
	protected boolean isOcclusionCulled(Entity entity, double partialTicks) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return ((ICullable) entity).isShadowCulled();
		}
		return super.isOcclusionCulled(entity, partialTicks);
	}

	@Override
	public void renderEntities(float partialTicks) {
		super.renderEntities(partialTicks);

		Minecraft mc = Minecraft.getMinecraft();
		Collection<Entity> outlineEntities = MinecraftForgeClient.getRenderPass() == 0 ? this.entityListOutlinePass0 : this.entityListOutlinePass1;
		if (!this.isRenderEntityOutlines() && (!outlineEntities.isEmpty() || mc.renderGlobal.entityOutlinesRendered)) {
			mc.world.profiler.endStartSection("entityOutlines");
			mc.renderGlobal.entityOutlinesRendered = !outlineEntities.isEmpty();

			if (!outlineEntities.isEmpty()) {
				if (this.isShaders) {
					BEGIN_ENTITIES_GLOWING.invoke(null);
				}
				GlStateManager.disableFog();
				GlStateManager.disableDepth();
				mc.entityRenderer.disableLightmap();
				RenderHelper.disableStandardItemLighting();
				mc.getRenderManager().setRenderOutlines(true);

				for (Entity entity : outlineEntities) {
					if (this.isShaders) {
						NEXT_ENTITY.invoke(null, entity);
					}
					mc.getRenderManager().renderEntityStatic(entity, mc.getRenderPartialTicks(), false);
				}

				mc.getRenderManager().setRenderOutlines(false);
				RenderHelper.enableStandardItemLighting();
				mc.entityRenderer.enableLightmap();
				GlStateManager.enableDepth();
				GlStateManager.enableFog();
				if (this.isShaders) {
					END_ENTITIES_GLOWING.invoke(null);
				}
			}
		}
	}

	@Override
	protected void preRenderEntity(Entity entity) {
		if (this.isShaders) {
			NEXT_ENTITY.invoke(null, entity);
		}
		RENDERED_ENTITY.set(Minecraft.getMinecraft().renderGlobal, entity);
	}

	@Override
	protected void postRenderEntity() {
		RENDERED_ENTITY.set(Minecraft.getMinecraft().renderGlobal, null);
	}

	@Override
	protected boolean isRenderEntityOutlines() {
		if (IS_FAST_RENDER.invoke(null)) {
			return false;
		}
		if (IS_SHADERS.invoke(null)) {
			return false;
		}
		if (IS_ANTIALIASING.invoke(null)) {
			return false;
		}
		return super.isRenderEntityOutlines();
	}

}
