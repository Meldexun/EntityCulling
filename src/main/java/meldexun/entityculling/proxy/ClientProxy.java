package meldexun.entityculling.proxy;

import meldexun.entityculling.ClientEventHandler;

public class ClientProxy implements IProxy {

	@Override
	public void preInit() {

	}

	@Override
	public void init() {
		updateConfig();
	}

	@Override
	public void postInit() {

	}

	public static void updateConfig() {
		ClientEventHandler.updateBlacklist();
	}

}
