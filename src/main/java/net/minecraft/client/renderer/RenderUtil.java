package net.minecraft.client.renderer;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import meldexun.entityculling.reflection.ReflectionField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;

public class RenderUtil {

	private static final ReflectionField<List<RenderGlobal.ContainerLocalRenderInformation>> FIELD_RENDER_INFOS = new ReflectionField<>(RenderGlobal.class, "field_72755_R", "renderInfos");

	private RenderUtil() {

	}

	public static Iterable<RenderChunk> getRenderChunks() {
		return () -> new Iterator<RenderChunk>() {
			private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = FIELD_RENDER_INFOS.get(Minecraft.getMinecraft().renderGlobal);
			private int index;

			@Override
			public boolean hasNext() {
				return this.index < this.renderInfos.size();
			}

			@Override
			public RenderChunk next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				return this.renderInfos.get(this.index++).renderChunk;
			}
		};
	}

	public static class Optifine {

		private static final ReflectionField<List<RenderGlobal.ContainerLocalRenderInformation>> FIELD_RENDER_INFOS_ENITTIES = new ReflectionField<>(RenderGlobal.class, "renderInfosEntities", "renderInfosEntities");
		private static final ReflectionField<List<RenderGlobal.ContainerLocalRenderInformation>> FIELD_RENDER_INFOS_TILE_ENITTIES = new ReflectionField<>(RenderGlobal.class, "renderInfosTileEntities", "renderInfosTileEntities");

		public static Iterable<RenderChunk> getRenderChunksEntities() {
			return () -> new Iterator<RenderChunk>() {
				private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = FIELD_RENDER_INFOS_ENITTIES.get(Minecraft.getMinecraft().renderGlobal);
				private int index;

				@Override
				public boolean hasNext() {
					return this.index < this.renderInfos.size();
				}

				@Override
				public RenderChunk next() {
					if (!this.hasNext()) {
						throw new NoSuchElementException();
					}
					return this.renderInfos.get(this.index++).renderChunk;
				}
			};
		}

		public static Iterable<RenderChunk> getRenderChunksTileEntities() {
			return () -> new Iterator<RenderChunk>() {
				private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = FIELD_RENDER_INFOS_TILE_ENITTIES.get(Minecraft.getMinecraft().renderGlobal);
				private int index;

				@Override
				public boolean hasNext() {
					return this.index < this.renderInfos.size();
				}

				@Override
				public RenderChunk next() {
					if (!this.hasNext()) {
						throw new NoSuchElementException();
					}
					return this.renderInfos.get(this.index++).renderChunk;
				}
			};
		}

	}

}
