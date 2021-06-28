package me.dhrubo.oneplayersleep.tasks;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ClearWeather extends BukkitRunnable{
	private World world;
	private Long duration = Long.valueOf(0);
	
	public ClearWeather(World world) {
		this.world = world;
	}
	
	public ClearWeather(World world, Long duration) {
		this.world = world;
		this.duration = duration;
	}

	@Override
	public void run() {
		this.world.setStorm(false);
		this.world.setThundering(false);
		if(this.duration == 0) {
			Double randomFactor = new Random().nextDouble()*168000;
			this.duration = 12000 + randomFactor.longValue();
		}
		this.world.setWeatherDuration(this.duration.intValue());
		this.cancel();
	}
}
