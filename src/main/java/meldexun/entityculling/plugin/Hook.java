package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.ICullable;
import meldexun.entityculling.integration.CubicChunks;
import meldexun.entityculling.reflection.ReflectionField;
import meldexun.entityculling.reflection.ReflectionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderUtil;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
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

public final class Hook {

	private static final ReflectionField<Integer> FIELD_COUNT_ENTITIES_RENDERED = new ReflectionField<>(RenderGlobal.class, "field_72749_I", "countEntitiesRendered");
	private static final ReflectionField<Set<TileEntity>> FIELD_SET_TILE_ENTITIES = new ReflectionField<>(RenderGlobal.class, "field_181024_n", "setTileEntities");
	private static final ReflectionField<Framebuffer> FIELD_ENTITY_OUTLINE_FRAMEBUFFER = new ReflectionField<>(RenderGlobal.class, "field_175015_z", "entityOutlineFramebuffer");
	private static final ReflectionField<ShaderGroup> FIELD_ENTITY_OUTLINE_SHADER = new ReflectionField<>(RenderGlobal.class, "field_174991_A", "entityOutlineShader");
	private static final List<Entity> ENTITY_LIST_NORMAL_0 = new ArrayList<>();
	private static final List<Entity> ENTITY_LIST_OUTLINE_0 = new ArrayList<>();
	private static final List<Entity> ENTITY_LIST_MULTIPASS_0 = new ArrayList<>();
	private static final List<Entity> ENTITY_LIST_NORMAL_1 = new ArrayList<>();
	private static final List<Entity> ENTITY_LIST_MULTIPASS_1 = new ArrayList<>();
	private static final List<TileEntity> TILE_ENTITY_LIST_NORMAL_0 = new ArrayList<>();
	private static final List<TileEntity> TILE_ENTITY_LIST_SYNCHRONIZED_0 = new ArrayList<>();
	private static final List<TileEntity> TILE_ENTITY_LIST_NORMAL_1 = new ArrayList<>();
	private static final List<TileEntity> TILE_ENTITY_LIST_SYNCHRONIZED_1 = new ArrayList<>();
	private static boolean entityOutlinesRendered = false;

	private Hook() {

	}

