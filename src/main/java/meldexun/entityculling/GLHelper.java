package meldexun.entityculling;

import static org.lwjgl.opengl.GL46C.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class GLHelper {

	private static boolean openGl45;
	private static boolean openGl43;
	private static boolean openGl33;

	public static void init() {
		GLCapabilities context = GL.getCapabilities();
		openGl45 = context.OpenGL45;
		openGl43 = context.OpenGL43;
		openGl33 = context.OpenGL33;
	}

	public static int createQuery() {
		if (openGl45) {
			return glCreateQueries(getQueryTarget());
		} else {
			return glGenQueries();
		}
	}

	public static void beginQuery(int query) {
		glBeginQuery(getQueryTarget(), query);
	}

	public static void endQuery() {
		glEndQuery(getQueryTarget());
	}

	private static int getQueryTarget() {
		if (openGl43) {
			return GL_ANY_SAMPLES_PASSED_CONSERVATIVE;
		} else if (openGl33) {
			return GL_ANY_SAMPLES_PASSED;
		} else {
			return GL_SAMPLES_PASSED;
		}
	}

}
