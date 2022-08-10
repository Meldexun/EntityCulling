package meldexun.entityculling.util;

public interface ICullable {

	// ----- opengl culling ----- //

	CullInfo getCullInfo();

	CullInfo getShadowCullInfo();

	class CullInfo {

		private int lastTimeUpdated = Integer.MIN_VALUE;
		private int prevLastTimeUpdated = Integer.MIN_VALUE;
		private int id;
		private int prevId;

		public boolean wasLastTimeUpdated(int frame) {
			return lastTimeUpdated == frame - 1 || prevLastTimeUpdated == frame - 1;
		}

		public int getLastTimeUpdated() {
			return lastTimeUpdated;
		}

		public void setLastTimeUpdated(int lastTimeUpdated) {
			this.prevLastTimeUpdated = this.lastTimeUpdated;
			this.lastTimeUpdated = lastTimeUpdated;
		}

		public int getId(int frame) {
			if (lastTimeUpdated == frame - 1)
				return id;
			if (prevLastTimeUpdated == frame - 1)
				return prevId;
			return -1;
		}

		public void setId(int id) {
			this.prevId = this.id;
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
