package meldexun.entityculling;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;

public class GLHelper {

	private static boolean openGl43;
	private static boolean openGl33;

	public static void init() {
		ContextCapabilities context = GLContext.getCapabilities();
		openGl43 = context.OpenGL43;
		openGl33 = context.OpenGL33;
	}

	public static void beginQuery(int query) {
		if (openGl43) {
			GL15.glBeginQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE, query);
		} else if (openGl33) {
			GL15.glBeginQuery(GL33.GL_ANY_SAMPLES_PASSED, query);
		} else {
			GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, query);
		}
	}

	public static void endQuery() {
		if (openGl43) {
			GL15.glEndQuery(GL43.GL_ANY_SAMPLES_PASSED_CONSERVATIVE);
		} else if (openGl33) {
			GL15.glEndQuery(GL33.GL_ANY_SAMPLES_PASSED);
		} else {
			GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
		}
	}

}
