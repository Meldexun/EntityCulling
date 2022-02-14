package meldexun.entityculling.renderer.entity;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import org.lwjgl.opengl.GL11;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.integration.FairyLights;
import meldexun.entityculling.util.BoundingBoxHelper;
import meldexun.entityculling.util.ICullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.MinecraftForgeClient;

public class EntityRenderer {

	protected final Queue<Entity> entityListStaticPass0 = new ArrayDeque<>();
	protected final Queue<Entity> entityListMultipassPass0 = new ArrayDeque<>();
	protected final Queue<Entity> entityListOutlinePass0 = new ArrayDeque<>();
	protected final Queue<Entity> entityListStaticPass1 = new ArrayDeque<>();
	protected final Queue<Entity> entityListMultipassPass1 = new ArrayDeque<>();
	public int renderedEntities;
	public int occludedEntities;
	public int totalEntities;

	public void setup(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		this.renderedEntities = 0;
		this.occludedEntities = 0;
		this.totalEntities = 0;
		this.clearEntityLists();
		this.fillEntityLists(camera, camX, camY, camZ, partialTicks);
	}

	protected void clearEntityLists() {
		this.entityListStaticPass0.clear();
		this.entityListMultipassPass0.clear();
		this.entityListOutlinePass0.clear();
		this.entityListStaticPass1.clear();
		this.entityListMultipassPass1.clear();
	}

	protected void fillEntityLists(ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.world.loadedEntityList.forEach(entity -> this.addToRenderLists(entity, camera, camX, camY, camZ, partialTicks));
	}

	protected <T extends Entity> boolean addToRenderLists(T entity, ICamera camera, double camX, double camY, double camZ, double partialTicks) {
		if (EntityCullingConfig.debugRenderBoxes) {
			this.drawBox(entity, camX, camY, camZ, partialTicks);
		}

		this.totalEntities++;

		if (!entity.shouldRenderInPass(0) && !entity.shouldRenderInPass(1)) {
			return false;
		}

		Minecraft mc = Minecraft.getMinecraft();
		RenderManager renderManager = mc.getRenderManager();
		Render<T> renderer = renderManager.getEntityRenderObject(entity);

		if (renderer == null) {
			return false;
		}
		if (!renderer.shouldRender(entity, camera, camX, camY, camZ)
				&& !entity.isRidingOrBeingRiddenBy(mc.player)
				&& (!EntityCulling.isFairyLightsInstalled || !FairyLights.isFairyLightEntity(entity))) {
			return false;
		}
		if (!this.shouldRenderEntity(entity, camX, camY, camZ)) {
			return false;
		}

		boolean rendered = false;

		if (entity.shouldRenderInPass(0)) {
			if (!this.isOcclusionCulled(entity, partialTicks)) {
				this.entityListStaticPass0.add(entity);
				if (renderer.isMultipass()) {
					this.entityListMultipassPass0.add(entity);
				}
				rendered = true;
			}
			if (this.shouldRenderOutlines(entity)) {
				this.entityListOutlinePass0.add(entity);
			}
		}
		if (entity.shouldRenderInPass(1)) {
			if (!this.isOcclusionCulled(entity, partialTicks)) {
				this.entityListStaticPass1.add(entity);
				if (renderer.isMultipass()) {
					this.entityListMultipassPass1.add(entity);
				}
				rendered = true;
			}
		}

		if (rendered) {
			this.renderedEntities++;
		} else {
			this.occludedEntities++;
		}

		Entity[] parts = entity.getParts();
		if (parts != null) {
			Arrays.stream(entity.getParts()).filter(part -> {
				return renderManager.getEntityRenderObject(part).getClass() != RenderEntity.class;
			}).forEach(part -> {
				this.addToRenderLists(part, camera, camX, camY, camZ, partialTicks);
			});
		}

		return true;
	}

