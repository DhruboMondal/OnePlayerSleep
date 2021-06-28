package me.dhrubo.oneplayersleep.display;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Message {
	public TextComponent msg;
	public String name;
	public String hoverText;
	public String wakeup;
	public String cantWakeup;
	public Double chance;

	public Message(String name, String global, String hover, String wakeup, String cantWakeup, Double chance) {
		this.msg = new TextComponent(global);
		this.msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( hover ).create()));
		this.msg.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleepwakeup " + name));
		this.name = name;
		this.wakeup = wakeup;
		this.hoverText = hover;
		this.cantWakeup = cantWakeup;
		this.chance = chance;
	}
}
