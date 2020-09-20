package net.minecraft.client.renderer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;

public class ModRenderHelper {

	private static Field fieldRenderInfos;

	@SuppressWarnings("unchecked")
	private static List<RenderGlobal.ContainerLocalRenderInformation> getFieldRenderInfos() {
		if (fieldRenderInfos == null) {
			try {
				try {
					fieldRenderInfos = RenderGlobal.class.getDeclaredField("renderInfos");
				} catch (NoSuchFieldException e) {
					fieldRenderInfos = RenderGlobal.class.getDeclaredField("renderInfos");
				}
				fieldRenderInfos.setAccessible(true);
			} catch (NoSuchFieldException | SecurityException e) {
				return Collections.emptyList();
			}
		}
		try {
			return (List<RenderGlobal.ContainerLocalRenderInformation>) fieldRenderInfos.get(Minecraft.getMinecraft().renderGlobal);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return Collections.emptyList();
		}
	}

	public static Iterable<RenderChunk> getRenderChunks() {
		return () -> new Iterator<RenderChunk>() {
			private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = getFieldRenderInfos();
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
