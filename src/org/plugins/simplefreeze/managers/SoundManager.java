package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;

/**
 * Created by Rory on 3/2/2017.
 */
public class SoundManager {

    private final SimpleFreezeMain plugin;

    private Sound freezeSound;
    private float freezeVolume;
    private float freezePitch;

    Sound unfreezeSound;
    private float unfreezeVolume;
    private float unfreezePitch;

    public SoundManager(SimpleFreezeMain plugin) {
        this.plugin = plugin;

        try {
            this.freezeSound = Sound.valueOf(this.plugin.getConfig().getString("freeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid freeze sound: &c" + plugin.getConfig().getString("freeze-sound.sound")));
        }

        try {
            this.unfreezeSound = Sound.valueOf(this.plugin.getConfig().getString("unfreeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid unfreeze sound: &c" + this.plugin.getConfig().getString("unfreeze-sound.sound")));
        }

        this.freezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound.volume");
        this.freezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound.pitch");

        this.unfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound.volume");
        this.unfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound.pitch");
    }

    public void reset() {
        try {
            Sound.valueOf(this.plugin.getConfig().getString("freeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid freeze sound: &c" + plugin.getConfig().getString("freeze-sound.sound")));
        }

        try {
            this.unfreezeSound = Sound.valueOf(this.plugin.getConfig().getString("unfreeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid unfreeze sound: &c" + this.plugin.getConfig().getString("unfreeze-sound.sound")));
        }

        this.freezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound.volume");
        this.freezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound.pitch");

        this.unfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound.volume");
        this.unfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound.pitch");
    }


    public void playFreezeSound(Player p) {
        try {
            Sound.valueOf(this.plugin.getConfig().getString("freeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid freeze sound: &c" + plugin.getConfig().getString("freeze-sound.sound")));
        }
        if (this.freezeSound != null) {
            p.playSound(p.getLocation().clone().add(0, 2, 0), this.freezeSound, this.freezeVolume, this.freezePitch);
        }
    }

    public void playUnfreezeSound(Player p) {
        if (this.unfreezeSound != null) {
            p.playSound(p.getLocation().clone().add(0, 2, 0), this.unfreezeSound, this.unfreezeVolume, this.unfreezePitch);
        }
    }

    public boolean setFreezeSound(String soundString) {
        try {
            this.freezeSound = Sound.NOTE_BASS;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public boolean setUnfreezeSound(String soundString) {
        try {
            this.unfreezeSound = Sound.NOTE_BASS;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public void setFreezeVolume(float freezeVolume) {
        this.freezeVolume = freezeVolume;
    }

    public void setUnfreezeVolume(float unfreezeVolume) {
        this.unfreezeVolume = unfreezeVolume;
    }

    public void setFreezePitch(float freezePitch) {
        this.freezePitch = freezePitch;
    }

    public void setUnfreezePitch(float unfreezePitch) {
        this.unfreezePitch = unfreezePitch;
    }
}
