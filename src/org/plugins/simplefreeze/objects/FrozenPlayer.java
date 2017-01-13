package org.plugins.simplefreeze.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FrozenPlayer {

    private final Long freezeDate;
    private final UUID freezeeUUID;
    private final UUID freezerUUID;
    private final boolean sqlFreeze;

    private ItemStack helmet = null;
    private Location originalLoc = null;
    private Location freezeLoc = null;

    public FrozenPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, boolean sqlFreeze, ItemStack helmet) {
        this.freezeDate = freezeDate;
        this.freezeeUUID = freezeeUUID;
        this.freezerUUID = freezerUUID;
        this.originalLoc = originalLoc;
        this.freezeLoc = freezeLoc;
        this.sqlFreeze = sqlFreeze;
        this.helmet = helmet;

    }

    public FrozenPlayer(Long date, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, boolean sqlFreeze) {
        this.freezeDate = date;
        this.freezeeUUID = freezeeUUID;
        this.freezerUUID = freezerUUID;
        this.originalLoc = originalLoc;
        this.freezeLoc = freezeLoc;
        this.sqlFreeze = sqlFreeze;
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
        return freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
    }

    public boolean isSqlFreeze() {
        return this.sqlFreeze;
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

}
