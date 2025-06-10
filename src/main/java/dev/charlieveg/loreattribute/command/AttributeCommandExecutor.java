package dev.charlieveg.loreattribute.command;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import dev.charlieveg.loreattribute.manager.LoreEditorManager;
import dev.charlieveg.loreattribute.ui.AttributeViewerUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 属性命令执行器
 */
public class AttributeCommandExecutor implements CommandExecutor, TabCompleter {
    
    private final LoreAttributePlugin plugin;
    
    // 武器属性列表
    private final List<String> weaponAttributes = Arrays.asList(
        "攻击伤害", "致命几率", "致命伤害", "生命偷取", "真实伤害", "护甲穿透", 
        "弱化几率", "范围伤害", "范围距离"
    );
    
    // 防具属性列表  
    private final List<String> armorAttributes = Arrays.asList(
        "生命值", "伤害减免", "真实抗性", "闪避几率", "格挡几率", "生命恢复", "反伤几率"
    );
    
    // 饰品属性列表
    private final List<String> accessoryAttributes = Arrays.asList(
        "移动速度", "致命几率", "生命值", "生命恢复", "攻击伤害"
    );
    
    // 不可编辑的属性
    private final List<String> readOnlyAttributes = Arrays.asList(
        "总伤害"
    );
    
    // 可用的物品类型列表
    private final List<String> itemTypeList = Arrays.asList(
        "武器", "防具", "饰品"
    );
    
