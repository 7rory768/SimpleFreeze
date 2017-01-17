package org.plugins.simplefreeze.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Rory on 12/3/2016.
 */
public class FreezeAllPlayer extends FrozenPlayer {

    public FreezeAllPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, ItemStack helmet) {
        super(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, false, helmet);
    }

    public FreezeAllPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc) {
        super(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, false);
    }

}
