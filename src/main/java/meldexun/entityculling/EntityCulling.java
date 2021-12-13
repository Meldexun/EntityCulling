package meldexun.entityculling;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import meldexun.entityculling.asm.hook.RenderGlobalHook;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.util.CullingThread;
import meldexun.entityculling.util.IBoundingBoxCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityCulling extends DummyModContainer {

	public static final String MOD_ID = "entity_culling";
	private static CullingThread cullingThread;
	private static final DecimalFormat FORMAT = new DecimalFormat("#.#");
	public static boolean isCubicChunksInstalled;

	public EntityCulling() {
		super(new ModMetadata());
		ModMetadata meta = this.getMetadata();
		meta.name = "Entity Culling";
		meta.version = "4.2.2";
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
		IBoundingBoxCache.updateBlacklist();

		MinecraftForge.EVENT_BUS.register(this);

		cullingThread = new CullingThread();
		cullingThread.start();
	}

	@Subscribe
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isCubicChunksInstalled = Loader.isModLoaded("cubicchunks");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
			CullingThread.updateBlacklists();
			IBoundingBoxCache.updateBlacklist();
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.ALL) {
			return;
		}
		if (!EntityCullingConfig.debugCullInfo) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);
		this.drawOnLeft("Time: " + FORMAT.format(Arrays.stream(cullingThread.time).average().getAsDouble() / 1_000_000.0D) + "ms", scaled.getScaledWidth(),
				160);
		this.drawOnLeft("E: " + RenderGlobalHook.entityRenderer.renderedEntities + "/" + RenderGlobalHook.entityRenderer.occludedEntities + "/"
				+ RenderGlobalHook.entityRenderer.totalEntities, scaled.getScaledWidth(), 170);
		this.drawOnLeft("TE: " + RenderGlobalHook.tileEntityRenderer.renderedTileEntities + "/" + RenderGlobalHook.tileEntityRenderer.occludedTileEntities + "/"
				+ RenderGlobalHook.tileEntityRenderer.totalTileEntities, scaled.getScaledWidth(), 180);
	}

	private void drawOnLeft(String string, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.fontRenderer.drawString(string, x - mc.fontRenderer.getStringWidth(string), y, 0xFFFFFFFF);
	}

	@SuppressWarnings("serial")
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			return;
		}
		event.getWorld().loadedEntityList = new ArrayList<Entity>() {
			private void check() {
				if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
					try {
						throw new IllegalAccessError("Please report this to EntityCulling!");
					} catch (Exception e) {
						Minecraft.getMinecraft().crashed(new CrashReport("Illegal access! Please report this to EntityCulling!", e));
					}
				}
			}

			@Override
			public void add(int index, Entity element) {
				this.check();
				super.add(index, element);
			}

			@Override
			public boolean add(Entity e) {
				this.check();
				return super.add(e);
			}

			@Override
			public boolean addAll(Collection<? extends Entity> c) {
				this.check();
				return super.addAll(c);
			}

			@Override
			public boolean addAll(int index, Collection<? extends Entity> c) {
				this.check();
				return super.addAll(index, c);
			}

			@Override
			public Entity remove(int index) {
				this.check();
				return super.remove(index);
			}

			@Override
			public boolean remove(Object o) {
				this.check();
				return super.remove(o);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				this.check();
				return super.removeAll(c);
			}

			@Override
			public boolean removeIf(Predicate<? super Entity> filter) {
				this.check();
				return super.removeIf(filter);
			}

			@Override
			public void replaceAll(UnaryOperator<Entity> operator) {
				this.check();
				super.replaceAll(operator);
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				this.check();
				return super.retainAll(c);
			}
		};
		event.getWorld().loadedTileEntityList = new ArrayList<TileEntity>() {
			private void check() {
				if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
					try {
						throw new IllegalAccessError("Please report this to EntityCulling!");
					} catch (Exception e) {
						Minecraft.getMinecraft().crashed(new CrashReport("Illegal access! Please report this to EntityCulling!", e));
					}
				}
			}

			@Override
			public void add(int index, TileEntity element) {
				this.check();
				super.add(index, element);
			}

			@Override
			public boolean add(TileEntity e) {
				this.check();
				return super.add(e);
			}

			@Override
			public boolean addAll(Collection<? extends TileEntity> c) {
				this.check();
				return super.addAll(c);
			}

			@Override
			public boolean addAll(int index, Collection<? extends TileEntity> c) {
				this.check();
				return super.addAll(index, c);
			}

			@Override
			public TileEntity remove(int index) {
				this.check();
				return super.remove(index);
			}

			@Override
			public boolean remove(Object o) {
				this.check();
				return super.remove(o);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				this.check();
				return super.removeAll(c);
			}

			@Override
			public boolean removeIf(Predicate<? super TileEntity> filter) {
				this.check();
				return super.removeIf(filter);
			}

			@Override
			public void replaceAll(UnaryOperator<TileEntity> operator) {
				this.check();
				super.replaceAll(operator);
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				this.check();
				return super.retainAll(c);
			}
		};
	}

}
