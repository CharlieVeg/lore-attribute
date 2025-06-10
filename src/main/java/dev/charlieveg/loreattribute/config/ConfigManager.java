package dev.charlieveg.loreattribute.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理器
 * 负责加载和管理插件配置
 * 
 * @author charlieveg
 */
public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    // 属性名称映射
    private final Map<String, String> attributeNames = new HashMap<>();
    // 武器类型配置
    private final Map<String, List<String>> weaponTypes = new HashMap<>();
    // 套装配置
    private final Map<String, Map<String, Object>> suitConfigs = new HashMap<>();
    // 消息配置
    private final Map<String, String> messages = new HashMap<>();
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        loadAttributeNames();
        loadWeaponTypes();
        loadSuitConfigs();
        loadMessages();
    }
    
    /**
     * 加载属性名称配置
     */
    private void loadAttributeNames() {
        attributeNames.clear();
        attributeNames.put("Damage", config.getString("Damage", "攻击伤害"));
        attributeNames.put("Crit", config.getString("Crit", "致命几率"));
        attributeNames.put("CritDamage", config.getString("CritDamage", "致命伤害"));
        attributeNames.put("CritArmor", config.getString("CritArmor", "致命抗性"));
        attributeNames.put("HealthHeal", config.getString("HealthHeal", "生命恢复"));
        attributeNames.put("Health", config.getString("Health", "生命值"));
        attributeNames.put("LifeSteal", config.getString("LifeSteal", "生命偷取"));
        attributeNames.put("MoveSpeed", config.getString("MoveSpeed", "移动速度"));
        attributeNames.put("BuffAdd", config.getString("BuffAdd", "穿戴时"));
        attributeNames.put("BuffDamage", config.getString("BuffDamage", "对拥有"));
        attributeNames.put("Armor", config.getString("Armor", "伤害减免"));
        attributeNames.put("MobDamage", config.getString("MobDamage", "对怪物造成的额外伤害"));
        attributeNames.put("SumMobDamage", config.getString("SumMobDamage", "对怪物造成的总额外伤害"));
        attributeNames.put("MobDamageRemove", config.getString("MobDamageRemove", "对怪物的伤害免疫"));
        attributeNames.put("SumDamage", config.getString("SumDamage", "总伤害"));
        attributeNames.put("CritBreaker", config.getString("CritBreaker", "招架几率"));
        attributeNames.put("AOEDamage", config.getString("AOEDamage", "范围伤害"));
        attributeNames.put("AOERange", config.getString("AOERange", "伤害范围"));
        attributeNames.put("Speed", config.getString("Speed", "移动速度"));
        attributeNames.put("AttackSpeed", config.getString("AttackSpeed", "攻击速度"));
        attributeNames.put("TrueDamage", config.getString("TrueDamage", "真实伤害"));
        attributeNames.put("ArmorBreak", config.getString("ArmorBreak", "护甲穿透"));
        attributeNames.put("Dodge", config.getString("Dodge", "闪避几率"));
        attributeNames.put("DodgeBreaker", config.getString("DodgeBreaker", "破闪几率"));
        attributeNames.put("Block", config.getString("Block", "格挡几率"));
        attributeNames.put("BlockBreaker", config.getString("BlockBreaker", "强化重击"));
        attributeNames.put("FinalDamage", config.getString("FinalDamage", "百分比伤害"));
        attributeNames.put("FinalTrueDamage", config.getString("FinalTrueDamage", "百分比真实伤害"));
        attributeNames.put("Injury", config.getString("Injury", "反伤几率"));
        attributeNames.put("TrueArmor", config.getString("TrueArmor", "真实抗性"));
        attributeNames.put("Weaken", config.getString("Weaken", "弱化几率"));
        attributeNames.put("FinalHealth", config.getString("FinalHealth", "百分比生命"));
        attributeNames.put("ArmorType", config.getString("ArmorType", "护甲"));
    }
    
    /**
     * 加载武器类型配置
     */
    private void loadWeaponTypes() {
        weaponTypes.clear();
        ConfigurationSection typeSection = config.getConfigurationSection("TypeList");
        if (typeSection != null) {
            for (String key : typeSection.getKeys(false)) {
                List<String> types = typeSection.getStringList(key);
                weaponTypes.put(key, types);
            }
        }
    }
    
    /**
     * 加载套装配置
     */
    private void loadSuitConfigs() {
        suitConfigs.clear();
        ConfigurationSection suitSection = config.getConfigurationSection("Suit");
        if (suitSection != null) {
            for (String key : suitSection.getKeys(false)) {
                ConfigurationSection suit = suitSection.getConfigurationSection(key);
                if (suit != null) {
                    Map<String, Object> suitData = new HashMap<>();
                    suitData.put("Lore", suit.getString("Lore"));
                    suitData.put("AttributeNeed", suit.getConfigurationSection("AttributeNeed"));
                    suitConfigs.put(key, suitData);
                }
            }
        }
    }
    
    /**
     * 加载消息配置
     */
    private void loadMessages() {
        messages.clear();
        messages.put("InventoryTitle", config.getString("InventoryTitle", "战斗背包"));
        messages.put("InvItemName", config.getString("InvItemName", "&c战斗背包"));
        messages.put("InvMessage", config.getString("InvMessage", "&c该位置不能放入此物品，请放入正确位置！"));
        messages.put("ShiftMessage", config.getString("ShiftMessage", "&c禁止在战斗背包界面使用Shift键与键盘按键快捷拖动物品！"));
        messages.put("DodgeMessage", config.getString("DodgeMessage", "&9闪避！闪避掉来自敌人的 &a<number> &6点伤害！"));
        messages.put("AtDodgeMessage", config.getString("AtDodgeMessage", "&c被敌人闪避掉 &a<number> &c点伤害！"));
        messages.put("CritMessage", config.getString("CritMessage", "&6暴击！对敌人造成 &a<number> &6点伤害！"));
        messages.put("BlockMessage", config.getString("BlockMessage", "&6格挡！格挡掉敌人的伤害！"));
        messages.put("InjuryMessage", config.getString("InjuryMessage", "&c反伤！返还给敌人十分之一伤害的真实伤害！"));
        messages.put("AtBlockMessage", config.getString("AtBlockMessage", "&c格挡！被敌人格挡一次伤害！"));
        messages.put("AtInjuryMessage", config.getString("AtInjuryMessage", "&c反伤！被反伤到本次伤害十分之一的真实伤害！"));
        messages.put("WeakMessage", config.getString("WeakMessage", "&c您被敌人弱化了，两秒内您的伤害将会被降低40%！"));
        messages.put("ArmorMessage", config.getString("ArmorMessage", "&c护甲禁止使用右键直接穿上！请打开背包穿戴！"));
        messages.put("TypeLore", config.getString("TypeLore", "&e类型: &7"));
        messages.put("TimeLore", config.getString("TimeLore", "&6%time%&4到期"));
        messages.put("TimeMessage", config.getString("TimeMessage", "&c您的某件战斗道具已到期！"));
    }
    
    /**
     * 获取属性显示名称
     */
    public String getAttributeName(String key) {
        return attributeNames.getOrDefault(key, key);
    }
    
    /**
     * 获取武器类型列表
     */
    public List<String> getWeaponTypeList(String type) {
        return weaponTypes.get(type);
    }
    
    /**
     * 获取套装配置
     */
    public Map<String, Object> getSuitConfig(String suitName) {
        return suitConfigs.get(suitName);
    }
    
    /**
     * 获取消息
     */
    public String getMessage(String key) {
        String message = messages.getOrDefault(key, key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * 获取消息列表
     */
    public List<String> getMessageList(String key) {
        List<String> messageList = config.getStringList(key);
        for (int i = 0; i < messageList.size(); i++) {
            messageList.set(i, ChatColor.translateAlternateColorCodes('&', messageList.get(i)));
        }
        return messageList;
    }
    
    /**
     * 获取配置值
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    /**
     * 获取所有属性名称映射
     */
    public Map<String, String> getAllAttributeNames() {
        return new HashMap<>(attributeNames);
    }
    

} 