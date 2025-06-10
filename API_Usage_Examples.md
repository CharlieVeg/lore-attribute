# LoreAttribute API 完整使用文档

本文档详细介绍了如何在外部插件中使用 LoreAttribute API，包括所有可用方法和高级功能。

## 📋 目录

1. [添加依赖](#1-添加依赖)
2. [基本用法](#2-基本用法)
3. [高级功能](#3-高级功能)
4. [调试和监控](#4-调试和监控)
5. [完整API参考](#5-完整api参考)
6. [实际应用场景](#6-实际应用场景)
7. [注意事项](#7-注意事项)

## 1. 添加依赖

在你的插件中添加 LoreAttribute 作为依赖：

```yaml
# plugin.yml 中添加
depend: [LoreAttribute]

# 或者作为软依赖
softdepend: [LoreAttribute]
```

在代码中检查插件是否可用：

```java
if (!LoreAttributeAPI.isPluginEnabled()) {
    getLogger().warning("LoreAttribute插件未启用，相关功能将被禁用");
    return;
}
```

## 2. 基本用法

### 2.1 获取玩家属性

```java
import dev.charlieveg.loreattribute.api.LoreAttributeAPI;
import org.bukkit.entity.Player;
import java.util.Map;

public class ExamplePlugin {
    
    public void getPlayerStats(Player player) {
        // 检查API是否可用
        if (!LoreAttributeAPI.isPluginEnabled()) {
            return;
        }
        
        // 获取玩家所有属性
        Map<String, Double> attributes = LoreAttributeAPI.getPlayerAttributes(player);
        
        // 获取特定属性
        double damage = LoreAttributeAPI.getPlayerAttribute(player, "攻击伤害");
        double health = LoreAttributeAPI.getPlayerAttribute(player, "生命值");
        double critChance = LoreAttributeAPI.getPlayerAttribute(player, "致命几率");
        
        // 使用属性值
        player.sendMessage("你的攻击伤害: " + damage);
        player.sendMessage("你的生命值加成: " + health);
        player.sendMessage("你的暴击几率: " + critChance + "%");
    }
}
```

### 2.2 修改物品属性

```java
import org.bukkit.inventory.ItemStack;

public void modifyItemAttributes(ItemStack item) {
    // 设置物品类型
    ItemStack weaponItem = LoreAttributeAPI.setItemType(item, "武器");
    
    // 添加属性（累加）
    ItemStack enhancedItem = LoreAttributeAPI.addItemAttribute(weaponItem, "攻击伤害", 50.0);
    enhancedItem = LoreAttributeAPI.addItemAttribute(enhancedItem, "致命几率", 15.0);
    
    // 设置属性（覆盖）
    ItemStack finalItem = LoreAttributeAPI.setItemAttribute(enhancedItem, "生命偷取", 10.0);
    
    // 解析物品属性
    Map<String, Double> itemAttributes = LoreAttributeAPI.parseItemAttributes(finalItem);
    System.out.println("物品属性: " + itemAttributes);
}
```

### 2.3 战斗背包操作

```java
public void manageBattleInventory(Player player) {
    // 获取战斗背包物品
    ItemStack[] battleItems = LoreAttributeAPI.getBattleInventoryItems(player);
    
    // 设置战斗背包物品
    ItemStack accessory = new ItemStack(Material.DIAMOND);
    accessory = LoreAttributeAPI.setItemType(accessory, "饰品");
    accessory = LoreAttributeAPI.addItemAttribute(accessory, "移动速度", 20.0);
    
    boolean success = LoreAttributeAPI.setBattleInventoryItem(player, 0, accessory);
    if (success) {
        player.sendMessage("饰品已放入战斗背包！");
    }
    
    // 打开战斗背包界面
    LoreAttributeAPI.openBattleInventory(player);
}
```

### 2.4 实时属性更新

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class AttributeListener implements Listener {
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // 强制更新玩家属性
        LoreAttributeAPI.updatePlayerAttributes(player);
        
        // 获取更新后的属性
        double currentDamage = LoreAttributeAPI.getPlayerAttribute(player, "攻击伤害");
        player.sendMessage("当前攻击伤害: " + currentDamage);
    }
}
```

## 3. 高级用法

### 3.1 属性验证

```java
public void validateAttributes() {
    // 检查属性名称是否有效
    boolean isValid = LoreAttributeAPI.isValidAttributeName("攻击伤害");
    
    // 获取所有支持的属性
    Set<String> supportedAttributes = LoreAttributeAPI.getSupportedAttributes();
    
    // 获取所有支持的物品类型
    String[] supportedTypes = LoreAttributeAPI.getSupportedItemTypes();
    
    System.out.println("支持的属性: " + supportedAttributes);
    System.out.println("支持的类型: " + Arrays.toString(supportedTypes));
}
```

### 3.2 配置访问

```java
public void accessConfig() {
    // 获取配置值
    boolean debugMode = LoreAttributeAPI.getConfigBoolean("Debug.Combat", false);
    int updateInterval = LoreAttributeAPI.getConfigInt("AttributeUpdate.Interval", 20);
    String prefix = LoreAttributeAPI.getConfigString("Messages.Prefix", "[LoreAttribute]");
    
    System.out.println("调试模式: " + debugMode);
    System.out.println("更新间隔: " + updateInterval);
}
```

### 3.3 战斗状态检查

```java
public void checkCombatStatus() {
    // 检查是否正在处理AOE伤害
    if (LoreAttributeAPI.isProcessingAoeDamage()) {
        // 避免在AOE伤害处理期间执行某些操作
        return;
    }
    
    // 手动触发属性效果计算
    Player player = /* 获取玩家 */;
    LoreAttributeAPI.applyAttributeEffects(player);
}
```

## 4. 完整示例插件

```java
package com.example.integration;

import dev.charlieveg.loreattribute.api.LoreAttributeAPI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class LoreAttributeIntegrationExample extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("LoreAttribute集成示例插件已启用！");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        
        Player player = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("createweapon")) {
            createExampleWeapon(player);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("mystats")) {
            showPlayerStats(player);
            return true;
        }
        
        return false;
    }
    
    private void createExampleWeapon(Player player) {
        if (!LoreAttributeAPI.isPluginEnabled()) {
            player.sendMessage("LoreAttribute插件未启用！");
            return;
        }
        
        // 创建一把强力武器
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon = LoreAttributeAPI.setItemType(weapon, "武器");
        weapon = LoreAttributeAPI.addItemAttribute(weapon, "攻击伤害", 100.0);
        weapon = LoreAttributeAPI.addItemAttribute(weapon, "致命几率", 25.0);
        weapon = LoreAttributeAPI.addItemAttribute(weapon, "致命伤害", 150.0);
        weapon = LoreAttributeAPI.addItemAttribute(weapon, "生命偷取", 15.0);
        
        player.getInventory().addItem(weapon);
        player.sendMessage("已获得强力武器！");
    }
    
    private void showPlayerStats(Player player) {
        if (!LoreAttributeAPI.isPluginEnabled()) {
            player.sendMessage("LoreAttribute插件未启用！");
            return;
        }
        
        // 更新属性
        LoreAttributeAPI.updatePlayerAttributes(player);
        
        // 显示属性
        double damage = LoreAttributeAPI.getPlayerAttribute(player, "攻击伤害");
        double health = LoreAttributeAPI.getPlayerAttribute(player, "生命值");
        double crit = LoreAttributeAPI.getPlayerAttribute(player, "致命几率");
        double armor = LoreAttributeAPI.getPlayerAttribute(player, "伤害减免");
        
        player.sendMessage("=== 你的属性 ===");
        player.sendMessage("攻击伤害: " + damage);
        player.sendMessage("生命值加成: " + health);
        player.sendMessage("暴击几率: " + crit + "%");
        player.sendMessage("伤害减免: " + armor + "%");
    }
}
```

## 5. 注意事项

1. **初始化检查**: 始终使用 `LoreAttributeAPI.isPluginEnabled()` 检查插件是否可用
2. **异常处理**: API方法可能抛出 `IllegalArgumentException`，请适当处理
3. **性能考虑**: 频繁调用 `updatePlayerAttributes()` 可能影响性能
4. **版本兼容**: API版本通过 `LoreAttributeAPI.getAPIVersion()` 获取
5. **类型限制**: 物品类型必须是 "武器"、"防具" 或 "饰品" 之一

## 4. 调试和监控

### 4.1 调试功能

```java
public void debugPlayer(Player player) {
    // 获取玩家详细调试信息
    String debugInfo = LoreAttributeAPI.getPlayerAttributeDebugInfo(player);
    player.sendMessage(debugInfo);
    
    // 获取物品调试信息
    ItemStack item = player.getInventory().getItemInHand();
    if (item != null) {
        String itemDebug = LoreAttributeAPI.getItemAttributeDebugInfo(item);
        player.sendMessage(itemDebug);
    }
}
```

### 4.2 服务器统计

```java
public void showServerStats() {
    Map<String, Object> stats = LoreAttributeAPI.getOnlinePlayersStats();
    
    getLogger().info("在线玩家数: " + stats.get("onlinePlayerCount"));
    getLogger().info("有属性的玩家数: " + stats.get("playersWithAttributes"));
    getLogger().info("战斗背包物品总数: " + stats.get("totalBattleItems"));
    getLogger().info("平均每人战斗背包物品数: " + stats.get("averageBattleItemsPerPlayer"));
}
```

### 4.3 实时监控

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AttributeMonitor implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家属性
        Map<String, Double> attributes = LoreAttributeAPI.getPlayerAttributes(player);
        if (!attributes.isEmpty()) {
            getLogger().info("玩家 " + player.getName() + " 加入游戏，拥有 " + 
                           attributes.size() + " 种属性");
        }
        
        // 检查战斗背包
        int battleItems = LoreAttributeAPI.getBattleItemCount(player);
        if (battleItems > 0) {
            getLogger().info("玩家战斗背包中有 " + battleItems + " 个物品");
        }
    }
}
```

## 5. 完整API参考

### 5.1 玩家属性管理

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `getPlayerAttributes(Player)` | 获取玩家所有属性 | `Map<String, Double>` |
| `getPlayerAttribute(Player, String)` | 获取指定属性值 | `double` |
| `updatePlayerAttributes(Player)` | 强制更新属性缓存 | `void` |
| `clearPlayerAttributes(Player)` | 清除属性缓存 | `void` |
| `calculatePlayerAttributes(Player)` | 重新计算属性 | `Map<String, Double>` |
| `applyAttributeEffects(Player)` | 手动触发属性效果 | `void` |

### 5.2 物品属性操作

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `parseItemAttributes(ItemStack)` | 解析物品属性 | `Map<String, Double>` |
| `addItemAttribute(ItemStack, String, double)` | 添加物品属性 | `ItemStack` |
| `setItemAttribute(ItemStack, String, double)` | 设置物品属性 | `ItemStack` |
| `removeItemAttribute(ItemStack, String)` | 移除指定属性 | `ItemStack` |
| `removeAllItemAttributes(ItemStack)` | 移除所有属性 | `ItemStack` |
| `hasAttribute(ItemStack, String)` | 检查是否有属性 | `boolean` |
| `getItemAttributeValue(ItemStack, String)` | 获取属性值 | `double` |
| `hasAnyAttributes(ItemStack)` | 检查是否有任何属性 | `boolean` |

### 5.3 物品类型管理

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `setItemType(ItemStack, String)` | 设置物品类型 | `ItemStack` |
| `getItemType(ItemStack)` | 获取物品类型 | `String` |
| `removeItemType(ItemStack)` | 移除物品类型 | `ItemStack` |
| `getSupportedItemTypes()` | 获取支持的类型 | `String[]` |
| `getAttributesForItemType(String)` | 获取类型支持的属性 | `List<String>` |
| `isValidAttributeForItemType(String, String)` | 检查属性是否适合类型 | `boolean` |
| `isValidAttributeForItem(ItemStack, String)` | 检查属性是否适合物品 | `boolean` |

### 5.4 战斗背包管理

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `getBattleInventoryItems(Player)` | 获取所有战斗背包物品 | `ItemStack[]` |
| `setBattleInventoryItem(Player, int, ItemStack)` | 设置指定槽位物品 | `boolean` |
| `getBattleInventoryItem(Player, int)` | 获取指定槽位物品 | `ItemStack` |
| `clearBattleInventory(Player)` | 清空战斗背包 | `void` |
| `openBattleInventory(Player)` | 打开战斗背包界面 | `void` |
| `hasBattleInventory(Player)` | 检查是否有战斗背包 | `boolean` |
| `getBattleItemCount(Player)` | 获取战斗背包物品数 | `int` |
| `removeBattleInventory(Player)` | 移除战斗背包 | `void` |
| `canPlaceItemInBattleSlot(int, ItemStack)` | 检查是否可放入槽位 | `boolean` |
| `createBattleInventoryItem()` | 创建战斗背包物品 | `ItemStack` |
| `getBattleInventory(Player)` | 获取战斗背包界面 | `Inventory` |

### 5.5 属性验证

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `isValidAttributeName(String)` | 检查属性名是否有效 | `boolean` |
| `getSupportedAttributes()` | 获取所有支持的属性 | `Set<String>` |
| `getReadOnlyAttributes()` | 获取只读属性列表 | `List<String>` |
| `isReadOnlyAttribute(String)` | 检查是否为只读属性 | `boolean` |
| `getAttributeDisplayName(String)` | 获取属性显示名称 | `String` |

### 5.6 配置和工具

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `getConfigValue(String, Object)` | 获取配置值 | `Object` |
| `getConfigString(String, String)` | 获取字符串配置 | `String` |
| `getConfigBoolean(String, boolean)` | 获取布尔配置 | `boolean` |
| `getConfigInt(String, int)` | 获取整数配置 | `int` |
| `getConfigDouble(String, double)` | 获取双精度配置 | `double` |
| `reloadConfig()` | 重新加载配置 | `void` |
| `getDataFolderPath()` | 获取数据文件夹路径 | `String` |
| `saveAllBattleInventories()` | 保存所有战斗背包 | `void` |

### 5.7 调试功能

| 方法名 | 说明 | 返回值 |
|-------|-----|-------|
| `getPlayerAttributeDebugInfo(Player)` | 获取玩家调试信息 | `String` |
| `getItemAttributeDebugInfo(ItemStack)` | 获取物品调试信息 | `String` |
| `getOnlinePlayersStats()` | 获取在线玩家统计 | `Map<String, Object>` |
| `openAttributeViewer(Player)` | 打开属性查看界面 | `void` |
| `isProcessingAoeDamage()` | 检查AOE处理状态 | `boolean` |

## 6. 实际应用场景

### 6.1 RPG插件集成

```java
public class RPGIntegration {
    
    // 职业系统集成
    public void setPlayerClass(Player player, String className) {
        // 根据职业设置基础属性
        switch (className.toLowerCase()) {
            case "warrior":
                // 创建战士装备
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword = LoreAttributeAPI.setItemType(sword, "武器");
                sword = LoreAttributeAPI.addItemAttribute(sword, "攻击伤害", 80.0);
                sword = LoreAttributeAPI.addItemAttribute(sword, "生命偷取", 10.0);
                player.getInventory().addItem(sword);
                break;
                
            case "mage":
                // 创建法师饰品
                ItemStack ring = new ItemStack(Material.DIAMOND);
                ring = LoreAttributeAPI.setItemType(ring, "饰品");
                ring = LoreAttributeAPI.addItemAttribute(ring, "移动速度", 30.0);
                LoreAttributeAPI.setBattleInventoryItem(player, 1, ring);
                break;
                
            case "tank":
                // 创建坦克防具
                ItemStack armor = new ItemStack(Material.DIAMOND_CHESTPLATE);
                armor = LoreAttributeAPI.setItemType(armor, "防具");
                armor = LoreAttributeAPI.addItemAttribute(armor, "生命值", 200.0);
                armor = LoreAttributeAPI.addItemAttribute(armor, "伤害减免", 25.0);
                player.getInventory().setChestplate(armor);
                break;
        }
        
        // 更新属性
        LoreAttributeAPI.updatePlayerAttributes(player);
        player.sendMessage("已设置为 " + className + " 职业！");
    }
}
```

### 6.2 任务系统集成

```java
public class QuestIntegration {
    
    // 任务奖励装备
    public void giveQuestReward(Player player, String questId) {
        ItemStack reward;
        
        switch (questId) {
            case "dragon_slayer":
                reward = new ItemStack(Material.DIAMOND_SWORD);
                reward = LoreAttributeAPI.setItemType(reward, "武器");
                reward = LoreAttributeAPI.addItemAttribute(reward, "攻击伤害", 150.0);
                reward = LoreAttributeAPI.addItemAttribute(reward, "致命几率", 20.0);
                reward = LoreAttributeAPI.addItemAttribute(reward, "范围伤害", 50.0);
                reward = LoreAttributeAPI.addItemAttribute(reward, "范围距离", 3.0);
                break;
                
            case "speed_runner":
                reward = new ItemStack(Material.GOLD_BOOTS);
                reward = LoreAttributeAPI.setItemType(reward, "饰品");
                reward = LoreAttributeAPI.addItemAttribute(reward, "移动速度", 50.0);
                break;
                
            default:
                return;
        }
        
        player.getInventory().addItem(reward);
        player.sendMessage("获得任务奖励装备！");
    }
}
```

### 6.3 商店系统集成

```java
public class ShopIntegration {
    
    // 装备强化系统
    public boolean enhanceItem(Player player, ItemStack item, int level) {
        if (!LoreAttributeAPI.hasAnyAttributes(item)) {
            player.sendMessage("该物品无法强化！");
            return false;
        }
        
        String itemType = LoreAttributeAPI.getItemType(item);
        if (itemType.isEmpty()) {
            player.sendMessage("未知类型的物品无法强化！");
            return false;
        }
        
        // 根据强化等级增加属性
        double multiplier = 1.0 + (level * 0.1); // 每级+10%
        
        Map<String, Double> currentAttributes = LoreAttributeAPI.parseItemAttributes(item);
        ItemStack enhanced = item.clone();
        
        // 移除所有属性后重新添加强化后的属性
        enhanced = LoreAttributeAPI.removeAllItemAttributes(enhanced);
        
        for (Map.Entry<String, Double> entry : currentAttributes.entrySet()) {
            String attrName = LoreAttributeAPI.getAttributeDisplayName(entry.getKey());
            double newValue = entry.getValue() * multiplier;
            enhanced = LoreAttributeAPI.addItemAttribute(enhanced, attrName, newValue);
        }
        
        // 替换玩家手中的物品
        player.getInventory().setItemInHand(enhanced);
        LoreAttributeAPI.updatePlayerAttributes(player);
        
        player.sendMessage("装备强化成功！等级: +" + level);
        return true;
    }
}
```

### 6.4 PVP插件集成

```java
public class PVPIntegration implements Listener {
    
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // 获取攻击者属性
        double attackDamage = LoreAttributeAPI.getPlayerAttribute(attacker, "攻击伤害");
        double critChance = LoreAttributeAPI.getPlayerAttribute(attacker, "致命几率");
        
        // 获取受害者属性
        double armor = LoreAttributeAPI.getPlayerAttribute(victim, "伤害减免");
        double dodgeChance = LoreAttributeAPI.getPlayerAttribute(victim, "闪避几率");
        
        // 检查闪避
        if (Math.random() * 100 < dodgeChance) {
            event.setCancelled(true);
            victim.sendMessage("§a成功闪避了攻击！");
            attacker.sendMessage("§c对方闪避了你的攻击！");
            return;
        }
        
        // 计算最终伤害
        double finalDamage = event.getDamage() + attackDamage;
        
        // 检查暴击
        if (Math.random() * 100 < critChance) {
            double critMultiplier = 1.5 + (LoreAttributeAPI.getPlayerAttribute(attacker, "致命伤害") / 100.0);
            finalDamage *= critMultiplier;
            attacker.sendMessage("§6暴击！");
            victim.sendMessage("§c受到暴击伤害！");
        }
        
        // 应用护甲减免
        finalDamage *= (1.0 - armor / 100.0);
        
        event.setDamage(finalDamage);
    }
}
```

## 7. 注意事项

### 7.1 性能优化

1. **缓存机制**: API内部使用缓存，避免频繁调用 `updatePlayerAttributes()`
2. **批量操作**: 在修改多个属性时，考虑批量处理
3. **异步处理**: 对于耗时操作，考虑使用异步任务

```java
// 好的做法 - 批量修改
ItemStack item = player.getInventory().getItemInHand();
item = LoreAttributeAPI.addItemAttribute(item, "攻击伤害", 50.0);
item = LoreAttributeAPI.addItemAttribute(item, "致命几率", 15.0);
item = LoreAttributeAPI.addItemAttribute(item, "生命偷取", 10.0);
player.getInventory().setItemInHand(item);
LoreAttributeAPI.updatePlayerAttributes(player); // 只调用一次更新

// 避免的做法 - 频繁更新
for (int i = 0; i < 10; i++) {
    LoreAttributeAPI.updatePlayerAttributes(player); // 每次循环都更新
}
```

### 7.2 错误处理

```java
public void safeAttributeOperation(Player player, ItemStack item) {
    try {
        // 检查前置条件
        if (!LoreAttributeAPI.isPluginEnabled()) {
            throw new IllegalStateException("LoreAttribute插件未启用");
        }
        
        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("无效的物品");
        }
        
        // 执行操作
        String itemType = LoreAttributeAPI.getItemType(item);
        if (itemType.isEmpty()) {
            player.sendMessage("§c该物品没有设置类型，无法添加属性");
            return;
        }
        
        if (!LoreAttributeAPI.isValidAttributeForItemType(itemType, "攻击伤害")) {
            player.sendMessage("§c该类型物品不支持攻击伤害属性");
            return;
        }
        
        // 安全执行
        ItemStack enhanced = LoreAttributeAPI.addItemAttribute(item, "攻击伤害", 50.0);
        player.getInventory().setItemInHand(enhanced);
        LoreAttributeAPI.updatePlayerAttributes(player);
        
    } catch (IllegalArgumentException e) {
        player.sendMessage("§c参数错误: " + e.getMessage());
    } catch (Exception e) {
        player.sendMessage("§c操作失败，请联系管理员");
        getLogger().severe("属性操作失败: " + e.getMessage());
    }
}
```

### 7.3 版本兼容性

```java
public class VersionCompatibility {
    
    public void checkCompatibility() {
        String apiVersion = LoreAttributeAPI.getAPIVersion();
        
        switch (apiVersion) {
            case "1.0":
                // 使用1.0版本的功能
                break;
            default:
                getLogger().warning("未知的API版本: " + apiVersion);
                break;
        }
    }
}
```

### 7.4 最佳实践

1. **类型检查**: 始终检查物品类型和属性兼容性
2. **权限验证**: 在修改玩家属性前检查权限
3. **输入验证**: 验证所有用户输入
4. **事务性操作**: 将相关操作组合在一起执行
5. **日志记录**: 记录重要操作和错误信息

```java
public class BestPractices {
    
    public void createPowerfulWeapon(Player player, String weaponType) {
        // 1. 权限检查
        if (!player.hasPermission("myplugin.create.weapon")) {
            player.sendMessage("§c你没有权限创建武器");
            return;
        }
        
        // 2. 输入验证
        if (weaponType == null || weaponType.trim().isEmpty()) {
            player.sendMessage("§c武器类型不能为空");
            return;
        }
        
        // 3. API可用性检查
        if (!LoreAttributeAPI.isPluginEnabled()) {
            player.sendMessage("§c属性系统未启用");
            return;
        }
        
        try {
            // 4. 事务性操作
            ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
            weapon = LoreAttributeAPI.setItemType(weapon, "武器");
            weapon = LoreAttributeAPI.addItemAttribute(weapon, "攻击伤害", 100.0);
            weapon = LoreAttributeAPI.addItemAttribute(weapon, "致命几率", 25.0);
            
            player.getInventory().addItem(weapon);
            LoreAttributeAPI.updatePlayerAttributes(player);
            
            // 5. 日志记录
            getLogger().info("为玩家 " + player.getName() + " 创建了强力武器");
            player.sendMessage("§a成功创建强力武器！");
            
        } catch (Exception e) {
            getLogger().severe("创建武器失败: " + e.getMessage());
            player.sendMessage("§c创建武器失败，请联系管理员");
        }
    }
}
```

---

## 🔗 相关链接

- **插件主页**: [LoreAttribute Plugin]
- **API版本**: `1.0`
- **支持版本**: Bukkit 1.7+
- **开发者**: charlieveg
