package meldexun.entityculling;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

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
		meta.version = "4.0.4";
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
				LOGGER.info("{}", Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10);
			}
			if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.getTotalWorldTime() % 4 == 0) {
				ICullable.deleteInvalidTileEntityQueries(Minecraft.getMinecraft().world);
			}
		}
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
