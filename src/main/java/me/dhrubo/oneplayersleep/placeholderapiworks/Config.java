package me.dhrubo.oneplayersleep.placeholderapiworks;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.dhrubo.oneplayersleep.display.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Config {
	public FileConfiguration config;
	public FileConfiguration messages;
	public String version;
	public List<String> messageNames = new ArrayList<>();

	private ArrayList<Message> messageArray = new ArrayList<>();
	private Double totalChance = 0.0;
	private ArrayList<Double> chanceRanges = new ArrayList<>();
	private final Main plugin;
	
	private Boolean hasPAPI;
	
	public Config(Main plugin) {
		this.plugin = plugin;
		String s = this.plugin.getServer().getClass().getPackage().getName();
		this.version = s.substring(s.lastIndexOf('.')+1);
	}
	
	
	public void refreshConfigs() {
		this.messageNames = new ArrayList<>();
		this.messageArray = new ArrayList<>();
		this.totalChance = 0.0;
		this.chanceRanges = new ArrayList<>();
		this.hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		
		//load config.yml file
		File f = new File(this.plugin.getDataFolder(), "config.yml");
		if(!f.exists())
		{
			plugin.saveResource("config.yml", false);
		}
		this.config = YamlConfiguration.loadConfiguration(f);

		//load messages.yml file
		f = new File(this.plugin.getDataFolder(), "messages.yml");
		if(!f.exists())
		{
			plugin.saveResource("messages.yml", false);
		}
		this.messages = YamlConfiguration.loadConfiguration(f);
		
		if( 	(this.messages.getDouble("version") < 1.6) ) {
			Bukkit.getConsoleSender().sendMessage("�b[Main] old messages.yml detected. Getting a newer version");
			this.renameFileInPluginDir("messages.yml","messages.old.yml");
			
			this.plugin.saveResource("messages.yml", false);
			this.messages = YamlConfiguration.loadConfiguration(f);
		}

		if( 	(this.config.getDouble("version") < 1.6) ) {
			Bukkit.getConsoleSender().sendMessage("�b[Main] old config.yml detected. Updating");
			
			updateConfig();
			
			f = new File(this.plugin.getDataFolder(), "config.yml");
			this.config = YamlConfiguration.loadConfiguration(f);
		}
		
		this.messages.set("onNoPlayersSleeping", ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.messages.getString("onNoPlayersSleeping"))));
		this.messages.set("cooldownMessage", ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.messages.getString("cooldownMessage"))));

		Set<String> allMessageNames = Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getKeys(false);
		this.messageArray = new ArrayList<>();
		this.totalChance = 0.0;
		this.chanceRanges = new ArrayList<>();
		this.chanceRanges.add(0, 0.0);
		int i = 0;
		for (String t : allMessageNames) {
			String msg = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getConfigurationSection(t)).getString("global", "[player] &bis sleeping")));
			String hover_msg = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getConfigurationSection(t)).getString("hover", "&eWake up!")));
			String response = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getConfigurationSection(t)).getString("wakeup", "[player] says &cWake up!")));
			String cantWakeup = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getConfigurationSection(t)).getString("cantWakeup", "&csomeone's a deep sleeper")));
			Double chance = Objects.requireNonNull(Objects.requireNonNull(this.messages.getConfigurationSection("messages")).getConfigurationSection(t)).getDouble("chance");
			this.messageArray.add(i, new Message(t, msg, hover_msg, response, cantWakeup, chance) );
			this.totalChance = this.totalChance + chance;
			this.chanceRanges.add(i+1, this.chanceRanges.get(i) + chance);
			this.messageNames.add(t);
			i = i+1;
		}
		
		String msg = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.messages.getString("onNoPlayersSleeping")));
		this.messages.set("onNoPlayersSleeping", msg);
	}
	
	public Boolean hasPAPI() {
		return this.hasPAPI;
	}
	
	public Message pickRandomMessage() {
		int numMessages = this.messageArray.size();
		if(numMessages == 1) return messageArray.get(0);
		
		//pick a random float
		Random r = new Random();
		double randomValue = (totalChance) * r.nextDouble();
		
		//lookup iterator by binary search of ranges
		int iter_low = 0;
		int iter_high = this.chanceRanges.size();
		int i = iter_high/2;
		while(true) {
			double range_low = this.chanceRanges.get(i);
			double range_high = this.chanceRanges.get(i+1);
			if(randomValue <= range_low) iter_high = i;
			else if(randomValue > range_high) iter_low = i;
			else break;
			i = (iter_high - iter_low)/2 + iter_low;
		}
		return messageArray.get(i);
	}

	public Message getMessage(String name) {
		Message res;
		ConfigurationSection cfg = this.messages.getConfigurationSection("messages").getConfigurationSection(name);
		if(cfg != null) {
			String msg = cfg.getString("global", "[player] &bis sleeping");
			String hover_msg = cfg.getString("hover", "&eWake up!");
			String response = cfg.getString("wakeup", "[player] says &cWake up!");
			String cantWakeup = cfg.getString("cantWakeup", "&csomeone's a deep sleeper");
			Double chance = cfg.getDouble("chance");
			res = new Message(name, msg, hover_msg, response, cantWakeup, chance);
		} else res = null;
		return res;
	}

	public Message getMessage(String name, Player player) {
		Message res;
		ConfigurationSection cfg = this.messages.getConfigurationSection("messages").getConfigurationSection(name);
		if(cfg != null) {
			String msg = LocalPlaceholders.fillPlaceHolders(cfg.getString("global", "[player] &bis sleeping"), player, this);
			String hover_msg = LocalPlaceholders.fillPlaceHolders(cfg.getString("hover", "&eWake up!"), player, this);
			String response = LocalPlaceholders.fillPlaceHolders(cfg.getString("wakeup", "[player] says &cWake up!"), player, this);
			String cantWakeup = LocalPlaceholders.fillPlaceHolders(cfg.getString("cantWakeup", "&csomeone's a deep sleeper"), player, this);
			Double chance = cfg.getDouble("chance");
			res = new Message(name, msg, hover_msg, response, cantWakeup, chance);
		} else res = null;
		return res;
	}
	
	//update config files based on version number
	private void updateConfig() {
		this.renameFileInPluginDir("config.yml","config.old.yml");
		plugin.saveResource("config.yml", false);
		Map<String, Object> oldValues = this.config.getValues(false);
		// Read default config to keep comments
		ArrayList<String> linesInDefaultConfig = new ArrayList<>();
		try {
			Scanner scanner = new Scanner(
					new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
			while (scanner.hasNextLine()) {
				linesInDefaultConfig.add(scanner.nextLine() + "");
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<String> newLines = new ArrayList<>();
		for (String line : linesInDefaultConfig) {
			String newline = line;
			if (line.startsWith("version:")) {
				newline = "version: 1.6";
			} else {
				for (String node : oldValues.keySet()) {
					if (line.startsWith(node + ":")) {
						String quotes = "";
						newline = node + ": " + quotes + oldValues.get(node).toString() + quotes;
						break;
					}
				}
			}
			newLines.add(newline);
		}

		FileWriter fw;
		String[] linesArray = newLines.toArray(new String[linesInDefaultConfig.size()]);
		try {
			fw = new FileWriter(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
			for (String s : linesArray) {
				fw.write(s + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void renameFileInPluginDir(String oldName, String newName) {
		File oldFile = new File(this.plugin.getDataFolder().getAbsolutePath() + File.separator + oldName);
		File newFile = new File(this.plugin.getDataFolder().getAbsolutePath() + File.separator + newName);
		try {
			Files.deleteIfExists(newFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
	}
}
