package dev.charlieveg.loreattribute.ui;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map;

/**
 * 属性查看界面
 * 提供便捷的玩家属性查看功能
 * 
 * @author charlieveg
 */
public class AttributeViewerUI {
    
    private final String title;
    private final int size;
    
    public String getTitle() {
        return title;
    }
    
    public AttributeViewerUI() {
        this.title = ChatColor.translateAlternateColorCodes('&', "&6玩家属性信息");
        this.size = 54; // 6行界面
    }
    
    /**
     * 打开属性查看界面
     */
    public void openAttributeViewer(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        setupDecoration(inventory);
        
        setupPlayerAttributes(inventory, player);
        
        setupActionButtons(inventory, player);
        
        player.openInventory(inventory);
    }
    
    /**
     * 设置界面装饰
     */
    private void setupDecoration(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        int[] borderSlots = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17, 18, 26, 27, 35, 36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
        };
        
        for (int slot : borderSlots) {
            inventory.setItem(slot, glass);
        }
    }
    
    /**
     * 显示玩家属性信息
     */
    private void setupPlayerAttributes(Inventory inventory, Player player) {
        ItemStack playerInfo = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta playerMeta = playerInfo.getItemMeta();
        playerMeta.setDisplayName(ChatColor.GOLD + "玩家: " + player.getName());
        
        List<String> playerLore = new ArrayList<>();
        playerLore.add(ChatColor.GRAY + "当前等级: " + ChatColor.GREEN + player.getLevel());
        playerLore.add(ChatColor.GRAY + "当前血量: " + ChatColor.RED + String.format("%.1f", player.getHealth()));
        playerLore.add(ChatColor.GRAY + "最大血量: " + ChatColor.RED + String.format("%.1f", player.getMaxHealth()));
        playerLore.add(ChatColor.GRAY + "移动速度: " + ChatColor.AQUA + String.format("%.2f", player.getWalkSpeed()));
        playerLore.add(ChatColor.GRAY + "游戏模式: " + ChatColor.YELLOW + player.getGameMode().name());
        playerLore.add("");
        playerLore.add(ChatColor.YELLOW + "属性按生效位置分类显示:");
        playerLore.add(ChatColor.GREEN + "✓ 绿色 = 属性生效中");
        playerLore.add(ChatColor.RED + "✗ 红色 = 位置不对/类型不匹配");
        
        playerMeta.setLore(playerLore);
        playerInfo.setItemMeta(playerMeta);
        inventory.setItem(22, playerInfo);
        
        // 装备状态显示
        setupEquipmentStatus(inventory, player);
        
        // 基础属性显示
        setupBasicAttributes(inventory, player);
        
        // 战斗属性显示
        setupCombatAttributes(inventory, player);
        
        // 特殊属性显示
        setupSpecialAttributes(inventory, player);
    }
    
    /**
     * 显示装备状态
     */
    private void setupEquipmentStatus(Inventory inventory, Player player) {
        // 主手武器状态
        ItemStack mainHand = player.getInventory().getItemInHand();
        boolean hasWeapon = mainHand != null && isWeaponType(mainHand);
        ItemStack weaponStatus = createStatusItem(Material.DIAMOND_SWORD, 
            hasWeapon ? "&a主手武器" : "&c主手武器", 
            hasWeapon ? "&7武器属性生效中" : "&7请手持武器类型物品",
            hasWeapon);
        inventory.setItem(14, weaponStatus);
        
        // 防具状态
        boolean hasFullArmor = checkArmorStatus(player);
        ItemStack armorStatus = createStatusItem(Material.DIAMOND_CHESTPLATE, 
            hasFullArmor ? "&a穿戴防具" : "&c穿戴防具", 
            hasFullArmor ? "&7防具属性生效中" : "&7请穿戴防具类型物品",
            hasFullArmor);
        inventory.setItem(15, armorStatus);
        
        // 战斗背包状态
        boolean hasBattleItems = checkBattleInventoryStatus(player);
        ItemStack battleStatus = createStatusItem(Material.CHEST, 
            hasBattleItems ? "&a战斗背包" : "&c战斗背包", 
            hasBattleItems ? "&7饰品属性生效中" : "&7请在战斗背包中放置饰品",
            hasBattleItems);
        inventory.setItem(16, battleStatus);
    }
    
    /**
     * 检查是否为武器类型
     */
    private boolean isWeaponType(ItemStack item) {
        return getItemType(item).equals("武器");
    }
    
    /**
     * 检查防具状态
     */
    private boolean checkArmorStatus(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null && getItemType(piece).equals("防具")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查战斗背包状态
     */
    private boolean checkBattleInventoryStatus(Player player) {
        try {
            LoreAttributePlugin plugin = LoreAttributePlugin.getInstance();
            ItemStack[] battleItems = plugin.getBattleInventoryManager().getAllBattleItems(player);
            if (battleItems != null) {
                for (ItemStack item : battleItems) {
                    if (item != null && getItemType(item).equals("饰品")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // 静默处理异常
        }
        return false;
    }
    
    /**
     * 获取物品类型
     */
    private String getItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return "";
        }
        
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line).trim();
            if (cleanLine.startsWith("类型: ")) {
                return cleanLine.substring(4).trim();
            }
        }
        return "";
    }
    
    /**
     * 创建状态显示物品
     */
    private ItemStack createStatusItem(Material material, String name, String description, boolean active) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', description));
        lore.add("");
        if (active) {
            lore.add(ChatColor.GREEN + "状态: 生效中");
        } else {
            lore.add(ChatColor.RED + "状态: 未生效");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 设置基础属性显示
     */
    private void setupBasicAttributes(Inventory inventory, Player player) {
        Map<String, Double> attributes = LoreAttributePlugin.getInstance().getAttributeManager().getCachedPlayerAttributes(player);
        
        // 生命值
        ItemStack healthItem = createAttributeItem(Material.GOLDEN_APPLE, 
            "&c生命值", "&7当前生命值加成", attributes.getOrDefault("health", 0.0));
        inventory.setItem(10, healthItem);
        
        // 攻击伤害
        ItemStack damageItem = createAttributeItem(Material.DIAMOND_SWORD, 
            "&e攻击伤害", "&7当前攻击伤害加成", attributes.getOrDefault("damage", 0.0));
        inventory.setItem(11, damageItem);
        
        // 移动速度
        ItemStack speedItem = createAttributeItem(Material.FEATHER, 
            "&b移动速度", "&7当前移动速度加成", attributes.getOrDefault("moveSpeed", 0.0));
        inventory.setItem(12, speedItem);
        
        // 伤害减免
        ItemStack armorItem = createAttributeItem(Material.DIAMOND_CHESTPLATE, 
            "&9伤害减免", "&7当前伤害减免加成", attributes.getOrDefault("armor", 0.0));
        inventory.setItem(13, armorItem);
    }
    
    /**
     * 设置战斗属性显示
     */
    private void setupCombatAttributes(Inventory inventory, Player player) {
        Map<String, Double> attributes = LoreAttributePlugin.getInstance().getAttributeManager().getCachedPlayerAttributes(player);
        
        // 致命几率
        ItemStack critItem = createAttributeItem(Material.NETHER_STAR, 
            "&6致命几率", "&7当前暴击几率", attributes.getOrDefault("crit", 0.0));
        inventory.setItem(19, critItem);
        
        // 致命伤害
        ItemStack critDamageItem = createAttributeItem(Material.BLAZE_POWDER, 
            "&c致命伤害", "&7当前暴击伤害加成", attributes.getOrDefault("critDamage", 0.0));
        inventory.setItem(20, critDamageItem);
        
        // 闪避几率
        ItemStack dodgeItem = createAttributeItem(Material.RAW_BEEF,
            "&a闪避几率", "&7当前闪避几率", attributes.getOrDefault("dodge", 0.0));
        inventory.setItem(21, dodgeItem);
        
        // 格挡几率
        ItemStack blockItem = createAttributeItem(Material.DIRT,
            "&8格挡几率", "&7当前格挡几率", attributes.getOrDefault("block", 0.0));
        inventory.setItem(23, blockItem);
    }
    
    /**
     * 设置特殊属性显示
     */
    private void setupSpecialAttributes(Inventory inventory, Player player) {
        Map<String, Double> attributes = LoreAttributePlugin.getInstance().getAttributeManager().getCachedPlayerAttributes(player);
        
        // 生命偷取
        ItemStack lifeStealItem = createAttributeItem(Material.GHAST_TEAR, 
            "&d生命偷取", "&7攻击时恢复生命值", attributes.getOrDefault("lifeSteal", 0.0));
        inventory.setItem(28, lifeStealItem);
        
        // 真实伤害
        ItemStack trueDamageItem = createAttributeItem(Material.ARROW, 
            "&4真实伤害", "&7无视护甲的伤害", attributes.getOrDefault("trueDamage", 0.0));
        inventory.setItem(29, trueDamageItem);
        
        // 护甲穿透
        ItemStack armorBreakItem = createAttributeItem(Material.GOLD_PICKAXE, 
            "&e护甲穿透", "&7穿透敌人护甲", attributes.getOrDefault("penetration", 0.0));
        inventory.setItem(30, armorBreakItem);
        
        // 生命恢复
        ItemStack healthHealItem = createAttributeItem(Material.GOLDEN_CARROT, 
            "&2生命恢复", "&7持续恢复生命值", attributes.getOrDefault("healthHeal", 0.0));
        inventory.setItem(31, healthHealItem);
    }
    
    /**
     * 设置操作按钮
     */
    private void setupActionButtons(Inventory inventory, Player player) {
        ItemStack refreshItem = createActionItem(Material.EMERALD, 
            "&a刷新属性", "&7点击刷新当前属性数据");
        inventory.setItem(40, refreshItem);
        
        ItemStack battleInvItem = createActionItem(Material.CHEST, 
            "&c战斗背包", "&7点击打开战斗背包");
        inventory.setItem(41, battleInvItem);
        
        ItemStack helpItem = createActionItem(Material.BOOK, 
            "&e帮助", "&7查看插件使用帮助");
        inventory.setItem(42, helpItem);
        
        ItemStack closeItem = createActionItem(Material.REDSTONE_BLOCK,
            "&4关闭", "&7关闭此界面");
        inventory.setItem(43, closeItem);
    }
    
    /**
     * 创建属性显示物品
     */
    private ItemStack createAttributeItem(Material material, String name, String description, double value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', description));
        lore.add("");
        lore.add(ChatColor.GRAY + "基础值: " + ChatColor.WHITE + "0");
        lore.add(ChatColor.GRAY + "装备加成: " + ChatColor.GREEN + "+" + formatValue(value));
        lore.add(ChatColor.GRAY + "战斗背包: " + ChatColor.AQUA + "+0");
        lore.add(ChatColor.GRAY + "总计: " + ChatColor.YELLOW + formatValue(value));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "点击查看详细信息");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 格式化属性值显示
     */
    private String formatValue(double value) {
        if (value == 0) {
            return "0";
        } else if (value % 1 == 0) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.1f", value);
        }
    }
    
    /**
     * 创建操作按钮物品
     */
    private ItemStack createActionItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', description));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
} 