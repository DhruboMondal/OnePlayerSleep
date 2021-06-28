package me.dhrubo.oneplayersleep.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabComplete implements TabCompleter {
	private Map<String,String> subCommands = new HashMap<String,String>();
	
	private Config config;
	
	public TabComplete(Config config) {
		//load Main.commands and permission nodes into map
		subCommands.put("reload","sleep.reload");
		subCommands.put("wakeup","sleep.wakeup");
		subCommands.put("test","sleep.test");
		subCommands.put("help","sleep.help");
		this.config = config;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		if(!sender.hasPermission("sleep.see")) return null;

		List<String> res = null;

		switch(args.length){
			case 1: {
				res = new ArrayList<String>();
				List<String> subCom = new ArrayList<String>();
				//fill list based on command permission nodes
				for (Map.Entry<String, String> entry : subCommands.entrySet()) {
					if (sender.hasPermission(entry.getValue()))
						subCom.add(entry.getKey());
				}
				StringUtil.copyPartialMatches(args[0],subCom,res);
				break;
			}
			default: {
				if( 	(args[0].equalsIgnoreCase("test")  && sender.hasPermission("sleep.test") ) ||
						(args[0].equalsIgnoreCase("wakeup")  && sender.hasPermission("sleep.wakeup") ) ) {
					res = new ArrayList<String>();
					StringUtil.copyPartialMatches(args[args.length-1],this.config.messageNames,res);
				}
			}
		}
		return res;
	}
}
