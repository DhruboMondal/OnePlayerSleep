package me.dhrubo.oneplayersleep.commands;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.tasks.AnnounceWakeup;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import me.dhrubo.oneplayersleep.display.Message;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Wakeup implements CommandExecutor {
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	Main plugin;
	Config config;
	
	public Wakeup(Main plugin, Config config) {
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("sleep.wakeup")) return false;
		Boolean isPlayer = (sender instanceof Player);
		Boolean messageOtherWorlds= config.config.getBoolean("messageOtherWorlds", false);
		Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions", false);
		Boolean KickFromBed = this.config.config.getBoolean("kickFromBed", true);
		Boolean messageToSleepingIgnored = config.config.getBoolean("messageToSleepingIgnored", true);
		Boolean cantKickAPlayer = false;
		Boolean hasSleepingPlayers = false;
		Set<World> worlds = new HashSet<World>(this.plugin.sleepingPlayers.keySet());
		Message msg;

		switch(args.length) {
			case 0: { //if no args, use last message given to player
				if(isPlayer) msg = this.plugin.wakeData.get(sender);
				else msg = null;
				break;
			}
			case 1: { //if 1 arg, look up message name
				if(isPlayer) msg = config.getMessage(args[0], (Player)sender);
				else msg = config.getMessage(args[0]);
				break;
			}
			default: { //else bad args
				sender.sendMessage(this.plugin.getPluginConfig().messages.getString("badArgs"));
				return true;
			}
		}
		if(msg == null) msg = config.pickRandomMessage();

		String myWorldName = "";
		if(isPlayer) myWorldName = dims.matcher(((Player)sender).getWorld().getName()).replaceAll("");
		//for each relevant world with sleeping players, kick from bed
		for(World w : worlds) {
			String theirWorld = dims.matcher(w.getName()).replaceAll("");
			if( isPlayer && !messageOtherWorlds && !myWorldName.equals(theirWorld) ) continue;
			if( isPlayer && !messageOtherDimensions && !((Player)sender).getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;
			HashSet<Player> sleepingPlayers = this.plugin.sleepingPlayers.get(w);
			for ( Player p : sleepingPlayers) {
				if(!messageToSleepingIgnored && p.isSleepingIgnored())
					continue;
				if(p.hasPermission("sleep.ignore"))
					continue;
				hasSleepingPlayers = true;
				if(p.hasPermission("sleep.bypass") || !isPlayer) {
					cantKickAPlayer = true;
				}
				else if(KickFromBed) {
					if(this.plugin.sleepingPlayers.get(w).contains(p)) this.plugin.sleepingPlayers.get(w).remove(p);
					if(this.plugin.sleepingPlayers.get(w).size() == 0) this.plugin.sleepingPlayers.remove(w);
					Double health = p.getHealth();
					p.damage(1);
					p.setHealth(health);
				}
			}
			if(!cantKickAPlayer && hasSleepingPlayers && this.plugin.doSleep.containsKey(w)) {
				this.plugin.doSleep.get(w).cancel();
			}
		}

		if(!hasSleepingPlayers) {
			String onNoPlayersSleeping = config.messages.getString("onNoPlayersSleeping");
			if(isPlayer && this.config.hasPAPI()) onNoPlayersSleeping = PlaceholderAPI.setPlaceholders((Player)sender, onNoPlayersSleeping);
			sender.sendMessage(onNoPlayersSleeping);
			return true;
		}
		
		if(isPlayer && cantKickAPlayer) {
			String send;
			if(config.hasPAPI())
				send = PlaceholderAPI.setPlaceholders((Player)sender, msg.cantWakeup);
			else send = msg.cantWakeup;
			sender.sendMessage(send);
			return true;
		}

		for (Map.Entry<World, Long> entry : plugin.numPlayers.entrySet()) {
			World theirWorld = entry.getKey();
			String theirWorldName = dims.matcher(theirWorld.getName()).replaceAll("");
			if( isPlayer && !messageOtherWorlds && !myWorldName.equals(theirWorldName) ) continue;
			if( isPlayer && !messageOtherDimensions && !((Player)sender).getWorld().getEnvironment().equals( theirWorld.getEnvironment() ) ) continue;
			new AnnounceWakeup(this.plugin, this.config, ((Player) sender), msg, theirWorld).runTaskAsynchronously(this.plugin);
		}
		return true;
	}
}
