package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.tasks.ClearWeather;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class OnPlayerBedLeave implements Listener {
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	private Main plugin;
	private Config config;
	
	public OnPlayerBedLeave(Main plugin, Config config) {
		this.plugin = plugin;
		this.config = config;
	}
	
	@EventHandler
	public void onPlayerBedLeave (PlayerBedLeaveEvent event) {
		Boolean messageFromSleepingIgnored = config.config.getBoolean("messageFromSleepingIgnored", true);
		if(messageFromSleepingIgnored && event.getPlayer().isSleepingIgnored()) return;
		if(event.getPlayer().hasPermission("sleep.ignore")) return;
		Boolean messageOtherWorlds = config.config.getBoolean("messageOtherWorlds", false);
		Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions", false);

		//remove player from sleep lookup table
		World myWorld = event.getPlayer().getWorld();
		String myWorldName = dims.matcher(myWorld.getName()).replaceAll("");
		if(		this.plugin.sleepingPlayers.containsKey(myWorld) &&
				this.plugin.sleepingPlayers.get(myWorld).contains(event.getPlayer())){
			this.plugin.sleepingPlayers.get(myWorld).remove(event.getPlayer());
			if(this.plugin.sleepingPlayers.get(myWorld).size() == 0) this.plugin.sleepingPlayers.remove(myWorld);
		}

		Long numSleepingPlayers = Long.valueOf(0);
		for (Map.Entry<World, HashSet<Player>> entry : this.plugin.sleepingPlayers.entrySet()) {
			World key = entry.getKey();
			String theirWorldName = dims.matcher(key.getName()).replaceAll("");
			if( !messageOtherWorlds && !myWorldName.equals(theirWorldName)) continue;
			if( !messageOtherDimensions && !event.getPlayer().getWorld().getEnvironment().equals( key.getEnvironment() ) ) continue;
			numSleepingPlayers = numSleepingPlayers + this.plugin.sleepingPlayers.get(key).size();
		}
		if(numSleepingPlayers == 0) {
			for (World w : Bukkit.getWorlds()) {
				String theirWorldName = dims.matcher(w.getName()).replaceAll("");
				if( !messageOtherWorlds && !myWorldName.equals(theirWorldName)) continue;
				if( !messageOtherDimensions && !event.getPlayer().getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;
				if( this.plugin.doSleep.containsKey(w)) {
					this.plugin.doSleep.get(w).cancel();
				}
			}
		}
		else if( event.getPlayer().getWorld().getTime() >= 23460) {
			for (World w : Bukkit.getWorlds()) {
				String theirWorldName = dims.matcher(w.getName()).replaceAll("");
				if( !messageOtherWorlds && !myWorldName.equals(theirWorldName)) continue;
				if( !messageOtherDimensions && !event.getPlayer().getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;
				if(w.getTime()!= myWorld.getTime()) w.setTime(myWorld.getTime());
				this.plugin.doSleep.get(w).cancel();
				this.plugin.clearWeather.get(w).cancel();
				this.plugin.clearWeather.remove(w);
				this.plugin.clearWeather.put(w, new ClearWeather(w).runTask(this.plugin));
				if(this.config.config.getBoolean("resetAllStatistics")) {
					for (Player p : w.getPlayers()) {
						if(p.hasPermission("sleep.ignore")) continue;
						p.setStatistic(Statistic.TIME_SINCE_REST, 0);
					}
				}
			}
		}
	}
}
