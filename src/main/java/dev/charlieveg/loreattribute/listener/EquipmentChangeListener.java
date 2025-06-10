package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 装备变化监听器 - 实时更新玩家属性
 */
@RequiredArgsConstructor
public class EquipmentChangeListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    
    /**
     * 玩家加入游戏时初始化属性
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 延迟1 tick后更新属性，确保玩家完全加载
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().updatePlayerAttributes(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * 玩家离开游戏时清理属性缓存
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAttributeManager().clearPlayerAttributes(player);
        
        // 重置玩家移动速度到默认值
        if (player.isOnline()) {
            player.setWalkSpeed(0.2f);
            player.setMaxHealth(20.0);
        }
    }
    
    /**
     * 玩家切换手持物品时更新属性
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 延迟1 tick后更新属性
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().updatePlayerAttributes(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * 监听背包操作，检测装备变化
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否涉及装备栏操作
        if (isEquipmentSlot(event.getSlot(), event.getInventory().getType()) ||
            isArmorSlot(event.getSlot()) ||
            isBattleInventoryOperation(event)) {
            
            // 延迟2 ticks后更新属性，确保物品移动完成
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getAttributeManager().updatePlayerAttributes(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }
    
    /**
     * 检查是否为装备相关的槽位
     */
    private boolean isEquipmentSlot(int slot, InventoryType type) {
        if (type == InventoryType.PLAYER) {
            // 玩家背包中的快捷栏 (0-8)
            return slot >= 0 && slot <= 8;
        }
        return false;
    }
    
    /**
     * 检查是否为盔甲槽位
     */
    private boolean isArmorSlot(int slot) {
        // 盔甲槽位通常是 36-39 (靴子、护腿、胸甲、头盔)
        return slot >= 36 && slot <= 39;
    }
    
    /**
     * 检查是否为战斗背包操作
     */
    private boolean isBattleInventoryOperation(InventoryClickEvent event) {
        // 检查是否为战斗背包界面
        String title = event.getInventory().getTitle();
        return title != null && (title.contains("战斗背包") || title.contains("饰品"));
    }
    
    /**
     * 定期更新所有在线玩家的属性（确保同步）
     */
    public void startPeriodicUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    try {
                        plugin.getAttributeManager().updatePlayerAttributes(player);
                    } catch (Exception e) {
                        // 静默处理异常，避免影响其他玩家
                        if (plugin.getConfigManager().getBoolean("Debug.Attributes", false)) {
                            plugin.getLogger().warning("更新玩家 " + player.getName() + " 属性时出错: " + e.getMessage());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L * 5); // 每5秒更新一次
    }
} 