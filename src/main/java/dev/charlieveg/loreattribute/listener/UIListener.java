package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.LoreAttributePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * UI界面事件监听器
 * 处理属性查看界面等UI的点击事件
 * 
 * @author charlieveg
 */
public class UIListener implements Listener {
    
    private final LoreAttributePlugin plugin;
    
    public UIListener(LoreAttributePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理UI界面点击事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();
        
        // 检查是否是属性查看界面
        if (title.contains("玩家属性信息")) {
            event.setCancelled(true); // 取消所有点击事件
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }
            
            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            
            // 处理按钮点击
            switch (itemName) {
                case "刷新属性":
                    handleRefreshClick(player);
                    break;
                case "战斗背包":
                    handleBattleInventoryClick(player);
                    break;
                case "帮助":
                    handleHelpClick(player);
                    break;
                case "关闭":
                    handleCloseClick(player);
                    break;
                default:
                    // 点击属性物品，显示详细信息
                    if (isAttributeItem(itemName)) {
                        handleAttributeClick(player, itemName);
                    }
                    break;
            }
        }
    }
    
    /**
     * 处理刷新按钮点击
     */
    private void handleRefreshClick(Player player) {
        player.sendMessage(ChatColor.GREEN + "正在刷新属性数据...");
        
        // 更新属性并重新打开界面
        plugin.getAttributeManager().updatePlayerAttributes(player);
        plugin.getAttributeViewerUI().openAttributeViewer(player);
        player.sendMessage(ChatColor.GREEN + "属性数据已刷新！");
    }
    
    /**
     * 处理战斗背包按钮点击
     */
    private void handleBattleInventoryClick(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "正在打开战斗背包...");
        
        // 打开战斗背包
        plugin.getBattleInventoryManager().openBattleInventory(player);
    }
    
    /**
     * 处理帮助按钮点击
     */
    private void handleHelpClick(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "========== LoreAttribute 插件帮助 ==========");
        player.sendMessage(ChatColor.YELLOW + "/latr info" + ChatColor.WHITE + " - 查看玩家属性信息");
        player.sendMessage(ChatColor.YELLOW + "/latr battle" + ChatColor.WHITE + " - 打开战斗背包");
        player.sendMessage(ChatColor.YELLOW + "/latr reload" + ChatColor.WHITE + " - 重载配置文件 (管理员)");
        player.sendMessage(ChatColor.YELLOW + "/latr add <属性> <数值>" + ChatColor.WHITE + " - 为手持物品添加属性 (管理员)");
        player.sendMessage(ChatColor.YELLOW + "/latr set <属性> <数值>" + ChatColor.WHITE + " - 为手持物品设置属性 (管理员)");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "战斗背包:");
        player.sendMessage(ChatColor.WHITE + "- 使用战斗背包物品右键打开");
        player.sendMessage(ChatColor.WHITE + "- 需要对应槽位核心的物品才能放入");
        player.sendMessage(ChatColor.WHITE + "- 放入物品后将自动计算属性加成");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "属性系统:");
        player.sendMessage(ChatColor.WHITE + "- 所有属性通过物品lore自动识别");
        player.sendMessage(ChatColor.WHITE + "- 支持装备、手持物品、战斗背包的属性计算");
        player.sendMessage(ChatColor.WHITE + "- 属性会在装备变化时自动更新");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "========================================");
    }
    
    /**
     * 处理关闭按钮点击
     */
    private void handleCloseClick(Player player) {
        player.closeInventory();
        player.sendMessage(ChatColor.GRAY + "界面已关闭");
    }
    
    /**
     * 处理属性物品点击
     */
    private void handleAttributeClick(Player player, String attributeName) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "========== " + attributeName + " 详细信息 ==========");
        
        // 这里应该显示详细的属性信息
        switch (attributeName) {
            case "生命值":
                player.sendMessage(ChatColor.RED + "生命值" + ChatColor.WHITE + " - 增加玩家的最大生命值");
                player.sendMessage(ChatColor.GRAY + "每1点生命值 = 0.5颗心");
                break;
            case "攻击伤害":
                player.sendMessage(ChatColor.YELLOW + "攻击伤害" + ChatColor.WHITE + " - 增加玩家的攻击伤害");
                player.sendMessage(ChatColor.GRAY + "直接增加攻击时造成的伤害");
                break;
            case "移动速度":
                player.sendMessage(ChatColor.AQUA + "移动速度" + ChatColor.WHITE + " - 增加玩家的移动速度");
                player.sendMessage(ChatColor.GRAY + "提升玩家的行走和奔跑速度");
                break;
            case "伤害减免":
                player.sendMessage(ChatColor.BLUE + "伤害减免" + ChatColor.WHITE + " - 减少受到的伤害");
                player.sendMessage(ChatColor.GRAY + "按百分比减少受到的伤害");
                break;
            case "致命几率":
                player.sendMessage(ChatColor.GOLD + "致命几率" + ChatColor.WHITE + " - 暴击触发概率");
                player.sendMessage(ChatColor.GRAY + "攻击时有概率触发暴击效果");
                break;
            case "致命伤害":
                player.sendMessage(ChatColor.RED + "致命伤害" + ChatColor.WHITE + " - 暴击时的额外伤害");
                player.sendMessage(ChatColor.GRAY + "暴击触发时增加的伤害倍数");
                break;
            case "闪避几率":
                player.sendMessage(ChatColor.GREEN + "闪避几率" + ChatColor.WHITE + " - 完全闪避攻击的概率");
                player.sendMessage(ChatColor.GRAY + "受到攻击时有概率完全闪避");
                break;
            case "格挡几率":
                player.sendMessage(ChatColor.DARK_GRAY + "格挡几率" + ChatColor.WHITE + " - 格挡攻击的概率");
                player.sendMessage(ChatColor.GRAY + "受到攻击时有概率格挡部分伤害");
                break;
            default:
                player.sendMessage(ChatColor.WHITE + "暂无详细信息");
                break;
        }
        
        player.sendMessage(ChatColor.GOLD + "=======================================");
    }
    
    /**
     * 检查是否是属性物品
     */
    private boolean isAttributeItem(String itemName) {
        String[] attributeNames = {
            "生命值", "攻击伤害", "移动速度", "伤害减免",
            "致命几率", "致命伤害", "闪避几率", "格挡几率",
            "生命偷取", "真实伤害", "护甲穿透", "生命恢复"
        };
        
        for (String name : attributeNames) {
            if (itemName.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
} 