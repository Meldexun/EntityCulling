package meldexun.entityculling;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityCullingClient {

	private static final Logger LOGGER = LogManager.getLogger();
	public static final CullingThread CULLING_THREAD = new CullingThread();

	public static void init() {
		CULLING_THREAD.start();
	}

	public static int cubeDisplayList;

	public static void generateCubeDisplayList() {
		cubeDisplayList = GL11.glGenLists(1);
		GL11.glNewList(cubeDisplayList, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(1, 0, 0);
		GL11.glVertex3f(1, 0, 1);
		GL11.glVertex3f(0, 0, 1);

		GL11.glVertex3f(0, 1, 1);
		GL11.glVertex3f(0, 1, 0);
		GL11.glVertex3f(1, 1, 0);
		GL11.glVertex3f(1, 0, 0);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex3f(1, 1, 1);
		GL11.glVertex3f(1, 1, 0);
		GL11.glVertex3f(0, 1, 0);
		GL11.glVertex3f(0, 1, 1);

		GL11.glVertex3f(0, 0, 1);
		GL11.glVertex3f(1, 0, 1);
		GL11.glVertex3f(1, 0, 0);
		GL11.glVertex3f(1, 1, 0);
		GL11.glEnd();
		GL11.glEndList();
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public void onWorldTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (EntityCullingConfig.CLIENT_CONFIG.debug.get() && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 40 == 0) {
				LOGGER.info("{}", Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10);
			}
			if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 4 == 0) {
				ICullable.deleteInvalidTileEntityQueries(Minecraft.getInstance().level);
			}
		}
	}

	@SubscribeEvent
	public void onEntityLeaveWorldEvent(EntityLeaveWorldEvent event) {
		if (event.getWorld().isClientSide()) {
			((ICullable) event.getEntity()).deleteQuery();
		}
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		if (event.getWorld().isClientSide()) {
			for (Entity e : ((ClientWorld) event.getWorld()).entitiesForRendering()) {
				((ICullable) e).deleteQuery();
			}
			for (TileEntity te : ((ClientWorld) event.getWorld()).blockEntityList) {
				((ICullable) te).deleteQuery();
			}
			ICullable.deleteTileEntityQueries();
		}
	}

	@SubscribeEvent
	public void onChunkUnloadEvent(ChunkEvent.Unload event) {
		if (event.getWorld().isClientSide()) {
			for (ClassInheritanceMultiMap<Entity> entityMap : ((Chunk) event.getChunk()).getEntitySections()) {
				for (Entity e : entityMap) {
					((ICullable) e).deleteQuery();
				}
			}
			for (TileEntity te : ((Chunk) event.getChunk()).getBlockEntities().values()) {
				((ICullable) te).deleteQuery();
			}
		}
	}

}
