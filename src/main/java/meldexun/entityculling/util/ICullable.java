package meldexun.entityculling.util;

public interface ICullable {

	boolean isCulled();

	void setCulled(boolean isCulled);

	boolean isShadowCulled();

	void setShadowCulled(boolean isShadowCulled);

}
