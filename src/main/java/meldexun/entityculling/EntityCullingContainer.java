package meldexun.entityculling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL15;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import meldexun.entityculling.plugin.Hook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class EntityCullingContainer extends DummyModContainer {

	public static final String MOD_ID = "entity_culling";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final CullingThread CULLING_THREAD = new CullingThread();

	public EntityCullingContainer() {
		super(new ModMetadata());
		ModMetadata meta = this.getMetadata();
		meta.name = "Entity Culling";
		meta.version = "4.1.2";
		meta.modId = MOD_ID;
		meta.authorList = Arrays.asList("Meldexun");
		meta.url = "https://github.com/Meldexun/EntityCulling";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@Subscribe
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
		CullingThread.updateBlacklists();
		GLHelper.init();
		generateCubeDisplayList();

		CULLING_THREAD.start();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			CullingThread.updateBlacklists();
		}
	}

	@SubscribeEvent
	public void onWorldTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (EntityCullingConfig.debug && Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.getTotalWorldTime() % 40 == 0) {
				int i1 = (int) Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10;
				int i2 = Hook.getRenderer().entitiesRendered;
				int i3 = Hook.getRenderer().entitiesOcclusionCulled;
				int i4 = Hook.getRenderer().tileEntitiesRendered;
				int i5 = Hook.getRenderer().tileEntitiesOcclusionCulled;
				LOGGER.info("Culling Thread: {}µs, Entities Rendered: {}, Entities Culled: {}, TileEntities Rendered: {}, TileEntities Culled: {}", i1, i2, i3, i4, i5);
			}
			if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.getTotalWorldTime() % 20 == 0) {
				ICullable.deleteInvalidTileEntityQueries(Minecraft.getMinecraft().world);
			}
		}
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

		vertexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexByteBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		indexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexByteBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		if (event.getWorld().isRemote) {
			event.getWorld().addEventListener(new EntityCullingWorldEventListener());
		}
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		if (event.getWorld().isRemote) {
			for (Entity e : event.getWorld().loadedEntityList) {
				((ICullable) e).deleteQuery();
			}
			for (TileEntity te : event.getWorld().loadedTileEntityList) {
				((ICullable) te).deleteQuery();
			}
			ICullable.deleteTileEntityQueries();
		}
	}

	@SubscribeEvent
	public void onChunkUnloadEvent(ChunkEvent.Unload event) {
		if (event.getWorld().isRemote) {
			for (ClassInheritanceMultiMap<Entity> entityMap : event.getChunk().getEntityLists()) {
				for (Entity e : entityMap) {
					((ICullable) e).deleteQuery();
				}
			}
			for (TileEntity te : event.getChunk().getTileEntityMap().values()) {
				((ICullable) te).deleteQuery();
			}
		}
	}

}
