package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 战斗背包事件监听器
 * 处理战斗背包的相关事件
 * 
 * @author charlieveg
 */
public class BattleInventoryListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    
    public BattleInventoryListener(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理玩家右键战斗背包物品
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        // 检查是否是战斗背包物品
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName == null) {
            return;
        }
        
        String itemName = ChatColor.stripColor(displayName);
        if (itemName.contains("战斗背包")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            plugin.getBattleInventoryManager().openBattleInventory(player);
            player.sendMessage(ChatColor.GREEN + "正在打开战斗背包...");
        }
    }
    
    /**
     * 处理战斗背包界面的点击事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否涉及战斗背包界面
        Inventory topInventory = event.getView().getTopInventory();
        Inventory bottomInventory = event.getView().getBottomInventory();
        
        // 如果顶部不是战斗背包，则不处理
        if (!isBattleInventory(topInventory)) {
            return;
        }
        
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();
        
        // 如果点击的是战斗背包界面
        if (clickedInventory != null && isBattleInventory(clickedInventory)) {
            // 战斗物品槽位
            int[] battleSlots = {11, 12, 13, 14, 15};
            boolean isBattleSlot = false;
            for (int battleSlot : battleSlots) {
                if (slot == battleSlot) {
                    isBattleSlot = true;
                    break;
                }
            }
            
            // 如果不是战斗物品槽，禁止操作
            if (!isBattleSlot) {
                event.setCancelled(true);
                return;
            }
        }
        
        // 禁止shift点击（只在战斗背包界面内禁止）
        if (event.isShiftClick() && clickedInventory != null && isBattleInventory(clickedInventory)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "禁止在战斗背包界面使用Shift键快捷拖动物品！");
            return;
        }
        
        // 允许正常的拖拽操作，包括从玩家背包到战斗背包的拖拽
        
        // 在物品变化后更新属性
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAttributeManager().updatePlayerAttributes(player);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    /**
     * 处理战斗背包关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 检查是否是战斗背包界面
        if (isBattleInventory(event.getInventory())) {
            // 背包关闭时更新属性
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getAttributeManager().updatePlayerAttributes(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    /**
     * 检查是否是战斗背包界面
     */
    private boolean isBattleInventory(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        
        // 检查界面标题
        String title = inventory.getTitle();
        return title.contains("战斗背包") && inventory.getSize() == 27;
    }
    
    /**
     * 检查物品是否可以放入指定槽位
     */
    private boolean canPlaceItem(int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        // 获取需要的lore
        String requiredLore = "";
        switch (slot) {
            case 11: requiredLore = "战斗背包①号栏核心"; break;
            case 12: requiredLore = "战斗背包②号栏核心"; break;
            case 13: requiredLore = "战斗背包③号栏核心"; break;
            case 14: requiredLore = "战斗背包④号栏核心"; break;
            case 15: requiredLore = "战斗背包⑤号栏核心"; break;
            default: return false;
        }
        
        // 检查物品lore是否包含需要的标识
        for (String line : item.getItemMeta().getLore()) {
            if (line == null) {
                continue;
            }
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine != null && cleanLine.contains(requiredLore)) {
                return true;
            }
        }
        
        return false;
    }
} 