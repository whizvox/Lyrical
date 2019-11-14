package me.whizvox.lyrical.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import me.whizvox.lyrical.Lyrical;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Lyrical");
		config.setWindowedMode(800, 450);
		new Lwjgl3Application(new Lyrical(), config);
	}
}
