package dev.charlieveg.loreattribute.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 战斗背包数据类
 * 管理玩家的战斗背包界面和物品
 * 
 * @author charlieveg
 */
public class BattleInventory {
    
    private final UUID playerId;
    private final Inventory inventory;
    private final String playerName;
    
    // 战斗背包格子位置
    public static final int[] BATTLE_SLOTS = {11, 12, 13, 14, 15};
    
    public BattleInventory(Player player, String title) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
        initializeInventory();
    }
    
    /**
     * 初始化背包界面
     */
    private void initializeInventory() {
        // 设置装饰物品
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        // 填充背景
        for (int i = 0; i < 27; i++) {
            if (!isBattleSlot(i)) {
                inventory.setItem(i, glass);
            }
        }
        
        // 设置核心物品
        setCoreItems();
    }
    
    /**
     * 设置核心物品
     */
    private void setCoreItems() {
        // 1号核心
        setCore(10, "1");
        // 2号核心
        setCore(11, "2");
        // 3号核心
        setCore(12, "3");
        // 4号核心
        setCore(13, "4");
        // 5号核心
        setCore(14, "5");
    }
    
    /**
     * 设置核心物品
     */
    private void setCore(int slot, String number) {
        ItemStack core = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = core.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "&e战斗背包&9" + number + "&e号栏核心"));
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "将特定装备放入此位置",
            ChatColor.GRAY + "以获得额外属性加成"
        ));
        core.setItemMeta(meta);
        inventory.setItem(slot, core);
    }
    
    /**
     * 检查是否为战斗物品槽
     */
    public static boolean isBattleSlot(int slot) {
        for (int battleSlot : BATTLE_SLOTS) {
            if (slot == battleSlot) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取战斗物品
     */
    public ItemStack getBattleItem(int index) {
        if (index < 1 || index > 5) {
            return null;
        }
        return inventory.getItem(BATTLE_SLOTS[index - 1]);
    }
    
    /**
     * 设置战斗物品
     */
    public boolean setBattleItem(int index, ItemStack item) {
        if (index < 1 || index > 5) {
            return false;
        }
        inventory.setItem(BATTLE_SLOTS[index - 1], item);
        return true;
    }
    
    /**
     * 获取所有战斗物品
     */
    public ItemStack[] getAllBattleItems() {
        ItemStack[] items = new ItemStack[5];
        for (int i = 0; i < 5; i++) {
            items[i] = inventory.getItem(BATTLE_SLOTS[i]);
        }
        return items;
    }
    
    /**
     * 清空所有战斗物品
     */
    public void clearAllBattleItems() {
        for (int slot : BATTLE_SLOTS) {
            inventory.setItem(slot, null);
        }
        setCoreItems();
    }
    
    /**
     * 检查物品是否可以放入指定位置
     */
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (!isBattleSlot(slot) || item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // 检查物品是否有战斗背包相关的lore
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        List<String> lore = item.getItemMeta().getLore();
        String requiredLore = getRequiredLoreForSlot(slot);
        
        for (String line : lore) {
            if (ChatColor.stripColor(line).contains(ChatColor.stripColor(requiredLore))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取指定槽位需要的lore
     */
    private String getRequiredLoreForSlot(int slot) {
        switch (slot) {
            case 11: return "&e战斗背包&9①&e号栏核心";
            case 12: return "&e战斗背包&9②&e号栏核心";
            case 13: return "&e战斗背包&9③&e号栏核心";
            case 14: return "&e战斗背包&9④&e号栏核心";
            case 15: return "&e战斗背包&c⑤&e号栏核心";
            default: return "";
        }
    }
    
    // Getter methods
    public UUID getPlayerId() {
        return playerId;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public String getPlayerName() {
        return playerName;
    }
} 