package dev.charlieveg.loreattribute.data;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家属性数据类
 * 存储玩家的所有战斗属性
 * 
 * @author charlieveg
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAttribute {
    
    // 基础攻击属性
    @Builder.Default
    private double damage = 0.0;              // 攻击伤害
    @Builder.Default
    private double trueDamage = 0.0;          // 真实伤害
    @Builder.Default
    private double sumDamage = 0.0;           // 总伤害
    @Builder.Default
    private double finalDamage = 0.0;         // 百分比伤害
    @Builder.Default
    private double finalTrueDamage = 0.0;     // 百分比真实伤害
    @Builder.Default
    private double mobDamage = 0.0;           // 对怪物额外伤害
    @Builder.Default
    private double sumMobDamage = 0.0;        // 对怪物总额外伤害
    @Builder.Default
    private double aoeDamage = 0.0;           // 范围伤害
    @Builder.Default
    private double aoeRange = 0.0;            // 伤害范围
    
    // 致命击相关
    @Builder.Default
    private double crit = 0.0;                // 致命几率
    @Builder.Default
    private double critDamage = 0.0;          // 致命伤害
    @Builder.Default
    private double critArmor = 0.0;           // 致命抗性
    @Builder.Default
    private double critBreaker = 0.0;         // 招架几率
    
    // 防御属性
    @Builder.Default
    private double armor = 0.0;               // 伤害减免
    @Builder.Default
    private double trueArmor = 0.0;           // 真实抗性
    @Builder.Default
    private double dodge = 0.0;               // 闪避几率
    @Builder.Default
    private double dodgeBreaker = 0.0;        // 破闪几率
    @Builder.Default
    private double block = 0.0;               // 格挡几率
    @Builder.Default
    private double blockBreaker = 0.0;        // 强化重击
    @Builder.Default
    private double armorBreak = 0.0;          // 护甲穿透
    
    // 生命相关
    @Builder.Default
    private double health = 0.0;              // 生命值
    @Builder.Default
    private double healthHeal = 0.0;          // 生命恢复
    @Builder.Default
    private double lifeSteal = 0.0;           // 生命偷取
    @Builder.Default
    private double finalHealth = 0.0;         // 百分比生命
    
    // 移动相关
    @Builder.Default
    private double moveSpeed = 0.0;           // 移动速度
    @Builder.Default
    private double attackSpeed = 0.0;         // 攻击速度
    
    // 特殊效果
    @Builder.Default
    private double injury = 0.0;              // 反伤几率
    @Builder.Default
    private double weaken = 0.0;              // 弱化几率
    @Builder.Default
    private double mobDamageRemove = 0.0;     // 对怪物的伤害免疫
    
    // 增益效果映射
    @Builder.Default
    private Map<String, Double> buffEffects = new HashMap<>();
    
    // 武器类型特效
    @Builder.Default
    private Map<String, Double> typeEffects = new HashMap<>();
    
    /**
     * 获取指定属性值
     */
    public double getAttribute(String attributeName) {
        switch (attributeName.toLowerCase()) {
            case "damage": case "攻击伤害":
                return damage;
            case "truedamage": case "真实伤害":
                return trueDamage;
            case "sumdamage": case "总伤害":
                return sumDamage;
            case "finaldamage": case "百分比伤害":
                return finalDamage;
            case "finaltruedamage": case "百分比真实伤害":
                return finalTrueDamage;
            case "mobdamage": case "对怪物造成的额外伤害":
                return mobDamage;
            case "summobdamage": case "对怪物造成的总额外伤害":
                return sumMobDamage;
            case "aoedamage": case "范围伤害":
                return aoeDamage;
            case "aoerange": case "伤害范围":
                return aoeRange;
            case "crit": case "致命几率":
                return crit;
            case "critdamage": case "致命伤害":
                return critDamage;
            case "critarmor": case "致命抗性":
                return critArmor;
            case "critbreaker": case "招架几率":
                return critBreaker;
            case "armor": case "伤害减免":
                return armor;
            case "truearmor": case "真实抗性":
                return trueArmor;
            case "dodge": case "闪避几率":
                return dodge;
            case "dodgebreaker": case "破闪几率":
                return dodgeBreaker;
            case "block": case "格挡几率":
                return block;
            case "blockbreaker": case "强化重击":
                return blockBreaker;
            case "armorbreak": case "护甲穿透":
                return armorBreak;
            case "health": case "生命值":
                return health;
            case "healthheal": case "生命恢复":
                return healthHeal;
            case "lifesteal": case "生命偷取":
                return lifeSteal;
            case "finalhealth": case "百分比生命":
                return finalHealth;
            case "movespeed": case "移动速度":
                return moveSpeed;
            case "attackspeed": case "攻击速度":
                return attackSpeed;
            case "injury": case "反伤几率":
                return injury;
            case "weaken": case "弱化几率":
                return weaken;
            case "mobdamageremove": case "对怪物的伤害免疫":
                return mobDamageRemove;
            default:
                return 0.0;
        }
    }
    
    /**
     * 设置指定属性值
     */
    public void setAttribute(String attributeName, double value) {
        switch (attributeName.toLowerCase()) {
            case "damage": case "攻击伤害":
                this.damage = value;
                break;
            case "truedamage": case "真实伤害":
                this.trueDamage = value;
                break;
            case "sumdamage": case "总伤害":
                this.sumDamage = value;
                break;
            case "finaldamage": case "百分比伤害":
                this.finalDamage = value;
                break;
            case "finaltruedamage": case "百分比真实伤害":
                this.finalTrueDamage = value;
                break;
            case "mobdamage": case "对怪物造成的额外伤害":
                this.mobDamage = value;
                break;
            case "summobdamage": case "对怪物造成的总额外伤害":
                this.sumMobDamage = value;
                break;
            case "aoedamage": case "范围伤害":
                this.aoeDamage = value;
                break;
            case "aoerange": case "伤害范围":
                this.aoeRange = value;
                break;
            case "crit": case "致命几率":
                this.crit = value;
                break;
            case "critdamage": case "致命伤害":
                this.critDamage = value;
                break;
            case "critarmor": case "致命抗性":
                this.critArmor = value;
                break;
            case "critbreaker": case "招架几率":
                this.critBreaker = value;
                break;
            case "armor": case "伤害减免":
                this.armor = value;
                break;
            case "truearmor": case "真实抗性":
                this.trueArmor = value;
                break;
            case "dodge": case "闪避几率":
                this.dodge = value;
                break;
            case "dodgebreaker": case "破闪几率":
                this.dodgeBreaker = value;
                break;
            case "block": case "格挡几率":
                this.block = value;
                break;
            case "blockbreaker": case "强化重击":
                this.blockBreaker = value;
                break;
            case "armorbreak": case "护甲穿透":
                this.armorBreak = value;
                break;
            case "health": case "生命值":
                this.health = value;
                break;
            case "healthheal": case "生命恢复":
                this.healthHeal = value;
                break;
            case "lifesteal": case "生命偷取":
                this.lifeSteal = value;
                break;
            case "finalhealth": case "百分比生命":
                this.finalHealth = value;
                break;
            case "movespeed": case "移动速度":
                this.moveSpeed = value;
                break;
            case "attackspeed": case "攻击速度":
                this.attackSpeed = value;
                break;
            case "injury": case "反伤几率":
                this.injury = value;
                break;
            case "weaken": case "弱化几率":
                this.weaken = value;
                break;
            case "mobdamageremove": case "对怪物的伤害免疫":
                this.mobDamageRemove = value;
                break;
        }
    }
    
    /**
     * 添加属性值
     */
    public void addAttribute(String attributeName, double value) {
        setAttribute(attributeName, getAttribute(attributeName) + value);
    }
    
    /**
     * 重置所有属性
     */
    public void reset() {
        damage = 0.0;
        trueDamage = 0.0;
        sumDamage = 0.0;
        finalDamage = 0.0;
        finalTrueDamage = 0.0;
        mobDamage = 0.0;
        sumMobDamage = 0.0;
        aoeDamage = 0.0;
        aoeRange = 0.0;
        crit = 0.0;
        critDamage = 0.0;
        critArmor = 0.0;
        critBreaker = 0.0;
        armor = 0.0;
        trueArmor = 0.0;
        dodge = 0.0;
        dodgeBreaker = 0.0;
        block = 0.0;
        blockBreaker = 0.0;
        armorBreak = 0.0;
        health = 0.0;
        healthHeal = 0.0;
        lifeSteal = 0.0;
        finalHealth = 0.0;
        moveSpeed = 0.0;
        attackSpeed = 0.0;
        injury = 0.0;
        weaken = 0.0;
        mobDamageRemove = 0.0;
        buffEffects.clear();
        typeEffects.clear();
    }
} 