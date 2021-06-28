package me.dhrubo.oneplayersleep.tasks;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class OnSleepChecks extends BukkitRunnable{
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	private Main plugin;
	private Config config;
	private Player player;
	private Boolean bypassSleep = false; //flag for testing messages
	
	public OnSleepChecks(Main plugin, Config config, Player player) {
		this.plugin = plugin;
		this.config = config;
		this.player = player;

	}

	public OnSleepChecks(Main plugin, Config config, Player player, Boolean bypassSleep) {
		this.plugin = plugin;
		this.config = config;
		this.player = player;
		this.bypassSleep = bypassSleep;
	}

	@Override
	public void run() {
		//if player isn't sleeping anymore when the server gets here, pull out
		Bukkit.getLogger().log(Level.INFO, this.bypassSleep.toString());
		if( !(this.player.isSleeping())  && !this.bypassSleep) {
			this.cancel();
			return;
		}

		World myWorld = this.player.getWorld();

		//add player to list of sleeping players
		this.plugin.sleepingPlayers.putIfAbsent(myWorld,new HashSet<Player>());
		this.plugin.sleepingPlayers.get(myWorld).add(this.player);
		Bukkit.getLogger().log(Level.INFO, ((Integer)this.plugin.sleepingPlayers.get(myWorld).size()).toString());


		Boolean messageOtherWorlds = config.config.getBoolean("messageOtherWorlds");
		Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions");

		String myWorldName = dims.matcher(this.player.getWorld().getName()).replaceAll("");

		int numPlayers = 0;
		int numSleepingPlayers = 0;
		//check for valid players
		for (World w : Bukkit.getWorlds()){
			String theirWorldName = dims.matcher(w.getName()).replaceAll("");
			if( !messageOtherWorlds && !myWorldName.equals(theirWorldName) ) continue;
			if( !messageOtherDimensions && !this.player.getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;
			if( this.plugin.sleepingPlayers.containsKey(w) ) numSleepingPlayers += this.plugin.sleepingPlayers.get(w).size();
			numPlayers += w.getPlayers().size();
			if(numPlayers > 1) break;
		}

		if(this.bypassSleep) {
			return;
		}

		//only announce the first bed entry, and only when there's more than one player to see it
		//skip if called by a test function
		if(numSleepingPlayers < 2 && numPlayers > 1) {
			//async message selection and delivery
			new AnnounceSleep(this.plugin, this.config, this.player).runTaskAsynchronously(this.plugin);
			
			//start sleep task
			if(plugin.doSleep.containsKey(this.player.getWorld())) plugin.doSleep.remove(this.player.getWorld());
			plugin.doSleep.put(this.player.getWorld(), new PassTime(this.plugin, this.config, this.player.getWorld())
					.runTaskLater(this.plugin, config.config.getInt("sleepDelay")));
		}
		
		//set up clear weather task for later
		if(!plugin.clearWeather.containsKey(this.player.getWorld())) {
			//calculate how long to wait before clearing weather
			Long dT = (this.config.config.getLong("stopTime") - this.player.getWorld().getTime()) / this.config.config.getLong("increment");
			Long cap = (this.player.getWorld().getTime() - this.config.config.getLong("startTime")) / this.config.config.getLong("increment");
			
			//calculate how long to clear weather for
			Double randomFactor = new Random().nextDouble()*168000;
			Long duration = 12000 + randomFactor.longValue();
			
			//start task
			if(dT>1 && cap>=0) plugin.clearWeather.put(this.player.getWorld(), new ClearWeather(this.player.getWorld(),duration).runTaskLater(this.plugin, dT+this.config.config.getLong("sleepDelay")));
			else plugin.clearWeather.put(this.player.getWorld(), new ClearWeather(this.player.getWorld(),duration).runTaskLater(this.plugin, 2*this.config.config.getLong("sleepDelay")));
		}
	}

}
