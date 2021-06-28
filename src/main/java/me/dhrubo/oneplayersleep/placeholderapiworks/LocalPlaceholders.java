package me.dhrubo.oneplayersleep.placeholderapiworks;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.regex.Pattern;

public class LocalPlaceholders {
	private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
	public static String fillPlaceHolders(String res, Player player, Config config) {
		if(res.isEmpty()) return res;
		ConfigurationSection worlds = config.messages.getConfigurationSection("worlds");
		String worldName = dims.matcher(player.getWorld().getName()).replaceAll("");
		if(!Objects.requireNonNull(worlds).contains(worldName)) {
			worlds.set(worldName, "&a" + worldName);
		}

		String dimStr = Objects.requireNonNull(config.messages.getConfigurationSection("dimensions")).getString(player.getWorld().getEnvironment().name());

		res = res.replace("[username]", player.getName());
		res = res.replace("[displayname]", player.getDisplayName());
		if(worldName != null) res = res.replace("[world]", worldName.replace("_nether","").replace("_the_end",""));
		if(dimStr!=null) res = res.replace("[dimension]", dimStr);

		//bukkit color codes
		res = ChatColor.translateAlternateColorCodes('&',res);
		return res;
	}
}
