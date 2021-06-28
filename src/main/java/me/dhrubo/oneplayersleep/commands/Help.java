package me.dhrubo.oneplayersleep.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Help implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("sleep.see")) return false;
		
		TextComponent msg = new TextComponent(ChatColor.BLUE.toString() + "/sleep help - see this");
		msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "/sleep" ).create()));
		msg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleep help"));
		sender.spigot().sendMessage(msg);
		
		if(sender.hasPermission("sleep.wakeup")) {
			msg = new TextComponent(ChatColor.BLUE.toString() + "/sleep wakeup - wake sleeping players");
			msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "/sleep wakeup" ).create()));
			msg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleep wakeup"));
			sender.spigot().sendMessage(msg);
		}
		
		if(sender.hasPermission("sleep.reload")) {
			msg = new TextComponent(ChatColor.BLUE.toString() + "/sleep reload - wake sleeping players");
			msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "/sleep reload" ).create()));
			msg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleep reload"));
			sender.spigot().sendMessage(msg);
		}

		if(sender.hasPermission("sleep.test")) {
			msg = new TextComponent(ChatColor.BLUE.toString() + "/sleep test - test sleep messages");
			msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "/sleep test" ).create()));
			msg.setClickEvent( new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sleep test"));
			sender.spigot().sendMessage(msg);
		}
		return true;
	}
	
}
