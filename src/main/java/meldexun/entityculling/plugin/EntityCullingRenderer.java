package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.EntityCullingContainer;
import meldexun.entityculling.GLHelper;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.reflection.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;

public class EntityCullingRenderer {

	protected static final ReflectionField<Integer> FIELD_COUNT_ENTITIES_RENDERED = new ReflectionField<>(RenderGlobal.class, "field_72749_I", "countEntitiesRendered");
	protected static final ReflectionField<Framebuffer> FIELD_ENTITY_OUTLINE_FRAMEBUFFER = new ReflectionField<>(RenderGlobal.class, "field_175015_z", "entityOutlineFramebuffer");
	protected static final ReflectionField<ShaderGroup> FIELD_ENTITY_OUTLINE_SHADER = new ReflectionField<>(RenderGlobal.class, "field_174991_A", "entityOutlineShader");

	protected static final Random RAND = new Random();

	protected boolean renderingPrepared;
	protected Minecraft mc;
	protected float partialTicks;
	protected Entity renderViewEntity;
	protected double x;
	protected double y;
	protected double z;
	protected Frustum frustum;

	protected Framebuffer entityOutlineFramebuffer;
	protected ShaderGroup entityOutlineShader;

	protected List<Entity> entityListNormalPass1;
	protected List<Entity> entityListMultipassPass1;
	protected List<TileEntity> tileEntityListPass1;

	protected boolean entityOutlinesRendered = false;

	protected void preRenderEntities() {
		this.renderingPrepared = true;
		this.mc = Minecraft.getMinecraft();
		this.partialTicks = this.mc.getRenderPartialTicks();
		this.renderViewEntity = this.mc.getRenderViewEntity();
		this.x = this.renderViewEntity.lastTickPosX + (this.renderViewEntity.posX - this.renderViewEntity.lastTickPosX) * this.partialTicks;
		this.y = this.renderViewEntity.lastTickPosY + (this.renderViewEntity.posY - this.renderViewEntity.lastTickPosY) * this.partialTicks;
		this.z = this.renderViewEntity.lastTickPosZ + (this.renderViewEntity.posZ - this.renderViewEntity.lastTickPosZ) * this.partialTicks;
		this.frustum = new Frustum();
		this.frustum.setPosition(this.x, this.y, this.z);

		this.entityOutlineFramebuffer = FIELD_ENTITY_OUTLINE_FRAMEBUFFER.get(this.mc.renderGlobal);
		this.entityOutlineShader = FIELD_ENTITY_OUTLINE_SHADER.get(this.mc.renderGlobal);

		this.entityListNormalPass1 = new ArrayList<>(16);
		this.entityListMultipassPass1 = new ArrayList<>(8);
		this.tileEntityListPass1 = new ArrayList<>(16);
	}

	protected void postRenderEntities() {
		this.renderingPrepared = false;
		this.mc = null;
		this.partialTicks = 0.0F;
		this.renderViewEntity = null;
		this.x = 0.0D;
		this.y = 0.0D;
		this.z = 0.0D;
		this.frustum = null;

		this.entityOutlineFramebuffer = null;
		this.entityOutlineShader = null;

		this.entityListNormalPass1 = null;
		this.entityListMultipassPass1 = null;
		this.tileEntityListPass1 = null;
	}

	public boolean renderEntities() {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		int pass = MinecraftForgeClient.getRenderPass();

		if (!this.renderingPrepared) {
			this.preRenderEntities();
			this.updateEntityCullingState();
		}

		RenderManager renderManager = this.mc.getRenderManager();

		if (pass == 0) {
			boolean isThirdPersonView = this.mc.gameSettings.thirdPersonView != 0;
			boolean isSleeping = this.renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) this.renderViewEntity).isPlayerSleeping();
			boolean firstPersonAndNotSleeping = !isThirdPersonView && !isSleeping;
			boolean spectatorAndOutlinesEnabled = this.mc.player.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
			int entitiesRendered = 0;

			List<Entity> multipassEntityList = new ArrayList<>();
			List<Entity> outlineEntityList = new ArrayList<>();
			BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
			for (Entity entity : this.mc.world.loadedEntityList) {
				boolean renderOutlines = entity.isGlowing() || (spectatorAndOutlinesEnabled && entity instanceof EntityPlayer);
				if (!((ICullable) entity).isVisible() && !renderOutlines) {
					continue;
				}
				Render<Entity> render = renderManager.getEntityRenderObject(entity);
				if (render == null) {
					continue;
				}
				if (!render.shouldRender(entity, this.frustum, this.x, this.y, this.z) && !entity.isRidingOrBeingRiddenBy(this.mc.player)) {
					continue;
				}
				if (entity == this.renderViewEntity && firstPersonAndNotSleeping) {
					continue;
				}
				if (!this.mc.world.isBlockLoaded(mutablePos.setPos(entity), false)) {
					continue;
				}

				boolean entityWasRendered = false;

				if (entity.shouldRenderInPass(0)) {
					if (((ICullable) entity).isVisible()) {
						renderManager.renderEntityStatic(entity, this.partialTicks, false);
						if (render.isMultipass()) {
							multipassEntityList.add(entity);
						}
						entityWasRendered = true;
					}
					if (renderOutlines) {
						outlineEntityList.add(entity);
						entityWasRendered = true;
					}
				}
				if (entity.shouldRenderInPass(1)) {
					if (((ICullable) entity).isVisible()) {
						this.entityListNormalPass1.add(entity);
						if (render.isMultipass()) {
							this.entityListMultipassPass1.add(entity);
						}
						entityWasRendered = true;
					}
				}

				if (entityWasRendered) {
					entitiesRendered++;
				}
			}

