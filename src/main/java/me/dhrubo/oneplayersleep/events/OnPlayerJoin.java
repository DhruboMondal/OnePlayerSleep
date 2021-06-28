package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;

public class OnPlayerJoin implements Listener {
    private Main plugin;
    private Config config;

    public OnPlayerJoin(Main plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Boolean messageFromSleepingIgnored = config.config.getBoolean("messageFromSleepingIgnored", true);
        if(messageFromSleepingIgnored && event.getPlayer().isSleepingIgnored()) return;
        if(event.getPlayer().hasPermission("sleep.ignore"))
            return;

        World myWorld = event.getPlayer().getWorld();
        if(!this.plugin.numPlayers.containsKey(myWorld))
            this.plugin.numPlayers.put(myWorld,Long.valueOf(1));

        this.plugin.numPlayers.put( event.getPlayer().getWorld() , this.plugin.numPlayers.get(event.getPlayer().getWorld())+1 );
    }
}
