package meldexun.entityculling.util.culling;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLSync;

import meldexun.entityculling.EntityCulling;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.opengl.Buffer;
import meldexun.entityculling.opengl.ShaderBuilder;
import meldexun.entityculling.util.CameraUtil;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.ResourceSupplier;
import meldexun.entityculling.util.matrix.Matrix4f;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class CullingInstance {

	private static final int MAX_OBJ_COUNT = 1 << 16;
	private static CullingInstance instance;

	private final int shader;
	private final int uniform_projViewMat;
	private final int uniform_frame;
	private final FloatBuffer matrixBuffer = GLAllocation.createDirectFloatBuffer(16);

	private final Buffer vboBuffer;
	private final int vao;
	private final Buffer ssboBuffer;

	private int objCount;
	private int frame;

	private GLSync fence;

	private CullingInstance() {
		shader = new ShaderBuilder().addShader(GL20.GL_VERTEX_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/vert.glsl")))
				.addShader(GL32.GL_GEOMETRY_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/geo.glsl")))
				.addShader(GL20.GL_FRAGMENT_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/frag.glsl"))).build();
		uniform_projViewMat = GL20.glGetUniformLocation(shader, "projectionViewMatrix");
		uniform_frame = GL20.glGetUniformLocation(shader, "frame");

		vboBuffer = new Buffer(MAX_OBJ_COUNT * 7 * 4, GL30.GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT, GL15.GL_DYNAMIC_DRAW);
		ssboBuffer = new Buffer(MAX_OBJ_COUNT * 4, GL30.GL_MAP_READ_BIT | GL44.GL_MAP_PERSISTENT_BIT, GL15.GL_DYNAMIC_READ);

		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboBuffer.getBuffer());
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 28, 0);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 28, 12);
		GL30.glVertexAttribIPointer(2, 1, GL11.GL_INT, 28, 24);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	public static CullingInstance getInstance() {
		if (instance == null) {
			instance = new CullingInstance();
		}
		return instance;
	}

	private void sync() {
		if (fence != null) {
			GL32.glClientWaitSync(fence, 0, 1_000_000_000);
			fence = null;
		}
	}

	public boolean isVisible(ICullable cullable) {
		if (cullable.culling_getLastTimeUpdated() < frame - 1) {
			return true;
		}
		sync();
		return ssboBuffer.getByteBuffer().getInt(cullable.culling_getId() * 4) == frame;
	}

	public void addBox(ICullable obj, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (obj.culling_getLastTimeUpdated() == frame) {
			return;
		}
		Vec3d pos = CameraUtil.getCamera(frame);
		if (pos.x >= minX && pos.x <= maxX && pos.y >= minY && pos.y <= maxY && pos.z >= minZ && pos.z <= maxZ) {
			return;
		}
		vboBuffer.getByteBuffer().putFloat(objCount * 28, (float) minX);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 4, (float) minY);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 8, (float) minZ);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 12, (float) maxX);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 16, (float) maxY);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 20, (float) maxZ);
		vboBuffer.getByteBuffer().putInt(objCount * 28 + 24, objCount);
		obj.culling_setLastTimeUpdated(frame);
		obj.culling_setId(objCount);
		objCount++;
	}

	public void updateResults(Matrix4f projViewMat) {
		frame++;

		int prevShaderProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GL20.glUseProgram(shader);
		projViewMat.store(matrixBuffer);
		GL20.glUniformMatrix4(uniform_projViewMat, false, matrixBuffer);
		GL20.glUniform1i(uniform_frame, frame);
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, ssboBuffer.getBuffer());

		setupRenderState();

		// render
		GL30.glBindVertexArray(vao);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, objCount);
		GL42.glMemoryBarrier(GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);
		fence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
		GL30.glBindVertexArray(0);

		clearRenderState();

		GL20.glUseProgram(prevShaderProgram);

		objCount = 0;
	}

	private void setupRenderState() {
		if (!EntityCullingConfig.debugRenderBoxes) {
			GlStateManager.colorMask(false, false, false, false);
		}
		GlStateManager.depthMask(false);
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.disableCull();
	}

	private void clearRenderState() {
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.depthMask(true);
	}

}