	protected void drawBox(Entity entity, double camX, double camY, double camZ, double partialTicks) {
		if (entity == Minecraft.getMinecraft().getRenderViewEntity()) {
			return;
		}
		AxisAlignedBB aabb = entity.getRenderBoundingBox().grow(0.5D);
		if (aabb.hasNaN()) {
			aabb = new AxisAlignedBB(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D);
		}
		double x = -(entity.posX - entity.lastTickPosX) * (1.0D - partialTicks);
		double y = -(entity.posY - entity.lastTickPosY) * (1.0D - partialTicks);
		double z = -(entity.posZ - entity.lastTickPosZ) * (1.0D - partialTicks);
		aabb = aabb.offset(x, y, z);
		BoundingBoxHelper.drawBox(aabb, camX, camY, camZ);
	}

	protected boolean isOcclusionCulled(Entity entity, double partialTicks) {
		return ((ICullable) entity).isCulled();
	}

	protected boolean shouldRenderEntity(Entity entity, double camX, double camY, double camZ) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.gameSettings.thirdPersonView != 0) {
			return true;
		}
		Entity viewEntity = mc.getRenderViewEntity();
		if (entity != viewEntity) {
			return true;
		}
		if (!(viewEntity instanceof EntityLivingBase)) {
			return true;
		}
		return ((EntityLivingBase) viewEntity).isPlayerSleeping();
	}

	protected boolean shouldRenderOutlines(Entity entity) {
		if (entity.isGlowing()) {
			return true;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (!mc.player.isSpectator()) {
			return false;
		}
		if (!mc.gameSettings.keyBindSpectatorOutlines.isKeyDown()) {
			return false;
		}
		return entity instanceof EntityPlayer;
	}

	public void renderEntities(float partialTicks) {
		int pass = MinecraftForgeClient.getRenderPass();
		Minecraft mc = Minecraft.getMinecraft();
		RenderManager renderManager = mc.getRenderManager();

		if (pass == 0) {
			this.entityListStaticPass0.forEach(entity -> {
				this.preRenderEntity(entity);
				renderManager.renderEntityStatic(entity, partialTicks, false);
				this.postRenderEntity();
			});
			this.entityListMultipassPass0.forEach(entity -> {
				this.preRenderEntity(entity);
				renderManager.renderMultipass(entity, partialTicks);
				this.postRenderEntity();
			});
			if (this.isRenderEntityOutlines() && (!this.entityListOutlinePass0.isEmpty() || mc.renderGlobal.entityOutlinesRendered)) {
				mc.world.profiler.endStartSection("entityOutlines");
				mc.renderGlobal.entityOutlineFramebuffer.framebufferClear();
				mc.renderGlobal.entityOutlinesRendered = !this.entityListOutlinePass0.isEmpty();

				if (!this.entityListOutlinePass0.isEmpty()) {
					GlStateManager.depthFunc(GL11.GL_ALWAYS);
					GlStateManager.disableFog();
					mc.renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false);
					RenderHelper.disableStandardItemLighting();
					renderManager.setRenderOutlines(true);

					this.entityListOutlinePass0.forEach(entity -> {
						this.preRenderEntity(entity);
						renderManager.renderEntityStatic(entity, partialTicks, false);
						this.postRenderEntity();
					});

					renderManager.setRenderOutlines(false);
					RenderHelper.enableStandardItemLighting();
					GlStateManager.depthMask(false);
					mc.renderGlobal.entityOutlineShader.render(partialTicks);
					GlStateManager.enableLighting();
					GlStateManager.depthMask(true);
					GlStateManager.enableFog();
					GlStateManager.enableBlend();
					GlStateManager.enableColorMaterial();
					GlStateManager.depthFunc(GL11.GL_LEQUAL);
					GlStateManager.enableDepth();
					GlStateManager.enableAlpha();
				}

				mc.getFramebuffer().bindFramebuffer(false);
			}
		} else if (pass == 1) {
			this.entityListStaticPass1.forEach(entity -> {
				this.preRenderEntity(entity);
				renderManager.renderEntityStatic(entity, partialTicks, false);
				this.postRenderEntity();
			});
			this.entityListMultipassPass1.forEach(entity -> {
				this.preRenderEntity(entity);
				renderManager.renderMultipass(entity, partialTicks);
				this.postRenderEntity();
			});
		}
	}

	protected void preRenderEntity(Entity entity) {

	}

	protected void postRenderEntity() {

	}

	protected boolean isRenderEntityOutlines() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.renderGlobal.entityOutlineFramebuffer != null && mc.renderGlobal.entityOutlineShader != null;
	}

}
