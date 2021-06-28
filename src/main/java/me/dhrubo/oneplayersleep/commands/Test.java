package me.dhrubo.oneplayersleep.commands;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.tasks.OnSleepChecks;
import me.dhrubo.oneplayersleep.tasks.SendMessage;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import me.dhrubo.oneplayersleep.placeholderapiworks.LocalPlaceholders;
import me.dhrubo.oneplayersleep.display.Message;

import java.util.regex.Pattern;

public class Test implements CommandExecutor {
	private Main plugin;
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);

	public Test(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Boolean messageToSleepingIgnored = plugin.getPluginConfig().config.getBoolean("messageToSleepingIgnored", true);
		if(command.getName().equalsIgnoreCase("sleep test")) {
			if(!(sender instanceof Player)){
				sender.sendMessage("[sleep] only players can use this command!");
				return true;
			}
			Player player = (Player)sender;
			Config config = this.plugin.getPluginConfig();

			Boolean messageOtherWorlds= config.config.getBoolean("messageOtherWorlds");
			Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions");
			ConfigurationSection worlds = config.messages.getConfigurationSection("worlds");
			String myWorldName = dims.matcher(player.getWorld().getName()).replaceAll("");
			if(!worlds.contains(myWorldName)) {
				worlds.set(myWorldName, "&a" + myWorldName);
			}

			new OnSleepChecks(this.plugin, config, player, true).runTaskAsynchronously(this.plugin);

			Message[] res;
			switch(args.length) {
				case 0: {
					//if just /sleep test, pick a random message
					res = new Message[1];
					res[0] = this.plugin.getPluginConfig().pickRandomMessage();
					String global = LocalPlaceholders.fillPlaceHolders(res[0].msg.getText(), player, config);
					String hover = LocalPlaceholders.fillPlaceHolders(res[0].hoverText, player, config);
					res[0] = new Message(res[0].name, global, hover, res[0].wakeup, res[0].cantWakeup, res[0].chance);
					break;
				}
				default: {
					//if trying to specify message
					res = new Message[args.length];
					for( int i = 0; i<args.length; i++) {
						if(!config.messages.getConfigurationSection("messages").contains(args[i])) {
							sender.sendMessage(config.messages.getString("badArgs"));
							return true;
						}
						res[i] = config.getMessage(args[i], player);
					}
				}
			}

			for( Message m : res) {
				for (World w : plugin.getServer().getWorlds()) {
					String theirWorldName = dims.matcher(w.getName()).replaceAll("");
					if(!worlds.contains(theirWorldName)) {
						worlds.set(theirWorldName, "&a" + theirWorldName);
					}

					if( !messageOtherWorlds && !myWorldName.equals(theirWorldName) ) continue;
					if( !messageOtherDimensions && !player.getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;

					for (Player p : w.getPlayers()) {
						if(messageToSleepingIgnored && p.isSleepingIgnored()) continue;
						//skip if has perm
						if(p.hasPermission("sleep.ignore")) continue;

						new SendMessage(this.plugin, config, player, p, m).runTaskAsynchronously(this.plugin);
					}
				}
			}
		}
		return true;
	}
	
}
