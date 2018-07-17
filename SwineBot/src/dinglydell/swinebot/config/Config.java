package dinglydell.swinebot.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import dinglydell.swinebot.SwineBot;

public class Config {
	public static boolean deathMessages;
	public static boolean scatterOnDeath;
	private static FileConfiguration namesConfig;
	private static FileConfiguration generalConfig;

	public static List<String> getNames() {
		return namesConfig.getStringList("botNames");
	}

	public static void initConfig(SwineBot plugin) {
		File namesConfigFile = new File(plugin.getDataFolder(), "names.yml");
		if (!namesConfigFile.exists()) {
			namesConfigFile.getParentFile().mkdirs();
			plugin.saveResource("names.yml", false);
		}
		namesConfig = new YamlConfiguration();
		File generalConfigFile = new File(plugin.getDataFolder(), "config.yml");
		if (!generalConfigFile.exists()) {
			generalConfigFile.getParentFile().mkdirs();
			plugin.saveResource("config.yml", false);
		}

		generalConfig = new YamlConfiguration();

		try {
			namesConfig.load(namesConfigFile);
			generalConfig.load(generalConfigFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		deathMessages = generalConfig.getBoolean("botDeathMessages");

		scatterOnDeath = generalConfig.getBoolean("scatterOnDeath");

	}
}