			for (Entity entity : multipassEntityList) {
				renderManager.renderEntityStatic(entity, this.partialTicks, false);
			}

			if (this.isRenderEntityOutlines() && (!outlineEntityList.isEmpty() || this.entityOutlinesRendered)) {
				this.mc.world.profiler.endStartSection("entityOutlines");
				this.entityOutlineFramebuffer.framebufferClear();
				this.entityOutlinesRendered = !outlineEntityList.isEmpty();

				if (!outlineEntityList.isEmpty()) {
					GlStateManager.depthFunc(GL11.GL_ALWAYS);
					GlStateManager.disableFog();
					this.entityOutlineFramebuffer.bindFramebuffer(false);
					RenderHelper.disableStandardItemLighting();
					renderManager.setRenderOutlines(true);

					for (Entity entity : outlineEntityList) {
						renderManager.renderEntityStatic(entity, this.partialTicks, false);
					}

					renderManager.setRenderOutlines(false);
					RenderHelper.enableStandardItemLighting();
					GlStateManager.depthMask(false);
					this.entityOutlineShader.render(this.partialTicks);
					GlStateManager.enableLighting();
					GlStateManager.depthMask(true);
					GlStateManager.enableFog();
					GlStateManager.enableBlend();
					GlStateManager.enableColorMaterial();
					GlStateManager.depthFunc(GL11.GL_LEQUAL);
					GlStateManager.enableDepth();
					GlStateManager.enableAlpha();
				}

				this.mc.getFramebuffer().bindFramebuffer(false);
			}

			FIELD_COUNT_ENTITIES_RENDERED.set(this.mc.renderGlobal, FIELD_COUNT_ENTITIES_RENDERED.get(this.mc.renderGlobal) + entitiesRendered);
		} else if (pass == 1) {
			for (Entity entity : this.entityListNormalPass1) {
				renderManager.renderEntityStatic(entity, this.partialTicks, false);
			}

			for (Entity entity : this.entityListMultipassPass1) {
				renderManager.renderEntityStatic(entity, this.partialTicks, false);
			}
		}

		return true;
	}

	protected boolean isRenderEntityOutlines() {
		return this.entityOutlineFramebuffer != null && this.entityOutlineShader != null;
	}

	public boolean renderTileEntities() {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		int pass = MinecraftForgeClient.getRenderPass();

		if (pass == 0) {
			for (TileEntity tileEntity : this.mc.world.loadedTileEntityList) {
				if (!((ICullable) tileEntity).isVisible()) {
					continue;
				}
				if (tileEntity.getDistanceSq(this.x, this.y, this.z) > tileEntity.getMaxRenderDistanceSquared()) {
					continue;
				}
				if (!this.frustum.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
					continue;
				}
				if (!this.mc.world.isBlockLoaded(tileEntity.getPos(), false)) {
					continue;
				}
				if (tileEntity.shouldRenderInPass(0)) {
					TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
				}
				if (tileEntity.shouldRenderInPass(1)) {
					this.tileEntityListPass1.add(tileEntity);
				}
			}
		} else if (pass == 1) {
			for (TileEntity tileEntity : this.tileEntityListPass1) {
				TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
			}
		}

		if (pass == 1) {
			this.postRenderEntities();
		}

		return true;
	}

	protected void updateEntityCullingState() {
		double updateChance = MathHelper.clamp(20.0D / (double) Minecraft.getDebugFPS(), 1.0e-7D, 0.5D);

		GL11.glDepthMask(false);
		GL11.glColorMask(false, false, false, false);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		for (Entity entity : this.mc.world.loadedEntityList) {
			AxisAlignedBB aabb = entity.getRenderBoundingBox();

			if (!((ICullable) entity).isCulledFast() || !this.frustum.isBoxInFrustum(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D) || this.mc.player.getDistanceSq(entity) < 4.0D * 4.0D) {
				((ICullable) entity).setCulledSlow(false);
				((ICullable) entity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) entity).initQuery();

				if (((ICullable) entity).isQueryResultDirty()) {
					((ICullable) entity).setCulledSlow(GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GLHelper.beginQuery(query);

					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - 0.5D - this.x, aabb.minY - 0.5D - this.y, aabb.minZ - 0.5D - this.z);
					GL11.glScaled(aabb.maxX - aabb.minX + 1.0D, aabb.maxY - aabb.minY + 1.0D, aabb.maxZ - aabb.minZ + 1.0D);
					GL11.glCallList(EntityCullingContainer.cubeDisplayList);
					GL11.glPopMatrix();

					GLHelper.endQuery();

					((ICullable) entity).setQueryResultDirty(true);
				} else {
					((ICullable) entity).setQueryResultDirty(false);
				}
			}
		}

		for (TileEntity tileEntity : this.mc.world.loadedTileEntityList) {
			AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();

			if (!((ICullable) tileEntity).isCulledFast() || !this.frustum.isBoxInFrustum(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) || this.mc.player.getDistanceSqToCenter(tileEntity.getPos()) < 4.0D * 4.0D) {
				((ICullable) tileEntity).setCulledSlow(false);
				((ICullable) tileEntity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) tileEntity).initQuery();

				if (((ICullable) tileEntity).isQueryResultDirty()) {
					((ICullable) tileEntity).setCulledSlow(GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GLHelper.beginQuery(query);

					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - this.x, aabb.minY - this.y, aabb.minZ - this.z);
					GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
					GL11.glCallList(EntityCullingContainer.cubeDisplayList);
					GL11.glPopMatrix();

					GLHelper.endQuery();

					((ICullable) tileEntity).setQueryResultDirty(true);
				} else {
					((ICullable) tileEntity).setQueryResultDirty(false);
				}
			}
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
	}

}
