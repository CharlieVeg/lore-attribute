package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
// 1.7.10不支持Attribute API，移除相关导入
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * 属性效果监听器
 * 处理生命值、移动速度、攻击速度等属性的实际应用
 * 
 * @author charlieveg
 */
public class AttributeEffectListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    
    // 攻击冷却时间记录 (玩家UUID -> 最后攻击时间)
    private final Map<UUID, Long> attackCooldowns = new HashMap<>();
    
    public AttributeEffectListener(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 玩家加入时启动属性效果更新
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 延迟3秒后开始应用属性效果，确保玩家完全加载
        new BukkitRunnable() {
            @Override
            public void run() {
                applyPlayerAttributeEffects(player);
            }
        }.runTaskLater(plugin, 60L);
    }
    

    
    /**
     * 处理攻击速度限制 - 仅处理近战攻击，不包括FlansModule枪械
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        // 只处理直接玩家攻击（近战），完全排除FlansModule相关攻击
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Entity target = event.getEntity();
        
        // 检查是否是AOE伤害
        if (isAoeDamage(attacker, target)) {
            return; // AOE伤害不受攻击速度限制
        }
        
        // 检查手持物品是否为FlansModule枪械，如果是则不处理攻击速度
        org.bukkit.inventory.ItemStack handItem = attacker.getInventory().getItemInHand();
        if (handItem != null && dev.charlieveg.loreattribute.util.FlansModuleIntegration.isFlansGun(handItem)) {
            return; // FlansModule枪械使用模组内置的攻击速度系统
        }
        
        UUID playerId = attacker.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // 获取玩家攻击速度属性
        Map<String, Double> attributes = plugin.getAttributeManager().getCachedPlayerAttributes(attacker);
        double attackSpeed = attributes.getOrDefault("attackSpeed", 0.0);
        
        // 近战攻击基础间隔
        double baseInterval = 600.0; // 600ms基础间隔
        
        // 攻击速度每点减少2%的攻击间隔
        double speedReduction = Math.min(attackSpeed, 200.0);
        double reductionFactor = speedReduction / 100.0 * 0.02;
        double interval = baseInterval * (1.0 - reductionFactor);
        interval = Math.max(interval, 50.0); // 最小攻击间隔50ms
        
        // 检查是否在攻击冷却中
        Long lastAttackTime = attackCooldowns.get(playerId);
        if (lastAttackTime != null && (currentTime - lastAttackTime) < interval) {
            // 取消这次攻击
            event.setCancelled(true);
            
            // 调试信息
            if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
                attacker.sendMessage("§c近战攻击限制: 剩余冷却 " + 
                    String.format("%.1f", (interval - (currentTime - lastAttackTime)) / 1000.0) + " 秒");
            }
            return;
        }
        
        // 记录这次攻击时间
        attackCooldowns.put(playerId, currentTime);
        
        // 调试信息
        if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
            attacker.sendMessage("§a近战攻击成功: 间隔 " + String.format("%.1f", interval) + "ms, 攻击速度: " + attackSpeed);
        }
    }
    
    /**
     * 手动触发属性更新 - 用于调试
     */
    public void forceUpdatePlayerAttributes(Player player) {
        // 清除旧缓存
        plugin.getAttributeManager().clearPlayerAttributes(player);
        // 重新计算属性
        plugin.getAttributeManager().updatePlayerAttributes(player);
        // 应用属性效果
        applyPlayerAttributeEffects(player);
        
        if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
            Map<String, Double> attributes = plugin.getAttributeManager().getCachedPlayerAttributes(player);
            player.sendMessage("§6手动更新属性完成！");
            player.sendMessage("§6当前攻击速度: " + attributes.getOrDefault("attackSpeed", 0.0));
        }
    }
    
    /**
     * 检查是否是AOE伤害
     * 通过CombatListener的静态方法检查AOE处理状态
     */
    private boolean isAoeDamage(Player attacker, Entity target) {
        return dev.charlieveg.loreattribute.listener.CombatListener.isCurrentlyProcessingAoe();
    }
    
    /**
     * 应用玩家属性效果
     */
    public void applyPlayerAttributeEffects(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        Map<String, Double> attributes = plugin.getAttributeManager().getCachedPlayerAttributes(player);
        
        // 应用生命值属性
        applyHealthAttribute(player, attributes);
        
        // 应用移动速度属性
        applySpeedAttribute(player, attributes);
        
        // 应用生命恢复
        applyHealthRegeneration(player, attributes);
    }
    
    /**
     * 应用生命值属性
     */
    private void applyHealthAttribute(Player player, Map<String, Double> attributes) {
        double baseHealth = 20.0; // 基础生命值
        double additionalHealth = attributes.getOrDefault("health", 0.0);
        double percentHealth = attributes.getOrDefault("finalHealth", 0.0);
        
        // 计算最终生命值
        double finalHealth = baseHealth + additionalHealth;
        if (percentHealth > 0) {
            finalHealth *= (1.0 + percentHealth / 100.0);
        }
        
        // 限制最大生命值
        finalHealth = Math.min(finalHealth, 100.0); // 最大100点生命值
        finalHealth = Math.max(finalHealth, 1.0);   // 最小1点生命值
        
        try {
            // 使用Bukkit 1.8的方式设置最大生命值
            player.setMaxHealth(finalHealth);
            
            // 如果当前生命值超过最大生命值，调整到最大值
            if (player.getHealth() > finalHealth) {
                player.setHealth(finalHealth);
            }
        } catch (Exception e) {
            // 如果设置失败，记录日志但不中断
            plugin.getLogger().warning("设置玩家 " + player.getName() + " 的生命值失败: " + e.getMessage());
        }
    }
    
    /**
     * 应用移动速度属性
     */
    private void applySpeedAttribute(Player player, Map<String, Double> attributes) {
        double baseSpeed = 0.2; // 基础移动速度
        double additionalSpeed = attributes.getOrDefault("moveSpeed", 0.0);
        
        // 计算最终移动速度（additionalSpeed按百分比计算）
        double finalSpeed = baseSpeed * (1.0 + additionalSpeed / 100.0);
        
        // 限制移动速度范围
        finalSpeed = Math.min(finalSpeed, 1.0);   // 最大1.0
        finalSpeed = Math.max(finalSpeed, 0.05);  // 最小0.05
        
        try {
            player.setWalkSpeed((float) finalSpeed);
        } catch (Exception e) {
            plugin.getLogger().warning("设置玩家 " + player.getName() + " 的移动速度失败: " + e.getMessage());
        }
    }
    
    /**
     * 应用生命恢复
     */
    private void applyHealthRegeneration(Player player, Map<String, Double> attributes) {
        double healthHeal = attributes.getOrDefault("healthHeal", 0.0);
        
        if (healthHeal > 0) {
            // 给予生命恢复效果
            int amplifier = Math.min((int) (healthHeal / 5), 10); // 每5点属性值提升1级效果，最大10级
            int duration = 20 * 300; // 5分钟效果
            
            PotionEffect regenEffect = new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier, false);
            player.addPotionEffect(regenEffect, true);
        }
    }
    
    /**
     * 清理离线玩家的攻击冷却记录
     */
    public void cleanupOfflinePlayerCooldowns() {
        // 清理超过10分钟没有攻击的玩家记录
        long currentTime = System.currentTimeMillis();
        long cleanupThreshold = 10 * 60 * 1000; // 10分钟
        
        // 1.7.10兼容写法
        java.util.Iterator<Map.Entry<UUID, Long>> iterator = attackCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > cleanupThreshold) {
                iterator.remove();
            }
        }
    }
    
    /**
     * 创建属性更新任务
     */
    public void startAttributeEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 每30秒更新一次所有在线玩家的属性效果
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    applyPlayerAttributeEffects(player);
                }
                
                // 清理攻击冷却记录
                cleanupOfflinePlayerCooldowns();
            }
        }.runTaskTimer(plugin, 600L, 600L); // 30秒后开始，每30秒执行一次
    }
} 