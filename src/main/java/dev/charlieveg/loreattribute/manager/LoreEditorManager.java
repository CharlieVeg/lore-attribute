package dev.charlieveg.loreattribute.manager;

import lombok.Data;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lore编辑管理器
 * 提供lore的解析、编辑和保存功能
 */
public class LoreEditorManager {
    
    @Getter
    private final Map<UUID, LoreEditSession> editSessions = new HashMap<>();
    
    // 匹配数字的正则表达式（支持小数、百分号、负数）
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([+-]?\\d*\\.?\\d+)([%]?)");
    
    /**
     * Lore行数据结构
     */
    @Data
    public static class LoreLine {
        private String originalText;
        private String prefix;          // 数字前的文字
        private String number;          // 数字部分
        private String suffix;          // 数字后的文字
        private String colorCode;       // 颜色代码
        private boolean hasNumber;      // 是否包含数字
        
        public LoreLine(String text) {
            this.originalText = text;
            this.colorCode = "";
            parseText(text);
        }
        
        /**
         * 解析文本，分离数字和文字
         */
        private void parseText(String text) {
            // 提取颜色代码
            String cleanText = text;
            StringBuilder colors = new StringBuilder();
            
            Matcher colorMatcher = Pattern.compile("§[0-9a-fk-or]").matcher(text);
            while (colorMatcher.find()) {
                colors.append(colorMatcher.group());
            }
            this.colorCode = colors.toString();
            cleanText = text.replaceAll("§[0-9a-fk-or]", "");
            
            // 查找数字
            Matcher numberMatcher = NUMBER_PATTERN.matcher(cleanText);
            if (numberMatcher.find()) {
                this.hasNumber = true;
                this.prefix = cleanText.substring(0, numberMatcher.start()).trim();
                this.number = numberMatcher.group(1) + numberMatcher.group(2);
                this.suffix = cleanText.substring(numberMatcher.end()).trim();
            } else {
                this.hasNumber = false;
                this.prefix = cleanText.trim();
                this.number = "";
                this.suffix = "";
            }
        }
        
        /**
         * 重建lore文本
         */
        public String buildText() {
            StringBuilder result = new StringBuilder();
            result.append(colorCode);
            
            if (hasNumber && !number.isEmpty()) {
                if (!prefix.isEmpty()) {
                    result.append(prefix).append(" ");
                }
                result.append(number);
                if (!suffix.isEmpty()) {
                    result.append(" ").append(suffix);
                }
            } else {
                result.append(prefix);
                if (!suffix.isEmpty()) {
                    result.append(" ").append(suffix);
                }
            }
            
            return result.toString();
        }
        
        /**
         * 获取显示用的干净文本（无颜色代码）
         */
        public String getDisplayText() {
            return originalText.replaceAll("§[0-9a-fk-or]", "");
        }
    }
    
    /**
     * Lore编辑会话
     */
    @Data
    public static class LoreEditSession {
        private Player player;
        private ItemStack originalItem;
        private ItemStack editingItem;
        private List<LoreLine> loreLines;
        private int currentLineIndex;
        private EditMode editMode;
        private boolean modified;
        
        public LoreEditSession(Player player, ItemStack item) {
            this.player = player;
            this.originalItem = item.clone();
            this.editingItem = item.clone();
            this.loreLines = new ArrayList<>();
            this.currentLineIndex = 0;
            this.editMode = EditMode.VIEW;
            this.modified = false;
            
            loadLoreLines();
        }
        
        /**
         * 从物品加载lore行
         */
        private void loadLoreLines() {
            if (editingItem.hasItemMeta() && editingItem.getItemMeta().hasLore()) {
                List<String> lore = editingItem.getItemMeta().getLore();
                for (String line : lore) {
                    loreLines.add(new LoreLine(line));
                }
            }
            
            // 如果没有lore，添加一个空行
            if (loreLines.isEmpty()) {
                loreLines.add(new LoreLine(""));
            }
        }
        
        /**
         * 保存lore到物品
         */
        public void saveLoreToItem() {
            ItemMeta meta = editingItem.getItemMeta();
            if (meta == null) return;
            
            List<String> newLore = new ArrayList<>();
            for (LoreLine line : loreLines) {
                String text = line.buildText();
                if (!text.trim().isEmpty()) {
                    newLore.add(text);
                }
            }
            
            meta.setLore(newLore);
            editingItem.setItemMeta(meta);
            modified = true;
        }
        
        /**
         * 获取当前行
         */
        public LoreLine getCurrentLine() {
            if (currentLineIndex >= 0 && currentLineIndex < loreLines.size()) {
                return loreLines.get(currentLineIndex);
            }
            return null;
        }
        
        /**
         * 添加新行
         */
        public void addNewLine() {
            loreLines.add(new LoreLine(""));
            modified = true;
        }
        
