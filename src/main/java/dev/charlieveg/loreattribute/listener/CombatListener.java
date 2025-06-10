package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import dev.charlieveg.loreattribute.util.FlansModuleIntegration;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 战斗事件监听器
 * 处理伤害计算、暴击、闪避等战斗机制
 * 
 * @author charlieveg
 */
public class CombatListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    private final Random random = new Random();
    
    // 使用 ThreadLocal 存储正在处理的 AOE 伤害事件，避免递归
    private final ThreadLocal<Boolean> isProcessingAoe = ThreadLocal.withInitial(() -> false);
    
    // 静态 ThreadLocal 用于外部检查AOE处理状态
    private static final ThreadLocal<Boolean> staticAoeProcessingStatus = ThreadLocal.withInitial(() -> false);
    
    // 静态方法用于外部检查AOE处理状态
    public static boolean isCurrentlyProcessingAoe() {
        return staticAoeProcessingStatus.get();
    }
    
    public CombatListener(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理玩家对实体的伤害
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player attacker = null;
        Map<String, Double> weaponAttributes = new java.util.HashMap<>();
        Entity target = event.getEntity();

        // FlansModule特殊处理：检查是否为子弹实体伤害
        if (isFlansModuleBullet(event.getDamager())) {
            // 子弹伤害需要特殊处理，因为伤害者是子弹实体而不是玩家
            attacker = getFlansModuleBulletOwner(event.getDamager());
            if (attacker != null) {
                // 获取射击时使用的FlansModule枪械属性
                weaponAttributes = getFlansModuleWeaponAttributes(attacker);
            }
        } else if (event.getDamager() instanceof Player) {
            // 普通玩家攻击（近战、其他武器等）
            attacker = (Player) event.getDamager();
            // 这些情况下属性已经在AttributeManager中正确计算
        } else {
            attacker = null;
        }

        if (attacker == null) {
            return;
        }
        
        // 检查是否是 AOE 递归伤害
        boolean isAoeRecursive = isProcessingAoe.get();
        
        // 调试：显示AOE状态
        if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
            attacker.sendMessage(ChatColor.GRAY + "伤害事件 - AOE递归状态: " + isAoeRecursive);
        }
        
        double baseDamage = event.getDamage();

        // 获取攻击者属性
        Map<String, Double> attackerAttributes = plugin.getAttributeManager().getCachedPlayerAttributes(attacker);

        // 合并武器属性
        Map<String, Double> combinedAttributes = new java.util.HashMap<>(attackerAttributes);
        mergeWeaponAttributes(combinedAttributes, weaponAttributes);
        
        // AOE递归伤害使用简化处理
        if (isAoeRecursive) {
            // 对于AOE递归伤害，只应用基础伤害计算和目标的防御属性
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                Map<String, Double> targetAttributes = plugin.getAttributeManager().getCachedPlayerAttributes(targetPlayer);
                
                // 检查闪避
                if (checkDodge(targetPlayer, targetAttributes)) {
                    event.setCancelled(true);
                    sendCombatMessage(targetPlayer, "dodge", event.getDamage());
                    sendCombatMessage(attacker, "atDodge", event.getDamage());
                    return;
                }
                
                // 检查格挡
                if (checkBlock(targetPlayer, targetAttributes)) {
                    double blockedDamage = event.getDamage() * 0.5;
                    event.setDamage(blockedDamage);
                    sendCombatMessage(targetPlayer, "block", event.getDamage() - blockedDamage);
                    sendCombatMessage(attacker, "atBlock", event.getDamage() - blockedDamage);
                }
                
                // 应用伤害减免
                double finalDamage = applyDamageReductionWithPenetration(event.getDamage(), targetAttributes, attackerAttributes);
                event.setDamage(finalDamage);
                
                if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
                    attacker.sendMessage(ChatColor.GREEN + "AOE递归伤害处理完成(玩家): " + String.format("%.1f", finalDamage));
                }
            } else {
                // 对非玩家目标（怪物）的AOE伤害，直接应用伤害，怪物没有复杂的属性系统
                double finalDamage = event.getDamage();
                event.setDamage(finalDamage);
                
                if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
                    attacker.sendMessage(ChatColor.GREEN + "AOE递归伤害处理完成(怪物): " + String.format("%.1f", finalDamage));
                }
            }
            return; // AOE递归伤害处理完成，不再执行后续逻辑
        } else {
            // 非 AOE 递归伤害，执行完整的伤害计算
        
        // 调试：显示关键属性
            if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
        double damageAttr = combinedAttributes.getOrDefault("damage", 0.0);
        double aoeDamageAttr = combinedAttributes.getOrDefault("aoeDamage", 0.0);
        double aoeRangeAttr = combinedAttributes.getOrDefault("aoeRange", 0.0);
        attacker.sendMessage(ChatColor.BLUE + "属性: 攻击伤害=" + damageAttr + 
                            ", 范围伤害=" + aoeDamageAttr + "%, 范围=" + aoeRangeAttr);
            }

        // 检查是否有设置的攻击伤害属性，如果有则忽略原武器伤害
        double attackDamage = combinedAttributes.getOrDefault("damage", 0.0);
        double weaponBaseDamage = baseDamage;
        
        // 如果设置了攻击伤害属性，使用属性值而不是物品原伤害
        if (attackDamage > 0) {
            weaponBaseDamage = 0.0; // 忽略物品原伤害
                if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
            attacker.sendMessage(ChatColor.YELLOW + "使用属性伤害替代物品原伤害！");
                }
        }
        
        // 计算最终伤害
        double finalDamage = calculateDamageWithTarget(attacker, target, weaponBaseDamage, combinedAttributes);

        // 如果目标是玩家，处理防御机制
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            Map<String, Double> targetAttributes = plugin.getAttributeManager().getCachedPlayerAttributes(targetPlayer);
            
            // 检查闪避
            if (checkDodge(targetPlayer, targetAttributes)) {
                event.setCancelled(true);
                sendCombatMessage(targetPlayer, "dodge", finalDamage);
                sendCombatMessage(attacker, "atDodge", finalDamage);
                return;
            }
            
            // 检查格挡
            if (checkBlock(targetPlayer, targetAttributes)) {
                double blockedDamage = finalDamage * 0.5;
                finalDamage = blockedDamage;
                sendCombatMessage(targetPlayer, "block", finalDamage - blockedDamage);
                sendCombatMessage(attacker, "atBlock", finalDamage - blockedDamage);
            }
            
            // 应用伤害减免和护甲穿透
            finalDamage = applyDamageReductionWithPenetration(finalDamage, targetAttributes, combinedAttributes);
            
            // 处理反伤
            handleInjury(targetPlayer, attacker, finalDamage, targetAttributes);
        }
        
        // 应用伤害
        event.setDamage(finalDamage);
        
            // 调试信息
            if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
        attacker.sendMessage(ChatColor.GREEN + "攻击伤害: " + String.format("%.1f", finalDamage) + 
                            " (基础: " + String.format("%.1f", baseDamage) + ")");
            }

        // 处理特殊效果 - 只有非AOE递归时才处理
            handleLifeSteal(attacker, finalDamage, combinedAttributes);
            handleWeakenEffect(target, combinedAttributes);
            // 只有非AOE递归攻击才触发AOE伤害
            handleAoeDamage(attacker, target, finalDamage, combinedAttributes);
        }
    }
    
    /**
     * 处理玩家受到非玩家伤害（环境伤害、怪物伤害等）
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // 如果是玩家攻击玩家，已经在onPlayerDamageEntity中处理了，这里跳过
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            if (damageEvent.getDamager() instanceof Player) {
                return; // 玩家攻击已在onPlayerDamageEntity中处理
            }
        }
        
        Player victim = (Player) event.getEntity();
        double baseDamage = event.getDamage();
        
        // 获取受害者属性
        Map<String, Double> victimAttributes = plugin.getAttributeManager().getCachedPlayerAttributes(victim);
        
        // 检查闪避
        if (checkDodge(victim, victimAttributes)) {
            event.setCancelled(true);
            sendCombatMessage(victim, "dodge", baseDamage);
            return;
        }
        
        // 检查格挡
        if (checkBlock(victim, victimAttributes)) {
            double blockedDamage = baseDamage * 0.5; // 格挡减少50%伤害
            event.setDamage(blockedDamage);
            sendCombatMessage(victim, "block", baseDamage - blockedDamage);
            baseDamage = blockedDamage;
        }
        
        // 应用伤害减免（非玩家攻击，使用普通伤害减免）
        double finalDamage = applyDamageReduction(baseDamage, victimAttributes);
        event.setDamage(finalDamage);
    }
    
    /**
     * 计算攻击伤害（带目标实体）
     */
    private double calculateDamageWithTarget(Player attacker, Entity target, double baseDamage, Map<String, Double> attributes) {
        double damage = baseDamage;
        
        // 添加基础攻击伤害
        damage += attributes.getOrDefault("damage", 0.0);
        
        // 添加总伤害（只读属性，由系统计算）
        damage += attributes.getOrDefault("sumDamage", 0.0);
        
        // 添加对怪物的额外伤害（如果目标是怪物）
        if (target != null && !(target instanceof Player)) {
            damage += attributes.getOrDefault("mobDamage", 0.0);
        }
        
        // 检查暴击
        boolean isCrit = checkCrit(attacker, attributes);
        if (isCrit) {
            double critMultiplier = 1.0 + (attributes.getOrDefault("critDamage", 0.0) / 100.0);
            damage *= critMultiplier;
            sendCombatMessage(attacker, "crit", damage);
        }
        
        // 添加真实伤害
        damage += attributes.getOrDefault("trueDamage", 0.0);
        
        // 应用百分比伤害
        double percentDamage = attributes.getOrDefault("finalDamage", 0.0);
        if (percentDamage > 0) {
            damage *= (1.0 + percentDamage / 100.0);
        }
        
        // 应用百分比真实伤害
        double percentTrueDamage = attributes.getOrDefault("finalTrueDamage", 0.0);
        if (percentTrueDamage > 0) {
            damage += (baseDamage * percentTrueDamage / 100.0);
        }
        
        return damage;
    }
    
    /**
     * 检查暴击
     */
    private boolean checkCrit(Player player, Map<String, Double> attributes) {
        double critChance = attributes.getOrDefault("crit", 0.0);
        return random.nextDouble() * 100 < critChance;
    }
    
    /**
     * 检查闪避
     */
    private boolean checkDodge(Player player, Map<String, Double> attributes) {
        double dodgeChance = attributes.getOrDefault("dodge", 0.0);
        return random.nextDouble() * 100 < dodgeChance;
    }
    
    /**
     * 检查格挡
     */
    private boolean checkBlock(Player player, Map<String, Double> attributes) {
        double blockChance = attributes.getOrDefault("block", 0.0);
        return random.nextDouble() * 100 < blockChance;
    }
    
    /**
     * 应用伤害减免
     */
    private double applyDamageReduction(double damage, Map<String, Double> attributes) {
        double armor = attributes.getOrDefault("armor", 0.0);
        double trueArmor = attributes.getOrDefault("trueArmor", 0.0);
        
        // 计算伤害减免（普通护甲 + 真实抗性）
        double reduction = (armor + trueArmor) / 100.0;
        reduction = Math.min(reduction, 0.75); // 最大减免75%
        
        return damage * (1.0 - reduction);
    }
    
    /**
     * 处理生命偷取
     */
    private void handleLifeSteal(Player attacker, double damage, Map<String, Double> attributes) {
        double lifeSteal = attributes.getOrDefault("lifeSteal", 0.0);
        if (lifeSteal > 0) {
            double healAmount = damage * (lifeSteal / 100.0);
            double currentHealth = attacker.getHealth();
            double newHealth = Math.min(attacker.getMaxHealth(), currentHealth + healAmount);
            if (newHealth > currentHealth) {
                attacker.setHealth(newHealth);
                // 发送生命偷取提示
                sendCombatMessage(attacker, "lifeSteal", healAmount);
            }
        }
    }
    
    /**
     * 处理反伤（这是防御方的属性，需要在防具中设置）
     */
    private void handleInjury(Player victim, Player attacker, double damage, Map<String, Double> attributes) {
        double injuryChance = attributes.getOrDefault("injury", 0.0);
        if (random.nextDouble() * 100 < injuryChance) {
            double injuryDamage = damage * 0.1; // 反伤10%
            attacker.damage(injuryDamage);
            
            sendCombatMessage(victim, "injury", injuryDamage);
            sendCombatMessage(attacker, "atInjury", injuryDamage);
        }
    }
    
    /**
     * 发送战斗消息
     */
    private void sendCombatMessage(Player player, String messageType, double value) {
        String message = plugin.getConfigManager().getString("Messages.Combat." + 
            messageType.substring(0, 1).toUpperCase() + messageType.substring(1), "");
        
        if (!message.isEmpty()) {
            message = message.replace("{damage}", String.format("%.1f", value));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    /**
     * 检查是否为FlansModule子弹实体
     */
    private boolean isFlansModuleBullet(Entity entity) {
        return FlansModuleIntegration.isFlansBullet(entity);
    }
    
    /**
     * 获取FlansModule子弹的射击者
     */
    private Player getFlansModuleBulletOwner(Entity bulletEntity) {
        return FlansModuleIntegration.getBulletShooter(bulletEntity);
    }
    
    /**
     * 获取FlansModule武器属性（用于子弹伤害）
     */
    private Map<String, Double> getFlansModuleWeaponAttributes(Player shooter) {
        Map<String, Double> attributes = new java.util.HashMap<>();
        
        // 优先使用子弹实体关联的武器
        ItemStack weaponItem = null;
        
        // 尝试从子弹实体获取武器（如果可能）
        ItemStack mainHand = shooter.getInventory().getItemInHand();
        if (mainHand != null && FlansModuleIntegration.isFlansGun(mainHand)) {
            weaponItem = mainHand;
        }
        
        // 如果找到了FlansModule武器，提取属性
        if (weaponItem != null) {
            attributes.putAll(FlansModuleIntegration.extractFlansGunAttributes(weaponItem));
        }
        
        return attributes;
    }
    
    /**
     * 合并武器属性到玩家属性
     */
    private void mergeWeaponAttributes(Map<String, Double> playerAttributes, Map<String, Double> weaponAttributes) {
        for (Map.Entry<String, Double> entry : weaponAttributes.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            
            // 某些属性叠加，某些属性取最大值
            switch (key) {
                case "damage":
                case "trueDamage":
                case "sumDamage":
                    // 伤害类属性叠加
                    playerAttributes.put(key, playerAttributes.getOrDefault(key, 0.0) + value);
                    break;
                case "crit":
                case "critDamage":
                case "armorPiercing":
                case "lifeSteal":
                    // 百分比属性取较大值
                    playerAttributes.put(key, Math.max(playerAttributes.getOrDefault(key, 0.0), value));
                    break;
                default:
                    // 其他属性叠加
                    playerAttributes.put(key, playerAttributes.getOrDefault(key, 0.0) + value);
                    break;
            }
        }
    }
    
    /**
     * 处理弱化效果（武器特殊效果）
     */
    private void handleWeakenEffect(Entity target, Map<String, Double> attributes) {
        double weakenChance = attributes.getOrDefault("weaken", 0.0);
        if (weakenChance > 0 && target instanceof LivingEntity && random.nextDouble() * 100 < weakenChance) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            // 给目标添加虚弱效果
            int duration = 20 * 5; // 5秒
            int amplifier = 1; // 虚弱II
            
            PotionEffect weaknessEffect = new PotionEffect(PotionEffectType.WEAKNESS, duration, amplifier, false);
            livingTarget.addPotionEffect(weaknessEffect, true);
            
            if (target instanceof Player) {
                sendCombatMessage((Player) target, "weakened", weakenChance);
            }
        }
    }
    
    /**
     * 处理范围伤害 - 使用延迟执行避免递归（武器特殊效果）
     */
    private void handleAoeDamage(Player attacker, Entity primaryTarget, double damage, Map<String, Double> attributes) {
        double aoeDamage = attributes.getOrDefault("aoeDamage", 0.0);
        double aoeRange = attributes.getOrDefault("aoeRange", 0.0);
        
        if (aoeDamage <= 0 || aoeRange <= 0) {
            return;
        }
        
        // 延迟1 tick执行AOE伤害，避免递归问题
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            executeAoeDamage(attacker, primaryTarget, damage, aoeDamage, aoeRange);
        }, 1L);
    }
    
    /**
     * 执行范围伤害
     */
    private void executeAoeDamage(Player attacker, Entity primaryTarget, double damage, double aoeDamage, double aoeRange) {
        Location targetLocation = primaryTarget.getLocation().add(0, 1, 0);
        
        // 计算范围伤害值（基于主伤害的百分比）
        double aoeBaseDamage = damage * (aoeDamage / 100.0);
        
        // 调试信息
        if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
        attacker.sendMessage(ChatColor.YELLOW + "触发范围伤害! 基础AOE伤害: " + String.format("%.1f", aoeBaseDamage) + 
                            " 范围: " + aoeRange);
        }
        
        // 播放范围伤害开始的特效和声音
        playAoeStartEffect(targetLocation, aoeRange);
        
        // 获取范围内的所有实体
        List<Entity> nearbyEntities = primaryTarget.getNearbyEntities(aoeRange, aoeRange, aoeRange);
        int hitCount = 0;
        
        // 设置 AOE 处理标记
        isProcessingAoe.set(true);
        staticAoeProcessingStatus.set(true);
        
        try {
            for (Entity entity : nearbyEntities) {
                if (entity == primaryTarget || entity == attacker) {
                    continue; // 跳过主目标和攻击者
                }
                
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;

                    // 计算距离衰减
                    double distance = entity.getLocation().distance(targetLocation);
                    double normalizedDistance = Math.min(distance / aoeRange, 1.0);
                    double damageMultiplier = Math.max(0.2, 1.0 - Math.pow(normalizedDistance, 2) * 0.8);
                    double finalAoeDamage = aoeBaseDamage * damageMultiplier;

                    // 创建特效
                    playAoeDamageEffect(targetLocation, entity.getLocation().add(0, 1, 0), damageMultiplier);

                    // 调试信息
                    if (plugin.getConfigManager().getBoolean("Debug.Combat", false)) {
                    attacker.sendMessage(ChatColor.RED + "AOE目标: " + entity.getType().name() +
                            " 距离: " + String.format("%.1f", distance) +
                            " 伤害倍数: " + String.format("%.2f", damageMultiplier) +
                            " 最终伤害: " + String.format("%.1f", finalAoeDamage));
                    }

                        // 使用damage方法，这会自动触发伤害事件并应用伤害
                        livingEntity.damage(finalAoeDamage, attacker);
                    hitCount++;

                                if (entity instanceof Player) {
                        sendCombatMessage((Player) entity, "aoeHit", finalAoeDamage);
                    }
                }
            }
        } finally {
            // 清除 AOE 处理标记
            isProcessingAoe.set(false);
            staticAoeProcessingStatus.set(false);
        }
        
        // 播放范围伤害结束特效
        if (hitCount > 0) {
            playAoeEndEffect(targetLocation, aoeRange, hitCount);
            sendCombatMessage(attacker, "aoeAttack", hitCount);
            }
    }
    
    /**
     * 播放范围伤害开始特效
     */
    private void playAoeStartEffect(Location center, double range) {
        // 播放简化的爆炸特效
        center.getWorld().playEffect(center, Effect.EXPLOSION_LARGE, 0);
        
        // 简化的范围指示 - 只显示几个关键点
        final int particles = Math.min((int)(range * 2), 8); // 大幅减少粒子数量
        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double x = center.getX() + range * Math.cos(angle);
            double z = center.getZ() + range * Math.sin(angle);
            Location particleLocation = new Location(center.getWorld(), x, center.getY(), z);
            
            // 使用更简单的烟雾粒子
            center.getWorld().playEffect(particleLocation, Effect.SMOKE, 0);
        }
        
        // 播放范围伤害音效
        center.getWorld().playSound(center, Sound.EXPLODE, 0.6f, 1.2f);
    }
    
    /**
     * 播放范围伤害传递特效
     */
    private void playAoeDamageEffect(Location from, Location to, double damageMultiplier) {
        // 简化特效 - 只在目标位置显示一个粒子
        if (damageMultiplier > 0.7) {
            // 高伤害：火焰粒子
            to.getWorld().playEffect(to, Effect.MOBSPAWNER_FLAMES, 0);
        } else if (damageMultiplier > 0.4) {
            // 中等伤害：烟雾粒子
            to.getWorld().playEffect(to, Effect.SMOKE, 0);
        } else {
            // 低伤害：小爆炸
            to.getWorld().playEffect(to, Effect.EXPLOSION, 0);
        }
    }
    
    /**
     * 播放范围伤害结束特效
     */
    private void playAoeEndEffect(Location center, double range, int hitCount) {
        // 播放结束音效，音调根据命中数量调整
        float pitch = Math.min(2.0f, 1.0f + (hitCount * 0.1f));
        center.getWorld().playSound(center, Sound.ORB_PICKUP, 0.8f, pitch);
        
        // 播放额外的粒子效果表示伤害扩散完成
        for (int i = 0; i < 5; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                center.getWorld().playEffect(center, Effect.ENDER_SIGNAL, 0);
            }, i * 2L);
        }
    }
    
    /**
     * 应用护甲穿透到伤害减免计算
     */
    private double applyDamageReductionWithPenetration(double damage, Map<String, Double> victimAttributes, 
                                                       Map<String, Double> attackerAttributes) {
        double armor = victimAttributes.getOrDefault("armor", 0.0);
        double trueArmor = victimAttributes.getOrDefault("trueArmor", 0.0);
        double armorPenetration = attackerAttributes.getOrDefault("penetration", 0.0);
        
        // 护甲穿透只影响普通护甲，不影响真实抗性
        double effectiveArmor = Math.max(0, armor - armorPenetration);
        
        // 计算伤害减免（护甲穿透后的普通护甲 + 真实抗性）
        double reduction = (effectiveArmor + trueArmor) / 100.0;
        reduction = Math.min(reduction, 0.75); // 最大减免75%
        
        return damage * (1.0 - reduction);
    }
} 