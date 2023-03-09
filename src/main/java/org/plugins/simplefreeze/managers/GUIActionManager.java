package org.plugins.simplefreeze.managers;

import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.guiactions.CommandsAction;
import org.plugins.simplefreeze.objects.guiactions.GUIAction;
import org.plugins.simplefreeze.objects.guiactions.MessageFreezerAction;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;
import roryslibrary.util.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIActionManager {

    private final SimpleFreezeMain plugin;
    private final HashMap<Integer, List<GUIAction>> guiActions = new HashMap<>();
    private final HashMap<Integer, Long> guiCooldowns = new HashMap<>();

    public GUIActionManager(SimpleFreezeMain plugin) {
        this.plugin = plugin;
        this.refreshGUIActions();
    }

    public void performGUIActions(int slot, FrozenPlayer frozenPlayer) {
        if (this.guiActions.get(slot) != null) {
            if (System.currentTimeMillis() >= frozenPlayer.getGUICooldown(slot) + this.getGUICooldown(slot)) {
                for (GUIAction guiAction : this.guiActions.get(slot)) {
                    guiAction.performAction(frozenPlayer);
                }
                frozenPlayer.refreshGUICooldown(slot);
            }
        }
    }

    public Long getGUICooldown(int slot) {
        return this.guiCooldowns.containsKey(slot) ? this.guiCooldowns.get(slot) : 0L;
    }

    public List<GUIAction> getGUIActions(int slot) {
        return this.guiActions.get(slot);
    }

    public void addGUIAction(int slot, GUIAction guiAction) {
        if (this.guiActions.get(slot) == null) {
            this.guiActions.put(slot, new ArrayList<>());
        }
        this.guiActions.get(slot).add(guiAction);
    }

    public void refreshGUIActions() {
        this.guiActions.clear();
        for (String itemName : this.plugin.getConfig().getConfigurationSection("freeze-gui.items").getKeys(false)) {
            String path = "freeze-gui.items." + itemName + ".gui-actions";
            if (this.plugin.getConfig().isConfigurationSection(path)) {
                int slot = ItemUtil.getSlot(this.plugin.getConfig().getInt("freeze-gui.items." + itemName + ".x-cord"), this.plugin.getConfig().getInt("freeze-gui.items." + itemName + ".y-cord"));
                this.guiCooldowns.put(slot, this.plugin.getConfig().getLong(path + ".cooldown") * 1000L);
                for (String guiType : this.plugin.getConfig().getConfigurationSection(path).getKeys(false)) {
                    GUIAction guiAction = null;
                    if (guiType.equalsIgnoreCase("message-freezer")) {
                        guiAction = new MessageFreezerAction(this.plugin.getConfig().getString(path + "." + guiType + ".message"));
                    }

                    if (guiType.equalsIgnoreCase("run-commands")) {
                        guiAction = new CommandsAction(this.plugin.getConfig().getStringList(path + "." + guiType + ".commands"));
                    }

                    if (guiAction != null) {
                        this.addGUIAction(slot, guiAction);
                    }
                }
            }
        }
    }

}
