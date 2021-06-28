package me.dhrubo.oneplayersleep.events;

import me.dhrubo.oneplayersleep.Main;
import me.dhrubo.oneplayersleep.placeholderapiworks.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class OnBedExplode implements Listener {
    private Main plugin;
    private Config config;

    public OnBedExplode(Main plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void onBedExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        if (!(block.getType().toString().toLowerCase().contains("bed") && block.getType() != Material.BEDROCK))
            return;

        Boolean isNether = block.getWorld().getName().contains("_nether");
        Boolean isEnd = block.getWorld().getName().contains("_the_end");
        if(isNether) {
            if (!config.config.getBoolean("allowSleepInNether", false)) return;
            event.setCancelled(true);
        }
        if(isEnd) {
            if (!config.config.getBoolean("allowSleepInEnd", false)) return;
            event.setCancelled(true);
        }
    }
}
