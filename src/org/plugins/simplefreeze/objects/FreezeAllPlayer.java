package org.plugins.simplefreeze.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Rory on 12/3/2016.
 */
public class FreezeAllPlayer extends FrozenPlayer {

    public FreezeAllPlayer(Long freezeDate, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, ItemStack helmet) {
        super(freezeDate, freezer, freezeeName, freezerName, originalLoc, freezeLoc, sqlFreeze, helmet);
    }

    public FreezeAllPlayer(Long freezeDate, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze) {
        super(freezeDate, freezer, freezeeName, freezerName, originalLoc, freezeLoc, sqlFreeze);
    }

}
