package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.tasks.OnSleepChecks;
import me.dhrubo.oneplayersleep.placeholderapiworks.LocalPlaceholders;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerBedSleep implements Listener {
	private final Main plugin;
	private final Config config;
	private final Map<Player,Long> lastTme = new HashMap<>();

	public OnPlayerBedSleep(Main plugin, Config config) {
		this.plugin = plugin;
		this.config = config;
	}
	
	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		//skip if player needs to be ignored by the plugin
		if(config.config.getBoolean("messageFromSleepingIgnored", false)
				&& event.getPlayer().isSleepingIgnored()) return;

		if(event.getPlayer().hasPermission("sleep.ignore")) return;

		World myWorld = event.getPlayer().getWorld();

		Boolean isNether = myWorld.getName().contains("_nether");
		Boolean isEnd = myWorld.getName().contains("_the_end");

		if(isNether) {
			if (!config.config.getBoolean("allowSleepInNether", false)) return;
			if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE) {
				event.setUseBed(Event.Result.ALLOW);
			}
		}

		if(isEnd) {
			if (!config.config.getBoolean("allowSleepInEnd", false)) return;
			if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE) {
				event.setUseBed(Event.Result.ALLOW);
			}
		}

		if(
			(	   event.getPlayer().getWorld().getTime() < config.config.getInt("startTime")
				|| event.getPlayer().getWorld().getTime() > config.config.getInt("stopTime"))
			&& !event.getPlayer().getWorld().hasStorm()
			) {
			String msg = config.messages.getString("badTimeMessage");
			msg = LocalPlaceholders.fillPlaceHolders(
					msg,
					event.getPlayer(),
					this.config);
			event.getPlayer().sendMessage(msg);
			event.setCancelled(true);
			return;
		}

		//cooldown logic
		Long currentTime = System.currentTimeMillis();
		if(		this.lastTme.containsKey(event.getPlayer()) &&
				currentTime < this.lastTme.get(event.getPlayer()) + config.config.getLong("sleepCooldown")) {
			event.setCancelled(true);
			String msg = config.messages.getString("cooldownMessage");
			msg = LocalPlaceholders.fillPlaceHolders(
					msg,
					event.getPlayer(),
					this.config);
			event.getPlayer().sendMessage(msg);
			event.setCancelled(true);
			return;
		}

		if(this.lastTme.containsKey(event.getPlayer()))
			this.lastTme.remove(event.getPlayer());
		this.lastTme.put(event.getPlayer(), currentTime);
		
		//do other relevant checks asynchronously
		new OnSleepChecks(this.plugin, this.config, event.getPlayer()).runTaskAsynchronously(plugin);
	}
}
