package me.dhrubo.oneplayersleep.tasks;

import me.dhrubo.oneplayersleep.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import me.dhrubo.oneplayersleep.placeholderapiworks.LocalPlaceholders;
import me.dhrubo.oneplayersleep.display.Message;

//set up message threads for all relevant players
public class AnnounceWakeup extends BukkitRunnable{
	private Main plugin;
	private Config config;
	private Player player;
	private Message msg;
	private World world;

	public AnnounceWakeup(Main plugin, Config config, Player player, Message msg) {
		this.plugin = plugin;
		this.config = config;
		this.player = player;
		this.msg = msg;
		this.world = null;
	}

	public AnnounceWakeup(Main plugin, Config config, Player player, Message msg, World world) {
		this.plugin = plugin;
		this.config = config;
		this.player = player;
		this.msg = msg;
		this.world = world;
	}
	
	@Override
	public void run() {
		Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions");
		Boolean messageOtherWorlds = config.config.getBoolean("messageOtherWorlds");
		Boolean messageToSleepingIgnored = config.config.getBoolean("messageToSleepingIgnored");
		World myWorld = this.player.getWorld();

		//if no world defined, generate task for each world
		if(this.world == null) {
			for (World w : Bukkit.getWorlds()) {
				if( !messageOtherDimensions && !myWorld.getEnvironment().equals( w.getEnvironment() ) ) continue;
				if( !messageOtherWorlds && !myWorld.getEnvironment().equals(w.getEnvironment())) continue;
				new AnnounceWakeup(this.plugin,this.config,this.player,msg, w).runTaskAsynchronously(this.plugin);
			}
			return;
		}

		//if we've gotten this far, format and ship it
		for (Player p : this.world.getPlayers()) {
			if(!messageToSleepingIgnored && p.isSleepingIgnored()) continue;
			if(p.hasPermission("sleep.ignore")) continue;
			String wakeupMsg = LocalPlaceholders.fillPlaceHolders(
					this.msg.wakeup,
					p,
					this.config);
			if(this.config.hasPAPI()) wakeupMsg = PlaceholderAPI.setPlaceholders(p, wakeupMsg);
			p.sendMessage(wakeupMsg);
		}
	}
}
