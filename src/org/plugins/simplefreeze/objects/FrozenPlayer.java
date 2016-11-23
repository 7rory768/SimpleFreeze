package org.plugins.simplefreeze.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FrozenPlayer {

	private final Long date;
	private final UUID freezee;
	private final String freezeeName;
	private final String freezerName;
	private final boolean sqlFreeze;

	private ItemStack helmet = null;
	private Location originalLoc = null;
	private Location freezeLoc = null;
	private boolean online = false;
	
	public FrozenPlayer(Long date, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, boolean online, ItemStack helmet) {
		this.date = date;
		this.freezee = freezer;
		this.freezeeName = freezeeName;
		this.freezerName = freezerName;
		this.originalLoc = originalLoc;
		this.freezeLoc = freezeLoc;
		this.sqlFreeze = sqlFreeze;
		this.online = online;
		this.helmet = helmet;
		
	}
	
	public FrozenPlayer(Long date, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, boolean online) {
		this.date = date;
		this.freezee = freezer;
		this.freezeeName = freezeeName;
		this.freezerName = freezerName;
		this.originalLoc = originalLoc;
		this.freezeLoc = freezeLoc;
		this.sqlFreeze = sqlFreeze;
		this.online = online;
	}

	public Long getDate() {
		return this.date;
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

	public boolean isOnline() {
		return this.online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	
}
