package meldexun.entityculling.plugin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.EntityCullingContainer;
import meldexun.entityculling.GLHelper;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.ITileEntityBBCache;
import meldexun.entityculling.integration.CubicChunks;
import meldexun.entityculling.reflection.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderUtil;
import net.minecraft.client.renderer.chunk.RenderChunk;
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
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.Loader;

public class EntityCullingRenderer {

	protected static final ReflectionField<Integer> FIELD_COUNT_ENTITIES_RENDERED = new ReflectionField<>(RenderGlobal.class, "field_72749_I", "countEntitiesRendered");
	protected static final ReflectionField<Framebuffer> FIELD_ENTITY_OUTLINE_FRAMEBUFFER = new ReflectionField<>(RenderGlobal.class, "field_175015_z", "entityOutlineFramebuffer");
	protected static final ReflectionField<ShaderGroup> FIELD_ENTITY_OUTLINE_SHADER = new ReflectionField<>(RenderGlobal.class, "field_174991_A", "entityOutlineShader");
	protected static final ReflectionField<Set<TileEntity>> FIELD_SET_TILE_ENTITIES = new ReflectionField<>(RenderGlobal.class, "field_181024_n", "setTileEntities");

	private static final ByteBuffer COLOR_MASK_BUFFER = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
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

	public int entitiesRendered;
	public int entitiesOcclusionCulled;
	public int tileEntitiesRendered;
	public int tileEntitiesOcclusionCulled;

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

