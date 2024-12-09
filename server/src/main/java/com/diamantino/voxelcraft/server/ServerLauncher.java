package com.diamantino.voxelcraft.server;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

/** Launches the server application. */
public class ServerLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static void createApplication() {
        new HeadlessApplication(new VoxelCraftServer(), getDefaultConfiguration());
    }

    private static HeadlessApplicationConfiguration getDefaultConfiguration() {
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();

        configuration.updatesPerSecond = 20;

        return configuration;
    }
}