	private static void updateEntityLists() {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		RenderManager renderManager = mc.getRenderManager();
		Entity renderViewEntity = mc.getRenderViewEntity();
		float partialTicks = mc.getRenderPartialTicks();
		double x = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
		double y = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
		double z = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
		ICamera camera = new Frustum();
		camera.setPosition(x, y, z);
		boolean isThirdPersonView = mc.gameSettings.thirdPersonView != 0;
		boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();
		boolean flag = !isThirdPersonView && !isSleeping;
		boolean flag1 = mc.player.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
		int entitiesRendered = 0;

		for (RenderChunk renderChunk : RenderUtil.getRenderChunks()) {
			BlockPos chunkPos = renderChunk.getPosition();
			Chunk chunk = mc.world.getChunk(chunkPos);

			ClassInheritanceMultiMap<Entity> entityList = !Loader.isModLoaded("cubicchunks") ? chunk.getEntityLists()[chunkPos.getY() >> 4] : CubicChunks.getEntityList(mc.world, renderChunk.getPosition());
			if (!entityList.isEmpty()) {
				for (Entity entity : entityList) {
					Render<Entity> render = renderManager.getEntityRenderObject(entity);
					if (render == null) {
						continue;
					}
					if (!render.shouldRender(entity, camera, x, y, z) && !entity.isRidingOrBeingRiddenBy(mc.player)) {
						continue;
					}
					if (entity == renderViewEntity && flag) {
						continue;
					}
					boolean flag2 = false;
					if (entity.shouldRenderInPass(0)) {
						if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
							ENTITY_LIST_OUTLINE_0.add(entity);
							flag2 = true;
						}
						if (!((ICullable) entity).isCulled()) {
							ENTITY_LIST_NORMAL_0.add(entity);
							if (render.isMultipass()) {
								ENTITY_LIST_MULTIPASS_0.add(entity);
							}
							flag2 = true;
						}
					}
					if (entity.shouldRenderInPass(1) && !((ICullable) entity).isCulled()) {
						ENTITY_LIST_NORMAL_1.add(entity);
						if (render.isMultipass()) {
							ENTITY_LIST_MULTIPASS_1.add(entity);
						}
						flag2 = true;
					}
					if (flag2) {
						entitiesRendered++;
					}
				}
			}

			if (chunk.isEmpty()) {
				continue;
			}

			List<TileEntity> tileEntityList = renderChunk.compiledChunk.getTileEntities();
			if (!tileEntityList.isEmpty()) {
				for (TileEntity tileEntity : tileEntityList) {
					if (tileEntity.getDistanceSq(x, y, z) > tileEntity.getMaxRenderDistanceSquared()) {
						continue;
					}
					if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
						continue;
					}
					if (((ICullable) tileEntity).isCulled()) {
						continue;
					}
					if (tileEntity.shouldRenderInPass(0)) {
						TILE_ENTITY_LIST_NORMAL_0.add(tileEntity);
					}
					if (tileEntity.shouldRenderInPass(1)) {
						TILE_ENTITY_LIST_NORMAL_1.add(tileEntity);
					}
				}
			}
		}
		FIELD_COUNT_ENTITIES_RENDERED.set(mc.renderGlobal, FIELD_COUNT_ENTITIES_RENDERED.get(mc.renderGlobal) + entitiesRendered);

		Set<TileEntity> setTileEntities = FIELD_SET_TILE_ENTITIES.get(mc.renderGlobal);
		if (!setTileEntities.isEmpty()) {
			synchronized (setTileEntities) {
				for (TileEntity tileEntity : setTileEntities) {
					if (tileEntity.getDistanceSq(x, y, z) > tileEntity.getMaxRenderDistanceSquared()) {
						continue;
					}
					if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
						continue;
					}
					if (!mc.world.isBlockLoaded(tileEntity.getPos(), false)) {
						continue;
					}
					if (((ICullable) tileEntity).isCulled()) {
						continue;
					}
					if (tileEntity.shouldRenderInPass(0)) {
						TILE_ENTITY_LIST_SYNCHRONIZED_0.add(tileEntity);
					}
					if (tileEntity.shouldRenderInPass(1)) {
						TILE_ENTITY_LIST_SYNCHRONIZED_1.add(tileEntity);
					}
				}
			}
		}
	}

	private static void clearEntityLists() {
		if (!ENTITY_LIST_NORMAL_0.isEmpty()) {
			ENTITY_LIST_NORMAL_0.clear();
		}
		if (!ENTITY_LIST_OUTLINE_0.isEmpty()) {
			ENTITY_LIST_OUTLINE_0.clear();
		}
		if (!ENTITY_LIST_MULTIPASS_0.isEmpty()) {
			ENTITY_LIST_MULTIPASS_0.clear();
		}
		if (!ENTITY_LIST_NORMAL_1.isEmpty()) {
			ENTITY_LIST_NORMAL_1.clear();
		}
		if (!ENTITY_LIST_MULTIPASS_1.isEmpty()) {
			ENTITY_LIST_MULTIPASS_1.clear();
		}
		if (!TILE_ENTITY_LIST_NORMAL_0.isEmpty()) {
			TILE_ENTITY_LIST_NORMAL_0.clear();
		}
		if (!TILE_ENTITY_LIST_SYNCHRONIZED_0.isEmpty()) {
			TILE_ENTITY_LIST_SYNCHRONIZED_0.clear();
		}
		if (!TILE_ENTITY_LIST_NORMAL_1.isEmpty()) {
			TILE_ENTITY_LIST_NORMAL_1.clear();
		}
		if (!TILE_ENTITY_LIST_SYNCHRONIZED_1.isEmpty()) {
			TILE_ENTITY_LIST_SYNCHRONIZED_1.clear();
		}
	}

	public static boolean renderEntities() {
		int pass = MinecraftForgeClient.getRenderPass();

		if (pass == 0) {
			updateEntityLists();
		}

		if (EntityCullingConfig.enabled) {
			Minecraft mc = Minecraft.getMinecraft();
			RenderManager renderManager = mc.getRenderManager();
			float partialTicks = mc.getRenderPartialTicks();

			if (pass == 0) {
				if (!ENTITY_LIST_NORMAL_0.isEmpty()) {
					for (Entity entity : ENTITY_LIST_NORMAL_0) {
						renderManager.renderEntityStatic(entity, partialTicks, false);
					}
				}
				if (!ENTITY_LIST_MULTIPASS_0.isEmpty()) {
					for (Entity entity : ENTITY_LIST_MULTIPASS_0) {
						renderManager.renderMultipass(entity, partialTicks);
					}
				}
				if (isRenderEntityOutlines() && (!ENTITY_LIST_OUTLINE_0.isEmpty() || entityOutlinesRendered)) {
					Framebuffer entityOutlineFramebuffer = FIELD_ENTITY_OUTLINE_FRAMEBUFFER.get(mc.renderGlobal);
					ShaderGroup entityOutlineShader = FIELD_ENTITY_OUTLINE_SHADER.get(mc.renderGlobal);
					mc.world.profiler.endStartSection("entityOutlines");
					entityOutlineFramebuffer.framebufferClear();
					entityOutlinesRendered = !ENTITY_LIST_OUTLINE_0.isEmpty();

					if (!ENTITY_LIST_OUTLINE_0.isEmpty()) {
						GlStateManager.depthFunc(519);
						GlStateManager.disableFog();
						entityOutlineFramebuffer.bindFramebuffer(false);
						RenderHelper.disableStandardItemLighting();
						renderManager.setRenderOutlines(true);

						for (Entity entity : ENTITY_LIST_OUTLINE_0) {
							renderManager.renderEntityStatic(entity, partialTicks, false);
						}

						renderManager.setRenderOutlines(false);
						RenderHelper.enableStandardItemLighting();
						GlStateManager.depthMask(false);
						entityOutlineShader.render(partialTicks);
						GlStateManager.enableLighting();
						GlStateManager.depthMask(true);
						GlStateManager.enableFog();
						GlStateManager.enableBlend();
						GlStateManager.enableColorMaterial();
						GlStateManager.depthFunc(515);
						GlStateManager.enableDepth();
						GlStateManager.enableAlpha();
					}

					mc.getFramebuffer().bindFramebuffer(false);
				}
			} else if (pass == 1) {
				if (!ENTITY_LIST_NORMAL_1.isEmpty()) {
					for (Entity entity : ENTITY_LIST_NORMAL_1) {
						renderManager.renderEntityStatic(entity, partialTicks, false);
					}
				}
				if (!ENTITY_LIST_MULTIPASS_1.isEmpty()) {
					for (Entity entity : ENTITY_LIST_MULTIPASS_1) {
						renderManager.renderMultipass(entity, partialTicks);
					}
				}
			}
		}

		return EntityCullingConfig.enabled;
	}

	private static boolean isRenderEntityOutlines() {
		RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
		if (FIELD_ENTITY_OUTLINE_FRAMEBUFFER.get(renderGlobal) == null) {
			return false;
		}
		return FIELD_ENTITY_OUTLINE_SHADER.get(renderGlobal) != null;
	}

	public static boolean renderTileEntities() {
		int pass = MinecraftForgeClient.getRenderPass();

		if (EntityCullingConfig.enabled) {
			Minecraft mc = Minecraft.getMinecraft();
			float partialTicks = mc.getRenderPartialTicks();

			if (pass == 0) {
				if (!TILE_ENTITY_LIST_NORMAL_0.isEmpty()) {
					for (TileEntity tileEntity : TILE_ENTITY_LIST_NORMAL_0) {
						TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
					}
				}
				if (!TILE_ENTITY_LIST_SYNCHRONIZED_0.isEmpty()) {
					for (TileEntity tileEntity : TILE_ENTITY_LIST_SYNCHRONIZED_0) {
						TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
					}
				}
			} else if (pass == 1) {
				if (!TILE_ENTITY_LIST_NORMAL_1.isEmpty()) {
					for (TileEntity tileEntity : TILE_ENTITY_LIST_NORMAL_1) {
						TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
					}
				}
				if (!TILE_ENTITY_LIST_SYNCHRONIZED_1.isEmpty()) {
					for (TileEntity tileEntity : TILE_ENTITY_LIST_SYNCHRONIZED_1) {
						TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
					}
				}
			}
		}

		if (pass == 1) {
			clearEntityLists();
		}

		return EntityCullingConfig.enabled;
	}

	public static boolean shouldRender(Render<?> render, Entity entity, ICamera camera, double camX, double camY, double camZ) {
		if (!EntityCullingConfig.enabled) {
			AxisAlignedBB axisalignedbb = entity.getRenderBoundingBox().grow(0.5D);

			if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
				axisalignedbb = new AxisAlignedBB(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D);
			}

			return entity.isInRangeToRender3d(camX, camY, camZ) && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
		}

		if (!entity.isInRangeToRender3d(camX, camY, camZ)) {
			return false;
		}

		if (entity.ignoreFrustumCheck) {
			return true;
		}

		AxisAlignedBB aabb = entity.getRenderBoundingBox();

		if (aabb.hasNaN()) {
			return ((Frustum) camera).isBoxInFrustum(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D);
		}

		return ((Frustum) camera).isBoxInFrustum(aabb.minX - 0.5D, aabb.minY - 0.5D, aabb.minZ - 0.5D, aabb.maxX + 0.5D, aabb.maxY + 0.5D, aabb.maxZ + 0.5D);
	}

	public static boolean render(TileEntity tileEntity, int destroyStage, boolean drawingBatch) {
		if (!EntityCullingConfig.enabled) {
			return false;
		}
		Minecraft mc = Minecraft.getMinecraft();
		float partialTicks = mc.getRenderPartialTicks();
		if (!drawingBatch || !tileEntity.hasFastRenderer()) {
			RenderHelper.enableStandardItemLighting();
			int i = mc.world.getCombinedLight(tileEntity.getPos(), 0);
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		BlockPos blockpos = tileEntity.getPos();
		double x = (double) blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX;
		double y = (double) blockpos.getY() - TileEntityRendererDispatcher.staticPlayerY;
		double z = (double) blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
		TileEntityRendererDispatcher.instance.render(tileEntity, x, y, z, partialTicks, destroyStage, 1.0F);
		return true;
	}

	public static final class Optifine {

		private static final ReflectionMethod<Boolean> METHOD_IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
		private static final ReflectionField<Entity> FIELD_RENDERED_ENTITY = new ReflectionField<>(RenderGlobal.class, "renderedEntity", "renderedEntity");
		private static final ReflectionMethod<?> METHOD_NEXT_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextEntity", "nextEntity", Entity.class);
		private static final ReflectionMethod<Boolean> METHOD_IS_FAST_RENDER = new ReflectionMethod<>("Config", "isFastRender", "isFastRender");
		private static final ReflectionMethod<Boolean> METHOD_IS_ANTIALIASING = new ReflectionMethod<>("Config", "isAntialiasing", "isAntialiasing");
		private static final ReflectionMethod<?> METHOD_NEXT_BLOCK_ENTITY = new ReflectionMethod<>("net.optifine.shaders.Shaders", "nextBlockEntity", "nextBlockEntity", TileEntity.class);
		private static final ReflectionMethod<?> METHOD_BEGIN_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "beginEntitiesGlowing", "beginEntitiesGlowing");
		private static final ReflectionMethod<?> METHOD_END_ENTITIES_GLOWING = new ReflectionMethod<>("net.optifine.shaders.Shaders", "endEntitiesGlowing", "endEntitiesGlowing");
		private static final List<Entity> ENTITY_LIST_OUTLINE_1 = new ArrayList<>();

		private Optifine() {

		}

		private static void updateEntityLists() {
			if (!EntityCullingConfig.enabled) {
				return;
			}
			Minecraft mc = Minecraft.getMinecraft();
			RenderManager renderManager = mc.getRenderManager();
			Entity renderViewEntity = mc.getRenderViewEntity();
			float partialTicks = mc.getRenderPartialTicks();
			double x = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
			double y = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
			double z = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
			ICamera camera = new Frustum();
			camera.setPosition(x, y, z);
			boolean isThirdPersonView = mc.gameSettings.thirdPersonView != 0;
			boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();
			boolean flag = !isThirdPersonView && !isSleeping;
			boolean flag1 = mc.player.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
			int entitiesRendered = 0;

			for (RenderChunk renderChunk : RenderUtil.Optifine.getRenderChunksEntities()) {
				BlockPos chunkPos = renderChunk.getPosition();
				Chunk chunk = mc.world.getChunk(chunkPos);

				ClassInheritanceMultiMap<Entity> entityList = !Loader.isModLoaded("cubicchunks") ? chunk.getEntityLists()[chunkPos.getY() >> 4] : CubicChunks.getEntityList(mc.world, renderChunk.getPosition());
				if (!entityList.isEmpty()) {
					for (Entity entity : entityList) {
						Render<Entity> render = renderManager.getEntityRenderObject(entity);
						if (render == null) {
							continue;
						}
						if (!render.shouldRender(entity, camera, x, y, z) && !entity.isRidingOrBeingRiddenBy(mc.player)) {
							continue;
						}
						if (entity == renderViewEntity && flag) {
							continue;
						}
						boolean flag2 = false;
						if (entity.shouldRenderInPass(0)) {
							if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
								ENTITY_LIST_OUTLINE_0.add(entity);
								flag2 = true;
							}
							if (!((ICullable) entity).isCulled()) {
								ENTITY_LIST_NORMAL_0.add(entity);
								if (render.isMultipass()) {
									ENTITY_LIST_MULTIPASS_0.add(entity);
								}
								flag2 = true;
							}
						}
						if (entity.shouldRenderInPass(1)) {
							if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
								ENTITY_LIST_OUTLINE_1.add(entity);
								flag2 = true;
							}
							if (!((ICullable) entity).isCulled()) {
								ENTITY_LIST_NORMAL_1.add(entity);
								if (render.isMultipass()) {
									ENTITY_LIST_MULTIPASS_1.add(entity);
								}
								flag2 = true;
							}
						}
						if (flag2) {
							entitiesRendered++;
						}
					}
				}
			}

			FIELD_COUNT_ENTITIES_RENDERED.set(mc.renderGlobal, FIELD_COUNT_ENTITIES_RENDERED.get(mc.renderGlobal) + entitiesRendered);

			for (RenderChunk renderChunk : RenderUtil.Optifine.getRenderChunksTileEntities()) {
				BlockPos chunkPos = renderChunk.getPosition();
				Chunk chunk = mc.world.getChunk(chunkPos);

				if (chunk.isEmpty()) {
					continue;
				}

				List<TileEntity> tileEntityList = renderChunk.compiledChunk.getTileEntities();
				if (!tileEntityList.isEmpty()) {
					for (TileEntity tileEntity : tileEntityList) {
						if (tileEntity.getDistanceSq(x, y, z) > tileEntity.getMaxRenderDistanceSquared()) {
							continue;
						}
						if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
							continue;
						}
						if (((ICullable) tileEntity).isCulled()) {
							continue;
						}
						if (tileEntity.shouldRenderInPass(0)) {
							TILE_ENTITY_LIST_NORMAL_0.add(tileEntity);
						}
						if (tileEntity.shouldRenderInPass(1)) {
							TILE_ENTITY_LIST_NORMAL_1.add(tileEntity);
						}
					}
				}
			}

			Set<TileEntity> setTileEntities = FIELD_SET_TILE_ENTITIES.get(mc.renderGlobal);
			if (!setTileEntities.isEmpty()) {
				synchronized (setTileEntities) {
					for (TileEntity tileEntity : setTileEntities) {
						if (tileEntity.getDistanceSq(x, y, z) > tileEntity.getMaxRenderDistanceSquared()) {
							continue;
						}
						if (!camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
							continue;
						}
						if (!mc.world.isBlockLoaded(tileEntity.getPos(), false)) {
							continue;
						}
						if (((ICullable) tileEntity).isCulled()) {
							continue;
						}
						if (tileEntity.shouldRenderInPass(0)) {
							TILE_ENTITY_LIST_SYNCHRONIZED_0.add(tileEntity);
						}
						if (tileEntity.shouldRenderInPass(1)) {
							TILE_ENTITY_LIST_SYNCHRONIZED_1.add(tileEntity);
						}
					}
				}
			}
		}

		private static void clearEntityLists() {
			if (!ENTITY_LIST_NORMAL_0.isEmpty()) {
				ENTITY_LIST_NORMAL_0.clear();
			}
			if (!ENTITY_LIST_OUTLINE_0.isEmpty()) {
				ENTITY_LIST_OUTLINE_0.clear();
			}
			if (!ENTITY_LIST_MULTIPASS_0.isEmpty()) {
				ENTITY_LIST_MULTIPASS_0.clear();
			}
			if (!ENTITY_LIST_NORMAL_1.isEmpty()) {
				ENTITY_LIST_NORMAL_1.clear();
			}
			if (!ENTITY_LIST_OUTLINE_1.isEmpty()) {
				ENTITY_LIST_OUTLINE_1.clear();
			}
			if (!ENTITY_LIST_MULTIPASS_1.isEmpty()) {
				ENTITY_LIST_MULTIPASS_1.clear();
			}
			if (!TILE_ENTITY_LIST_NORMAL_0.isEmpty()) {
				TILE_ENTITY_LIST_NORMAL_0.clear();
			}
			if (!TILE_ENTITY_LIST_SYNCHRONIZED_0.isEmpty()) {
				TILE_ENTITY_LIST_SYNCHRONIZED_0.clear();
			}
			if (!TILE_ENTITY_LIST_NORMAL_1.isEmpty()) {
				TILE_ENTITY_LIST_NORMAL_1.clear();
			}
			if (!TILE_ENTITY_LIST_SYNCHRONIZED_1.isEmpty()) {
				TILE_ENTITY_LIST_SYNCHRONIZED_1.clear();
			}
		}

		public static boolean renderEntities() {
			int pass = MinecraftForgeClient.getRenderPass();

			if (pass == 0) {
				updateEntityLists();
			}

			if (EntityCullingConfig.enabled) {
				Minecraft mc = Minecraft.getMinecraft();
				RenderManager renderManager = mc.getRenderManager();
				float partialTicks = mc.getRenderPartialTicks();
				boolean shadersEnabled = Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null));

				if (pass == 0) {
					if (!ENTITY_LIST_NORMAL_0.isEmpty()) {
						for (Entity entity : ENTITY_LIST_NORMAL_0) {
							FIELD_RENDERED_ENTITY.set(mc.renderGlobal, entity);
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
							FIELD_RENDERED_ENTITY.set(mc.renderGlobal, null);
						}
					}
					if (!ENTITY_LIST_MULTIPASS_0.isEmpty()) {
						for (Entity entity : ENTITY_LIST_MULTIPASS_0) {
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderMultipass(entity, partialTicks);
						}
					}
					if (isRenderEntityOutlines() && (!ENTITY_LIST_OUTLINE_0.isEmpty() || entityOutlinesRendered)) {
						Framebuffer entityOutlineFramebuffer = FIELD_ENTITY_OUTLINE_FRAMEBUFFER.get(mc.renderGlobal);
						ShaderGroup entityOutlineShader = FIELD_ENTITY_OUTLINE_SHADER.get(mc.renderGlobal);
						mc.world.profiler.endStartSection("entityOutlines");
						entityOutlineFramebuffer.framebufferClear();
						entityOutlinesRendered = !ENTITY_LIST_OUTLINE_0.isEmpty();

						if (!ENTITY_LIST_OUTLINE_0.isEmpty()) {
							GlStateManager.depthFunc(519);
							GlStateManager.disableFog();
							entityOutlineFramebuffer.bindFramebuffer(false);
							RenderHelper.disableStandardItemLighting();
							renderManager.setRenderOutlines(true);

							for (Entity entity : ENTITY_LIST_OUTLINE_0) {
								renderManager.renderEntityStatic(entity, partialTicks, false);
							}

							renderManager.setRenderOutlines(false);
							RenderHelper.enableStandardItemLighting();
							GlStateManager.depthMask(false);
							entityOutlineShader.render(partialTicks);
							GlStateManager.enableLighting();
							GlStateManager.depthMask(true);
							GlStateManager.enableFog();
							GlStateManager.enableBlend();
							GlStateManager.enableColorMaterial();
							GlStateManager.depthFunc(515);
							GlStateManager.enableDepth();
							GlStateManager.enableAlpha();
						}

						mc.getFramebuffer().bindFramebuffer(false);
					}
				} else if (pass == 1) {
					if (!ENTITY_LIST_NORMAL_1.isEmpty()) {
						for (Entity entity : ENTITY_LIST_NORMAL_1) {
							FIELD_RENDERED_ENTITY.set(mc.renderGlobal, entity);
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
							FIELD_RENDERED_ENTITY.set(mc.renderGlobal, null);
						}
					}
					if (!ENTITY_LIST_MULTIPASS_1.isEmpty()) {
						for (Entity entity : ENTITY_LIST_MULTIPASS_1) {
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderMultipass(entity, partialTicks);
						}
					}
					if (!ENTITY_LIST_OUTLINE_1.isEmpty() && !isRenderEntityOutlines()) {
						mc.world.profiler.endStartSection("entityOutlines");
						if (shadersEnabled) {
							METHOD_BEGIN_ENTITIES_GLOWING.invoke(null);
						}
						GlStateManager.disableFog();
						GlStateManager.disableDepth();
						mc.entityRenderer.disableLightmap();
						RenderHelper.disableStandardItemLighting();
						renderManager.setRenderOutlines(true);

						for (Entity entity : ENTITY_LIST_OUTLINE_1) {
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
						}

						renderManager.setRenderOutlines(false);
						RenderHelper.enableStandardItemLighting();
						mc.entityRenderer.enableLightmap();
						GlStateManager.enableDepth();
						GlStateManager.enableFog();
						if (shadersEnabled) {
							METHOD_END_ENTITIES_GLOWING.invoke(null);
						}
					}
				}
			}

			return EntityCullingConfig.enabled;
		}

		private static boolean isRenderEntityOutlines() {
			if (Boolean.TRUE.equals(METHOD_IS_FAST_RENDER.invoke(null))) {
				return false;
			}
			if (Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null))) {
				return false;
			}
			if (Boolean.TRUE.equals(METHOD_IS_ANTIALIASING.invoke(null))) {
				return false;
			}
			RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
			if (FIELD_ENTITY_OUTLINE_FRAMEBUFFER.get(renderGlobal) == null) {
				return false;
			}
			return FIELD_ENTITY_OUTLINE_SHADER.get(renderGlobal) != null;
		}

		public static boolean renderTileEntities() {
			int pass = MinecraftForgeClient.getRenderPass();

			if (EntityCullingConfig.enabled) {
				Minecraft mc = Minecraft.getMinecraft();
				float partialTicks = mc.getRenderPartialTicks();
				boolean shadersEnabled = Boolean.TRUE.equals(METHOD_IS_SHADERS.invoke(null));

				if (pass == 0) {
					if (!TILE_ENTITY_LIST_NORMAL_0.isEmpty()) {
						for (TileEntity tileEntity : TILE_ENTITY_LIST_NORMAL_0) {
							if (shadersEnabled) {
								METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
							}
							TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
						}
					}
					if (!TILE_ENTITY_LIST_SYNCHRONIZED_0.isEmpty()) {
						for (TileEntity tileEntity : TILE_ENTITY_LIST_SYNCHRONIZED_0) {
							if (shadersEnabled) {
								METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
							}
							TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
						}
					}
				} else if (pass == 1) {
					if (!TILE_ENTITY_LIST_NORMAL_1.isEmpty()) {
						for (TileEntity tileEntity : TILE_ENTITY_LIST_NORMAL_1) {
							if (shadersEnabled) {
								METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
							}
							TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
						}
					}
					if (!TILE_ENTITY_LIST_SYNCHRONIZED_1.isEmpty()) {
						for (TileEntity tileEntity : TILE_ENTITY_LIST_SYNCHRONIZED_1) {
							if (shadersEnabled) {
								METHOD_NEXT_BLOCK_ENTITY.invoke(null, tileEntity);
							}
							TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
						}
					}
				}
			}

			if (pass == 1) {
				clearEntityLists();
			}

			return EntityCullingConfig.enabled;
		}

	}

}