    public AttributeCommandExecutor(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelpMessage(player);
                break;
                
            case "add":
                handleAddCommand(player, args);
                break;
                
            case "set":
                handleSetCommand(player, args);
                break;
                
            case "type":
                handleTypeCommand(player, args);
                break;
                
            case "view":
            case "show":
                handleViewCommand(player);
                break;
                
            case "battle":
                handleBattleCommand(player);
                break;
                
            case "reload":
                handleReloadCommand(player);
                break;
                
            case "debug":
                handleDebugCommand(player, args);
                break;
                
            case "edit":
                handleEditCommand(player, args);
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "未知命令！使用 /latr help 查看帮助");
                break;
        }
        
        return true;
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelpMessage(Player player) {
        player.sendMessage("§6========== LoreAttribute 命令帮助 ==========");
        player.sendMessage("§e/latr add <属性名> <数值> §7- 为手持物品添加属性");
        player.sendMessage("§e/latr set <属性名> <数值> §7- 为手持物品设置属性");
        player.sendMessage("§e/latr type <类型> §7- 设置手持物品类型");
        player.sendMessage("§e/latr view §7- 查看当前属性");
        player.sendMessage("§e/latr battle §7- 打开战斗背包");
        player.sendMessage("§e/latr debug item §7- 调试物品属性解析");
        player.sendMessage("§e/latr debug update §7- 强制更新属性");
        player.sendMessage("§e/latr debug show §7- 显示详细属性信息");
        player.sendMessage("§e/latr reload §7- 重载配置文件");
        player.sendMessage("§6==========================================");
        player.sendMessage("§7可用类型: 武器、防具、饰品");
        player.sendMessage("§7特殊效果会在添加时给出提示！");
    }
    
    /**
     * 处理添加属性命令
     */
    private void handleAddCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /latr add <属性> <数值>");
            return;
        }
        
        if (!player.hasPermission("loreattribute.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return;
        }
        
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持一个物品！");
            return;
        }
        
        String attributeName = args[1];
        String valueStr = args[2];
        
        // 检查是否为不可编辑属性
        if (readOnlyAttributes.contains(attributeName)) {
            player.sendMessage(ChatColor.RED + "属性 '" + attributeName + "' 是只读属性，无法编辑！");
            player.sendMessage(ChatColor.YELLOW + "该属性由系统自动计算生成。");
            return;
        }
        
        // 检查属性是否适合物品类型
        if (!isValidAttributeForItem(item, attributeName)) {
            String itemType = getItemType(item);
            player.sendMessage(ChatColor.RED + "属性 '" + attributeName + "' 不适用于 " + itemType + " 类型的物品！");
            showValidAttributesForType(player, itemType);
            return;
        }
        
        try {
            double value = Double.parseDouble(valueStr);
            addAttributeToItem(item, attributeName, value);
            
            // 更新玩家属性
            plugin.getAttributeManager().updatePlayerAttributes(player);
            
            player.sendMessage(ChatColor.GREEN + "成功为物品添加属性: " + attributeName + " +" + value);
            
            // 添加特殊效果提示
            showAttributeEffectTip(player, attributeName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "无效的数值格式！");
        }
    }
    
    /**
     * 处理设置属性命令
     */
    private void handleSetCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /latr set <属性> <数值>");
            return;
        }
        
        if (!player.hasPermission("loreattribute.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return;
        }
        
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持一个物品！");
            return;
        }
        
        String attributeName = args[1];
        String valueStr = args[2];
        
        try {
            double value = Double.parseDouble(valueStr);
            setAttributeToItem(item, attributeName, value);
            
            // 更新玩家属性
            plugin.getAttributeManager().updatePlayerAttributes(player);
            
            player.sendMessage(ChatColor.GREEN + "成功设置物品属性: " + attributeName + " = " + value);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "无效的数值格式！");
        }
    }
    
    /**
     * 处理类型设置命令
     */
    private void handleTypeCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /latr type <类型>");
            player.sendMessage(ChatColor.YELLOW + "可用类型: " + String.join(", ", itemTypeList));
            return;
        }
        
        if (!player.hasPermission("loreattribute.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return;
        }
        
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持一个物品！");
            return;
        }
        
        String type = args[1];
        if (!itemTypeList.contains(type)) {
            player.sendMessage(ChatColor.RED + "无效的类型！可用类型: " + String.join(", ", itemTypeList));
            return;
        }
        
        setTypeToItem(item, type);
        player.sendMessage(ChatColor.GREEN + "成功设置物品类型: " + type);
    }
    
    /**
     * 处理查看属性命令
     */
    private void handleViewCommand(Player player) {
        AttributeViewerUI ui = new AttributeViewerUI();
        ui.openAttributeViewer(player);
    }
    
    /**
     * 处理战斗背包命令
     */
    private void handleBattleCommand(Player player) {
        plugin.getBattleInventoryManager().openBattleInventory(player);
    }
    
    /**
     * 处理重载命令
     */
    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("loreattribute.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return;
        }
        
        plugin.getConfigManager().loadConfig();
        player.sendMessage(ChatColor.GREEN + "配置文件已重载！");
    }
    
    /**
     * 处理编辑命令
     */
    private void handleEditCommand(Player player, String[] args) {
        LoreEditorManager editorManager = plugin.getLoreEditorManager();
        
        if (args.length > 1) {
            String action = args[1].toLowerCase();
            switch (action) {
                case "save":
                case "保存":
                    if (editorManager.saveEdit(player)) {
                        player.sendMessage(ChatColor.GREEN + "已保存lore更改！");
                    } else {
                        player.sendMessage(ChatColor.RED + "没有正在进行的编辑或保存失败！");
                    }
                    break;
                case "cancel":
                case "取消":
                    editorManager.cancelEdit(player);
                    player.sendMessage(ChatColor.YELLOW + "已取消lore编辑。");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "未知的编辑操作！");
                    break;
            }
        } else {
            // 开始编辑手持物品
            ItemStack item = player.getInventory().getItemInHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "请手持一个物品！");
                return;
            }
            
            if (editorManager.startEdit(player, item) != null) {
                player.sendMessage(ChatColor.GREEN + "已开始编辑lore，正在打开编辑器...");
                plugin.getLoreEditorUI().openEditor(player);
            } else {
                player.sendMessage(ChatColor.RED + "无法开始编辑该物品！");
            }
        }
    }
    
    /**
     * 处理调试命令
     */
    private void handleDebugCommand(Player player, String[] args) {
        if (!player.hasPermission("loreattribute.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /latr debug <item|update|show>");
            return;
        }
        
        String debugType = args[1].toLowerCase();
        switch (debugType) {
            case "item":
                debugItemAttributes(player);
                break;
            case "update":
                plugin.getAttributeManager().updatePlayerAttributes(player);
                player.sendMessage(ChatColor.GREEN + "属性已强制更新！");
                break;
            case "show":
                showDetailedAttributes(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "未知的调试类型！");
                break;
        }
    }
    
    /**
     * 为物品添加属性（累加到现有属性）
     */
    private void addAttributeToItem(ItemStack item, String attributeName, double value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta(); // 获取默认meta
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // 查找现有的同名属性
        double existingValue = 0.0;
        boolean foundExisting = false;
        
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine.startsWith(attributeName + ":")) {
                // 解析现有值
                String[] parts = cleanLine.split(":");
                if (parts.length > 1) {
                    String valueStr = parts[1].trim().replace("+", "");
                    try {
                        existingValue = Double.parseDouble(valueStr);
                        foundExisting = true;
                        lore.remove(i); // 移除旧行
                        break;
                    } catch (NumberFormatException e) {
                        // 如果解析失败，移除旧行并重新添加
                        lore.remove(i);
                        break;
                    }
                }
            }
        }
        
        // 计算新值
        double newValue = foundExisting ? existingValue + value : value;
        
        // 添加新属性行
        String attributeLine = "§e" + attributeName + ": §a+" + formatValue(newValue);
        lore.add(attributeLine);
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * 为物品设置属性（替换现有）
     */
    private void setAttributeToItem(ItemStack item, String attributeName, double value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta(); // 获取默认meta
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // 移除现有的同名属性
        lore.removeIf(line -> {
            String cleanLine = ChatColor.stripColor(line);
            return cleanLine.startsWith(attributeName + ":");
        });
        
        // 添加新属性
        String attributeLine = "§e" + attributeName + ": §a+" + formatValue(value);
        lore.add(attributeLine);
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * 为物品设置类型
     */
    private void setTypeToItem(ItemStack item, String type) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta(); // 获取默认meta
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // 移除现有的类型
        lore.removeIf(line -> {
            String cleanLine = ChatColor.stripColor(line);
            return cleanLine.startsWith("类型:") || itemTypeList.stream().anyMatch(cleanLine::contains);
        });
        
        // 添加新类型
        String typeLine = "§7类型: §b" + type;
        lore.add(0, typeLine); // 添加到第一行
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * 调试物品属性解析
     */
    private void debugItemAttributes(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持一个物品！");
            return;
        }
        
        player.sendMessage("§6========== 物品属性解析调试 ==========");
        player.sendMessage("§b物品: " + item.getType().name());
        
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            player.sendMessage("§e原始Lore:");
            for (int i = 0; i < item.getItemMeta().getLore().size(); i++) {
                player.sendMessage("  " + (i + 1) + ". " + item.getItemMeta().getLore().get(i));
            }
            
            // 显示解析出的属性
            Map<String, Double> attributes = plugin.getAttributeManager().parseItemAttributes(item);
            player.sendMessage("§e解析出的属性 (" + attributes.size() + "个):");
            for (Map.Entry<String, Double> entry : attributes.entrySet()) {
                player.sendMessage("  " + entry.getKey() + " = " + entry.getValue());
            }
            
            // 显示物品信息
            String itemTypeName = getItemTypeName(item);
            player.sendMessage("§e物品类型: " + itemTypeName);
        } else {
            player.sendMessage("§e该物品没有lore信息");
        }
        
        player.sendMessage("§6========================================");
    }
    
    /**
     * 获取物品类型名称
     */
    private String getItemTypeName(ItemStack item) {
        if (item == null) return "未知";
        
        String materialName = item.getType().name();
        
        // 检查lore中的类型信息
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            for (String line : lore) {
                String cleanLine = ChatColor.stripColor(line);
                if (cleanLine.startsWith("类型:")) {
                    return cleanLine.substring(3).trim();
                }
                // 检查是否包含已知的物品类型
                for (String type : itemTypeList) {
                    if (cleanLine.contains(type)) {
                        return type;
                    }
                }
            }
        }
        
        // 根据材质自动识别
        if (materialName.contains("SWORD")) {
            return "近战武器";
        } else if (materialName.contains("BOW")) {
            return "弓箭";
        } else if (materialName.contains("HELMET") || materialName.contains("CHESTPLATE") || 
                   materialName.contains("LEGGINGS") || materialName.contains("BOOTS")) {
            return "护甲";
        } else if (materialName.contains("PICKAXE") || materialName.contains("AXE") || 
                   materialName.contains("SHOVEL") || materialName.contains("HOE")) {
            return "工具";
        }
        
        return "其他";
    }
    
    /**
     * 显示详细属性信息
     */
    private void showDetailedAttributes(Player player) {
        Map<String, Double> attributes = plugin.getAttributeManager().getCachedPlayerAttributes(player);
        
        player.sendMessage("§6========== 详细属性信息 ==========");
        player.sendMessage("§7缓存属性数量: " + attributes.size());
        
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            player.sendMessage("  §e" + entry.getKey() + ": §a" + formatValue(entry.getValue()));
        }
        
        player.sendMessage("§6==================================");
    }
    
    /**
     * 格式化数值显示
     */
    private String formatValue(double value) {
        if (value % 1 == 0) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.1f", value);
        }
    }
    
    /**
     * 获取物品类型（简化版）
     */
    private String getItemType(ItemStack item) {
        if (item == null) return "未知";
        
        // 检查lore中的类型信息
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            for (String line : lore) {
                String cleanLine = ChatColor.stripColor(line);
                if (cleanLine.startsWith("类型:")) {
                    String type = cleanLine.substring(3).trim();
                    if (itemTypeList.contains(type)) {
                        return type;
                    }
                }
            }
        }
        
        // 默认根据材质推断
        String materialName = item.getType().name();
        if (materialName.contains("HELMET") || materialName.contains("CHESTPLATE") || 
            materialName.contains("LEGGINGS") || materialName.contains("BOOTS")) {
            return "防具";
        } else if (materialName.contains("SWORD") || materialName.contains("BOW") || 
                   materialName.contains("AXE")) {
            return "武器";
        }
        
        return "饰品"; // 默认为饰品
    }
    
    /**
     * 检查属性是否适合物品类型
     */
    private boolean isValidAttributeForItem(ItemStack item, String attributeName) {
        String itemType = getItemType(item);
        
        switch (itemType) {
            case "武器":
                return weaponAttributes.contains(attributeName);
            case "防具":
                return armorAttributes.contains(attributeName);
            case "饰品":
                return accessoryAttributes.contains(attributeName);
            default:
                return false;
        }
    }
    
    /**
     * 显示类型对应的有效属性
     */
    private void showValidAttributesForType(Player player, String itemType) {
        player.sendMessage(ChatColor.YELLOW + itemType + " 类型可用属性:");
        
        List<String> validAttributes;
        String effectInfo;
        
        switch (itemType) {
            case "武器":
                validAttributes = weaponAttributes;
                effectInfo = "§7武器在手持时生效";
                break;
            case "防具":
                validAttributes = armorAttributes;
                effectInfo = "§7防具在穿戴时生效";
                break;
            case "饰品":
                validAttributes = accessoryAttributes;
                effectInfo = "§7饰品在战斗背包中时生效";
                break;
            default:
                return;
        }
        
        player.sendMessage(effectInfo);
        for (String attr : validAttributes) {
            player.sendMessage("  §e- " + attr);
        }
    }
    
    /**
     * 显示属性特殊效果提示
     */
    private void showAttributeEffectTip(Player player, String attributeName) {
        switch (attributeName) {
            case "致命几率":
                player.sendMessage("§6💡 提示: 致命几率决定暴击触发概率，暴击时会显示特殊提示！");
                break;
            case "致命伤害":
                player.sendMessage("§6💡 提示: 致命伤害决定暴击时的额外伤害倍数！");
                break;
            case "生命偷取":
                player.sendMessage("§6💡 提示: 生命偷取会在造成伤害时恢复生命值，会显示恢复提示！");
                break;
            case "闪避几率":
                player.sendMessage("§6💡 提示: 闪避成功时会显示闪避提示并完全免疫伤害！");
                break;
            case "格挡几率":
                player.sendMessage("§6💡 提示: 格挡成功时会显示格挡提示并减少伤害！");
                break;
            case "真实伤害":
                player.sendMessage("§6💡 提示: 真实伤害无视敌人的护甲和伤害减免！");
                break;
            case "护甲穿透":
                player.sendMessage("§6💡 提示: 护甲穿透会降低敌人的伤害减免效果！");
                break;
            case "弱化几率":
                player.sendMessage("§6💡 提示: 弱化几率会给敌人添加虚弱效果，降低其攻击力！");
                break;
            case "范围伤害":
                player.sendMessage("§6💡 提示: 范围伤害会对主目标周围的敌人造成额外伤害！需配合范围距离属性使用！");
                break;
            case "范围距离":
                player.sendMessage("§6💡 提示: 范围距离决定范围伤害的作用范围！需配合范围伤害属性使用！");
                break;
            case "反伤几率":
                player.sendMessage("§6💡 提示: 反伤几率会在受到攻击时对攻击者造成反击伤害！");
                break;
            case "真实抗性":
                player.sendMessage("§6💡 提示: 真实抗性提供不受护甲穿透影响的伤害减免！");
                break;
        }
    }
    
    /**
     * 获取所有可用属性（用于Tab补全）
     */
    private List<String> getAllAttributes() {
        List<String> allAttributes = new ArrayList<>();
        allAttributes.addAll(weaponAttributes);
        allAttributes.addAll(armorAttributes);
        allAttributes.addAll(accessoryAttributes);
        return allAttributes;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            String[] subCommands = {"help", "add", "set", "type", "view", "battle", "edit", "debug", "reload"};
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("add".equals(subCommand) || "set".equals(subCommand)) {
                // 属性名补全
                List<String> allAttributes = getAllAttributes();
                for (String attr : allAttributes) {
                    if (attr.startsWith(args[1])) {
                        completions.add(attr);
                    }
                }
            } else if ("type".equals(subCommand)) {
                // 类型补全
                for (String type : itemTypeList) {
                    if (type.startsWith(args[1])) {
                        completions.add(type);
                    }
                }
            } else if ("debug".equals(subCommand)) {
                String[] debugTypes = {"item", "update", "show"};
                for (String debugType : debugTypes) {
                    if (debugType.startsWith(args[1].toLowerCase())) {
                        completions.add(debugType);
                    }
                }
            } else if ("edit".equals(subCommand)) {
                String[] editActions = {"save", "cancel"};
                for (String action : editActions) {
                    if (action.startsWith(args[1].toLowerCase())) {
                        completions.add(action);
                    }
                }
            }
        }
        
        return completions;
    }
} 