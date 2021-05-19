package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.integration.CubicChunks;
import meldexun.entityculling.reflection.ReflectionField;
import meldexun.entityculling.reflection.ReflectionMethod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderUtil;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.Loader;

public class EntityCullingRendererOptifine extends EntityCullingRenderer {

	protected static final ReflectionField<Set<TileEntity>> FIELD_SET_TILE_ENTITIES = new ReflectionField<>(RenderGlobal.class, "field_181024_n", "setTileEntities");
	protected static final ReflectionField<Boolean> FIELD_IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	protected static final ReflectionMethod<Boolean> METHOD_IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	protected static final ReflectionField<Entity> FIELD_RENDERED_ENTITY = new ReflectionField<>(RenderGlobal.class, "renderedEntity", "renderedEntity");
	protected static final ReflectionMethod<?> METHOD_NEXT_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextEntity", "nextEntity", Entity.class);
	protected static final ReflectionMethod<Boolean> METHOD_IS_FAST_RENDER = new ReflectionMethod<>("Config", "isFastRender", "isFastRender");
	protected static final ReflectionMethod<Boolean> METHOD_IS_ANTIALIASING = new ReflectionMethod<>("Config", "isAntialiasing", "isAntialiasing");
	protected static final ReflectionMethod<?> METHOD_NEXT_BLOCK_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextBlockEntity", "nextBlockEntity", TileEntity.class);
	protected static final ReflectionMethod<?> METHOD_BEGIN_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "beginEntitiesGlowing", "beginEntitiesGlowing");
	protected static final ReflectionMethod<?> METHOD_END_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "endEntitiesGlowing", "endEntitiesGlowing");

	protected List<Entity> entityListOutlinePass1;

	@Override
	protected void preRenderEntities() {
		super.preRenderEntities();
		this.entityListOutlinePass1 = new ArrayList<>();
	}

	@Override
	protected void postRenderEntities() {
		super.postRenderEntities();
		this.entityListOutlinePass1 = null;
	}

	@Override
	public boolean renderEntities() {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		int pass = MinecraftForgeClient.getRenderPass();
		boolean isShadowPass = Boolean.TRUE.equals(FIELD_IS_SHADOW_PASS.get(null));
		boolean shadersEnabled = Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null));

		if (isShadowPass && !EntityCullingConfig.optifineShaderOptions.entityShadowsEnabled) {
			if (!this.renderingPrepared) {
				this.preRenderEntities();
			}

			return true;
		}

		if (!this.renderingPrepared) {
			this.preRenderEntities();
			if (!isShadowPass) {
				this.updateEntityCullingState();
			}
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

			for (RenderChunk renderChunk : RenderUtil.Optifine.getRenderChunksEntities()) {
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
					if (entity == this.renderViewEntity && firstPersonAndNotSleeping && !isShadowPass) {
						continue;
					}
					if (isShadowPass) {
						if (((ICullable) entity).isCulledShadowPass()) {
							continue;
						}
						if (EntityCullingConfig.optifineShaderOptions.entityShadowsDistanceLimited && entity.getDistanceSq(this.x, this.y, this.z) > EntityCullingConfig.optifineShaderOptions.entityShadowsMaxDistance * 16.0D * EntityCullingConfig.optifineShaderOptions.entityShadowsMaxDistance * 16.0D) {
							continue;
						}
					}

					boolean renderOutlines = entity.isGlowing() || (spectatorAndOutlinesEnabled && entity instanceof EntityPlayer);
					boolean entityWasRendered = false;

					if (entity.shouldRenderInPass(0)) {
						if (((ICullable) entity).isVisible() || isShadowPass) {
							FIELD_RENDERED_ENTITY.set(this.mc.renderGlobal, entity);
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, this.partialTicks, false);
							FIELD_RENDERED_ENTITY.set(this.mc.renderGlobal, null);
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
						if (((ICullable) entity).isVisible() || isShadowPass) {
							this.entityListNormalPass1.add(entity);
							if (render.isMultipass()) {
								this.entityListMultipassPass1.add(entity);
							}
							entityWasRendered = true;
						}
						if (renderOutlines) {
							this.entityListOutlinePass1.add(entity);
						}
					}

					if (!isShadowPass) {
						if (entityWasRendered) {
							entitiesRendered++;
							this.entitiesRendered++;
						} else {
							this.entitiesOcclusionCulled++;
						}
					}
				}
			}

			for (Entity entity : multipassEntityList) {
				if (shadersEnabled) {
					METHOD_NEXT_ENTITY.invoke(null, entity);
				}
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
						if (shadersEnabled) {
							METHOD_NEXT_ENTITY.invoke(null, entity);
						}
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

			if (!this.isRenderEntityOutlines() && (!this.entityListOutlinePass1.isEmpty() || this.entityOutlinesRendered)) {
				this.mc.world.profiler.endStartSection("entityOutlines");
				this.entityOutlinesRendered = !this.entityListOutlinePass1.isEmpty();

				if (!this.entityListOutlinePass1.isEmpty()) {
					if (shadersEnabled) {
						METHOD_BEGIN_ENTITIES_GLOWING.invoke(null);
					}
					GlStateManager.disableFog();
					GlStateManager.disableDepth();
					this.mc.entityRenderer.disableLightmap();
					RenderHelper.disableStandardItemLighting();
					renderManager.setRenderOutlines(true);

					for (Entity entity : this.entityListOutlinePass1) {
						if (shadersEnabled) {
							METHOD_NEXT_ENTITY.invoke(null, entity);
						}
						renderManager.renderEntityStatic(entity, this.partialTicks, false);
					}

					renderManager.setRenderOutlines(false);
					RenderHelper.enableStandardItemLighting();
					this.mc.entityRenderer.enableLightmap();
					GlStateManager.enableDepth();
					GlStateManager.enableFog();
					if (shadersEnabled) {
						METHOD_END_ENTITIES_GLOWING.invoke(null);
					}
				}
			}
		}

		return true;
	}

	@Override
	protected boolean isRenderEntityOutlines() {
		if (Boolean.TRUE.equals(METHOD_IS_FAST_RENDER.invoke(null))) {
			return false;
		}
		if (Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null))) {
			return false;
		}
		if (Boolean.TRUE.equals(METHOD_IS_ANTIALIASING.invoke(null))) {
			return false;
		}
		return super.isRenderEntityOutlines();
	}

	@Override
	public boolean renderTileEntities() {
		if (!EntityCullingConfig.enabled) {
			return false;
		}

		int pass = MinecraftForgeClient.getRenderPass();
		boolean isShadowPass = Boolean.TRUE.equals(FIELD_IS_SHADOW_PASS.get(null));
		boolean shadersEnabled = Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null));

		if (isShadowPass && !EntityCullingConfig.optifineShaderOptions.tileEntityShadowsEnabled) {
			if (pass == 1) {
				this.postRenderEntities();
			}

			return true;
		}

		if (pass == 0) {
			for (RenderChunk renderChunk : RenderUtil.Optifine.getRenderChunksTileEntities()) {
				BlockPos chunkPos = renderChunk.getPosition();
				Chunk chunk = this.mc.world.getChunk(chunkPos);

				if (chunk.isEmpty()) {
					continue;
				}

				for (TileEntity tileEntity : renderChunk.compiledChunk.getTileEntities()) {
					if (!tileEntity.shouldRenderInPass(0) && !tileEntity.shouldRenderInPass(1)) {
						continue;
					}
					if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
						continue;
					}
					if (tileEntity.getDistanceSq(this.x, this.y, this.z) > tileEntity.getMaxRenderDistanceSquared()) {
						continue;
					}
					if (!this.frustum.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
						continue;
					}
					if (isShadowPass) {
						if (((ICullable) tileEntity).isCulledShadowPass()) {
							continue;
						}
						if (EntityCullingConfig.optifineShaderOptions.tileEntityShadowsDistanceLimited && tileEntity.getDistanceSq(this.x, this.y, this.z) > EntityCullingConfig.optifineShaderOptions.tileEntityShadowsMaxDistance * 16.0D * EntityCullingConfig.optifineShaderOptions.tileEntityShadowsMaxDistance * 16.0D) {
							continue;
						}
					} else {
						if (!((ICullable) tileEntity).isVisible()) {
							this.tileEntitiesOcclusionCulled++;
							continue;
						} else {
							this.tileEntitiesRendered++;
						}
					}

					if (tileEntity.shouldRenderInPass(0)) {
						if (shadersEnabled) {
							METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
						}
						TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
					}
					if (tileEntity.shouldRenderInPass(1)) {
						this.tileEntityListPass1.add(tileEntity);
					}
				}
			}

			Set<TileEntity> setTileEntities = FIELD_SET_TILE_ENTITIES.get(this.mc.renderGlobal);
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
						if (!this.frustum.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
							continue;
						}
						if (isShadowPass) {
							if (((ICullable) tileEntity).isCulledShadowPass()) {
								continue;
							}
							if (EntityCullingConfig.optifineShaderOptions.tileEntityShadowsDistanceLimited && tileEntity.getDistanceSq(this.x, this.y, this.z) > EntityCullingConfig.optifineShaderOptions.tileEntityShadowsMaxDistance * 16.0D * EntityCullingConfig.optifineShaderOptions.tileEntityShadowsMaxDistance * 16.0D) {
								continue;
							}
						} else {
							if (!((ICullable) tileEntity).isVisible()) {
								this.tileEntitiesOcclusionCulled++;
								continue;
							} else {
								this.tileEntitiesRendered++;
							}
						}

						if (tileEntity.shouldRenderInPass(0)) {
							if (shadersEnabled) {
								METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
							}
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
				if (shadersEnabled) {
					METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
				}
				TileEntityRendererDispatcher.instance.render(tileEntity, this.partialTicks, -1);
			}
		}

		if (pass == 1) {
			this.postRenderEntities();
		}

		return true;
	}

}
