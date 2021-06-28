package me.dhrubo.oneplayersleep.tasks;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.regex.Pattern;

public class PassTime extends BukkitRunnable{
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	private Main plugin;
	private Config config;
	private World world;
	private Boolean didNightPass;

	public PassTime(Main plugin, Config config, World world) {
		this.plugin = plugin;
		this.config = config;
		this.world = world;
		this.didNightPass= false;
	}
	
	public PassTime(Main plugin, Config config, World world, Boolean didNightPass) {
		this.plugin = plugin;
		this.config = config;
		this.world = world;
		this.didNightPass= didNightPass;
	}

	@Override
	public void run() {
		String myWorldName = dims.matcher(this.world.getName()).replaceAll("");
		Boolean syncDimensionTime = this.config.config.getBoolean("syncDimensionTime", false);
		Boolean syncWorldTime = this.config.config.getBoolean("syncWorldTime", false);
		if (this.world.getTime() < config.config.getInt("stopTime")
				&& this.world.getTime() >= config.config.getInt("startTime")) {
			this.world.setTime(this.world.getTime() + config.config.getInt("increment"));
			this.plugin.doSleep.remove(this.world);
			this.plugin.doSleep.put(this.world, new PassTime(this.plugin, this.config, this.world, true)
					.runTaskLater(this.plugin, this.config.config.getLong("timeBetweenIncrements", 4)));
			if (syncWorldTime || syncDimensionTime) {
				for (World w : this.plugin.numPlayers.keySet()) {
					String theirWorldName = dims.matcher(w.getName()).replaceAll("");
					if (!syncWorldTime && !myWorldName.equals(theirWorldName)) continue;
					if (!syncDimensionTime && !this.world.getEnvironment().equals(w.getEnvironment())) continue;
					w.setTime(world.getTime());
				}
			}
			return;
		}

		for (World w : this.plugin.numPlayers.keySet()) {
			String theirWorldName = dims.matcher(w.getName()).replaceAll("");
			if (!syncWorldTime && !myWorldName.equals(theirWorldName)) continue;
			if (!syncDimensionTime && !this.world.getEnvironment().equals(w.getEnvironment())) continue;
			this.plugin.doSleep.remove(w);
			if (didNightPass) this.plugin.doSleep.put(w, new ClearWeather(w).runTask(this.plugin));
			else this.plugin.doSleep.put(w, new ClearWeather(w).runTaskLater(this.plugin, this.config.config.getLong("sleepDelay")));
		}
		this.cancel();
		this.plugin.doSleep.remove(this.world);
	}
}
