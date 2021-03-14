package meldexun.entityculling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICullable {

	boolean isCulledFast();

	void setCulledFast(boolean isCulledFast);

	boolean isCulledSlow();

	void setCulledSlow(boolean isCulledSlow);

	boolean isCulledShadowPass();

	void setCulledShadowPass(boolean isCulledShadowPass);

	int getQuery();

	void setQuery(int query);

	boolean isQueryInitialized();

	void setQueryInitialized(boolean queryInitialized);

	boolean isQueryResultDirty();

	void setQueryResultDirty(boolean queryResultUpToDate);

	default boolean isVisible() {
		return !this.isCulledFast() && !this.isCulledSlow();
	}

	default int initQuery() {
		if (!this.isQueryInitialized()) {
			this.setQuery(GL15.glGenQueries());
			GL15.glBeginQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE, this.getQuery());
			GL15.glEndQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE);
			if (this instanceof TileEntity) {
				BlockPos pos = ((TileEntity) this).getBlockPos();
				TileEntity te = TILE_ENTITY_MAP.get(pos);
				if (te != null) {
					((ICullable) te).deleteQuery();
				}
				TILE_ENTITY_MAP.put(pos, (TileEntity) this);
			}
			this.setQueryInitialized(true);
		}
		return this.getQuery();
	}

	default void deleteQuery() {
		if (this.isQueryInitialized()) {
			GL15.glDeleteQueries(this.getQuery());
			this.setQuery(-1);
			this.setQueryInitialized(false);
		}
	}

	Map<BlockPos, TileEntity> TILE_ENTITY_MAP = new HashMap<>();

	static void deleteInvalidTileEntityQueries(World world) {
		Iterator<Map.Entry<BlockPos, TileEntity>> iterator = TILE_ENTITY_MAP.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, TileEntity> entry = iterator.next();
			if (world.getBlockEntity(entry.getKey()) != entry.getValue()) {

				((ICullable) entry.getValue()).deleteQuery();
			}
		}
	}

	static void deleteTileEntityQueries() {
		for (TileEntity tileEntity : TILE_ENTITY_MAP.values()) {
			((ICullable) tileEntity).deleteQuery();
		}
		TILE_ENTITY_MAP.clear();
	}

}
