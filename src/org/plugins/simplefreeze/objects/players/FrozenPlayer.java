package org.plugins.simplefreeze.objects.players;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.util.FrozenType;

import java.util.HashMap;
import java.util.UUID;

public class FrozenPlayer {

    private final Long freezeDate;
    private final UUID freezeeUUID;
    private final UUID freezerUUID;
    private final String reason;
    private final boolean sqlFrozen;

    private ItemStack helmet = null;
    private Location originalLoc = null;
    private Location freezeLoc = null;
    private HashMap<Integer, Long> guiCooldowns = new HashMap<>();

    public FrozenPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, String reason, boolean sqlFrozen, ItemStack helmet) {
        this.freezeDate = freezeDate;
        this.freezeeUUID = freezeeUUID;
        this.freezerUUID = freezerUUID;
        this.originalLoc = originalLoc;
        this.freezeLoc = freezeLoc;
        this.reason = reason;
        this.sqlFrozen = sqlFrozen;
        this.helmet = helmet;
    }

    public FrozenPlayer(Long date, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, String reason, boolean sqlFrozen) {
        this.freezeDate = date;
        this.freezeeUUID = freezeeUUID;
        this.freezerUUID = freezerUUID;
        this.originalLoc = originalLoc;
        this.freezeLoc = freezeLoc;
        this.reason = reason;
        this.sqlFrozen = sqlFrozen;
    }

    public Long getFreezeDate() {
        return this.freezeDate;
    }

    public UUID getFreezeeUUID() {
        return this.freezeeUUID;
    }

    public UUID getFreezerUUID() {
        return this.freezerUUID;
    }

    public String getFreezeeName() {
        return Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();
    }

    public String getFreezerName() {
        return freezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
    }

    public ItemStack getHelmet() {
        return this.helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public Location getOriginalLoc() {
        return this.originalLoc;
    }

    public void setOriginalLoc(Location originalLoc) {
        this.originalLoc = originalLoc;
    }

    public Location getFreezeLoc() {
        return freezeLoc;
    }

    public void setFreezeLoc(Location freezeLoc) {
        this.freezeLoc = freezeLoc;
    }

    public String getReason() {
        return this.reason;
    }

    public FrozenType getType() {
        return FrozenType.FROZEN;
    }

    public void refreshGUICooldown(int slot) {
        if (this.guiCooldowns.containsKey(slot)) {
            this.guiCooldowns.remove(slot);
        }
        this.guiCooldowns.put(slot, System.currentTimeMillis());
    }

    public Long getGUICooldown(int slot) {
        return this.guiCooldowns.containsKey(slot) ? this.guiCooldowns.get(slot) : -1L;
    }

}