        /**
         * 删除当前行
         */
        public void deleteLine() {
            if (loreLines.size() > 1 && currentLineIndex >= 0 && currentLineIndex < loreLines.size()) {
                loreLines.remove(currentLineIndex);
                if (currentLineIndex >= loreLines.size()) {
                    currentLineIndex = loreLines.size() - 1;
                }
                modified = true;
            }
        }
        
        /**
         * 移动到上一行
         */
        public void movePrevious() {
            if (currentLineIndex > 0) {
                currentLineIndex--;
            }
        }
        
        /**
         * 移动到下一行
         */
        public void moveNext() {
            if (currentLineIndex < loreLines.size() - 1) {
                currentLineIndex++;
            }
        }
    }
    
    /**
     * 编辑模式枚举
     */
    public enum EditMode {
        VIEW("查看模式"),
        EDIT_TEXT("编辑文字"),
        EDIT_PREFIX("编辑前缀"),
        EDIT_NUMBER("编辑数字"),
        EDIT_SUFFIX("编辑后缀");
        
        @Getter
        private final String displayName;
        
        EditMode(String displayName) {
            this.displayName = displayName;
        }
    }
    
    /**
     * 开始编辑lore
     */
    public LoreEditSession startEdit(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        LoreEditSession session = new LoreEditSession(player, item);
        editSessions.put(player.getUniqueId(), session);
        return session;
    }
    
    /**
     * 获取编辑会话
     */
    public LoreEditSession getEditSession(Player player) {
        return editSessions.get(player.getUniqueId());
    }
    
    /**
     * 结束编辑会话
     */
    public void endEdit(Player player) {
        editSessions.remove(player.getUniqueId());
    }
    
    /**
     * 保存编辑结果到玩家物品
     */
    public boolean saveEdit(Player player) {
        LoreEditSession session = getEditSession(player);
        if (session == null) {
            return false;
        }
        
        session.saveLoreToItem();
        
        // 更新玩家手持物品
        ItemStack handItem = player.getItemInHand();
        if (handItem != null && handItem.isSimilar(session.getOriginalItem())) {
            player.setItemInHand(session.getEditingItem());
        } else {
            // 如果手持物品已变化，尝试在背包中找到并替换
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.isSimilar(session.getOriginalItem())) {
                    player.getInventory().setItem(i, session.getEditingItem());
                    break;
                }
            }
        }
        
        player.updateInventory();
        return true;
    }
    
    /**
     * 取消编辑
     */
    public void cancelEdit(Player player) {
        endEdit(player);
    }
    
    /**
     * 处理聊天输入
     */
    public boolean handleChatInput(Player player, String message) {
        LoreEditSession session = getEditSession(player);
        if (session == null || session.getEditMode() == EditMode.VIEW) {
            return false;
        }
        
        LoreLine currentLine = session.getCurrentLine();
        if (currentLine == null) {
            return false;
        }
        
        switch (session.getEditMode()) {
            case EDIT_TEXT:
                // 编辑整行文本
                currentLine.setOriginalText(ChatColor.translateAlternateColorCodes('&', message));
                currentLine.parseText(currentLine.getOriginalText());
                break;
                
            case EDIT_PREFIX:
                // 编辑前缀
                currentLine.setPrefix(message);
                break;
                
            case EDIT_NUMBER:
                // 编辑数字
                if (isValidNumber(message)) {
                    currentLine.setNumber(message);
                    currentLine.setHasNumber(true);
                } else {
                    player.sendMessage("§c无效的数字格式！请输入有效的数字。");
                    return true;
                }
                break;
                
            case EDIT_SUFFIX:
                // 编辑后缀
                currentLine.setSuffix(message);
                break;
        }
        
        session.setEditMode(EditMode.VIEW);
        session.setModified(true);
        player.sendMessage("§a已更新lore行！");
        return true;
    }
    
    /**
     * 验证数字格式
     */
    private boolean isValidNumber(String input) {
        try {
            if (input.endsWith("%")) {
                Double.parseDouble(input.substring(0, input.length() - 1));
            } else {
                Double.parseDouble(input);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 应用颜色代码到文本
     */
    public String applyColors(String text, String colorCode) {
        return colorCode + text;
    }
    
    /**
     * 获取所有可用的颜色代码
     */
    public List<String> getAvailableColors() {
        return Arrays.asList(
            "§0黑色", "§1深蓝", "§2深绿", "§3深青", "§4深红", "§5紫色",
            "§6金色", "§7灰色", "§8深灰", "§9蓝色", "§a绿色", "§b青色",
            "§c红色", "§d粉色", "§e黄色", "§f白色",
            "§k混乱", "§l粗体", "§m删除线", "§n下划线", "§o斜体", "§r重置"
        );
    }
} 