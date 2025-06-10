package dev.charlieveg.loreattribute.api;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import dev.charlieveg.loreattribute.manager.AttributeManager;
import dev.charlieveg.loreattribute.manager.BattleInventoryManager;
import dev.charlieveg.loreattribute.manager.EquipmentRestrictionManager;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * LoreAttribute插件API
 * 提供所有外部插件可访问的功能接口
 * 
 * @author charlieveg
 * @version 1.0
 */
public class LoreAttributeAPI {
    
    private static LoreAttributePlugin plugin;
    
    /**
     * 初始化API（由插件内部调用）
     */
    public static void initialize(LoreAttributePlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * 检查API是否已初始化
     */
    private static void checkInitialized() {
        if (plugin == null) {
            throw new IllegalStateException("LoreAttributeAPI未初始化！请确保LoreAttribute插件已正确加载。");
        }
    }
    
    // ========== 玩家属性相关API ==========
    
    /**
     * 获取玩家的所有属性
     * 
     * @param player 玩家对象
     * @return 属性映射表，键为属性名，值为属性值
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static Map<String, Double> getPlayerAttributes(@NonNull Player player) {
        checkInitialized();
        return new HashMap<>(plugin.getAttributeManager().getCachedPlayerAttributes(player));
    }
    
    /**
     * 获取玩家指定属性的值
     * 
     * @param player 玩家对象
     * @param attributeName 属性名称（支持中文名或英文键）
     * @return 属性值，如果不存在则返回0.0
     * @throws IllegalArgumentException 如果参数为null
     */
    public static double getPlayerAttribute(@NonNull Player player, @NonNull String attributeName) {
        checkInitialized();
        return plugin.getAttributeManager().getPlayerAttribute(player, attributeName);
    }
    
    /**
     * 强制更新玩家属性缓存
     * 当玩家装备发生变化时可以调用此方法立即更新属性
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void updatePlayerAttributes(@NonNull Player player) {
        checkInitialized();
        plugin.getAttributeManager().updatePlayerAttributes(player);
    }
    
    /**
     * 清除玩家属性缓存
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void clearPlayerAttributes(@NonNull Player player) {
        checkInitialized();
        plugin.getAttributeManager().clearPlayerAttributes(player);
    }
    
    /**
     * 重新计算并获取玩家最新属性
     * 
     * @param player 玩家对象
     * @return 最新的属性映射表
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static Map<String, Double> calculatePlayerAttributes(@NonNull Player player) {
        checkInitialized();
        return plugin.getAttributeManager().calculatePlayerAttributes(player);
    }
    
    // ========== 物品属性相关API ==========
    
    /**
     * 解析物品的属性
     * 
     * @param item 物品对象
     * @return 属性映射表，键为属性名，值为属性值
     * @throws IllegalArgumentException 如果物品为null
     */
    public static Map<String, Double> parseItemAttributes(@NonNull ItemStack item) {
        checkInitialized();
        return plugin.getAttributeManager().parseItemAttributes(item);
    }
    
    /**
     * 为物品添加属性
     * 
     * @param item 物品对象
     * @param attributeName 属性名称（中文）
     * @param value 属性值
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static ItemStack addItemAttribute(@NonNull ItemStack item, @NonNull String attributeName, double value) {
        checkInitialized();
        // 注意：此方法需要通过命令执行器实现，建议直接修改lore
        // 这里提供一个简化的实现
        return modifyItemLore(item, attributeName, value, false);
    }
    
    /**
     * 设置物品的属性值（覆盖已有值）
     * 
     * @param item 物品对象
     * @param attributeName 属性名称（中文）
     * @param value 属性值
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static ItemStack setItemAttribute(@NonNull ItemStack item, @NonNull String attributeName, double value) {
        checkInitialized();
        // 注意：此方法需要通过命令执行器实现，建议直接修改lore
        // 这里提供一个简化的实现
        return modifyItemLore(item, attributeName, value, true);
    }
    
    /**
     * 设置物品类型
     * 
     * @param item 物品对象
     * @param itemType 物品类型（"武器"、"防具"、"饰品"）
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static ItemStack setItemType(@NonNull ItemStack item, @NonNull String itemType) {
        checkInitialized();
        if (!java.util.Arrays.asList("武器", "防具", "饰品").contains(itemType)) {
            throw new IllegalArgumentException("无效的物品类型: " + itemType);
        }
        
        ItemStack newItem = item.clone();
        if (!newItem.hasItemMeta()) {
            newItem.setItemMeta(org.bukkit.Bukkit.getItemFactory().getItemMeta(newItem.getType()));
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = newItem.getItemMeta();
        List<String> lore = meta.hasLore() ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<>();
        
        // 移除已有的类型信息
        lore.removeIf(line -> org.bukkit.ChatColor.stripColor(line).trim().startsWith("类型: "));
        
        // 添加新的类型信息
        lore.add(org.bukkit.ChatColor.GRAY + "类型: " + org.bukkit.ChatColor.YELLOW + itemType);
        
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
    
    /**
     * 修改物品lore（内部辅助方法）
     */
    private static ItemStack modifyItemLore(ItemStack item, String attributeName, double value, boolean replace) {
        ItemStack newItem = item.clone();
        if (!newItem.hasItemMeta()) {
            newItem.setItemMeta(org.bukkit.Bukkit.getItemFactory().getItemMeta(newItem.getType()));
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = newItem.getItemMeta();
        List<String> lore = meta.hasLore() ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<>();
        
        String attributeLine = org.bukkit.ChatColor.GRAY + attributeName + ": " + 
                              org.bukkit.ChatColor.GREEN + "+" + String.format("%.1f", value);
        
        if (replace) {
            // 移除已有的同名属性
            lore.removeIf(line -> org.bukkit.ChatColor.stripColor(line).trim().startsWith(attributeName + ":"));
        }
        
        lore.add(attributeLine);
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
    
    /**
     * 获取物品的类型
     * 
     * @param item 物品对象
     * @return 物品类型字符串，如果未设置则返回空字符串
     * @throws IllegalArgumentException 如果物品为null
     */
    public static String getItemType(@NonNull ItemStack item) {
        checkInitialized();
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return "";
        }
        
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            String cleanLine = org.bukkit.ChatColor.stripColor(line).trim();
            if (cleanLine.startsWith("类型: ")) {
                return cleanLine.substring(4).trim();
            }
        }
        return "";
    }
    
    /**
     * 检查属性名称是否有效
     * 
     * @param attributeName 属性名称
     * @return 是否为有效的属性名称
     * @throws IllegalArgumentException 如果属性名称为null
     */
    public static boolean isValidAttributeName(@NonNull String attributeName) {
        checkInitialized();
        return plugin.getAttributeManager().isValidAttributeName(attributeName);
    }
    
    /**
     * 获取属性的显示名称
     * 
     * @param attributeKey 属性键
     * @return 属性的显示名称
     * @throws IllegalArgumentException 如果属性键为null
     */
    public static String getAttributeDisplayName(@NonNull String attributeKey) {
        checkInitialized();
        return plugin.getAttributeManager().getAttributeDisplayName(attributeKey);
    }
    
    // ========== 战斗背包相关API ==========
    
    /**
     * 获取玩家战斗背包中的所有物品
     * 
     * @param player 玩家对象
     * @return 物品数组，包含5个战斗背包槽位的物品
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static ItemStack[] getBattleInventoryItems(@NonNull Player player) {
        checkInitialized();
        ItemStack[] items = plugin.getBattleInventoryManager().getAllBattleItems(player);
        return items != null ? items : new ItemStack[5];
    }
    
    /**
     * 设置玩家战斗背包中指定位置的物品
     * 
     * @param player 玩家对象
     * @param slot 槽位（1-5，对应战斗背包的5个槽位）
     * @param item 物品对象，null表示清空该槽位
     * @return 操作是否成功
     * @throws IllegalArgumentException 如果玩家为null或槽位无效
     */
    public static boolean setBattleInventoryItem(@NonNull Player player, int slot, ItemStack item) {
        checkInitialized();
        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException("战斗背包槽位必须在1-5之间");
        }
        
        return plugin.getBattleInventoryManager().setBattleItem(player, slot, item);
    }
    
