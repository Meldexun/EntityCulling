package meldexun.entityculling.util;

public interface ICullable {

	int culling_getLastTimeUpdated();

	void culling_setLastTimeUpdated(int lastTimeUpdated);

	int culling_getId();

	void culling_setId(int id);

	boolean isCulled();

	void setCulled(boolean culled);

	boolean isShadowCulled();

	void setShadowCulled(boolean shadowCulled);

}
