package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class OnPlayerChangeWorld implements Listener {
    private static final Pattern dims = Pattern.compile("_nether|_the_end", Pattern.CASE_INSENSITIVE);
    private Main plugin;
    private Config config;

    public OnPlayerChangeWorld(Main plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Boolean messageOtherWorlds = config.config.getBoolean("messageOtherWorlds");
        Boolean messageOtherDimensions = config.config.getBoolean("messageOtherDimensions");
        Boolean messageFromSleepingIgnored = config.config.getBoolean("messageFromSleepingIgnored", true);
        if(messageFromSleepingIgnored && event.getPlayer().isSleepingIgnored()) return;
        if(event.getPlayer().hasPermission("sleep.ignore"))
            return;

        //handle if changing world while sleeping somehow
        Player me = event.getPlayer();
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

        World from = event.getFrom();
        World to = me.getWorld();

        //decrement previous world counter, remove if zero to reduce loop ranges
        this.plugin.numPlayers.put( from , this.plugin.numPlayers.get(from)-1 );
        if(this.plugin.numPlayers.get(from) == 0) this.plugin.numPlayers.remove(from);

        //increment next world counter, add if nonexistent
        this.plugin.numPlayers.putIfAbsent(to,Long.valueOf(0));
        this.plugin.numPlayers.put( event.getPlayer().getWorld() , this.plugin.numPlayers.get(event.getPlayer().getWorld())+1 );
    }
}
