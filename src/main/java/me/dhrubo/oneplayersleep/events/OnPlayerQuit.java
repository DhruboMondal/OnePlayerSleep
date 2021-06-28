package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class OnPlayerQuit implements Listener {
    private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
    private Main plugin;
    private Config config;

    public OnPlayerQuit(Main plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Boolean messageFromSleepingIgnored = config.config.getBoolean("messageFromSleepingIgnored", true);
        if(messageFromSleepingIgnored && event.getPlayer().isSleepingIgnored()) return;
        if(event.getPlayer().hasPermission("sleep.ignore"))
            return;

        Boolean messageOtherWorlds = config.config.getBoolean("messageOtherWorlds");
        Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions");

        Player me = event.getPlayer();

        //if quit while sleeping, cancel events if no players sleeping
        if(me.isSleeping()){
            Integer numSleepingPlayers = 0;
            this.plugin.sleepingPlayers.get(me.getWorld()).remove(me);
            String myWorldName = dims.matcher(event.getPlayer().getWorld().getName()).replaceAll("");

            for(Map.Entry<World, HashSet<Player>> entry : this.plugin.sleepingPlayers.entrySet())
            {
                String theirWorldName = dims.matcher(entry.getKey().getName()).replaceAll("");
                if( !messageOtherWorlds && !myWorldName.equals(theirWorldName) ) continue;
                if( !messageOtherDimensions && !me.getWorld().getEnvironment().equals( entry.getKey().getEnvironment() ) ) continue;
                numSleepingPlayers += entry.getValue().size();
            }

            if(numSleepingPlayers == 0) {
                for (World w : Bukkit.getWorlds()) {
                    String theirWorldName = dims.matcher(w.getName()).replaceAll("");
                    if( !messageOtherWorlds && !myWorldName.equals(theirWorldName) ) continue;
                    if( !messageOtherDimensions && !event.getPlayer().getWorld().getEnvironment().equals( w.getEnvironment() ) ) continue;
                    if( this.plugin.doSleep.containsKey(w)) {
                        this.plugin.doSleep.get(w).cancel();
                    }
                }
            }
        }
    }
}
