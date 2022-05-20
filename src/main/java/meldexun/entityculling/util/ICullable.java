package meldexun.entityculling.util;

public interface ICullable {

	// ----- opengl culling ----- //

	CullInfo getCullInfo();

	CullInfo getShadowCullInfo();

	class CullInfo {

		private int lastTimeUpdated = Integer.MIN_VALUE;
		private int id;

		public int getLastTimeUpdated() {
			return lastTimeUpdated;
		}

		public void setLastTimeUpdated(int lastTimeUpdated) {
			this.lastTimeUpdated = lastTimeUpdated;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

	}

	// ----- raytraced culling ----- //

	boolean isCulled();

	void setCulled(boolean culled);

	boolean isShadowCulled();

	void setShadowCulled(boolean shadowCulled);

	boolean canBeOcclusionCulled();

	void setCanBeOcclusionCulled(boolean canBeOcclusionCulled);

}
