package me.dhrubo.oneplayersleep.commands;

import me.dhrubo.oneplayersleep.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

public class Reload implements CommandExecutor {
	private Main plugin;
	
	public Reload(Main plugin, Config config) {
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.hasPermission("sleep.reload"))
		{
			String str = ChatColor.BLUE + "[Main] reloading.";
			Bukkit.getConsoleSender().sendMessage(str);
			if(sender instanceof Player) {
				if(this.plugin.getPluginConfig().hasPAPI()) str = PlaceholderAPI.setPlaceholders((Player)sender, str);
				sender.sendMessage(str);
			}
			
			Config config = plugin.getPluginConfig();
			config.refreshConfigs();
			
			str = ChatColor.BLUE + "[Main] successfully reloaded.";
			Bukkit.getConsoleSender().sendMessage(str);
			if(sender instanceof Player) {
				if(this.plugin.getPluginConfig().hasPAPI()) str = PlaceholderAPI.setPlaceholders((Player)sender, str);
				sender.sendMessage(str);
			}
			
			return true;
		}
		return false;
	}
}
