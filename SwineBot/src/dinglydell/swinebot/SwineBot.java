package dinglydell.swinebot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import dinglydell.swinebot.event.SwineEventHandler;

public class SwineBot extends JavaPlugin {

	public static List<Bot> npcs;

	public void onEnable() {
		super.onEnable();
		npcs = new ArrayList<Bot>();
		getServer().getPluginManager().registerEvents(new SwineEventHandler(),
				this);
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				ArrayList<Bot> botsCopy = new ArrayList<Bot>(npcs);
				for (Bot b : botsCopy) {
					b.tick();
				}
			}
		}, 0L, 1L);
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			SwineEventHandler.injectPlayer(p);
		}
	}

	public void onDisable() {
		super.onDisable();
		clearBots();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			SwineEventHandler.removePlayer(p);
		}
	}

	public static Bot createNPC(Location location, String npcName) {
		Bot bot = new Bot(location, npcName);
		npcs.add(bot);
		return bot;

	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (command.getName().equalsIgnoreCase("swine")) {
				if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
					createNPC(player.getLocation(), args[1]);
					sender.sendMessage("What a swine!");
					return true;
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("clear")) {

						clearBots();
						sender.sendMessage("Swines cleared.");
						return true;
					} else if (args[0].equalsIgnoreCase("start")) {
						startBots();
						sender.sendMessage("The game begins");
						return true;
					}
				}

			}

		}
		return false;
	}

	public static void startBots() {
		for (Bot b : npcs) {
			b.start();
		}

	}

	public static void clearBots() {
		for (Bot b : npcs) {
			b.leave();
		}
		npcs.clear();

	}

}
