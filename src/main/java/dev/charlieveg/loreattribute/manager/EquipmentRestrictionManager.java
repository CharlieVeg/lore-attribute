package dev.charlieveg.loreattribute.manager;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * 装备限制管理器
 * 管理三种类型的装备限制：冲突限制、专属限制、前置限制
 */
@Getter
public class EquipmentRestrictionManager {
    
    private final File pluginFolder;
    private FileConfiguration restrictionConfig;
    
    // 冲突限制组 - 同组内的lore互相冲突
    private Map<String, Set<String>> conflictGroups = new HashMap<>();
    
    // 专属限制 - 穿戴特定lore后只能使用该lore的物品
    private Set<String> exclusiveGroups = new HashSet<>();
    
    // 前置限制 - 需要穿戴特定lore才能使用某些武器
    private Set<String> prerequisiteGroups = new HashSet<>();
    
    public EquipmentRestrictionManager(File pluginFolder) {
        this.pluginFolder = pluginFolder;
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        File configFile = new File(pluginFolder, "equipment-restrictions.yml");
        
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        
        restrictionConfig = YamlConfiguration.loadConfiguration(configFile);
        loadRestrictions();
    }
    
    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig(File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            
            // 冲突限制示例
            config.set("conflict-restrictions.group1", Arrays.asList("A型装备", "B型武器", "B型装备"));
            config.set("conflict-restrictions.group2", Arrays.asList("猎人", "冲锋枪", "手枪"));
            config.set("conflict-restrictions.group3", Arrays.asList("A", "B", "C"));
            
            // 专属限制示例
            config.set("exclusive-restrictions", Arrays.asList("钢铁侠", "蜘蛛侠"));
            
            // 前置限制示例
            config.set("prerequisite-restrictions", Arrays.asList("超能"));
            
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载限制规则
     */
    private void loadRestrictions() {
        conflictGroups.clear();
        exclusiveGroups.clear();
        prerequisiteGroups.clear();
        
        // 加载冲突限制
        ConfigurationSection conflictSection = restrictionConfig.getConfigurationSection("conflict-restrictions");
        if (conflictSection != null) {
            for (String groupName : conflictSection.getKeys(false)) {
                List<String> lores = conflictSection.getStringList(groupName);
                Set<String> loreSet = new HashSet<>(lores);
                conflictGroups.put(groupName, loreSet);
            }
        }
        
        // 加载专属限制
        List<String> exclusiveList = restrictionConfig.getStringList("exclusive-restrictions");
        exclusiveGroups.addAll(exclusiveList);
        
        // 加载前置限制
        List<String> prerequisiteList = restrictionConfig.getStringList("prerequisite-restrictions");
        prerequisiteGroups.addAll(prerequisiteList);
    }
    
    /**
     * 从物品中提取lore文本（去除颜色代码）
     */
    private Set<String> extractLoreTexts(ItemStack item) {
        Set<String> loreTexts = new HashSet<>();
        
        if (item == null || !item.hasItemMeta()) {
            return loreTexts;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            for (String lore : meta.getLore()) {
                String cleanLore = lore.replaceAll("§[0-9a-fk-or]", "").trim();
                loreTexts.add(cleanLore);
            }
        }
        
        return loreTexts;
    }
    
    /**
     * 检查物品是否包含指定的lore
     */
    public boolean hasLore(ItemStack item, String targetLore) {
        Set<String> loreTexts = extractLoreTexts(item);
        return loreTexts.stream().anyMatch(lore -> lore.contains(targetLore));
    }
    
    /**
     * 检查冲突限制
     * @param equippedItems 已装备的物品
     * @param targetItem 要检查的目标物品
     * @return 是否存在冲突
     */
    public boolean hasConflictRestriction(List<ItemStack> equippedItems, ItemStack targetItem) {
        Set<String> targetLores = extractLoreTexts(targetItem);
        
        for (Map.Entry<String, Set<String>> group : conflictGroups.entrySet()) {
            Set<String> groupLores = group.getValue();
            
            // 检查目标物品是否属于某个冲突组
            boolean targetInGroup = targetLores.stream().anyMatch(lore -> 
                groupLores.stream().anyMatch(lore::contains));
            
            if (targetInGroup) {
                // 检查已装备物品是否有同组的其他lore
                for (ItemStack equipped : equippedItems) {
                    if (equipped == null || equipped.equals(targetItem)) continue;
                    
                    Set<String> equippedLores = extractLoreTexts(equipped);
                    boolean equippedInGroup = equippedLores.stream().anyMatch(lore -> 
                        groupLores.stream().anyMatch(lore::contains));
                    
                    if (equippedInGroup) {
                        return true; // 发现冲突
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查专属限制
     * @param equippedItems 已装备的物品
     * @param targetItem 要检查的目标物品
     * @return 是否违反专属限制
     */
    public boolean hasExclusiveRestriction(List<ItemStack> equippedItems, ItemStack targetItem) {
        Set<String> targetLores = extractLoreTexts(targetItem);
        
        // 检查已装备物品中是否有专属lore
        Set<String> equippedExclusiveLores = new HashSet<>();
        for (ItemStack equipped : equippedItems) {
            if (equipped == null || equipped.equals(targetItem)) continue;
            
            Set<String> equippedLores = extractLoreTexts(equipped);
            for (String exclusiveLore : exclusiveGroups) {
                if (equippedLores.stream().anyMatch(lore -> lore.contains(exclusiveLore))) {
                    equippedExclusiveLores.add(exclusiveLore);
                }
            }
        }
        
        // 如果没有装备专属物品，则无限制
        if (equippedExclusiveLores.isEmpty()) {
            return false;
        }
        
        // 检查目标物品是否符合已装备的专属要求
        for (String exclusiveLore : equippedExclusiveLores) {
            boolean targetHasExclusiveLore = targetLores.stream()
                .anyMatch(lore -> lore.contains(exclusiveLore));
            
            if (!targetHasExclusiveLore) {
                return true; // 违反专属限制
            }
        }
        
        return false;
    }
    
    /**
     * 检查前置限制
     * @param equippedItems 已装备的物品
     * @param targetItem 要检查的目标物品
     * @return 是否违反前置限制
     */
    public boolean hasPrerequisiteRestriction(List<ItemStack> equippedItems, ItemStack targetItem) {
        Set<String> targetLores = extractLoreTexts(targetItem);
        
        // 检查目标物品是否包含前置限制的lore
        for (String prerequisiteLore : prerequisiteGroups) {
            if (targetLores.stream().anyMatch(lore -> lore.contains(prerequisiteLore))) {
                // 目标物品包含前置lore，检查是否已有相应的前置装备
                boolean hasPrerequisiteEquipment = false;
                
                for (ItemStack equipped : equippedItems) {
                    if (equipped == null || equipped.equals(targetItem)) continue;
                    
                    Set<String> equippedLores = extractLoreTexts(equipped);
                    // 检查已装备物品是否包含相同的前置lore
                    if (equippedLores.stream().anyMatch(lore -> lore.contains(prerequisiteLore))) {
                        hasPrerequisiteEquipment = true;
                        break;
                    }
                }
                
                // 如果没有前置装备，则受限制
                if (!hasPrerequisiteEquipment) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查物品是否被限制
     * @param equippedItems 已装备的物品（包括护甲）
     * @param targetItem 要检查的目标物品
     * @return 是否被限制
     */
    public boolean isRestricted(List<ItemStack> equippedItems, ItemStack targetItem) {
        if (targetItem == null) {
            return false;
        }
        
        return hasConflictRestriction(equippedItems, targetItem) ||
               hasExclusiveRestriction(equippedItems, targetItem) ||
               hasPrerequisiteRestriction(equippedItems, targetItem);
    }
    
    /**
     * 重载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
} 