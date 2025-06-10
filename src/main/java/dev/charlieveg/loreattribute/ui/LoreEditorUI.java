package dev.charlieveg.loreattribute.ui;

import dev.charlieveg.loreattribute.manager.LoreEditorManager;
import dev.charlieveg.loreattribute.manager.LoreEditorManager.LoreEditSession;
import dev.charlieveg.loreattribute.manager.LoreEditorManager.LoreLine;
import dev.charlieveg.loreattribute.manager.LoreEditorManager.EditMode;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lore编辑UI界面
 * 提供直观的图形化lore编辑界面
 */
@RequiredArgsConstructor
public class LoreEditorUI {
    
    private final LoreEditorManager loreEditorManager;
    
    // UI标题
    private static final String UI_TITLE = "§6§lLore编辑器";
    private static final int UI_SIZE = 54; // 6行
    
    // 按钮位置
    private static final int ITEM_PREVIEW_SLOT = 4;
    private static final int LORE_START_SLOT = 9;
    private static final int LORE_END_SLOT = 35;
    
    // 控制按钮位置
    private static final int PREV_LINE_SLOT = 36;
    private static final int NEXT_LINE_SLOT = 37;
    private static final int ADD_LINE_SLOT = 38;
    private static final int DELETE_LINE_SLOT = 39;
    
    // 编辑按钮位置
    private static final int EDIT_TEXT_SLOT = 42;
    private static final int EDIT_PREFIX_SLOT = 43;
    private static final int EDIT_NUMBER_SLOT = 44;
    private static final int EDIT_SUFFIX_SLOT = 45;
    
    // 操作按钮位置
    private static final int SAVE_SLOT = 48;
    private static final int CANCEL_SLOT = 49;
    private static final int HELP_SLOT = 50;
    
    /**
     * 打开lore编辑界面
     */
    public void openEditor(Player player) {
        LoreEditSession session = loreEditorManager.getEditSession(player);
        if (session == null) {
            player.sendMessage("§c没有正在进行的编辑会话！");
            return;
        }
        
        Inventory inventory = Bukkit.createInventory(null, UI_SIZE, UI_TITLE);
        updateInventory(inventory, session);
        player.openInventory(inventory);
    }
    
    /**
     * 更新界面内容
     */
    public void updateInventory(Inventory inventory, LoreEditSession session) {
        // 清空界面
        inventory.clear();
        
        // 物品预览
        setItemPreview(inventory, session);
        
        // Lore行显示
        setLoreDisplay(inventory, session);
        
        // 控制按钮
        setControlButtons(inventory, session);
        
        // 编辑按钮
        setEditButtons(inventory, session);
        
        // 操作按钮
        setActionButtons(inventory, session);
        
        // 填充空白区域
        fillEmptySlots(inventory);
    }
    
    /**
     * 设置物品预览
     */
    private void setItemPreview(Inventory inventory, LoreEditSession session) {
        ItemStack previewItem = session.getEditingItem().clone();
        ItemMeta meta = previewItem.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7正在编辑的物品");
            lore.add("§e点击查看完整lore");
            
            if (session.isModified()) {
                lore.add("§a● 已修改");
            } else {
                lore.add("§7○ 未修改");
            }
            
            meta.setLore(lore);
            previewItem.setItemMeta(meta);
        }
        
