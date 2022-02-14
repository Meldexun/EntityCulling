package meldexun.entityculling.util;

public interface ICullable {

	boolean isCulled();

	void setCulled(boolean culled);

	boolean isShadowCulled();

	void setShadowCulled(boolean shadowCulled);

}
