package dev.charlieveg.loreattribute.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗背包管理器
 * 负责管理玩家的战斗背包系统
 * 
 * @author charlieveg
 */
public class BattleInventoryManager {
    
    private final Object plugin;
    
    private final Map<UUID, Inventory> battleInventories = new ConcurrentHashMap<>();
    
    public static final int[] BATTLE_SLOTS = {11, 12, 13, 14, 15};
    
    private String inventoryTitle = "战斗背包";
    private String inventoryItemName = "&c战斗背包";
    private List<String> inventoryItemLore;
    private String invalidSlotMessage = "&c该位置不能放入此物品，请放入正确位置！";
    private String shiftClickMessage = "&c禁止在战斗背包界面使用Shift键与键盘按键快捷拖动物品！";
    
    public BattleInventoryManager(Object plugin) {
        this.plugin = plugin;
        initializeDefaultConfig();
    }
    
    /**
     * 初始化默认配置
     */
    private void initializeDefaultConfig() {
        inventoryItemLore = Arrays.asList(
            "§e什么是战斗背包？",
            "§7战斗背包向你提供一个额外的5格物品栏",
            "§7你可以把特定装备放入特定位置增加自身属性",
            "§7但这些装备在你死亡时和你的背包的装备一样处理哦~"
        );
    }
    
    /**
     * 创建战斗背包物品
     */
    public ItemStack createBattleInventoryItem() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', inventoryItemName));
        meta.setLore(inventoryItemLore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 打开玩家的战斗背包
     */
    public void openBattleInventory(Player player) {
        Inventory battleInv = getBattleInventory(player);
        player.openInventory(battleInv);
    }
    
    /**
     * 获取玩家的战斗背包
     */
    public Inventory getBattleInventory(Player player) {
        UUID playerId = player.getUniqueId();
        
        return battleInventories.computeIfAbsent(playerId, k -> {
            Inventory inv = Bukkit.createInventory(null, 27, 
                ChatColor.translateAlternateColorCodes('&', inventoryTitle));
            initializeBattleInventory(inv);
            return inv;
        });
    }
    
    /**
     * 初始化战斗背包界面
     */
    private void initializeBattleInventory(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 27; i++) {
            if (!isBattleSlot(i)) {
                inventory.setItem(i, glass);
            }
        }
   
    }
    
    /**
     * 设置核心指示物品
     */
    private void setCoreIndicators(Inventory inventory) {
        for (int i = 0; i < BATTLE_SLOTS.length; i++) {
            ItemStack core = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = core.getItemMeta();
            
            String number = getSlotNumber(i + 1);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                "&e战斗背包&9" + number + "&e号栏核心"));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "将特定装备放入此位置",
                ChatColor.GRAY + "以获得额外属性加成"
            ));
            
            core.setItemMeta(meta);
            inventory.setItem(BATTLE_SLOTS[i], core);
        }
    }
    
    /**
     * 获取槽位编号显示
     */
    private String getSlotNumber(int slot) {
        switch (slot) {
            case 1: return "①";
            case 2: return "②";
            case 3: return "③";
            case 4: return "④";
            case 5: return "⑤";
            default: return String.valueOf(slot);
        }
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
     * 获取玩家战斗背包中的物品
     */
    public ItemStack getBattleItem(Player player, int index) {
        if (index < 1 || index > 5) {
            return null;
        }
        
        Inventory battleInv = getBattleInventory(player);
        ItemStack item = battleInv.getItem(BATTLE_SLOTS[index - 1]);
        
        // 直接返回物品，不再检查占位符
        return item;
    }
    
    /**
     * 设置玩家战斗背包中的物品
     */
    public boolean setBattleItem(Player player, int index, ItemStack item) {
        if (index < 1 || index > 5) {
            return false;
        }
        
        Inventory battleInv = getBattleInventory(player);
        battleInv.setItem(BATTLE_SLOTS[index - 1], item);
        return true;
    }
    
    /**
     * 获取玩家所有战斗背包物品
     */
    public ItemStack[] getAllBattleItems(Player player) {
        ItemStack[] items = new ItemStack[5];
        for (int i = 0; i < 5; i++) {
            items[i] = getBattleItem(player, i + 1);
        }
        return items;
    }
    
    /**
     * 清空玩家战斗背包
     */
    public void clearBattleInventory(Player player) {
        Inventory battleInv = getBattleInventory(player);
        for (int slot : BATTLE_SLOTS) {
            battleInv.setItem(slot, null);
        }
        setCoreIndicators(battleInv);
    }
    
    /**
     * 检查物品是否可以放入指定槽位
     */
    public boolean canPlaceItem(int slot, ItemStack item) {
        if (!isBattleSlot(slot) || item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        List<String> lore = item.getItemMeta().getLore();
        String requiredLore = getRequiredLoreForSlot(slot);
        
        for (String line : lore) {
            if (line == null) {
                continue;
            }
            String cleanLine = ChatColor.stripColor(line);
            String cleanRequired = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', requiredLore));
            if (cleanLine != null && cleanRequired != null && cleanLine.contains(cleanRequired)) {
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
    
    /**
     * 保存所有战斗背包数据
     */
    public void saveAllBattleInventories() {
        // 这里可以实现数据持久化
        // 由于是内存管理，玩家重新登录时会重新创建
    }
    
    /**
     * 移除玩家战斗背包
     */
    public void removeBattleInventory(Player player) {
        battleInventories.remove(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否有战斗背包
     */
    public boolean hasBattleInventory(Player player) {
        return battleInventories.containsKey(player.getUniqueId());
    }
    
    /**
     * 获取战斗背包中的有效物品数量
     */
    public int getBattleItemCount(Player player) {
        int count = 0;
        for (int i = 1; i <= 5; i++) {
            if (getBattleItem(player, i) != null) {
                count++;
            }
        }
        return count;
    }
    
    // Getter/Setter methods
    public String getInventoryTitle() {
        return inventoryTitle;
    }
    
    public void setInventoryTitle(String inventoryTitle) {
        this.inventoryTitle = inventoryTitle;
    }
    
    public String getInventoryItemName() {
        return inventoryItemName;
    }
    
    public void setInventoryItemName(String inventoryItemName) {
        this.inventoryItemName = inventoryItemName;
    }
    
    public List<String> getInventoryItemLore() {
        return inventoryItemLore;
    }
    
    public void setInventoryItemLore(List<String> inventoryItemLore) {
        this.inventoryItemLore = inventoryItemLore;
    }
    
    public String getInvalidSlotMessage() {
        return ChatColor.translateAlternateColorCodes('&', invalidSlotMessage);
    }
    
    public String getShiftClickMessage() {
        return ChatColor.translateAlternateColorCodes('&', shiftClickMessage);
    }
} 