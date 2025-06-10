package dev.charlieveg.loreattribute;

import dev.charlieveg.loreattribute.command.AttributeCommandExecutor;
import dev.charlieveg.loreattribute.command.EquipmentRestrictionCommand;
import dev.charlieveg.loreattribute.config.ConfigManager;
import dev.charlieveg.loreattribute.listener.AttributeEffectListener;
import dev.charlieveg.loreattribute.listener.BattleInventoryListener;
import dev.charlieveg.loreattribute.listener.CombatListener;
import dev.charlieveg.loreattribute.listener.EquipmentChangeListener;
import dev.charlieveg.loreattribute.listener.EquipmentRestrictionListener;
import dev.charlieveg.loreattribute.listener.PlayerListener;
import dev.charlieveg.loreattribute.listener.UIListener;
import dev.charlieveg.loreattribute.manager.AttributeManager;
import dev.charlieveg.loreattribute.manager.BattleInventoryManager;
import dev.charlieveg.loreattribute.manager.EquipmentRestrictionManager;
import dev.charlieveg.loreattribute.manager.LoreEditorManager;
import dev.charlieveg.loreattribute.ui.AttributeViewerUI;
import dev.charlieveg.loreattribute.ui.LoreEditorUI;
import dev.charlieveg.loreattribute.listener.LoreEditorListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LoreAttribute 主插件类
 * 负责插件的初始化和关闭
 * 
 * @author charlieveg
 */
@Getter
public final class LoreAttributePlugin extends JavaPlugin {
    
    @Getter
    private static LoreAttributePlugin instance;
    
    private ConfigManager configManager;
    private AttributeManager attributeManager;
    private BattleInventoryManager battleInventoryManager;
    private EquipmentRestrictionManager equipmentRestrictionManager;
    private LoreEditorManager loreEditorManager;
    private AttributeViewerUI attributeViewerUI;
    private LoreEditorUI loreEditorUI;
    private AttributeEffectListener attributeEffectListener;
    private CombatListener combatListener;
    private EquipmentChangeListener equipmentChangeListener;
    private EquipmentRestrictionListener equipmentRestrictionListener;
    private LoreEditorListener loreEditorListener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        initializeManagers();
        
        dev.charlieveg.loreattribute.api.LoreAttributeAPI.initialize(this);
        
        registerListeners();
        
        registerCommands();
        
        startTasks();
        
        getLogger().info("LoreAttribute插件已启用！");
        getLogger().info("LoreAttributeAPI已初始化，外部插件可通过API访问功能");
    }
    
    @Override
    public void onDisable() {
        if (attributeManager != null) {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                attributeManager.clearPlayerAttributes(player);
            }
        }
        if (battleInventoryManager != null) {
            battleInventoryManager.saveAllBattleInventories();
        }
        
        getLogger().info("LoreAttribute插件已禁用！");
    }
    
    /**
     * 初始化管理器
     */
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        attributeManager = new AttributeManager(this);
        battleInventoryManager = new BattleInventoryManager(this);
        equipmentRestrictionManager = new EquipmentRestrictionManager(getDataFolder());
        loreEditorManager = new LoreEditorManager();
        attributeViewerUI = new AttributeViewerUI();
        loreEditorUI = new LoreEditorUI(loreEditorManager);
        attributeEffectListener = new AttributeEffectListener(this);
        combatListener = new CombatListener(this);
        equipmentChangeListener = new EquipmentChangeListener(this);
        equipmentRestrictionListener = new EquipmentRestrictionListener(equipmentRestrictionManager);
        loreEditorListener = new LoreEditorListener(loreEditorManager, loreEditorUI);
    }
    
    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BattleInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new UIListener(this), this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(attributeEffectListener, this);
        getServer().getPluginManager().registerEvents(equipmentChangeListener, this);
        getServer().getPluginManager().registerEvents(equipmentRestrictionListener, this);
        getServer().getPluginManager().registerEvents(loreEditorListener, this);
        getLogger().info("已注册所有事件监听器");
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        AttributeCommandExecutor commandExecutor = new AttributeCommandExecutor(this);
        getCommand("latr").setExecutor(commandExecutor);
        getCommand("latr").setTabCompleter(commandExecutor);
        
        EquipmentRestrictionCommand restrictionCommand = new EquipmentRestrictionCommand(equipmentRestrictionManager);
        getCommand("restriction").setExecutor(restrictionCommand);
        getCommand("restriction").setTabCompleter(restrictionCommand);
    }
    
    /**
     * 启动定时任务
     */
    private void startTasks() {
        if (!configManager.getBoolean("AttributeUpdate.Enabled", true)) {
            getLogger().info("属性自动更新已禁用");
            return;
        }
        
        int interval = configManager.getInt("AttributeUpdate.Interval", 20);
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                attributeManager.updatePlayerAttributes(player);
            }
        }, interval, interval);
        
        getLogger().info("已启动属性更新任务，更新间隔: " + interval + " ticks");
        
        attributeEffectListener.startAttributeEffectTask();
        getLogger().info("已启动属性效果任务");

        equipmentChangeListener.startPeriodicUpdate();
        getLogger().info("已启动装备变化实时监听");
    }
} 