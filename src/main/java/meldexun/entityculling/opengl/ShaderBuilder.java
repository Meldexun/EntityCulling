package meldexun.entityculling.opengl;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class ShaderBuilder {

	private final Int2ObjectMap<Supplier<String>> shaderMap = new Int2ObjectOpenHashMap<>();

	public ShaderBuilder addShader(int type, Supplier<String> source) {
		shaderMap.put(type, source);
		return this;
	}

	public int build() {
		int program = GL20.glCreateProgram();

		IntList shaderList = new IntArrayList();
		for (Int2ObjectMap.Entry<Supplier<String>> entry : shaderMap.int2ObjectEntrySet()) {
			int shader = GL20.glCreateShader(entry.getIntKey());
			GL20.glShaderSource(shader, entry.getValue().get());
			GL20.glCompileShader(shader);

			int compileStatus = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
			if (compileStatus != GL11.GL_TRUE) {
				int logLength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
				String shaderInfoLog = GL20.glGetShaderInfoLog(shader, logLength);
				throw new RuntimeException(String.format("Failed to compile shader: %d%n%s", compileStatus, shaderInfoLog));
			}

			shaderList.add(shader);
		}

		shaderList.forEach(shader -> GL20.glAttachShader(program, shader));
		GL20.glLinkProgram(program);

		int linkStatus = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
		if (linkStatus != GL11.GL_TRUE) {
			int logLength = GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH);
			String programInfoLog = GL20.glGetProgramInfoLog(program, logLength);
			throw new RuntimeException(String.format("Failed to link program: %d%n%s", linkStatus, programInfoLog));
		}

		shaderList.forEach(GL20::glDeleteShader);

		return program;
	}

}
