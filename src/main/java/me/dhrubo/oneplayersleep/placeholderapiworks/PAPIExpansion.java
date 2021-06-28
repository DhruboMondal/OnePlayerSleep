package me.dhrubo.oneplayersleep.placeholderapiworks;

import me.dhrubo.oneplayersleep.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;

public class PAPIExpansion extends PlaceholderExpansion{
	private final Main plugin;

	public PAPIExpansion(Main plugin){
	    this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

	@Override
    public boolean canRegister(){
        return true;
    }
	
	@Override
	public @NotNull String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public @NotNull String getIdentifier() {
		return "oneplayersleep";
	}

	@Override
    public @NotNull String getVersion(){
        return "2.0.2";
    }
	
	@Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier){
		if(player == null){
            return "";
        }

        // %oneplayersleep_sleeping_player_count%
        if(identifier.equalsIgnoreCase("sleeping_player_count")){
            Integer res = 0;
            for(Map.Entry<World, HashSet<Player>> entry : this.plugin.sleepingPlayers.entrySet()){
                res += entry.getValue().size();
            }
            return res.toString();
        }

        // %oneplayersleep_total_player_count%
        if(identifier.equalsIgnoreCase("total_player_count")){
            Long res = Long.valueOf(0);
            for(Map.Entry<World,Long> entry : this.plugin.numPlayers.entrySet()){
                res += entry.getValue();
            }
            return res.toString();
        }

        return null;
    }
}
