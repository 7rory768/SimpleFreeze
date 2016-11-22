package org.plugins.simplefreeze.objects;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class TempFrozenPlayer extends FrozenPlayer {

	private final Long unfreezeDate;
	
	private BukkitTask task = null;
	
	public TempFrozenPlayer(Long date, Long unfreezeDate, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, boolean online, ItemStack helmet) {
		super(date, freezer, freezeeName, freezerName, originalLoc, freezeLoc, sqlFreeze, online, helmet);
		this.unfreezeDate = unfreezeDate;
	}
	
	public TempFrozenPlayer(Long date, Long unfreezeDate, UUID freezer, String freezeeName, String freezerName, Location originalLoc, Location freezeLoc, boolean sqlFreeze, boolean online) {
		super(date, freezer, freezeeName, freezerName, originalLoc, freezeLoc, sqlFreeze, online);
		this.unfreezeDate = unfreezeDate;
		
	}

	public Long getUnfreezeDate() {
		return unfreezeDate;
	}

	public BukkitTask getTask() {
		return task;
	}

	public void setTask(BukkitTask task) {
		this.task = task;
	}
	
	public void cancelTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}

}