    /**
     * 获取玩家战斗背包中指定位置的物品
     * 
     * @param player 玩家对象
     * @param slot 槽位（1-5，对应战斗背包的5个槽位）
     * @return 物品对象，如果槽位为空则返回null
     * @throws IllegalArgumentException 如果玩家为null或槽位无效
     */
    public static ItemStack getBattleInventoryItem(@NonNull Player player, int slot) {
        checkInitialized();
        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException("战斗背包槽位必须在1-5之间");
        }
        
        return plugin.getBattleInventoryManager().getBattleItem(player, slot);
    }
    
    /**
     * 清空玩家的战斗背包
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void clearBattleInventory(@NonNull Player player) {
        checkInitialized();
        plugin.getBattleInventoryManager().clearBattleInventory(player);
    }
    
    /**
     * 打开玩家的战斗背包界面
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void openBattleInventory(@NonNull Player player) {
        checkInitialized();
        plugin.getBattleInventoryManager().openBattleInventory(player);
    }
    
    /**
     * 检查玩家是否拥有战斗背包
     * 
     * @param player 玩家对象
     * @return 是否拥有战斗背包
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static boolean hasBattleInventory(@NonNull Player player) {
        checkInitialized();
        return plugin.getBattleInventoryManager().hasBattleInventory(player);
    }
    
    /**
     * 获取玩家战斗背包中的物品数量
     * 
     * @param player 玩家对象
     * @return 战斗背包中的物品数量
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static int getBattleItemCount(@NonNull Player player) {
        checkInitialized();
        return plugin.getBattleInventoryManager().getBattleItemCount(player);
    }
    
    /**
     * 移除玩家的战斗背包
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void removeBattleInventory(@NonNull Player player) {
        checkInitialized();
        plugin.getBattleInventoryManager().removeBattleInventory(player);
    }
    
    /**
     * 检查物品是否可以放入指定战斗背包槽位
     * 
     * @param slot 槽位（1-5）
     * @param item 物品对象
     * @return 是否可以放入
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean canPlaceItemInBattleSlot(int slot, @NonNull ItemStack item) {
        checkInitialized();
        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException("战斗背包槽位必须在1-5之间");
        }
        
        // 将1-5槽位转换为0-4索引，然后转换为实际槽位
        int actualSlot = BattleInventoryManager.BATTLE_SLOTS[slot - 1];
        return plugin.getBattleInventoryManager().canPlaceItem(actualSlot, item);
    }
    
    /**
     * 创建战斗背包物品
     * 
     * @return 战斗背包物品
     */
    public static ItemStack createBattleInventoryItem() {
        checkInitialized();
        return plugin.getBattleInventoryManager().createBattleInventoryItem();
    }
    
    /**
     * 获取玩家的战斗背包界面对象
     * 
     * @param player 玩家对象
     * @return 战斗背包界面
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static Inventory getBattleInventory(@NonNull Player player) {
        checkInitialized();
        return plugin.getBattleInventoryManager().getBattleInventory(player);
    }
    
    // ========== 装备限制相关API ==========
    
    /**
     * 获取玩家的装备限制信息
     * 注意：此功能可能需要根据实际实现调整
     * 
     * @param player 玩家对象
     * @return 装备限制映射表
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static Map<String, Object> getPlayerRestrictions(@NonNull Player player) {
        checkInitialized();
        // 返回空的映射表，实际实现可能需要调整
        return new HashMap<>();
    }
    
    /**
     * 设置玩家的装备限制
     * 注意：此功能可能需要根据实际实现调整
     * 
     * @param player 玩家对象
     * @param restrictionType 限制类型
     * @param value 限制值
     * @return 操作是否成功
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean setPlayerRestriction(@NonNull Player player, @NonNull String restrictionType, Object value) {
        checkInitialized();
        // 暂时返回false，实际实现可能需要调整
        return false;
    }
    
    /**
     * 移除玩家的装备限制
     * 注意：此功能可能需要根据实际实现调整
     * 
     * @param player 玩家对象
     * @param restrictionType 限制类型
     * @return 操作是否成功
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean removePlayerRestriction(@NonNull Player player, @NonNull String restrictionType) {
        checkInitialized();
        // 暂时返回false，实际实现可能需要调整
        return false;
    }
    
    // ========== 属性效果相关API ==========
    
    /**
     * 检查玩家是否正在处理AOE伤害（用于避免递归）
     * 
     * @return 是否正在处理AOE伤害
     */
    public static boolean isProcessingAoeDamage() {
        checkInitialized();
        return dev.charlieveg.loreattribute.listener.CombatListener.isCurrentlyProcessingAoe();
    }
    
    /**
     * 手动触发玩家属性效果计算
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void applyAttributeEffects(@NonNull Player player) {
        checkInitialized();
        updatePlayerAttributes(player);
    }
    
    // ========== 配置相关API ==========
    
    /**
     * 获取配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     * @throws IllegalArgumentException 如果路径为null
     */
    public static Object getConfigValue(@NonNull String path, Object defaultValue) {
        checkInitialized();
        // 使用Plugin的getConfig()方法获取配置值
        return plugin.getConfig().get(path, defaultValue);
    }
    
    /**
     * 获取字符串配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 字符串配置值
     * @throws IllegalArgumentException 如果路径为null
     */
    public static String getConfigString(@NonNull String path, String defaultValue) {
        checkInitialized();
        return plugin.getConfigManager().getString(path, defaultValue);
    }
    
    /**
     * 获取布尔配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 布尔配置值
     * @throws IllegalArgumentException 如果路径为null
     */
    public static boolean getConfigBoolean(@NonNull String path, boolean defaultValue) {
        checkInitialized();
        return plugin.getConfigManager().getBoolean(path, defaultValue);
    }
    
    /**
     * 获取整数配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 整数配置值
     * @throws IllegalArgumentException 如果路径为null
     */
    public static int getConfigInt(@NonNull String path, int defaultValue) {
        checkInitialized();
        return plugin.getConfigManager().getInt(path, defaultValue);
    }
    
    /**
     * 获取双精度配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 双精度配置值
     * @throws IllegalArgumentException 如果路径为null
     */
    public static double getConfigDouble(@NonNull String path, double defaultValue) {
        checkInitialized();
        return plugin.getConfigManager().getDouble(path, defaultValue);
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 获取插件实例（用于高级用法）
     * 
     * @return LoreAttributePlugin实例
     * @throws IllegalStateException 如果API未初始化
     */
    public static LoreAttributePlugin getPlugin() {
        checkInitialized();
        return plugin;
    }
    
    /**
     * 获取API版本
     * 
     * @return API版本字符串
     */
    public static String getAPIVersion() {
        return "1.0";
    }
    
    /**
     * 检查插件是否启用
     * 
     * @return 插件是否启用
     */
    public static boolean isPluginEnabled() {
        return plugin != null && plugin.isEnabled();
    }
    
    /**
     * 获取所有支持的属性名称
     * 
     * @return 属性名称集合
     */
    public static Set<String> getSupportedAttributes() {
        checkInitialized();
        // 返回硬编码的属性名称集合
        return new java.util.HashSet<>(java.util.Arrays.asList(
            "攻击伤害", "致命几率", "致命伤害", "生命偷取", "真实伤害", "护甲穿透", "弱化几率", "范围伤害", "范围距离",
            "生命值", "伤害减免", "真实抗性", "闪避几率", "格挡几率", "生命恢复", "反伤几率",
            "移动速度"
        ));
    }
    
    /**
     * 获取所有支持的物品类型
     * 
     * @return 物品类型数组
     */
    public static String[] getSupportedItemTypes() {
        return new String[]{"武器", "防具", "饰品"};
    }
    
    // ========== 高级功能API ==========
    
    /**
     * 获取指定类型物品支持的属性
     * 
     * @param itemType 物品类型（"武器"、"防具"、"饰品"）
     * @return 支持的属性列表
     * @throws IllegalArgumentException 如果物品类型无效
     */
    public static List<String> getAttributesForItemType(@NonNull String itemType) {
        checkInitialized();
        
        switch (itemType) {
            case "武器":
                return Arrays.asList("攻击伤害", "致命几率", "致命伤害", "生命偷取", "真实伤害", 
                                   "护甲穿透", "弱化几率", "范围伤害", "范围距离");
            case "防具":
                return Arrays.asList("生命值", "伤害减免", "真实抗性", "闪避几率", "格挡几率", "生命恢复", "反伤几率");
            case "饰品":
                return Arrays.asList("移动速度", "致命几率", "生命值", "生命恢复", "攻击伤害");
            default:
                throw new IllegalArgumentException("无效的物品类型: " + itemType);
        }
    }
    
    /**
     * 检查属性是否适合指定类型的物品
     * 
     * @param itemType 物品类型
     * @param attributeName 属性名称
     * @return 是否适合
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean isValidAttributeForItemType(@NonNull String itemType, @NonNull String attributeName) {
        checkInitialized();
        return getAttributesForItemType(itemType).contains(attributeName);
    }
    
    /**
     * 检查物品是否适合指定属性
     * 
     * @param item 物品对象
     * @param attributeName 属性名称
     * @return 是否适合
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean isValidAttributeForItem(@NonNull ItemStack item, @NonNull String attributeName) {
        checkInitialized();
        String itemType = getItemType(item);
        if (itemType.isEmpty()) {
            return false; // 没有设置类型的物品不支持属性
        }
        return isValidAttributeForItemType(itemType, attributeName);
    }
    
    /**
     * 获取只读属性列表
     * 
     * @return 只读属性列表
     */
    public static List<String> getReadOnlyAttributes() {
        return Arrays.asList("总伤害");
    }
    
    /**
     * 检查属性是否为只读
     * 
     * @param attributeName 属性名称
     * @return 是否为只读属性
     * @throws IllegalArgumentException 如果属性名称为null
     */
    public static boolean isReadOnlyAttribute(@NonNull String attributeName) {
        return getReadOnlyAttributes().contains(attributeName);
    }
    
    /**
     * 移除物品的指定属性
     * 
     * @param item 物品对象
     * @param attributeName 属性名称
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static ItemStack removeItemAttribute(@NonNull ItemStack item, @NonNull String attributeName) {
        checkInitialized();
        ItemStack newItem = item.clone();
        if (!newItem.hasItemMeta() || !newItem.getItemMeta().hasLore()) {
            return newItem;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = newItem.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        
        // 移除指定属性
        lore.removeIf(line -> org.bukkit.ChatColor.stripColor(line).trim().startsWith(attributeName + ":"));
        
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
    
    /**
     * 移除物品的所有属性
     * 
     * @param item 物品对象
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果物品为null
     */
    public static ItemStack removeAllItemAttributes(@NonNull ItemStack item) {
        checkInitialized();
        ItemStack newItem = item.clone();
        if (!newItem.hasItemMeta() || !newItem.getItemMeta().hasLore()) {
            return newItem;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = newItem.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        
        // 移除所有属性行，但保留类型信息和其他lore
        Set<String> allAttributes = getSupportedAttributes();
        lore.removeIf(line -> {
            String cleanLine = org.bukkit.ChatColor.stripColor(line).trim();
            return allAttributes.stream().anyMatch(attr -> cleanLine.startsWith(attr + ":"));
        });
        
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
    
    /**
     * 移除物品类型
     * 
     * @param item 物品对象
     * @return 修改后的物品（新对象）
     * @throws IllegalArgumentException 如果物品为null
     */
    public static ItemStack removeItemType(@NonNull ItemStack item) {
        checkInitialized();
        ItemStack newItem = item.clone();
        if (!newItem.hasItemMeta() || !newItem.getItemMeta().hasLore()) {
            return newItem;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = newItem.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        
        // 移除类型信息
        lore.removeIf(line -> org.bukkit.ChatColor.stripColor(line).trim().startsWith("类型: "));
        
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
    
    /**
     * 检查物品是否有任何属性
     * 
     * @param item 物品对象
     * @return 是否有属性
     * @throws IllegalArgumentException 如果物品为null
     */
    public static boolean hasAnyAttributes(@NonNull ItemStack item) {
        checkInitialized();
        Map<String, Double> attributes = parseItemAttributes(item);
        return !attributes.isEmpty();
    }
    
    /**
     * 检查物品是否有指定属性
     * 
     * @param item 物品对象
     * @param attributeName 属性名称
     * @return 是否有该属性
     * @throws IllegalArgumentException 如果参数无效
     */
    public static boolean hasAttribute(@NonNull ItemStack item, @NonNull String attributeName) {
        checkInitialized();
        Map<String, Double> attributes = parseItemAttributes(item);
        return attributes.containsKey(getAttributeKey(attributeName));
    }
    
    /**
     * 获取物品指定属性的值
     * 
     * @param item 物品对象
     * @param attributeName 属性名称
     * @return 属性值，如果不存在则返回0.0
     * @throws IllegalArgumentException 如果参数无效
     */
    public static double getItemAttributeValue(@NonNull ItemStack item, @NonNull String attributeName) {
        checkInitialized();
        Map<String, Double> attributes = parseItemAttributes(item);
        String key = getAttributeKey(attributeName);
        return attributes.getOrDefault(key, 0.0);
    }
    
    /**
     * 获取属性对应的内部键名
     * 
     * @param attributeName 属性名称
     * @return 内部键名
     * @throws IllegalArgumentException 如果属性名称无效
     */
    private static String getAttributeKey(@NonNull String attributeName) {
        // 属性名称到键的映射
        Map<String, String> attributeKeyMap = new HashMap<>();
        attributeKeyMap.put("攻击伤害", "damage");
        attributeKeyMap.put("致命几率", "crit");
        attributeKeyMap.put("致命伤害", "critDamage");
        attributeKeyMap.put("生命偷取", "lifeSteal");
        attributeKeyMap.put("真实伤害", "trueDamage");
        attributeKeyMap.put("护甲穿透", "penetration");
        attributeKeyMap.put("弱化几率", "weaken");
        attributeKeyMap.put("范围伤害", "aoeDamage");
        attributeKeyMap.put("范围距离", "aoeRange");
        attributeKeyMap.put("生命值", "health");
        attributeKeyMap.put("伤害减免", "armor");
        attributeKeyMap.put("真实抗性", "trueArmor");
        attributeKeyMap.put("闪避几率", "dodge");
        attributeKeyMap.put("格挡几率", "block");
        attributeKeyMap.put("生命恢复", "regen");
        attributeKeyMap.put("反伤几率", "injury");
        attributeKeyMap.put("移动速度", "speed");
        attributeKeyMap.put("总伤害", "sumDamage");
        
        return attributeKeyMap.getOrDefault(attributeName, attributeName.toLowerCase());
    }
    
    // ========== 调试和信息API ==========
    
    /**
     * 获取玩家属性的详细调试信息
     * 
     * @param player 玩家对象
     * @return 调试信息字符串
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static String getPlayerAttributeDebugInfo(@NonNull Player player) {
        checkInitialized();
        StringBuilder info = new StringBuilder();
        
        info.append("=== 玩家 ").append(player.getName()).append(" 属性调试信息 ===\n");
        
        Map<String, Double> attributes = getPlayerAttributes(player);
        if (attributes.isEmpty()) {
            info.append("该玩家没有任何属性加成\n");
        } else {
            info.append("当前生效的属性:\n");
            attributes.forEach((key, value) -> 
                info.append("  ").append(getAttributeDisplayName(key))
                    .append(": ").append(String.format("%.2f", value)).append("\n"));
        }
        
        // 装备状态
        info.append("\n装备状态:\n");
        ItemStack mainHand = player.getInventory().getItemInHand();
        info.append("  主手: ").append(mainHand != null ? mainHand.getType() : "空")
            .append(" (类型: ").append(getItemType(mainHand != null ? mainHand : new ItemStack(Material.AIR))).append(")\n");
        
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            String slotName = new String[]{"靴子", "护腿", "胸甲", "头盔"}[i];
            info.append("  ").append(slotName).append(": ")
                .append(armor[i] != null ? armor[i].getType() : "空")
                .append(" (类型: ").append(getItemType(armor[i] != null ? armor[i] : new ItemStack(Material.AIR))).append(")\n");
        }
        
        info.append("  战斗背包物品数: ").append(getBattleItemCount(player)).append("\n");
        
        return info.toString();
    }
    
    /**
     * 获取物品属性的详细调试信息
     * 
     * @param item 物品对象
     * @return 调试信息字符串
     * @throws IllegalArgumentException 如果物品为null
     */
    public static String getItemAttributeDebugInfo(@NonNull ItemStack item) {
        checkInitialized();
        StringBuilder info = new StringBuilder();
        
        info.append("=== 物品属性调试信息 ===\n");
        info.append("物品: ").append(item.getType()).append("\n");
        info.append("类型: ").append(getItemType(item)).append("\n");
        
        Map<String, Double> attributes = parseItemAttributes(item);
        if (attributes.isEmpty()) {
            info.append("该物品没有任何属性\n");
        } else {
            info.append("物品属性:\n");
            attributes.forEach((key, value) -> 
                info.append("  ").append(getAttributeDisplayName(key))
                    .append(": ").append(String.format("%.2f", value)).append("\n"));
        }
        
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            info.append("\n原始Lore:\n");
            List<String> lore = item.getItemMeta().getLore();
            for (int i = 0; i < lore.size(); i++) {
                info.append("  [").append(i).append("] ").append(lore.get(i)).append("\n");
            }
        }
        
        return info.toString();
    }
    
    /**
     * 打开属性查看界面
     * 
     * @param player 玩家对象
     * @throws IllegalArgumentException 如果玩家为null
     */
    public static void openAttributeViewer(@NonNull Player player) {
        checkInitialized();
        plugin.getAttributeViewerUI().openAttributeViewer(player);
    }
    
    /**
     * 保存所有玩家的战斗背包数据
     */
    public static void saveAllBattleInventories() {
        checkInitialized();
        plugin.getBattleInventoryManager().saveAllBattleInventories();
    }
    
    /**
     * 重新加载插件配置
     */
    public static void reloadConfig() {
        checkInitialized();
        plugin.reloadConfig();
        // 可以添加更多重载逻辑
    }
    
    /**
     * 获取插件数据文件夹路径
     * 
     * @return 数据文件夹路径
     */
    public static String getDataFolderPath() {
        checkInitialized();
        return plugin.getDataFolder().getAbsolutePath();
    }
    
    /**
     * 获取在线玩家的属性统计信息
     * 
     * @return 统计信息映射表
     */
    public static Map<String, Object> getOnlinePlayersStats() {
        checkInitialized();
        Map<String, Object> stats = new HashMap<>();
        
        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
        stats.put("onlinePlayerCount", onlinePlayers.size());
        
        int playersWithAttributes = 0;
        int totalBattleItems = 0;
        
        for (Player player : onlinePlayers) {
            Map<String, Double> attributes = getPlayerAttributes(player);
            if (!attributes.isEmpty()) {
                playersWithAttributes++;
            }
            totalBattleItems += getBattleItemCount(player);
        }
        
        stats.put("playersWithAttributes", playersWithAttributes);
        stats.put("totalBattleItems", totalBattleItems);
        stats.put("averageBattleItemsPerPlayer", 
                 onlinePlayers.size() > 0 ? (double) totalBattleItems / onlinePlayers.size() : 0.0);
        
        return stats;
    }
} 