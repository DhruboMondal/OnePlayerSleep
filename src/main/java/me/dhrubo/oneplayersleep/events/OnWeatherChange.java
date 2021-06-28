package me.dhrubo.oneplayersleep.events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class OnWeatherChange implements Listener {
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	private Config config;

	public OnWeatherChange(Config config) {
		this.config = config;
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if(!this.config.config.getBoolean("doOtherWorlds")) return;
		ArrayList<World> worlds = (ArrayList<World>) Bukkit.getWorlds();
		World world = event.getWorld();
		for (World w : worlds) {
			if(w.equals(event.getWorld())) continue;
			if(dims.matcher(w.getName()).find()) continue;
			w.setStorm(event.toWeatherState());

			//set other weathers a little longer than this world's weather duration so only one world causes the next weather update
			w.setWeatherDuration(world.getWeatherDuration()+5);
			w.setThundering(world.isThundering());
			w.setThunderDuration(world.getThunderDuration()+5);
		}
	}
}
