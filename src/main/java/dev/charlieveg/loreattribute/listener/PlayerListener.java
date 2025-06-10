package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 玩家事件监听器
 * 处理玩家登录、退出、装备变化等事件
 * 
 * @author charlieveg
 */
public class PlayerListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    
    public PlayerListener(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 玩家加入服务器时初始化属性
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 延迟1tick后计算属性，确保玩家完全加载
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().updatePlayerAttributes(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * 玩家退出时清理数据
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 清除属性缓存
        plugin.getAttributeManager().clearPlayerAttributes(player);
        
        // 移除战斗背包
        plugin.getBattleInventoryManager().removeBattleInventory(player);
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
        
        // 检查配置是否启用物品变化更新
        if (plugin.getConfigManager().getBoolean("AttributeUpdate.UpdateOnItemChange", true)) {
            // 延迟1tick更新属性，确保物品切换完成
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getAttributeManager().updatePlayerAttributes(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    /**
     * 玩家点击背包时检查装备变化
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否是装备相关的点击
        if (isEquipmentRelatedClick(event)) {
            // 检查配置是否启用装备变化更新
            if (plugin.getConfigManager().getBoolean("AttributeUpdate.UpdateOnEquipChange", true)) {
                // 延迟2tick更新属性，确保装备变化完成
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getAttributeManager().updatePlayerAttributes(player);
                    }
                }.runTaskLater(plugin, 2L);
            }
        }
    }
    
    /**
     * 检查是否是装备相关的点击
     */
    private boolean isEquipmentRelatedClick(InventoryClickEvent event) {
        // 检查点击的槽位是否是装备槽位
        int slot = event.getSlot();
        
        // 装备栏槽位: 5-8 (头盔、胸甲、护腿、靴子)
        // 主手槽位: 当前选中的槽位
        if (slot >= 5 && slot <= 8) {
            return true;
        }
        
        // 检查是否是主手槽位
        if (event.getInventory().equals(event.getWhoClicked().getInventory())) {
            Player player = (Player) event.getWhoClicked();
            if (slot == player.getInventory().getHeldItemSlot()) {
                return true;
            }
        }
        
        // 检查是否是shift点击装备（自动穿戴）
        if (event.isShiftClick() && event.getCurrentItem() != null) {
            switch (event.getCurrentItem().getType()) {
                case LEATHER_HELMET:
                case CHAINMAIL_HELMET:
                case IRON_HELMET:
                case DIAMOND_HELMET:
                case GOLD_HELMET:
                case LEATHER_CHESTPLATE:
                case CHAINMAIL_CHESTPLATE:
                case IRON_CHESTPLATE:
                case DIAMOND_CHESTPLATE:
                case GOLD_CHESTPLATE:
                case LEATHER_LEGGINGS:
                case CHAINMAIL_LEGGINGS:
                case IRON_LEGGINGS:
                case DIAMOND_LEGGINGS:
                case GOLD_LEGGINGS:
                case LEATHER_BOOTS:
                case CHAINMAIL_BOOTS:
                case IRON_BOOTS:
                case DIAMOND_BOOTS:
                case GOLD_BOOTS:
                    return true;
            }
        }
        
        return false;
    }
} 