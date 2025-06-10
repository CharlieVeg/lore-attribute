package dev.charlieveg.loreattribute.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * FlansModule集成工具类
 * 参考BigAttribute的实现，提供正确的FlansModule支持
 * 
 * @author charlieveg
 */
public class FlansModuleIntegration {
    
    private static boolean flansModuleAvailable = false;
    private static Class<?> bulletHandlerClass;
    private static Object bulletHandlerInstance;
    private static Method getBulletOwnerMethod;
    
    static {
        init();
    }
    
    /**
     * 初始化FlansModule集成
     */
    public static void init() {
        try {
            // 首先检查FlansModule是否存在
            Class.forName("com.flansmod.common.guns.EntityBullet");
            
            // 尝试初始化FlansAPI BulletHandler
            try {
                bulletHandlerClass = Class.forName("flansapi.handlers.BulletHandler");
                bulletHandlerInstance = bulletHandlerClass.newInstance();
                getBulletOwnerMethod = bulletHandlerClass.getMethod("getBulletOwner", String.class);
                flansModuleAvailable = true;
                Bukkit.getLogger().info("[LoreAttribute] 成功加载FlansAPI BulletHandler");
            } catch (Exception e) {
                flansModuleAvailable = false;
                Bukkit.getLogger().warning("[LoreAttribute] 无法加载FlansAPI BulletHandler: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            flansModuleAvailable = false;
            Bukkit.getLogger().info("[LoreAttribute] FlansModule未检测到");
        }
    }
    
    /**
     * 检查FlansModule是否可用
     */
    public static boolean isFlansModuleAvailable() {
        return flansModuleAvailable;
    }
    
    /**
     * 检查实体是否为FlansModule子弹
     * 参考BAFlansBulletUtil.isFlansBullet()
     */
    public static boolean isFlansBullet(Entity entity) {
        if (entity == null || !flansModuleAvailable) {
            return false;
        }
        
        String entityName = entity.getType().name();
        String className = entity.getClass().getName();
        
        return entityName.contains("FLANSMOD") || 
               className.contains("FLANSMOD") || 
               className.contains("flansmod") || 
               className.contains("EntityBullet");
    }
    
    /**
     * 获取FlansModule子弹的射击者
     * 参考BAFlansBulletUtil.getBulletShooter()
     */
    public static Player getBulletShooter(Entity bulletEntity) {
        if (bulletEntity == null || !flansModuleAvailable || 
            bulletHandlerInstance == null || getBulletOwnerMethod == null) {
            return null;
        }
        
        try {
            String uuid = bulletEntity.getUniqueId().toString();
            String playerName = (String) getBulletOwnerMethod.invoke(bulletHandlerInstance, uuid);
            if (playerName != null) {
                return Bukkit.getPlayerExact(playerName);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LoreAttribute] 获取子弹射击者时出错: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 获取射击者手持的武器
     * 参考BAFlansBulletUtil.getShooterHeldItem()
     */
    public static ItemStack getShooterHeldItem(Entity bulletEntity) {
        Player shooter = getBulletShooter(bulletEntity);
        if (shooter != null) {
            return shooter.getItemInHand();
        }
        return null;
    }
    
    /**
     * 检查物品是否为FlansModule枪械
     */
    public static boolean isFlansGun(ItemStack item) {
        if (!flansModuleAvailable || item == null) {
            return false;
        }
        
        try {
            // 通过反射检查是否为FlansModule枪械
            Class<?> itemGunClass = Class.forName("com.flansmod.common.guns.ItemGun");
            
            // 使用FlansAPI ItemHandler检查
            try {
                Class<?> itemHandlerClass = Class.forName("flansapi.handlers.ItemHandler");
                Object itemHandler = itemHandlerClass.newInstance();
                Method isGunMethod = itemHandlerClass.getMethod("isGun", int.class);
                Boolean isGun = (Boolean) isGunMethod.invoke(itemHandler, item.getTypeId());
                if (isGun != null && isGun) {
                    return true;
                }
            } catch (Exception e) {
                // FlansAPI不可用，使用fallback检查
            }
            
            // 备用检查：通过物品名称判断
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String itemName = item.getItemMeta().getDisplayName().toLowerCase();
                if (itemName.contains("枪") || itemName.contains("gun") || 
                    itemName.contains("rifle") || itemName.contains("pistol") || 
                    itemName.contains("sniper") || itemName.contains("shotgun")) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从FlansModule枪械提取属性（用于子弹伤害）
     * 这个方法专门用于子弹实体伤害时的属性提取
     */
    public static Map<String, Double> extractFlansGunAttributes(ItemStack gunItem) {
        Map<String, Double> attributes = new HashMap<>();
        
        if (!isFlansGun(gunItem)) {
            return attributes;
        }
        
        try {

            if (gunItem.hasItemMeta() && gunItem.getItemMeta().hasLore()) {
                for (String lore : gunItem.getItemMeta().getLore()) {
                    parseAttributeFromLore(lore, attributes);
                }
            }
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LoreAttribute] 提取FlansModule枪械属性时出错: " + e.getMessage());
        }
        
        return attributes;
    }
    
    /**
     * 从lore解析属性
     */
    private static void parseAttributeFromLore(String lore, Map<String, Double> attributes) {
        lore = lore.replaceAll("§[0-9a-fk-or]", ""); // 移除颜色代码
        
        // 解析各种属性
        if (lore.contains("伤害") || lore.contains("攻击")) {
            try {
                double damage = Double.parseDouble(lore.replaceAll("[^0-9.]", ""));
                attributes.put("damage", damage);
            } catch (NumberFormatException ignored) {}
        }
        
        // 移除自动暴击解析，避免枪械意外获得暴击
        // 如果枪械需要暴击，应该通过明确的属性lore格式指定
        // 例如: "致命几率: 10%"
        if (lore.contains("致命几率:") || lore.contains("暴击率:")) {
            try {
                String[] parts = lore.split(":");
                if (parts.length > 1) {
                    double crit = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                    attributes.put("crit", crit);
                }
            } catch (NumberFormatException ignored) {}
        }
        
        if (lore.contains("穿透")) {
            try {
                double armor = Double.parseDouble(lore.replaceAll("[^0-9.]", ""));
                attributes.put("armorBreak", armor);
            } catch (NumberFormatException ignored) {}
        }
    }
} 