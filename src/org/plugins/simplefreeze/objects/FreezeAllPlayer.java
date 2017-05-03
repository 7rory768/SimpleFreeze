package org.plugins.simplefreeze.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.plugins.simplefreeze.util.FrozenType;

import java.util.UUID;

/**
 * Created by Rory on 12/3/2016.
 */
public class FreezeAllPlayer extends FrozenPlayer {

    private final FrozenType type = FrozenType.FREEZEALL_FROZEN;

    public FreezeAllPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, String reason, ItemStack helmet) {
        super(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, false, helmet);
    }

    public FreezeAllPlayer(Long freezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, String reason) {
        super(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, false);
    }

    @Override
    public FrozenType getType() {
        return FrozenType.FREEZEALL_FROZEN;
    }

}