        inventory.setItem(ITEM_PREVIEW_SLOT, previewItem);
    }
    
    /**
     * 设置lore行显示
     */
    private void setLoreDisplay(Inventory inventory, LoreEditSession session) {
        List<LoreLine> loreLines = session.getLoreLines();
        int currentLineIndex = session.getCurrentLineIndex();
        
        for (int i = 0; i < Math.min(loreLines.size(), 27); i++) {
            int slot = LORE_START_SLOT + i;
            LoreLine line = loreLines.get(i);
            
            ItemStack item;
            ItemMeta meta;
            
            if (i == currentLineIndex) {
                // 当前选中的行
                item = new ItemStack(Material.EMERALD_BLOCK);
                meta = item.getItemMeta();
                meta.setDisplayName("§a§l► 第" + (i + 1) + "行 (当前)");
            } else {
                // 其他行
                item = new ItemStack(Material.STONE);
                meta = item.getItemMeta();
                meta.setDisplayName("§7第" + (i + 1) + "行");
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("§f显示: §r" + line.getOriginalText());
            lore.add("§7干净文本: " + line.getDisplayText());
            
            if (line.isHasNumber()) {
                lore.add("§e前缀: §f" + line.getPrefix());
                lore.add("§a数字: §f" + line.getNumber());
                lore.add("§e后缀: §f" + line.getSuffix());
            } else {
                lore.add("§e文本: §f" + line.getPrefix());
            }
            
            lore.add("");
            lore.add("§7左键选择此行");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * 设置控制按钮
     */
    private void setControlButtons(Inventory inventory, LoreEditSession session) {
        // 上一行
        ItemStack prevItem = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevItem.getItemMeta();
        prevMeta.setDisplayName("§e上一行");
        prevMeta.setLore(Arrays.asList("§7切换到上一行", "§7当前: 第" + (session.getCurrentLineIndex() + 1) + "行"));
        prevItem.setItemMeta(prevMeta);
        inventory.setItem(PREV_LINE_SLOT, prevItem);
        
        // 下一行
        ItemStack nextItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.setDisplayName("§e下一行");
        nextMeta.setLore(Arrays.asList("§7切换到下一行", "§7当前: 第" + (session.getCurrentLineIndex() + 1) + "行"));
        nextItem.setItemMeta(nextMeta);
        inventory.setItem(NEXT_LINE_SLOT, nextItem);
        
        // 添加行
        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName("§a添加新行");
        addMeta.setLore(Arrays.asList("§7在末尾添加一个新的lore行"));
        addItem.setItemMeta(addMeta);
        inventory.setItem(ADD_LINE_SLOT, addItem);
        
        // 删除行
        ItemStack deleteItem = new ItemStack(Material.REDSTONE);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName("§c删除当前行");
        deleteMeta.setLore(Arrays.asList("§7删除当前选中的lore行", "§c注意: 此操作不可撤销"));
        deleteItem.setItemMeta(deleteMeta);
        inventory.setItem(DELETE_LINE_SLOT, deleteItem);
    }
    
    /**
     * 设置编辑按钮
     */
    private void setEditButtons(Inventory inventory, LoreEditSession session) {
        LoreLine currentLine = session.getCurrentLine();
        EditMode editMode = session.getEditMode();
        
        // 编辑整行文本
        ItemStack textItem = new ItemStack(editMode == EditMode.EDIT_TEXT ? Material.DIAMOND : Material.BOOK);
        ItemMeta textMeta = textItem.getItemMeta();
        textMeta.setDisplayName("§b编辑整行");
        List<String> textLore = new ArrayList<>();
        textLore.add("§7编辑整行lore文本");
        textLore.add("§7支持颜色代码 (&a、&c等)");
        if (currentLine != null) {
            textLore.add("§e当前: §r" + currentLine.getOriginalText());
        }
        if (editMode == EditMode.EDIT_TEXT) {
            textLore.add("§a► 正在编辑");
        }
        textMeta.setLore(textLore);
        textItem.setItemMeta(textMeta);
        inventory.setItem(EDIT_TEXT_SLOT, textItem);
        
        // 编辑前缀
        ItemStack prefixItem = new ItemStack(editMode == EditMode.EDIT_PREFIX ? Material.DIAMOND : Material.PAPER);
        ItemMeta prefixMeta = prefixItem.getItemMeta();
        prefixMeta.setDisplayName("§e编辑前缀");
        List<String> prefixLore = new ArrayList<>();
        prefixLore.add("§7编辑数字前的文字部分");
        if (currentLine != null) {
            prefixLore.add("§e当前: " + currentLine.getPrefix());
        }
        if (editMode == EditMode.EDIT_PREFIX) {
            prefixLore.add("§a► 正在编辑");
        }
        prefixMeta.setLore(prefixLore);
        prefixItem.setItemMeta(prefixMeta);
        inventory.setItem(EDIT_PREFIX_SLOT, prefixItem);
        
        // 编辑数字
        ItemStack numberItem = new ItemStack(editMode == EditMode.EDIT_NUMBER ? Material.DIAMOND : Material.GOLD_INGOT);
        ItemMeta numberMeta = numberItem.getItemMeta();
        numberMeta.setDisplayName("§a编辑数字");
        List<String> numberLore = new ArrayList<>();
        numberLore.add("§7编辑数字部分");
        numberLore.add("§7支持小数、负数、百分号");
        if (currentLine != null && currentLine.isHasNumber()) {
            numberLore.add("§e当前: " + currentLine.getNumber());
        } else {
            numberLore.add("§7当前: 无数字");
        }
        if (editMode == EditMode.EDIT_NUMBER) {
            numberLore.add("§a► 正在编辑");
        }
        numberMeta.setLore(numberLore);
        numberItem.setItemMeta(numberMeta);
        inventory.setItem(EDIT_NUMBER_SLOT, numberItem);
        
        // 编辑后缀
        ItemStack suffixItem = new ItemStack(editMode == EditMode.EDIT_SUFFIX ? Material.DIAMOND : Material.PAPER);
        ItemMeta suffixMeta = suffixItem.getItemMeta();
        suffixMeta.setDisplayName("§e编辑后缀");
        List<String> suffixLore = new ArrayList<>();
        suffixLore.add("§7编辑数字后的文字部分");
        if (currentLine != null) {
            suffixLore.add("§e当前: " + currentLine.getSuffix());
        }
        if (editMode == EditMode.EDIT_SUFFIX) {
            suffixLore.add("§a► 正在编辑");
        }
        suffixMeta.setLore(suffixLore);
        suffixItem.setItemMeta(suffixMeta);
        inventory.setItem(EDIT_SUFFIX_SLOT, suffixItem);
    }
    
    /**
     * 设置操作按钮
     */
    private void setActionButtons(Inventory inventory, LoreEditSession session) {
        // 保存
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName("§a§l保存");
        List<String> saveLore = new ArrayList<>();
        saveLore.add("§7保存所有更改到物品");
        if (session.isModified()) {
            saveLore.add("§e有未保存的更改");
        } else {
            saveLore.add("§7没有更改");
        }
        saveMeta.setLore(saveLore);
        saveItem.setItemMeta(saveMeta);
        inventory.setItem(SAVE_SLOT, saveItem);
        
        // 取消
        ItemStack cancelItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§c§l取消");
        cancelMeta.setLore(Arrays.asList("§7取消编辑，不保存更改", "§c所有更改将丢失"));
        cancelItem.setItemMeta(cancelMeta);
        inventory.setItem(CANCEL_SLOT, cancelItem);
        
        // 帮助
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6帮助");
        helpMeta.setLore(Arrays.asList(
            "§7=== Lore编辑器使用说明 ===",
            "§e1. 选择要编辑的lore行",
            "§e2. 点击编辑按钮进入编辑模式",
            "§e3. 在聊天中输入新内容",
            "§e4. 点击保存完成编辑",
            "",
            "§7颜色代码: &a绿色 &c红色 &e黄色",
            "§7格式代码: &l粗体 &o斜体 &n下划线"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(HELP_SLOT, helpItem);
    }
    
    /**
     * 填充空白区域
     */
    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    /**
     * 处理UI点击事件
     */
    public boolean handleClick(Player player, int slot, ItemStack clickedItem) {
        LoreEditSession session = loreEditorManager.getEditSession(player);
        if (session == null) {
            return false;
        }
        
        // 取消当前编辑模式
        if (session.getEditMode() != EditMode.VIEW) {
            session.setEditMode(EditMode.VIEW);
            player.sendMessage("§7已退出编辑模式");
        }
        
        // 处理lore行选择
        if (slot >= LORE_START_SLOT && slot <= LORE_END_SLOT) {
            int lineIndex = slot - LORE_START_SLOT;
            if (lineIndex < session.getLoreLines().size()) {
                session.setCurrentLineIndex(lineIndex);
                player.sendMessage("§e已选择第" + (lineIndex + 1) + "行");
                openEditor(player);
                return true;
            }
        }
        
        // 处理控制按钮
        switch (slot) {
            case PREV_LINE_SLOT:
                session.movePrevious();
                player.sendMessage("§e切换到第" + (session.getCurrentLineIndex() + 1) + "行");
                openEditor(player);
                return true;
                
            case NEXT_LINE_SLOT:
                session.moveNext();
                player.sendMessage("§e切换到第" + (session.getCurrentLineIndex() + 1) + "行");
                openEditor(player);
                return true;
                
            case ADD_LINE_SLOT:
                session.addNewLine();
                player.sendMessage("§a已添加新行");
                openEditor(player);
                return true;
                
            case DELETE_LINE_SLOT:
                session.deleteLine();
                player.sendMessage("§c已删除当前行");
                openEditor(player);
                return true;
                
            // 编辑按钮
            case EDIT_TEXT_SLOT:
                session.setEditMode(EditMode.EDIT_TEXT);
                player.closeInventory();
                player.sendMessage("§a请在聊天中输入新的lore文本 (支持 &颜色代码):");
                return true;
                
            case EDIT_PREFIX_SLOT:
                session.setEditMode(EditMode.EDIT_PREFIX);
                player.closeInventory();
                player.sendMessage("§a请在聊天中输入新的前缀文本:");
                return true;
                
            case EDIT_NUMBER_SLOT:
                session.setEditMode(EditMode.EDIT_NUMBER);
                player.closeInventory();
                player.sendMessage("§a请在聊天中输入新的数字 (支持小数、负数、百分号):");
                return true;
                
            case EDIT_SUFFIX_SLOT:
                session.setEditMode(EditMode.EDIT_SUFFIX);
                player.closeInventory();
                player.sendMessage("§a请在聊天中输入新的后缀文本:");
                return true;
                
            // 操作按钮
            case SAVE_SLOT:
                if (loreEditorManager.saveEdit(player)) {
                    player.sendMessage("§a已保存lore更改到物品！");
                    loreEditorManager.endEdit(player);
                    player.closeInventory();
                } else {
                    player.sendMessage("§c保存失败！");
                }
                return true;
                
            case CANCEL_SLOT:
                loreEditorManager.cancelEdit(player);
                player.sendMessage("§c已取消编辑");
                player.closeInventory();
                return true;
                
            case HELP_SLOT:
                return true; // 显示帮助信息，不需要额外操作
        }
        
        return false;
    }
} 