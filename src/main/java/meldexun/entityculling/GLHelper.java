package meldexun.entityculling;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLContext;

public class GLHelper {

	private static boolean openGl45;
	private static boolean openGl43;
	private static boolean openGl33;

	public static void init() {
		ContextCapabilities context = GLContext.getCapabilities();
		openGl45 = context.OpenGL45;
		openGl43 = context.OpenGL43;
		openGl33 = context.OpenGL33;
	}

	public static int createQuery() {
		if (openGl45) {
			return GL45.glCreateQueries(getQueryTarget());
		} else {
			return GL15.glGenQueries();
		}
	}

	public static void beginQuery(int query) {
		GL15.glBeginQuery(getQueryTarget(), query);
	}

	public static void endQuery() {
		GL15.glEndQuery(getQueryTarget());
	}

	private static int getQueryTarget() {
		if (openGl43) {
			return GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE;
		} else if (openGl33) {
			return GL33.GL_ANY_SAMPLES_PASSED;
		} else {
			return GL15.GL_SAMPLES_PASSED;
		}
	}

}
