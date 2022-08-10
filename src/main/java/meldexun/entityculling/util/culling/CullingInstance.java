package meldexun.entityculling.util.culling;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;

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
	private static final String A_POS = "a_Pos";
	private static final String A_OFFSET = "a_Offset";
	private static final String A_SIZE = "a_Size";
	private static final String A_OBJID = "a_ObjID";
	private static final String U_MATRIX = "u_ModelViewProjectionMatrix";
	private static CullingInstance instance;
	private static CullingInstance shadow_instance;

	private final GLShader shader;

	public final int cubeVertexBuffer;
	public final int cubeIndexBuffer;
	private final BBBuffer vboBuffer;
	private final int vao;
	private GLBuffer ssboBuffer;

	private int objCount;
	private int frame;

	public CullingInstance() {
		shader = new GLShader.Builder()
				.addShader(GL20.GL_VERTEX_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/vert.glsl")))
				.addShader(GL20.GL_FRAGMENT_SHADER, new ResourceSupplier(new ResourceLocation(EntityCulling.MOD_ID, "shaders/frag.glsl")))
				.build();

		vboBuffer = new BBBuffer(MAX_OBJ_COUNT * 7 * 4, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, true, GL30.GL_MAP_WRITE_BIT);

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
		GL20.glVertexAttribPointer(shader.getAttribute(A_POS), 3, GL11.GL_BYTE, false, 0, 0);
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_POS));
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboBuffer.getBuffer());
		GL20.glVertexAttribPointer(shader.getAttribute(A_OFFSET), 3, GL11.GL_FLOAT, false, 28, 0);
		GL20.glVertexAttribPointer(shader.getAttribute(A_SIZE), 3, GL11.GL_FLOAT, false, 28, 12);
		GL30.glVertexAttribIPointer(shader.getAttribute(A_OBJID), 1, GL11.GL_INT, 28, 24);
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_OFFSET));
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_SIZE));
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_OBJID));
		GL33.glVertexAttribDivisor(shader.getAttribute(A_OFFSET), 1);
		GL33.glVertexAttribDivisor(shader.getAttribute(A_SIZE), 1);
		GL33.glVertexAttribDivisor(shader.getAttribute(A_OBJID), 1);
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

	public boolean isVisible(CullInfo cullInfo) {
		if (!cullInfo.wasLastTimeUpdated(frame)) {
			return true;
		}
		if (ssboBuffer == null) {
			return false;
		}
		ssboBuffer.map(GL30.GL_MAP_READ_BIT, GL15.GL_READ_ONLY);
		return ssboBuffer.getByteBuffer().getInt(cullInfo.getId(frame) * 4) == 1;
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
		vboBuffer.put(
				(float) (minX - RenderUtil.getCameraEntityX()),
				(float) (minY - RenderUtil.getCameraEntityY()),
				(float) (minZ - RenderUtil.getCameraEntityZ()),
				(float) (maxX - minX),
				(float) (maxY - minY),
				(float) (maxZ - minZ),
				objCount);
		cullInfo.setLastTimeUpdated(frame);
		cullInfo.setId(objCount);
		objCount++;
	}

	public void updateResults(Matrix4f projViewMat) {
		if (ssboBuffer != null) {
			ssboBuffer.dispose();
			ssboBuffer = null;
		}

		frame++;

		if (objCount > 0) {
			ssboBuffer = new GLBuffer(objCount * 4, GL30.GL_MAP_READ_BIT, GL15.GL_STREAM_READ);
			GLHelper.clearBufferData(ssboBuffer.getBuffer(), 0);

			GLShader.push();
			shader.use();
			GLUtil.setMatrix(shader.getUniform(U_MATRIX), projViewMat);
			GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, ssboBuffer.getBuffer());

			setupRenderState();

			// render
			GL30.glBindVertexArray(vao);
			GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, 14, GL11.GL_UNSIGNED_BYTE, 0, objCount);
			GL30.glBindVertexArray(0);

			clearRenderState();

			GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, 0);
			GLShader.pop();

			objCount = 0;
		}
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
