package me.dhrubo.oneplayersleep.commands;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class Sleep implements CommandExecutor {
	private Main plugin;
	private Map<String,String> subCommands = new HashMap<String,String>();
	
	public Sleep(Main plugin) {
		this.plugin = plugin;
		subCommands.put("reload","sleep.reload");
		subCommands.put("wakeup","sleep.wakeup");
		subCommands.put("test","sleep.test");
		subCommands.put("help","sleep.help");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("sleep")) {
			switch(args.length) {
				case 0: {
					plugin.getCommand("sleep help").execute(sender, label, args);
					break;
				}
				default: {
					String cmd = command.toString();
					for(int i = 0; i<args.length; i++) cmd = cmd + args[i];
					if(subCommands.containsKey(args[0])) {
						if(!sender.hasPermission(subCommands.get(args[0]))) return false;
						String[] new_args = new String[args.length-1];
						for(int i = 1; i<args.length; i++) new_args[i-1] = args[i];
						plugin.getCommand("sleep " + args[0]).execute(sender, label, new_args);
					}
					else { 
						sender.sendMessage(this.plugin.getPluginConfig().messages.getString("badArgs"));
					}
					return true; 
				}
			}
		}
		return true;
		
	}
}
