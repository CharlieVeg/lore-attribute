package dev.charlieveg.loreattribute.task;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 属性更新任务
 * 定期更新所有在线玩家的属性
 * 
 * @author charlieveg
 */
public class AttributeUpdateTask extends BukkitRunnable {
    
    private final LoreAttributePlugin plugin;
    
    public AttributeUpdateTask(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getAttributeManager().updatePlayerAttributes(player);
        }
    }
} 