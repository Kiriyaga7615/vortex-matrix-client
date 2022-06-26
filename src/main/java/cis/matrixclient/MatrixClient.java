package cis.matrixclient;

import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ConfigManager;
import cis.matrixclient.feature.manager.HoleManager;
import cis.matrixclient.feature.manager.SettingManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.util.player.DamageUtils;
import cis.matrixclient.util.player.Rotations;
import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixClient implements ModInitializer {
	public static MatrixClient INSTANCE;
	public static final Logger LOGGER = LoggerFactory.getLogger("matrix");
	public static final String MOD_ID = "matrix";

	public static MinecraftClient mc = MinecraftClient.getInstance();
	public static EventBus EVENT_BUS = new EventBus();


	@Override
	public void onInitialize() {
		if (INSTANCE == null) {
			INSTANCE = this;
			return;
		}
		LOGGER.info("Welcome to MatrixClient!");

		SettingManager.init();
		ModuleManager.init();
		CommandManager.init();
		HoleManager.init();

		INIT();

		ConfigManager.load();
		Runtime.getRuntime().addShutdownHook(new ConfigManager());
	}

	public void INIT(){
		DamageUtils.init();
		Rotations.init();
	}
}
