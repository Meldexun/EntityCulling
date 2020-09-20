package meldexun.entityculling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ModRenderHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = EntityCulling.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler {

	@SubscribeEvent
	public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(EntityCulling.MOD_ID)) {
			ConfigManager.sync(EntityCulling.MOD_ID, Config.Type.INSTANCE);
			BLACKLIST.clear();
			for (String s : ModConfig.blacklist) {
				BLACKLIST.add(new ResourceLocation(s));
			}
		}
	}

	private static final Set<ResourceLocation> BLACKLIST = new HashSet<>();
	private static boolean shouldRenderAll = false;
	private static final Set<Entity> ENTITIES_TO_RENDER = new HashSet<>();
	private static final Comparator<Entity> ENTITY_SORTER = (entity1, entity2) -> {
		EntityPlayer player = Minecraft.getMinecraft().player;
		double distance1 = player.getDistanceSq(entity1);
		double distance2 = player.getDistanceSq(entity2);
		if (distance1 < distance2) {
			return -1;
		}
		if (distance1 > distance2) {
			return 1;
		}
		return 0;
	};

	@SubscribeEvent
	public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.START) {
			return;
		}
		shouldRenderAll = true;
		ENTITIES_TO_RENDER.clear();
		if (!ModConfig.limitEntityRendering) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.world == null) {
			return;
		}
		List<Entity> list = new ArrayList<>();
		for (RenderChunk renderChunk : ModRenderHelper.getRenderChunks()) {
			BlockPos pos = renderChunk.getPosition();
			Chunk chunk = mc.world.getChunk(pos);
			for (Entity entity : chunk.getEntityLists()[pos.getY() >> 4]) {
				if (!(entity instanceof EntityLivingBase)) {
					continue;
				}
				if (!entity.isNonBoss()) {
					continue;
				}
				if (entity.width >= ModConfig.maxEntitySizeToSkipRendering || entity.height >= ModConfig.maxEntitySizeToSkipRendering) {
					continue;
				}
				if (!BLACKLIST.isEmpty() && BLACKLIST.contains(EntityList.getKey(entity))) {
					continue;
				}
				list.add(entity);
			}
		}
		if (list.size() >= ModConfig.limitEntityRenderingCount) {
			shouldRenderAll = false;
			list.sort(ENTITY_SORTER);
			for (int i = 0; i < ModConfig.limitEntityRenderingCount; i++) {
				ENTITIES_TO_RENDER.add(list.get(i));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRenderLivingEvent(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (!shouldRenderEntity(event.getEntity())) {
			event.setCanceled(true);
		}
	}

	private static boolean shouldRenderEntity(Entity entity) {
		if (!(entity instanceof EntityLivingBase)) {
			return true;
		}
		if (!entity.isNonBoss()) {
			return true;
		}
		if (entity.width >= ModConfig.maxEntitySizeToSkipRendering || entity.height >= ModConfig.maxEntitySizeToSkipRendering) {
			return true;
		}
		if (!BLACKLIST.isEmpty() && BLACKLIST.contains(EntityList.getKey(entity))) {
			return true;
		}
		if (ModConfig.limitEntityRendering && !shouldRenderAll && !ENTITIES_TO_RENDER.contains(entity)) {
			return false;
		}
		if (!ModConfig.skipHiddenEntityRendering) {
			return true;
		}
		int maxDiff = ModConfig.skipHiddenEntityRenderingDiff * ModConfig.skipHiddenEntityRenderingDiff;
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d start = mc.player.getPositionEyes(mc.getRenderPartialTicks());
		Vec3d end = entity.getPositionEyes(mc.getRenderPartialTicks());
		if (start.squareDistanceTo(end) <= maxDiff) {
			return true;
		}
		RayTraceResult result1 = rayTraceBlocks(mc.world, start, end, false, true);
		if (result1 == null || result1.hitVec.squareDistanceTo(end) <= maxDiff) {
			return true;
		}
		RayTraceResult result2 = rayTraceBlocks(mc.world, end, start, false, true);
		if (result2 == null) {
			return true;
		}
		return result1.hitVec.squareDistanceTo(result2.hitVec) <= maxDiff;
	}

	@Nullable
	private static RayTraceResult rayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
			if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
				int i = MathHelper.floor(vec32.x);
				int j = MathHelper.floor(vec32.y);
				int k = MathHelper.floor(vec32.z);
				int l = MathHelper.floor(vec31.x);
				int i1 = MathHelper.floor(vec31.y);
				int j1 = MathHelper.floor(vec31.z);
				BlockPos blockpos = new BlockPos(l, i1, j1);
				IBlockState iblockstate = world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if (iblockstate.isOpaqueCube() && iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && block.canCollideCheck(iblockstate, stopOnLiquid)) {
					RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

					if (raytraceresult != null) {
						return raytraceresult;
					}
				}

				int k1 = 200;

				while (k1-- >= 0) {
					if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
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
					double d6 = vec32.x - vec31.x;
					double d7 = vec32.y - vec31.y;
					double d8 = vec32.z - vec31.z;

					if (flag2) {
						d3 = (d0 - vec31.x) / d6;
					}

					if (flag) {
						d4 = (d1 - vec31.y) / d7;
					}

					if (flag1) {
						d5 = (d2 - vec31.z) / d8;
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
						vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
					} else if (d4 < d5) {
						enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
						vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
					} else {
						enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
						vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
					}

					l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
					i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
					j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
					blockpos = new BlockPos(l, i1, j1);
					iblockstate = world.getBlockState(blockpos);
					block = iblockstate.getBlock();

					if (iblockstate.isOpaqueCube() && iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && block.canCollideCheck(iblockstate, stopOnLiquid)) {
						RayTraceResult raytraceresult1 = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

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

}
