package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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
import meldexun.entityculling.asm.EntityCullingClassTransformer;
import meldexun.entityculling.config.EntityCullingConfig;
import meldexun.entityculling.opengl.Buffer;
import meldexun.entityculling.opengl.ShaderBuilder;
import meldexun.entityculling.util.CameraUtil;
import meldexun.entityculling.util.ICullable;
import meldexun.entityculling.util.ResourceSupplier;
import meldexun.entityculling.util.matrix.Matrix4f;
import meldexun.reflectionutil.ReflectionMethod;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class CullingInstance {

	private static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	private static final int MAX_OBJ_COUNT = 1 << 16;
	private static CullingInstance instance;

	private final int shader;
	private final int uniform_projViewMat;
	private final int uniform_frame;
	private final FloatBuffer matrixBuffer = GLAllocation.createDirectFloatBuffer(16);

	private final int cubeVertexBuffer;
	private final int cubeIndexBuffer;
	private final Buffer vboBuffer;
	private final int vao;
	private final Buffer ssboBuffer;

	private int objCount;
	private int frame;

	private GLSync fence;

	private CullingInstance() {
		shader = new ShaderBuilder()
				.addShader(GL20.GL_VERTEX_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/vert.glsl")))
				.addShader(GL20.GL_FRAGMENT_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/frag.glsl")))
				.build();
		uniform_projViewMat = GL20.glGetUniformLocation(shader, "projectionViewMatrix");
		uniform_frame = GL20.glGetUniformLocation(shader, "frame");

		vboBuffer = new Buffer(MAX_OBJ_COUNT * 7 * 4, GL30.GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT, GL15.GL_DYNAMIC_DRAW);
		ssboBuffer = new Buffer(MAX_OBJ_COUNT * 4, GL30.GL_MAP_READ_BIT | GL44.GL_MAP_PERSISTENT_BIT, GL15.GL_DYNAMIC_READ);

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
		Vec3d pos = CameraUtil.getCamera();
		if (pos.x >= minX && pos.x <= maxX && pos.y >= minY && pos.y <= maxY && pos.z >= minZ && pos.z <= maxZ) {
			return;
		}
		vboBuffer.getByteBuffer().putFloat(objCount * 28, (float) minX);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 4, (float) minY);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 8, (float) minZ);
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 12, (float) (maxX - minX));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 16, (float) (maxY - minY));
		vboBuffer.getByteBuffer().putFloat(objCount * 28 + 20, (float) (maxZ - minZ));
		vboBuffer.getByteBuffer().putInt(objCount * 28 + 24, objCount);
		obj.culling_setLastTimeUpdated(frame);
		obj.culling_setId(objCount);
		objCount++;
	}

	public void updateResults(Matrix4f projViewMat) {
		frame++;

		int prevShaderProgram = EntityCullingClassTransformer.OPTIFINE_DETECTED && IS_SHADERS.invoke(null) ? GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) : 0;
		GL20.glUseProgram(shader);
		projViewMat.store(matrixBuffer);
		GL20.glUniformMatrix4(uniform_projViewMat, false, matrixBuffer);
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

		GL20.glUseProgram(prevShaderProgram);

		objCount = 0;
	}

	private void setupRenderState() {
		/*if (!EntityCullingConfig.debugRenderBoxes) {
			GlStateManager.colorMask(false, false, false, false);
		} else {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		}*/
		GlStateManager.colorMask(false, false, false, false);
		GlStateManager.disableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.disableLight(0);
		GlStateManager.disableLight(1);
		GlStateManager.disableColorMaterial();
		GlStateManager.depthMask(false);
		GlStateManager.disableFog();
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	}

	private void clearRenderState() {
		/*if (!EntityCullingConfig.debugRenderBoxes) {
			GlStateManager.colorMask(true, true, true, true);
		} else {
			GlStateManager.disableBlend();
		}*/
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.enableLight(0);
		GlStateManager.enableLight(1);
		GlStateManager.enableColorMaterial();
		GlStateManager.depthMask(true);
		GlStateManager.enableFog();
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	}

}
