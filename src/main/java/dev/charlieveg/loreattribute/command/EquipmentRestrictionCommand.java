package dev.charlieveg.loreattribute.command;

import dev.charlieveg.loreattribute.manager.EquipmentRestrictionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 装备限制命令处理器
 */
@RequiredArgsConstructor
public class EquipmentRestrictionCommand implements CommandExecutor, TabCompleter {
    
    private final EquipmentRestrictionManager restrictionManager;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("loreattribute.restriction")) {
            sender.sendMessage("§c你没有权限使用此命令。");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                restrictionManager.reloadConfig();
                sender.sendMessage("§a装备限制配置已重载。");
                break;
                
            case "check":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c此命令只能由玩家执行。");
                    return true;
                }
                
                Player player = (Player) sender;
                checkPlayerRestrictions(player);
                break;
                
            case "test":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c此命令只能由玩家执行。");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /restriction test <lore文本>");
                    return true;
                }
                
                Player testPlayer = (Player) sender;
                String loreName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                testLoreRestriction(testPlayer, loreName);
                break;
                
            case "info":
                showRestrictionInfo(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "check", "test", "info"));
        }
        
        return completions;
    }
    
    /**
     * 显示帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== 装备限制系统命令 ===");
        sender.sendMessage("§e/restriction reload §7- 重载配置文件");
        sender.sendMessage("§e/restriction check §7- 检查当前装备的限制状态");
        sender.sendMessage("§e/restriction test <lore> §7- 测试指定lore的限制情况");
        sender.sendMessage("§e/restriction info §7- 显示当前限制规则信息");
    }
    
    /**
     * 检查玩家当前装备的限制状态
     */
    private void checkPlayerRestrictions(Player player) {
        player.sendMessage("§6=== 装备限制检查 ===");
        
        List<ItemStack> equippedItems = getEquippedItems(player);
        ItemStack handItem = player.getItemInHand();
        
        // 检查手持物品
        if (handItem != null && handItem.hasItemMeta() && handItem.getItemMeta().hasDisplayName()) {
            boolean restricted = restrictionManager.isRestricted(equippedItems, handItem);
            player.sendMessage("§e手持物品: §f" + handItem.getItemMeta().getDisplayName() + 
                             " §7- " + (restricted ? "§c被限制" : "§a允许"));
        }
        
        // 检查护甲
        checkArmorPiece(player, player.getInventory().getHelmet(), "头盔", equippedItems);
        checkArmorPiece(player, player.getInventory().getChestplate(), "胸甲", equippedItems);
        checkArmorPiece(player, player.getInventory().getLeggings(), "护腿", equippedItems);
        checkArmorPiece(player, player.getInventory().getBoots(), "靴子", equippedItems);
    }
    
    /**
     * 检查护甲部件
     */
    private void checkArmorPiece(Player player, ItemStack armor, String type, List<ItemStack> equippedItems) {
        if (armor != null && armor.hasItemMeta() && armor.getItemMeta().hasDisplayName()) {
            boolean restricted = restrictionManager.isRestricted(equippedItems, armor);
            player.sendMessage("§e" + type + ": §f" + armor.getItemMeta().getDisplayName() + 
                             " §7- " + (restricted ? "§c被限制" : "§a允许"));
        }
    }
    
    /**
     * 测试指定lore的限制情况
     */
    private void testLoreRestriction(Player player, String loreName) {
        player.sendMessage("§6=== 测试Lore限制: §f" + loreName + " §6===");
        
        // 检查冲突限制
        for (String groupName : restrictionManager.getConflictGroups().keySet()) {
            if (restrictionManager.getConflictGroups().get(groupName).contains(loreName)) {
                player.sendMessage("§e冲突限制组 §f" + groupName + "§e: §c与以下lore冲突");
                for (String conflictLore : restrictionManager.getConflictGroups().get(groupName)) {
                    if (!conflictLore.equals(loreName)) {
                        player.sendMessage("  §7- §f" + conflictLore);
                    }
                }
            }
        }
        
        // 检查专属限制
        if (restrictionManager.getExclusiveGroups().contains(loreName)) {
            player.sendMessage("§e专属限制: §c装备此lore后只能使用同样lore的物品");
        }
        
        // 检查前置限制
        if (restrictionManager.getPrerequisiteGroups().contains(loreName)) {
            player.sendMessage("§e前置限制: §c需要装备此lore才能使用相关武器");
        }
    }
    
    /**
     * 显示限制规则信息
     */
    private void showRestrictionInfo(CommandSender sender) {
        sender.sendMessage("§6=== 装备限制规则信息 ===");
        
        // 冲突限制
        sender.sendMessage("§e冲突限制组 §7(" + restrictionManager.getConflictGroups().size() + "个):");
        for (String groupName : restrictionManager.getConflictGroups().keySet()) {
            sender.sendMessage("  §f" + groupName + "§7: " + 
                             String.join(", ", restrictionManager.getConflictGroups().get(groupName)));
        }
        
        // 专属限制
        sender.sendMessage("§e专属限制 §7(" + restrictionManager.getExclusiveGroups().size() + "个):");
        sender.sendMessage("  §f" + String.join(", ", restrictionManager.getExclusiveGroups()));
        
        // 前置限制
        sender.sendMessage("§e前置限制 §7(" + restrictionManager.getPrerequisiteGroups().size() + "个):");
        sender.sendMessage("  §f" + String.join(", ", restrictionManager.getPrerequisiteGroups()));
    }
    
    /**
     * 获取玩家已装备的所有物品
     */
    private List<ItemStack> getEquippedItems(Player player) {
        List<ItemStack> equippedItems = new ArrayList<>();
        
        // 添加护甲
        if (player.getInventory().getHelmet() != null) {
            equippedItems.add(player.getInventory().getHelmet());
        }
        if (player.getInventory().getChestplate() != null) {
            equippedItems.add(player.getInventory().getChestplate());
        }
        if (player.getInventory().getLeggings() != null) {
            equippedItems.add(player.getInventory().getLeggings());
        }
        if (player.getInventory().getBoots() != null) {
            equippedItems.add(player.getInventory().getBoots());
        }
        
        // 添加手持物品
        if (player.getItemInHand() != null) {
            equippedItems.add(player.getItemInHand());
        }
        
        return equippedItems;
    }
} 