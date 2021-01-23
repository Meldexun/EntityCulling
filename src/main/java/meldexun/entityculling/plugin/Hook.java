package meldexun.entityculling.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import meldexun.entityculling.EntityCullingConfig;
import meldexun.entityculling.integration.CubicChunks;
import meldexun.entityculling.reflection.ReflectionField;
import meldexun.entityculling.reflection.ReflectionMethod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.Loader;

public class Hook {

	private static final ReflectionField<RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>>> FIELD_REGISTRY = new ReflectionField<>(TileEntity.class, "field_190562_f", "REGISTRY");
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
	private static final Set<Class<? extends Entity>> ENTITY_BLACKLIST = new HashSet<>();
	private static final Set<Class<? extends TileEntity>> TILE_ENTITY_BLACKLIST = new HashSet<>();
	private static boolean entityOutlinesRendered = false;

	private Hook() {

	}

	public static void updateBlacklists() {
		ENTITY_BLACKLIST.clear();
		TILE_ENTITY_BLACKLIST.clear();

		for (String s : EntityCullingConfig.skipHiddenEntityRenderingBlacklist) {
			Class<? extends Entity> entityClass = EntityList.getClassFromName(s);
			if (entityClass != null) {
				ENTITY_BLACKLIST.add(entityClass);
			}
		}

		RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> tileEntityRegistry = FIELD_REGISTRY.get(null);
		for (String s : EntityCullingConfig.skipHiddenTileEntityRenderingBlacklist) {
			Class<? extends TileEntity> entityClass = tileEntityRegistry.getObject(new ResourceLocation(s));
			if (entityClass != null) {
				TILE_ENTITY_BLACKLIST.add(entityClass);
			}
		}
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
		Vec3d camVec = new Vec3d(x, y + renderViewEntity.getEyeHeight(), z);
		boolean isThirdPersonView = mc.gameSettings.thirdPersonView != 0;
		boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();
		boolean flag = !isThirdPersonView && !isSleeping;
		boolean flag1 = mc.player.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();

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
					boolean flag2 = checkEntityVisibility(entity, camVec);
					if (entity.shouldRenderInPass(0)) {
						if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
							ENTITY_LIST_OUTLINE_0.add(entity);
						}
						if (flag2) {
							ENTITY_LIST_NORMAL_0.add(entity);
							if (render.isMultipass()) {
								ENTITY_LIST_MULTIPASS_0.add(entity);
							}
						}
					}
					if (entity.shouldRenderInPass(1) && flag2) {
						ENTITY_LIST_NORMAL_1.add(entity);
						if (render.isMultipass()) {
							ENTITY_LIST_MULTIPASS_1.add(entity);
						}
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
					if (!checkTileEntityVisibility(tileEntity, camVec)) {
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
					if (!checkTileEntityVisibility(tileEntity, camVec)) {
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
						renderManager.renderEntityStatic(entity, partialTicks, false);
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
						renderManager.renderEntityStatic(entity, partialTicks, false);
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
		AxisAlignedBB axisalignedbb = entity.getRenderBoundingBox().grow(0.5D);

		if (axisalignedbb.hasNaN()) {
			axisalignedbb = new AxisAlignedBB(entity.posX - 2.0D, entity.posY - 2.0D, entity.posZ - 2.0D, entity.posX + 2.0D, entity.posY + 2.0D, entity.posZ + 2.0D);
		}

		return camera.isBoundingBoxInFrustum(axisalignedbb);
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

	private static boolean checkEntityVisibility(Entity entity, Vec3d camVec) {
		if (!EntityCullingConfig.skipHiddenEntityRendering) {
			return true;
		}
		if (!entity.isNonBoss()) {
			return true;
		}
		if (entity.width >= EntityCullingConfig.skipHiddenEntityRenderingSize || entity.height >= EntityCullingConfig.skipHiddenEntityRenderingSize) {
			return true;
		}
		if (!ENTITY_BLACKLIST.isEmpty() && ENTITY_BLACKLIST.contains(entity.getClass())) {
			return true;
		}
		double maxDiffSquared = EntityCullingConfig.skipHiddenEntityRenderingDiff * EntityCullingConfig.skipHiddenEntityRenderingDiff;
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d end = entity.getPositionEyes(mc.getRenderPartialTicks());
		if (camVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result1 = rayTraceBlocks(mc.world, camVec, end, false, true, null);
		if (result1 == null || result1.hitVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result2 = rayTraceBlocks(mc.world, end, camVec, false, true, null);
		if (result2 == null) {
			return true;
		}
		return result1.hitVec.squareDistanceTo(result2.hitVec) <= maxDiffSquared;
	}

	private static boolean checkTileEntityVisibility(TileEntity tileEntity, Vec3d camVec) {
		if (!EntityCullingConfig.skipHiddenTileEntityRendering) {
			return true;
		}
		AxisAlignedBB aabb = tileEntity.getRenderBoundingBox();
		if (aabb.maxX - aabb.minX > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxY - aabb.minY > EntityCullingConfig.skipHiddenTileEntityRenderingSize || aabb.maxZ - aabb.minZ > EntityCullingConfig.skipHiddenTileEntityRenderingSize) {
			return true;
		}
		if (!TILE_ENTITY_BLACKLIST.isEmpty() && TILE_ENTITY_BLACKLIST.contains(tileEntity.getClass())) {
			return true;
		}
		double maxDiffSquared = EntityCullingConfig.skipHiddenTileEntityRenderingDiff * EntityCullingConfig.skipHiddenTileEntityRenderingDiff;
		Minecraft mc = Minecraft.getMinecraft();
		BlockPos pos = tileEntity.getPos();
		Vec3d end = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
		if (camVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result1 = rayTraceBlocks(mc.world, camVec, end, false, true, pos);
		if (result1 == null || result1.hitVec.squareDistanceTo(end) <= maxDiffSquared) {
			return true;
		}
		RayTraceResult result2 = rayTraceBlocks(mc.world, end, camVec, false, true, pos);
		if (result2 == null) {
			return true;
		}
		return result1.hitVec.squareDistanceTo(result2.hitVec) <= maxDiffSquared;
	}

	@Nullable
	private static RayTraceResult rayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, @Nullable BlockPos toIgnore) {
		if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
			if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
				int i = MathHelper.floor(vec32.x);
				int j = MathHelper.floor(vec32.y);
				int k = MathHelper.floor(vec32.z);
				int l = MathHelper.floor(vec31.x);
				int i1 = MathHelper.floor(vec31.y);
				int j1 = MathHelper.floor(vec31.z);
				BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos(l, i1, j1);
				IBlockState iblockstate = world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if ((toIgnore == null || !blockpos.equals(toIgnore)) && iblockstate.isOpaqueCube() && iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && block.canCollideCheck(iblockstate, stopOnLiquid)) {
					RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

					if (raytraceresult != null) {
						return raytraceresult;
					}
				}

				int k1 = 200;
				double x = vec31.x;
				double y = vec31.y;
				double z = vec31.z;

				while (k1-- >= 0) {
					if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
						return null;
					}

					if (l == i && i1 == j && j1 == k) {
						return null;
					}

					boolean flag2 = true;
					boolean flag = true;
					boolean flag1 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (i > l) {
						d0 = (double) l + 1.0D;
					} else if (i < l) {
						d0 = (double) l + 0.0D;
					} else {
						flag2 = false;
					}

					if (j > i1) {
						d1 = (double) i1 + 1.0D;
					} else if (j < i1) {
						d1 = (double) i1 + 0.0D;
					} else {
						flag = false;
					}

					if (k > j1) {
						d2 = (double) j1 + 1.0D;
					} else if (k < j1) {
						d2 = (double) j1 + 0.0D;
					} else {
						flag1 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = vec32.x - x;
					double d7 = vec32.y - y;
					double d8 = vec32.z - z;

					if (flag2) {
						d3 = (d0 - x) / d6;
					}

					if (flag) {
						d4 = (d1 - y) / d7;
					}

					if (flag1) {
						d5 = (d2 - z) / d8;
					}

					if (d3 == -0.0D) {
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D) {
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D) {
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5) {
						enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
						x = d0;
						y = y + d7 * d3;
						z = z + d8 * d3;
					} else if (d4 < d5) {
						enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
						x = x + d6 * d4;
						y = d1;
						z = z + d8 * d4;
					} else {
						enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
						x = x + d6 * d5;
						y = y + d7 * d5;
						z = d2;
					}

					l = MathHelper.floor(x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
					i1 = MathHelper.floor(y) - (enumfacing == EnumFacing.UP ? 1 : 0);
					j1 = MathHelper.floor(z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
					blockpos.setPos(l, i1, j1);
					iblockstate = world.getBlockState(blockpos);
					block = iblockstate.getBlock();

					if ((toIgnore == null || !blockpos.equals(toIgnore)) && iblockstate.isOpaqueCube() && iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && block.canCollideCheck(iblockstate, stopOnLiquid)) {
						RayTraceResult raytraceresult1 = iblockstate.collisionRayTrace(world, blockpos, new Vec3d(x, y, z), vec32);

						if (raytraceresult1 != null) {
							return raytraceresult1;
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static class Optifine {

		private static final ReflectionMethod<Boolean> METHOD_IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
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
			Vec3d camVec = new Vec3d(x, y + renderViewEntity.getEyeHeight(), z);
			boolean isThirdPersonView = mc.gameSettings.thirdPersonView != 0;
			boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();
			boolean flag = !isThirdPersonView && !isSleeping;
			boolean flag1 = mc.player.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();

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
						boolean flag2 = checkEntityVisibility(entity, camVec);
						if (entity.shouldRenderInPass(0)) {
							if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
								ENTITY_LIST_OUTLINE_0.add(entity);
							}
							if (flag2) {
								ENTITY_LIST_NORMAL_0.add(entity);
								if (render.isMultipass()) {
									ENTITY_LIST_MULTIPASS_0.add(entity);
								}
							}
						}
						if (entity.shouldRenderInPass(1)) {
							if (entity.isGlowing() || (flag1 && entity instanceof EntityPlayer)) {
								ENTITY_LIST_OUTLINE_1.add(entity);
							}
							if (flag2) {
								ENTITY_LIST_NORMAL_1.add(entity);
								if (render.isMultipass()) {
									ENTITY_LIST_MULTIPASS_1.add(entity);
								}
							}
						}
					}
				}
			}

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
						if (!checkTileEntityVisibility(tileEntity, camVec)) {
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
						if (!checkTileEntityVisibility(tileEntity, camVec)) {
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
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
						}
					}
					if (!ENTITY_LIST_MULTIPASS_0.isEmpty()) {
						for (Entity entity : ENTITY_LIST_MULTIPASS_0) {
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
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
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
						}
					}
					if (!ENTITY_LIST_MULTIPASS_1.isEmpty()) {
						for (Entity entity : ENTITY_LIST_MULTIPASS_1) {
							if (shadersEnabled) {
								METHOD_NEXT_ENTITY.invoke(null, entity);
							}
							renderManager.renderEntityStatic(entity, partialTicks, false);
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
