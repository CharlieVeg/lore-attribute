package dev.charlieveg.loreattribute.manager;

import dev.charlieveg.loreattribute.data.BattleInventory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 属性管理器
 * 负责解析物品lore中的属性，计算玩家总属性
 * 
 * @author charlieveg
 */
public class AttributeManager {
    
    private final Object plugin;
    
    private final Map<UUID, Map<String, Double>> playerAttributes = new ConcurrentHashMap<>();
    
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(.+?): ([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    
    private final Map<String, String> attributeKeyMap = new HashMap<>();
    
    public AttributeManager(Object plugin) {
        this.plugin = plugin;
        initializeAttributeKeys();
    }
    
    /**
     * 初始化属性键映射
     */
    private void initializeAttributeKeys() {
        attributeKeyMap.put("攻击伤害", "damage");
        attributeKeyMap.put("致命几率", "crit");
        attributeKeyMap.put("致命伤害", "critDamage");
        attributeKeyMap.put("致命抗性", "critArmor");
        attributeKeyMap.put("生命恢复", "healthHeal");
        attributeKeyMap.put("生命值", "health");
        attributeKeyMap.put("生命偷取", "lifeSteal");
        attributeKeyMap.put("移动速度", "moveSpeed");
        attributeKeyMap.put("伤害减免", "armor");
        attributeKeyMap.put("对怪物造成的额外伤害", "mobDamage");
        attributeKeyMap.put("对怪物造成的总额外伤害", "sumMobDamage");
        attributeKeyMap.put("对怪物的伤害免疫", "mobDamageRemove");
        attributeKeyMap.put("总伤害", "sumDamage");
        attributeKeyMap.put("招架几率", "critBreaker");
        attributeKeyMap.put("范围伤害", "aoeDamage");
        attributeKeyMap.put("范围距离", "aoeRange");
        attributeKeyMap.put("攻击速度", "attackSpeed");
        attributeKeyMap.put("真实伤害", "trueDamage");
        attributeKeyMap.put("护甲穿透", "armorBreak");
        attributeKeyMap.put("闪避几率", "dodge");
        attributeKeyMap.put("破闪几率", "dodgeBreaker");
        attributeKeyMap.put("格挡几率", "block");
        attributeKeyMap.put("强化重击", "blockBreaker");
        attributeKeyMap.put("百分比伤害", "finalDamage");
        attributeKeyMap.put("百分比真实伤害", "finalTrueDamage");
        attributeKeyMap.put("反伤几率", "injury");
        attributeKeyMap.put("真实抗性", "trueArmor");
        attributeKeyMap.put("弱化几率", "weaken");
        attributeKeyMap.put("百分比生命", "finalHealth");
        
        attributeKeyMap.put("攻击伤害".toLowerCase(), "damage");
        attributeKeyMap.put("致命几率".toLowerCase(), "crit");
        attributeKeyMap.put("致命伤害".toLowerCase(), "critDamage");
        attributeKeyMap.put("致命抗性".toLowerCase(), "critArmor");
        attributeKeyMap.put("生命恢复".toLowerCase(), "healthHeal");
        attributeKeyMap.put("生命值".toLowerCase(), "health");
        attributeKeyMap.put("生命偷取".toLowerCase(), "lifeSteal");
        attributeKeyMap.put("移动速度".toLowerCase(), "moveSpeed");
        attributeKeyMap.put("伤害减免".toLowerCase(), "armor");
        attributeKeyMap.put("对怪物造成的额外伤害".toLowerCase(), "mobDamage");
        attributeKeyMap.put("对怪物造成的总额外伤害".toLowerCase(), "sumMobDamage");
        attributeKeyMap.put("对怪物的伤害免疫".toLowerCase(), "mobDamageRemove");
        attributeKeyMap.put("总伤害".toLowerCase(), "sumDamage");
        attributeKeyMap.put("招架几率".toLowerCase(), "critBreaker");
        attributeKeyMap.put("范围伤害".toLowerCase(), "aoeDamage");
        attributeKeyMap.put("范围距离".toLowerCase(), "aoeRange");
        attributeKeyMap.put("攻击速度".toLowerCase(), "attackSpeed");
        attributeKeyMap.put("真实伤害".toLowerCase(), "trueDamage");
        attributeKeyMap.put("护甲穿透".toLowerCase(), "armorBreak");
        attributeKeyMap.put("闪避几率".toLowerCase(), "dodge");
        attributeKeyMap.put("破闪几率".toLowerCase(), "dodgeBreaker");
        attributeKeyMap.put("格挡几率".toLowerCase(), "block");
        attributeKeyMap.put("强化重击".toLowerCase(), "blockBreaker");
        attributeKeyMap.put("百分比伤害".toLowerCase(), "finalDamage");
        attributeKeyMap.put("百分比真实伤害".toLowerCase(), "finalTrueDamage");
        attributeKeyMap.put("反伤几率".toLowerCase(), "injury");
        attributeKeyMap.put("真实抗性".toLowerCase(), "trueArmor");
        attributeKeyMap.put("弱化几率".toLowerCase(), "weaken");
        attributeKeyMap.put("百分比生命".toLowerCase(), "finalHealth");
        
        attributeKeyMap.put("damage", "damage");
        attributeKeyMap.put("crit", "crit");
        attributeKeyMap.put("critdamage", "critDamage");
        attributeKeyMap.put("attackspeed", "attackSpeed");
    }
    
    /**
     * 从物品lore中解析属性
     */
    public Map<String, Double> parseItemAttributes(ItemStack item) {
        Map<String, Double> attributes = new HashMap<>();
        
        if (item == null || !item.hasItemMeta()) {
            return attributes;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            
            for (String line : lore) {
                // 移除颜色代码
                String cleanLine = ChatColor.stripColor(line).trim();
                
                // 解析属性行
                parseAttributeLine(cleanLine, attributes);
            }
        }
        
        return attributes;
    }
    
    /**
     * 解析单行属性
     */
    private void parseAttributeLine(String line, Map<String, Double> attributes) {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(line);
        if (matcher.find()) {
            String attributeName = matcher.group(1).trim();
            String valueStr = matcher.group(2).trim();
            
            try {
                double value = Double.parseDouble(valueStr);
                String key = getAttributeKey(attributeName);
                if (key != null) {
                    attributes.put(key, attributes.getOrDefault(key, 0.0) + value);
                }
            } catch (NumberFormatException e) {
                // 忽略无法解析的数值
            }
        } else {
            parseAlternativeFormat(line, attributes);
        }
    }
    
    /**
     * 解析其他格式的属性行
     */
    private void parseAlternativeFormat(String line, Map<String, Double> attributes) {
        for (String attributeName : attributeKeyMap.keySet()) {
            if (line.contains(attributeName)) {
                Matcher numberMatcher = NUMBER_PATTERN.matcher(line);
                if (numberMatcher.find()) {
                    try {
                        double value = Double.parseDouble(numberMatcher.group(1));
                        String key = getAttributeKey(attributeName);
                        if (key != null) {
                            attributes.put(key, attributes.getOrDefault(key, 0.0) + value);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的数值
                    }
                }
                break;
            }
        }
    }
    
    /**
     * 获取属性键
     */
    private String getAttributeKey(String attributeName) {
        String key = attributeKeyMap.get(attributeName);
        if (key != null) {
            return key;
        }
        return attributeKeyMap.get(attributeName.toLowerCase());
    }
    
    /**
     * 计算玩家总属性（严格按照类型限制）
     */
    public Map<String, Double> calculatePlayerAttributes(Player player) {
        Map<String, Double> totalAttributes = new HashMap<>();
        
        ItemStack mainHand = player.getInventory().getItemInHand();
        if (mainHand != null && isWeaponType(mainHand)) {
            Map<String, Double> weaponAttrs = parseItemAttributesWithTypeCheck(mainHand, "武器");
            mergeAttributes(totalAttributes, weaponAttrs);
        }
        
        ItemStack[] equipment = player.getInventory().getArmorContents();
        for (ItemStack armor : equipment) {
            if (armor != null && isArmorType(armor)) {
                Map<String, Double> armorAttrs = parseItemAttributesWithTypeCheck(armor, "防具");
                mergeAttributes(totalAttributes, armorAttrs);
            }
        }
        
        try {
            if (plugin instanceof dev.charlieveg.loreattribute.LoreAttributePlugin) {
                dev.charlieveg.loreattribute.LoreAttributePlugin lorePlugin = (dev.charlieveg.loreattribute.LoreAttributePlugin) plugin;
                
                ItemStack[] battleItems = lorePlugin.getBattleInventoryManager().getAllBattleItems(player);
                if (battleItems != null) {
                    for (ItemStack battleItem : battleItems) {
                        if (battleItem != null && isAccessoryType(battleItem)) {
                            Map<String, Double> accessoryAttrs = parseItemAttributesWithTypeCheck(battleItem, "饰品");
                            mergeAttributes(totalAttributes, accessoryAttrs);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 静默处理异常，战斗背包可能不存在
        }
        
        playerAttributes.put(player.getUniqueId(), totalAttributes);
        applySpecialAttributes(player, totalAttributes);
        
        return totalAttributes;
    }
    
    /**
     * 检查是否为武器类型
     */
    private boolean isWeaponType(ItemStack item) {
        return getItemType(item).equals("武器");
    }
    
    /**
     * 检查是否为防具类型
     */
    private boolean isArmorType(ItemStack item) {
        return getItemType(item).equals("防具");
    }
    
    /**
     * 检查是否为饰品类型
     */
    private boolean isAccessoryType(ItemStack item) {
        return getItemType(item).equals("饰品");
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
            String cleanLine = org.bukkit.ChatColor.stripColor(line).trim();
            if (cleanLine.startsWith("类型: ")) {
                return cleanLine.substring(4).trim();
            }
        }
        return "";
    }
    
    /**
     * 解析物品属性并进行类型检查
     */
    private Map<String, Double> parseItemAttributesWithTypeCheck(ItemStack item, String expectedType) {
        String itemType = getItemType(item);
        if (!itemType.equals(expectedType)) {
            return new HashMap<>(); // 类型不匹配，返回空属性
        }
        return parseItemAttributes(item);
    }
    
    /**
     * 应用特殊属性效果（如移动速度）
     */
    private void applySpecialAttributes(Player player, Map<String, Double> attributes) {
        // 应用移动速度
        double speed = attributes.getOrDefault("speed", 0.0);
        float baseSpeed = 0.2f; // 默认玩家移动速度
        float newSpeed = (float) (baseSpeed + (speed / 100.0 * baseSpeed));
        newSpeed = Math.max(0.0f, Math.min(1.0f, newSpeed)); // 限制在0-1之间
        
        if (Math.abs(player.getWalkSpeed() - newSpeed) > 0.01f) {
            player.setWalkSpeed(newSpeed);
        }
        
        // 应用生命值上限
        double health = attributes.getOrDefault("health", 0.0);
        if (health > 0) {
            double baseHealth = 20.0; // 基础生命值
            double newMaxHealth = baseHealth + health;
            newMaxHealth = Math.max(1.0, Math.min(2048.0, newMaxHealth)); // 限制范围
            
            if (Math.abs(player.getMaxHealth() - newMaxHealth) > 0.1) {
                double currentPercent = player.getHealth() / player.getMaxHealth();
                player.setMaxHealth(newMaxHealth);
                player.setHealth(newMaxHealth * currentPercent); // 保持血量百分比
            }
        }
        
        // 应用生命恢复
        double regen = attributes.getOrDefault("regen", 0.0);
        if (regen > 0) {
            // 通过定时任务处理生命恢复，这里只是标记
            // 生命恢复会由其他系统处理，这里只是计算属性值
        }
    }
    
    /**
     * 合并属性映射
     */
    private void mergeAttributes(Map<String, Double> target, Map<String, Double> source) {
        for (Map.Entry<String, Double> entry : source.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            target.put(key, target.getOrDefault(key, 0.0) + value);
        }
    }
    
    /**
     * 获取玩家缓存的属性
     */
    public Map<String, Double> getCachedPlayerAttributes(Player player) {
        return playerAttributes.getOrDefault(player.getUniqueId(), new HashMap<>());
    }
    
    /**
     * 获取玩家指定属性值
     */
    public double getPlayerAttribute(Player player, String attributeName) {
        Map<String, Double> attributes = getCachedPlayerAttributes(player);
        String key = getAttributeKey(attributeName);
        if (key != null) {
            return attributes.getOrDefault(key, 0.0);
        }
        return attributes.getOrDefault(attributeName, 0.0);
    }
    
    /**
     * 注意：玩家基础属性不支持直接修改
     * 所有属性值都是从装备和物品lore中计算得出
     * 此方法仅用于调试或特殊需求
     */
    @Deprecated
    public void setPlayerAttribute(Player player, String attributeName, double value) {
        // 不推荐直接修改玩家属性
        // 属性应该通过装备和物品lore自动计算
        System.out.println("警告: 不推荐直接修改玩家属性，应通过装备lore计算");
    }
    
    /**
     * 注意：玩家基础属性不支持直接修改
     * 所有属性值都是从装备和物品lore中计算得出
     * 此方法仅用于调试或特殊需求
     */
    @Deprecated
    public void addPlayerAttribute(Player player, String attributeName, double value) {
        // 不推荐直接修改玩家属性
        // 属性应该通过装备和物品lore自动计算
        System.out.println("警告: 不推荐直接修改玩家属性，应通过装备lore计算");
    }
    
    /**
     * 清除玩家属性缓存
     */
    public void clearPlayerAttributes(Player player) {
        playerAttributes.remove(player.getUniqueId());
    }
    
    /**
     * 重新计算并更新玩家属性
     */
    public void updatePlayerAttributes(Player player) {
        calculatePlayerAttributes(player);
    }
    
    /**
     * 获取所有缓存的玩家属性
     */
    public Map<UUID, Map<String, Double>> getAllPlayerAttributes() {
        return new HashMap<>(playerAttributes);
    }
    
    /**
     * 检查属性名称是否有效
     */
    public boolean isValidAttributeName(String attributeName) {
        return attributeKeyMap.containsKey(attributeName.toLowerCase()) ||
               attributeKeyMap.containsValue(attributeName.toLowerCase());
    }
    
    /**
     * 获取属性显示名称
     */
    public String getAttributeDisplayName(String attributeKey) {
        for (Map.Entry<String, String> entry : attributeKeyMap.entrySet()) {
            if (entry.getValue().equals(attributeKey)) {
                return entry.getKey();
            }
        }
        return attributeKey;
    }
} 