		this.entitiesRendered = 0;
		this.entitiesOcclusionCulled = 0;
		this.tileEntitiesRendered = 0;
		this.tileEntitiesOcclusionCulled = 0;
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
			for (RenderChunk renderChunk : RenderUtil.getRenderChunks()) {
				BlockPos chunkPos = renderChunk.getPosition();
				Chunk chunk = this.mc.world.getChunk(chunkPos);
				ClassInheritanceMultiMap<Entity> entityMap = !Loader.isModLoaded("cubicchunks") ? chunk.getEntityLists()[chunkPos.getY() >> 4] : CubicChunks.getEntityList(this.mc.world, renderChunk.getPosition());

				for (Entity entity : entityMap) {
					if (!entity.shouldRenderInPass(0) && !entity.shouldRenderInPass(1)) {
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

					boolean renderOutlines = entity.isGlowing() || (spectatorAndOutlinesEnabled && entity instanceof EntityPlayer);
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
						this.entitiesRendered++;
					} else {
						this.entitiesOcclusionCulled++;
					}
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
			for (RenderChunk renderChunk : RenderUtil.getRenderChunks()) {
				List<TileEntity> tileEntities = renderChunk.compiledChunk.getTileEntities();
				if (tileEntities.isEmpty()) {
					continue;
				}
				if (!this.mc.world.isBlockLoaded(renderChunk.getPosition(), false)) {
					continue;
				}
				for (TileEntity tileEntity : tileEntities) {
					if (!tileEntity.shouldRenderInPass(0) && !tileEntity.shouldRenderInPass(1)) {
						continue;
					}
					if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
						continue;
					}
					if (tileEntity.getDistanceSq(this.x, this.y, this.z) > tileEntity.getMaxRenderDistanceSquared()) {
						continue;
					}
					if (!this.frustum.isBoundingBoxInFrustum(((ITileEntityBBCache) tileEntity).getCachedAABB())) {
						continue;
					}
					if (!this.mc.world.isBlockLoaded(tileEntity.getPos(), false)) {
						continue;
					}
					if (!((ICullable) tileEntity).isVisible()) {
						this.tileEntitiesOcclusionCulled++;
						continue;
					} else {
						this.tileEntitiesRendered++;
					}
					if (tileEntity.shouldRenderInPass(0)) {
						TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
					}
					if (tileEntity.shouldRenderInPass(1)) {
						this.tileEntityListPass1.add(tileEntity);
					}
				}
			}
			Set<TileEntity> setTileEntities = FIELD_SET_TILE_ENTITIES.get(mc.renderGlobal);
			if (!setTileEntities.isEmpty()) {
				synchronized (setTileEntities) {
					for (TileEntity tileEntity : setTileEntities) {
						if (!tileEntity.shouldRenderInPass(0) && !tileEntity.shouldRenderInPass(1)) {
							continue;
						}
						if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
							continue;
						}
						if (tileEntity.getDistanceSq(this.x, this.y, this.z) > tileEntity.getMaxRenderDistanceSquared()) {
							continue;
						}
						if (!this.frustum.isBoundingBoxInFrustum(((ITileEntityBBCache) tileEntity).getCachedAABB())) {
							continue;
						}
						if (!this.mc.world.isBlockLoaded(tileEntity.getPos(), false)) {
							continue;
						}
						if (!((ICullable) tileEntity).isVisible()) {
							this.tileEntitiesOcclusionCulled++;
							continue;
						} else {
							this.tileEntitiesRendered++;
						}
						if (tileEntity.shouldRenderInPass(0)) {
							TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
						}
						if (tileEntity.shouldRenderInPass(1)) {
							this.tileEntityListPass1.add(tileEntity);
						}
					}
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
		if (!EntityCullingConfig.betaFeatures) {
			return;
		}

		double updateChance = MathHelper.clamp(10.0D / (double) Minecraft.getDebugFPS(), 1.0e-7D, 0.5D);

		GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, COLOR_MASK_BUFFER);
		boolean depthMaskEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
		boolean depthTestEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		boolean texture2dEnabled = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPushMatrix();
		GL11.glTranslated(-this.x, -this.y, -this.z);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, EntityCullingContainer.vertexBuffer);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EntityCullingContainer.indexBuffer);

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
					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D);
					GL11.glScaled(aabb.maxX - aabb.minX + 1.0D, aabb.maxY - aabb.minY + 1.0D, aabb.maxZ - aabb.minZ + 1.0D);

					GLHelper.beginQuery(query);
					GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);
					GLHelper.endQuery();

					GL11.glPopMatrix();

					((ICullable) entity).setQueryResultDirty(true);
				} else {
					((ICullable) entity).setQueryResultDirty(false);
				}
			}
		}

		for (TileEntity tileEntity : this.mc.world.loadedTileEntityList) {
			AxisAlignedBB aabb = ((ITileEntityBBCache) tileEntity).getCachedAABB();

			if (!((ICullable) tileEntity).isCulledFast() || !this.frustum.isBoxInFrustum(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ) || this.mc.player.getDistanceSqToCenter(tileEntity.getPos()) < 4.0D * 4.0D) {
				((ICullable) tileEntity).setCulledSlow(false);
				((ICullable) tileEntity).setQueryResultDirty(false);
			} else {
				int query = ((ICullable) tileEntity).initQuery();

				if (((ICullable) tileEntity).isQueryResultDirty()) {
					((ICullable) tileEntity).setCulledSlow(GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) == 0);
				}

				if (RAND.nextDouble() < updateChance) {
					GL11.glPushMatrix();
					GL11.glTranslated(aabb.minX, aabb.minY, aabb.minZ);
					GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

					GLHelper.beginQuery(query);
					GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0);
					GLHelper.endQuery();

					GL11.glPopMatrix();

					((ICullable) tileEntity).setQueryResultDirty(true);
				} else {
					((ICullable) tileEntity).setQueryResultDirty(false);
				}
			}
		}

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL11.glPopMatrix();

		if (texture2dEnabled) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		} else {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		if (depthTestEnabled) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
		GL11.glDepthMask(depthMaskEnabled);
		GL11.glColorMask(COLOR_MASK_BUFFER.get(0) == 1, COLOR_MASK_BUFFER.get(1) == 1, COLOR_MASK_BUFFER.get(2) == 1, COLOR_MASK_BUFFER.get(3) == 1);
	}

}
