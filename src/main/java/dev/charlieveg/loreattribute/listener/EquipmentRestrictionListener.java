package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.manager.EquipmentRestrictionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 装备限制监听器
 * 监听物品切换和装备变化，强制执行装备限制规则
 */
@RequiredArgsConstructor
public class EquipmentRestrictionListener implements Listener {
    
    private final EquipmentRestrictionManager restrictionManager;
    
    /**
     * 监听玩家切换手持物品
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack newItem = inventory.getItem(event.getNewSlot());
        
        if (newItem == null || newItem.getType() == Material.AIR) {
            return;
        }
        
        // 检查新手持物品是否违反限制
        if (isItemRestricted(player, newItem)) {
            event.setCancelled(true);
            
            // 尝试切换到允许的物品槽
            int allowedSlot = findAllowedSlot(player);
            if (allowedSlot != -1) {
                player.getInventory().setHeldItemSlot(allowedSlot);
                player.sendMessage("§c该物品违反装备限制规则，已自动切换到允许的物品。");
            } else {
                // 没有允许的物品，强制切换到空槽
                for (int i = 0; i < 9; i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item == null || item.getType() == Material.AIR) {
                        player.getInventory().setHeldItemSlot(i);
                        player.sendMessage("§c该物品违反装备限制规则，已切换到空槽。");
                        return;
                    }
                }
                
                // 如果快捷栏都满了禁止物品，切换到空手
                player.sendMessage("§c该物品违反装备限制规则，背包中无可用物品，已切换到空手。");
                inventory.setHeldItemSlot(inventory.getHeldItemSlot());
            }
        }
    }
    
    /**
     * 监听背包点击事件（装备护甲）
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 只处理玩家自己的背包
        if (event.getInventory().getType() != InventoryType.PLAYER) {
            return;
        }
        
        // 检查是否是装备护甲的操作
        if (event.getSlotType() == null) {
            return;
        }
        
        // 延迟检查，确保物品已经被移动
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndFixEquipmentViolations(player);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("LoreAttribute"), 1L);
    }
    
    /**
     * 监听玩家交互事件（右键装备）
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !isArmorItem(item)) {
            return;
        }
        
        // 延迟检查，确保装备已经被穿戴
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndFixEquipmentViolations(player);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("LoreAttribute"), 1L);
    }
    
    /**
     * 检查物品是否为护甲
     */
    private boolean isArmorItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        Material type = item.getType();
        return type.name().endsWith("_HELMET") ||
               type.name().endsWith("_CHESTPLATE") ||
               type.name().endsWith("_LEGGINGS") ||
               type.name().endsWith("_BOOTS");
    }
    
    /**
     * 检查并修复装备违规
     */
    private void checkAndFixEquipmentViolations(Player player) {
        PlayerInventory inventory = player.getInventory();
        boolean hasViolation = false;
        
        // 检查护甲
        if (isItemRestricted(player, inventory.getHelmet())) {
            ItemStack helmet = inventory.getHelmet();
            inventory.setHelmet(null);
            returnItemToPlayer(player, helmet);
            player.sendMessage("§c头盔违反装备限制规则，已自动卸下。");
            hasViolation = true;
        }
        
        if (isItemRestricted(player, inventory.getChestplate())) {
            ItemStack chestplate = inventory.getChestplate();
            inventory.setChestplate(null);
            returnItemToPlayer(player, chestplate);
            player.sendMessage("§c胸甲违反装备限制规则，已自动卸下。");
            hasViolation = true;
        }
        
        if (isItemRestricted(player, inventory.getLeggings())) {
            ItemStack leggings = inventory.getLeggings();
            inventory.setLeggings(null);
            returnItemToPlayer(player, leggings);
            player.sendMessage("§c护腿违反装备限制规则，已自动卸下。");
            hasViolation = true;
        }
        
        if (isItemRestricted(player, inventory.getBoots())) {
            ItemStack boots = inventory.getBoots();
            inventory.setBoots(null);
            returnItemToPlayer(player, boots);
            player.sendMessage("§c靴子违反装备限制规则，已自动卸下。");
            hasViolation = true;
        }
        
        // 检查手持物品
        if (isItemRestricted(player, inventory.getItemInHand())) {
            int allowedSlot = findAllowedSlot(player);
            if (allowedSlot != -1) {
                inventory.setHeldItemSlot(allowedSlot);
                player.sendMessage("§c手持物品违反装备限制规则，已自动切换。");
            } else {
                // 没有允许的物品，清空手持
                ItemStack handItem = inventory.getItemInHand();
                inventory.setItemInHand(new ItemStack(Material.AIR));
                returnItemToPlayer(player, handItem);
                player.sendMessage("§c手持物品违反装备限制规则，已卸下。");
            }
            hasViolation = true;
        }
        
        if (hasViolation) {
            player.updateInventory();
        }
    }
    
    /**
     * 将物品返回给玩家背包
     */
    private void returnItemToPlayer(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        PlayerInventory inventory = player.getInventory();
        
        // 尝试放入背包
        HashMap<Integer, ItemStack> leftover = inventory.addItem(item);
        
        // 如果背包满了，掉落到地上
        if (!leftover.isEmpty()) {
            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }
            player.sendMessage("§e背包已满，物品已掉落在地上。");
        }
    }
    
    /**
     * 检查物品是否被限制
     */
    private boolean isItemRestricted(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        List<ItemStack> equippedItems = getEquippedItems(player);
        return restrictionManager.isRestricted(equippedItems, item);
    }
    
    /**
     * 获取玩家已装备的所有物品
     */
    private List<ItemStack> getEquippedItems(Player player) {
        List<ItemStack> equippedItems = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        
        // 添加护甲
        if (inventory.getHelmet() != null) {
            equippedItems.add(inventory.getHelmet());
        }
        if (inventory.getChestplate() != null) {
            equippedItems.add(inventory.getChestplate());
        }
        if (inventory.getLeggings() != null) {
            equippedItems.add(inventory.getLeggings());
        }
        if (inventory.getBoots() != null) {
            equippedItems.add(inventory.getBoots());
        }
        
        // 添加手持物品
        if (inventory.getItemInHand() != null && inventory.getItemInHand().getType() != Material.AIR) {
            equippedItems.add(inventory.getItemInHand());
        }
        
        return equippedItems;
    }
    
    /**
     * 寻找允许的物品槽
     */
    private int findAllowedSlot(Player player) {
        PlayerInventory inventory = player.getInventory();
        
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i; // 空槽是允许的
            }
            
            // 只检查手持物品的限制，不检查背包物品
            List<ItemStack> equippedItems = getEquippedItemsExcludingHand(player);
            if (!restrictionManager.isRestricted(equippedItems, item)) {
                return i; // 找到允许的物品
            }
        }
        
        return -1; // 没有找到允许的物品槽
    }
    
    /**
     * 获取已装备物品（不包括手持物品）
     */
    private List<ItemStack> getEquippedItemsExcludingHand(Player player) {
        List<ItemStack> equippedItems = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        
        // 只添加护甲
        if (inventory.getHelmet() != null) {
            equippedItems.add(inventory.getHelmet());
        }
        if (inventory.getChestplate() != null) {
            equippedItems.add(inventory.getChestplate());
        }
        if (inventory.getLeggings() != null) {
            equippedItems.add(inventory.getLeggings());
        }
        if (inventory.getBoots() != null) {
            equippedItems.add(inventory.getBoots());
        }
        
        return equippedItems;
    }
} 