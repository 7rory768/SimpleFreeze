package org.plugins.simplefreeze.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FrozenPlayer {

	private final Long freezeDate;
	private final UUID freezee;
	private final String freezeeName;
	private final String freezerName;
	private final boolean sqlFreeze;

	private ItemStack helmet = null;
	private Location originalLoc = null;
	private Location freezeLoc = null;
	
	public FrozenPlayer(Long freezeDate, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, ItemStack helmet) {
		this.freezeDate = freezeDate;
		this.freezee = freezer;
		this.freezeeName = freezeeName;
		this.freezerName = freezerName;
		this.originalLoc = originalLoc;
		this.freezeLoc = freezeLoc;
		this.sqlFreeze = sqlFreeze;
		this.helmet = helmet;
		
	}
	
	public FrozenPlayer(Long date, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze) {
		this.freezeDate = date;
		this.freezee = freezer;
		this.freezeeName = freezeeName;
		this.freezerName = freezerName;
		this.originalLoc = originalLoc;
		this.freezeLoc = freezeLoc;
		this.sqlFreeze = sqlFreeze;
	}

	public Long getFreezeDate() {
		return this.freezeDate;
	}

	public UUID getFreezee() {
		return this.freezee;
	}

	public boolean isSqlFreeze() {
		return this.sqlFreeze;
	}

	public ItemStack getHelmet() {
		return this.helmet;
	}

	public void setHelmet(ItemStack helmet) { this.helmet = helmet; }

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
	
	public String getFreezerName() {
		return this.freezerName;
	}
	
	public String getFreezeeName() {
		return this.freezeeName;
	}
	
	
}
