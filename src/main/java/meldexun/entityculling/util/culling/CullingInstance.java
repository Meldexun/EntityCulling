package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLSync;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.util.ICullable.CullInfo;
import meldexun.entityculling.util.ResourceSupplier;
import meldexun.matrixutil.Matrix4f;
import meldexun.renderlib.util.GLBuffer;
import meldexun.renderlib.util.GLShader;
import meldexun.renderlib.util.GLUtil;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.util.ResourceLocation;

public class CullingInstance {

	private static final int MAX_OBJ_COUNT = 1 << 16;
	private static CullingInstance instance;
	private static CullingInstance shadow_instance;

	private final GLShader shader;
	private final int uniform_projViewMat;
	private final int uniform_frame;

	public final int cubeVertexBuffer;
	public final int cubeIndexBuffer;
	private final GLBuffer vboBuffer;
	private final int vao;
	private final GLBuffer ssboBuffer;

	private int objCount;
	private int frame;

	private GLSync fence;

	public CullingInstance() {
		shader = new GLShader.Builder()
				.addShader(GL20.GL_VERTEX_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/vert.glsl")))
				.addShader(GL20.GL_FRAGMENT_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/frag.glsl")))
				.build();
		uniform_projViewMat = shader.getUniform("projectionViewMatrix");
		uniform_frame = shader.getUniform("frame");

		vboBuffer = new GLBuffer(MAX_OBJ_COUNT * 7 * 4, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, true, GL30.GL_MAP_WRITE_BIT);
		ssboBuffer = new GLBuffer(MAX_OBJ_COUNT * 4, GL30.GL_MAP_READ_BIT, GL15.GL_STREAM_DRAW, true, GL30.GL_MAP_READ_BIT);

		cubeVertexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVertexBuffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, asByteBuffer(new byte[] {
				0, 0, 0,
				0, 0, 1,
				0, 1, 0,
				0, 1, 1,
				1, 0, 0,
				1, 0, 1,
				1, 1, 0,
				1, 1, 1
		}), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		cubeIndexBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, cubeIndexBuffer);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, asByteBuffer(new byte[] {
				7, 3, 5, 1, 0, 3, 2, 7, 6, 5, 4, 0, 6, 2
		}), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, cubeVertexBuffer);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_BYTE, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboBuffer.getBuffer());
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 28, 0);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 28, 12);
		GL30.glVertexAttribIPointer(3, 1, GL11.GL_INT, 28, 24);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL33.glVertexAttribDivisor(1, 1);
		GL33.glVertexAttribDivisor(2, 1);
		GL33.glVertexAttribDivisor(3, 1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, cubeIndexBuffer);
		GL30.glBindVertexArray(0);
	}

	public static CullingInstance getInstance() {
		if (instance == null) {
			instance = new CullingInstance();
		}
		return instance;
	}

	public static CullingInstance getShadowInstance() {
		if (shadow_instance == null) {
			shadow_instance = new CullingInstance();
		}
		return shadow_instance;
	}

	private static ByteBuffer asByteBuffer(byte[] data) {
		return (ByteBuffer) GLAllocation.createDirectByteBuffer(data.length).put(data).flip();
	}

	private void sync() {
		if (fence != null) {
			GL32.glClientWaitSync(fence, 0, 1_000_000_000);
			GL32.glDeleteSync(fence);
			fence = null;
		}
	}

	public boolean isVisible(CullInfo cullInfo) {
		if (cullInfo.getLastTimeUpdated() < frame - 1) {
			return true;
		}
		sync();
		return ssboBuffer.getByteBuffer().getInt(cullInfo.getId() * 4) == frame;
	}

	public void addBox(CullInfo cullInfo, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (cullInfo.getLastTimeUpdated() == frame) {
			return;
		}
		if ((RenderUtil.getCameraX() >= minX && RenderUtil.getCameraX() <= maxX)
				&& (RenderUtil.getCameraY() >= minY && RenderUtil.getCameraY() <= maxY)
				&& (RenderUtil.getCameraZ() >= minZ && RenderUtil.getCameraZ() <= maxZ)) {
			return;
		}
		vboBuffer.getByteBuffer().putFloat(objCount * 28, (float) (minX - RenderUtil.getCameraEntityX()));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 4, (float) (minY - RenderUtil.getCameraEntityY()));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 8, (float) (minZ - RenderUtil.getCameraEntityZ()));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 12, (float) (maxX - minX));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 16, (float) (maxY - minY));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 20, (float) (maxZ - minZ));
		vboBuffer.getByteBuffer().putInt(objCount * 28 + 24, objCount);
		cullInfo.setLastTimeUpdated(frame);
		cullInfo.setId(objCount);
		objCount++;
	}

	public void updateResults(Matrix4f projViewMat) {
		frame++;

		GLShader.push();
		shader.use();
		GLUtil.setMatrix(uniform_projViewMat, projViewMat);
		GL20.glUniform1i(uniform_frame, frame);
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, ssboBuffer.getBuffer());

		setupRenderState();

		// render
		GL30.glBindVertexArray(vao);
		GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0, objCount);
		GL42.glMemoryBarrier(GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);
		fence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
		GL30.glBindVertexArray(0);

		clearRenderState();

		GLShader.pop();

		objCount = 0;
	}

	private void setupRenderState() {
		GlStateManager.disableBlend();

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.depthMask(false);

		GlStateManager.enableCull();
		GlStateManager.cullFace(CullFace.BACK);

		GlStateManager.colorMask(false, false, false, false);
	}

	private void clearRenderState() {
		GlStateManager.enableBlend();

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.depthMask(true);

		GlStateManager.disableCull();
		GlStateManager.cullFace(CullFace.BACK);

		GlStateManager.colorMask(true, true, true, true);
	}

}
