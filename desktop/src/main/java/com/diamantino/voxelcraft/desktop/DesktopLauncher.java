package com.diamantino.voxelcraft.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.diamantino.voxelcraft.launchers.VoxelCraftClient;

public class DesktopLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static void createApplication() {
        new Lwjgl3Application(new VoxelCraftClient(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        //configuration.setOpenGLEmulation();
        configuration.setTitle("VoxelCraft");
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(1920, 1080);
        configuration.setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png");
        return configuration;
    }
}
