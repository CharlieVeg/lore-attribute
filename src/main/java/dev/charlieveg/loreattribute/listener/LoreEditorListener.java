package dev.charlieveg.loreattribute.listener;

import dev.charlieveg.loreattribute.manager.LoreEditorManager;
import dev.charlieveg.loreattribute.ui.LoreEditorUI;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Lore编辑器事件监听器
 * 处理UI交互、聊天输入和会话管理
 */
@RequiredArgsConstructor
public class LoreEditorListener implements Listener {
    
    private final LoreEditorManager loreEditorManager;
    private final LoreEditorUI loreEditorUI;
    
    /**
     * 处理背包点击事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();
        
        // 检查是否是lore编辑器界面
        if (!"§6§lLore编辑器".equals(title)) {
            return;
        }
        
        event.setCancelled(true);
        
        // 处理UI点击
        boolean handled = loreEditorUI.handleClick(player, event.getSlot(), event.getCurrentItem());
        
        if (!handled) {
            // 如果没有处理，可能是无效点击
            player.sendMessage("§7无效的点击位置");
        }
    }
    
    /**
     * 处理背包关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        String title = event.getInventory().getTitle();
        
        // 检查是否是lore编辑器界面
        if (!"§6§lLore编辑器".equals(title)) {
            return;
        }
        
        // 检查玩家是否有正在进行的编辑会话
        LoreEditorManager.LoreEditSession session = loreEditorManager.getEditSession(player);
        if (session != null) {
            // 如果不是在编辑模式中，显示提示
            if (session.getEditMode() == LoreEditorManager.EditMode.VIEW) {
                player.sendMessage("§e已关闭lore编辑器");
                player.sendMessage("§7使用 §e/latr edit §7重新打开编辑器");
                player.sendMessage("§7或使用 §c/latr edit cancel §7取消编辑");
            }
        }
    }
    
    /**
     * 处理聊天事件（用于编辑输入）
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // 检查玩家是否在编辑模式中
        LoreEditorManager.LoreEditSession session = loreEditorManager.getEditSession(player);
        if (session == null || session.getEditMode() == LoreEditorManager.EditMode.VIEW) {
            return;
        }
        
        // 取消聊天事件，防止消息被发送到聊天
        event.setCancelled(true);
        
        // 处理特殊命令
        if ("cancel".equalsIgnoreCase(message) || "取消".equals(message)) {
            session.setEditMode(LoreEditorManager.EditMode.VIEW);
            player.sendMessage("§c已取消当前编辑");
            
            // 使用同步任务重新打开UI
            org.bukkit.Bukkit.getScheduler().runTask(
                org.bukkit.Bukkit.getPluginManager().getPlugin("LoreAttribute"), 
                () -> loreEditorUI.openEditor(player)
            );
            return;
        }
        
        if ("back".equalsIgnoreCase(message) || "返回".equals(message)) {
            session.setEditMode(LoreEditorManager.EditMode.VIEW);
            player.sendMessage("§e已返回编辑器界面");
            
            // 使用同步任务重新打开UI
            org.bukkit.Bukkit.getScheduler().runTask(
                org.bukkit.Bukkit.getPluginManager().getPlugin("LoreAttribute"), 
                () -> loreEditorUI.openEditor(player)
            );
            return;
        }
        
        // 处理编辑输入
        boolean success = loreEditorManager.handleChatInput(player, message);
        
        if (success) {
            // 使用同步任务重新打开UI
            org.bukkit.Bukkit.getScheduler().runTask(
                org.bukkit.Bukkit.getPluginManager().getPlugin("LoreAttribute"), 
                () -> loreEditorUI.openEditor(player)
            );
        } else {
            player.sendMessage("§c输入处理失败，请重试");
        }
    }
    
    /**
     * 处理玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 清理编辑会话
        LoreEditorManager.LoreEditSession session = loreEditorManager.getEditSession(player);
        if (session != null) {
            // 如果有未保存的更改，尝试自动保存
            if (session.isModified()) {
                try {
                    loreEditorManager.saveEdit(player);
                } catch (Exception e) {
                    // 保存失败时静默处理
                }
            }
            
            loreEditorManager.endEdit(player);
        }
    }
} 