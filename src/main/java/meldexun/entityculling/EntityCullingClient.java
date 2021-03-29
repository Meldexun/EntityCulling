package meldexun.entityculling;

import static org.lwjgl.opengl.GL21C.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import meldexun.entityculling.plugin.Hook;
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

	public static int vertexBuffer;
	public static int indexBuffer;

	public static void generateCubeDisplayList() {
		FloatBuffer vertexByteBuffer = ByteBuffer.allocateDirect(8 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexByteBuffer.put(new float[] {
				0, 0, 0,
				0, 0, 1,
				0, 1, 0,
				0, 1, 1,
				1, 0, 0,
				1, 0, 1,
				1, 1, 0,
				1, 1, 1
		});
		vertexByteBuffer.rewind();
		ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(14).order(ByteOrder.nativeOrder());
		indexByteBuffer.put(new byte[] {
				0, 4,
				1, 5,
				7, 4,
				6, 0,
				2, 1,
				3, 7,
				2, 6
		});
		indexByteBuffer.rewind();

		vertexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertexByteBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		indexBuffer = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexByteBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public void onWorldTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (EntityCullingConfig.CLIENT_CONFIG.debug.get() && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 40 == 0) {
				int i1 = (int) Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10;
				int i2 = Hook.entitiesRendered;
				int i3 = Hook.entitiesOcclusionCulled;
				int i4 = Hook.tileEntitiesRendered;
				int i5 = Hook.tileEntitiesOcclusionCulled;
				LOGGER.info("Culling Thread: {}µs, Entities Rendered: {}, Entities Culled: {}, TileEntities Rendered: {}, TileEntities Culled: {}", i1, i2, i3, i4, i5);
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
