package me.dhrubo.oneplayersleep;

import me.dhrubo.oneplayersleep.commands.*;
import me.dhrubo.oneplayersleep.events.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import me.dhrubo.oneplayersleep.placeholderapiworks.Metrics;
import me.dhrubo.oneplayersleep.placeholderapiworks.PAPIExpansion;
import me.dhrubo.oneplayersleep.display.Message;

import java.util.*;

public final class Main extends JavaPlugin implements Listener {
	private Config config;

	public Map<World,BukkitTask> doSleep;
	public Map<World,BukkitTask> clearWeather;
	public Map<Player, Message> wakeData; //list of players receiving wakeup option
	public Map<World, HashSet<Player>> sleepingPlayers; //list of sleeping players for each world
	public Map<World,Long> numPlayers;

	@Override
	public void onEnable() {
		this.config = new Config(this);
		this.numPlayers = new HashMap<>();
		this.doSleep = new HashMap<>();
		this.clearWeather = new HashMap<>();
		this.wakeData = new HashMap<>();
		this.sleepingPlayers = new HashMap<>();

		//make /sleep work
		Objects.requireNonNull(getCommand("sleep")).setExecutor(new Sleep(this));

		//let players tab for available subcommands
		Objects.requireNonNull(getCommand("sleep")).setTabCompleter(new TabComplete(this.config));

		//set executors so i can look them up from /sleep
		Objects.requireNonNull(getCommand("sleep help")).setExecutor(new Help());
		Objects.requireNonNull(getCommand("sleep reload")).setExecutor(new Reload(this, this.config));
		Objects.requireNonNull(getCommand("sleep test")).setExecutor(new Test(this));
		Objects.requireNonNull(getCommand("sleep wakeup")).setExecutor(new Wakeup(this, this.config));

		//other way to call wakeup, so sleep.wakeup doesn't depend on sleep.see
		Objects.requireNonNull(getCommand("sleepwakeup")).setExecutor(new Wakeup(this, this.config));


		//load config files and check if they're up to date
		// 	also check for hooks
		this.config.refreshConfigs();

		//if this plugin got reloaded, redo bed enter Main.events
		for( Player p : Bukkit.getOnlinePlayers() ) {
			if(p.isSleeping()) {
				getServer().getPluginManager().callEvent(new PlayerBedEnterEvent(p, p.getWorld().getBlockAt(p.getBedLocation()), BedEnterResult.OK));
			}
		}
		
		//register all the spigot Main.events I need
		getServer().getPluginManager().registerEvents(new OnBedExplode(this, config), this);
		getServer().getPluginManager().registerEvents(new OnPlayerBedSleep(this, config), this);
		getServer().getPluginManager().registerEvents(new OnPlayerBedLeave(this, config), this);
		getServer().getPluginManager().registerEvents(new OnPlayerJoin(this, config), this);
		getServer().getPluginManager().registerEvents(new OnPlayerQuit(this, config), this);
		getServer().getPluginManager().registerEvents(new OnPlayerChangeWorld(this, config), this);
		getServer().getPluginManager().registerEvents(new OnWeatherChange(config), this);
		getServer().getPluginManager().registerEvents(this, this);
		
		if(this.config.hasPAPI()) {
			new PAPIExpansion(this).register();
		}

		int pluginId = 7096; // <-- Replace with the id of your plugin!
		new Metrics(this, pluginId);

		//fix player counts on reload
		boolean messageFromSleepingIgnored = this.config.config.getBoolean("messageFromSleepingIgnored", true);
		for (org.bukkit.World w : Bukkit.getWorlds()) {
			this.numPlayers.put(w,Long.valueOf(0));
			for (Player p : w.getPlayers()){
				if(p.hasPermission("sleep.ignore")) continue;
				this.numPlayers.put(w, this.numPlayers.get(w)+1);
				if(messageFromSleepingIgnored && p.isSleepingIgnored()) continue;
				if(p.isSleeping()){
					if(!this.sleepingPlayers.containsKey(w)){
						this.sleepingPlayers.put(w, new HashSet<>());
					}
					this.sleepingPlayers.get(w).add(p);
				}
			}
			if(numPlayers.get(w) == 0) numPlayers.remove(w);
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public Config getPluginConfig() {
		return this.config;
	}
}
