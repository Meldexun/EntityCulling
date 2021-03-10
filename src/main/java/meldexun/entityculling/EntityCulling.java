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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EntityCulling.MOD_ID)
public class EntityCulling {

	public static final String MOD_ID = "entity_culling";
	private static final Logger LOGGER = LogManager.getLogger();
	public static final CullingThread CULLING_THREAD = new CullingThread();
	public static final boolean IS_OPTIFINE_DETECTED;

	static {
		boolean flag = false;
		try {
			Class.forName("net.optifine.Config", false, Thread.currentThread().getContextClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		IS_OPTIFINE_DETECTED = flag;
	}

	public EntityCulling() {
		ModLoadingContext.get().registerConfig(Type.CLIENT, EntityCullingConfig.CLIENT_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		CullingThread.updateBlacklists();

		event.enqueueWork(this::generateCubeDisplayList);
		CULLING_THREAD.start();
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public void onWorldTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (EntityCullingConfig.CLIENT_CONFIG.debug.get() && Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 40 == 0) {
				LOGGER.info("{}", Arrays.stream(CULLING_THREAD.time).sum() / 1_000 / 10);
			}
			if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 4 == 0) {
				ICullable.deleteInvalidTileEntityQueries(Minecraft.getInstance().world);
			}
		}
	}

	public static int cubeDisplayList;

	private void generateCubeDisplayList() {
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
	public void onEntityLeaveWorldEvent(EntityLeaveWorldEvent event) {
		if (event.getWorld().isRemote()) {
			((ICullable) event.getEntity()).deleteQuery();
		}
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		if (event.getWorld().isRemote()) {
			for (Entity e : ((ClientWorld) event.getWorld()).getAllEntities()) {
				((ICullable) e).deleteQuery();
			}
			for (TileEntity te : ((ClientWorld) event.getWorld()).loadedTileEntityList) {
				((ICullable) te).deleteQuery();
			}
			ICullable.deleteTileEntityQueries();
		}
	}

	@SubscribeEvent
	public void onChunkUnloadEvent(ChunkEvent.Unload event) {
		if (event.getWorld().isRemote()) {
			for (ClassInheritanceMultiMap<Entity> entityMap : ((Chunk) event.getChunk()).getEntityLists()) {
				for (Entity e : entityMap) {
					((ICullable) e).deleteQuery();
				}
			}
			for (TileEntity te : ((Chunk) event.getChunk()).getTileEntityMap().values()) {
				((ICullable) te).deleteQuery();
			}
		}
	}

}
