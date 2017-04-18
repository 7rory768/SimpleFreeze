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

    private Sound unfreezeSound;
    private float unfreezeVolume;
    private float unfreezePitch;

    public SoundManager(SimpleFreezeMain plugin) {
        this.plugin = plugin;

        String freezeString = this.plugin.getConfig().getString("freeze-sound.sound");
        try {
            this.freezeSound = Sound.valueOf(this.plugin.getConfig().getString("freeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            if (freezeString.startsWith("BLOCK_") && freezeString.length() > 6) {
                this.freezeSound = Sound.valueOf(freezeString.substring(6));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid freeze sound: &c" + freezeString));
            }
        }

        String unfreezeString = this.plugin.getConfig().getString("unfreeze-sound.sound");
        try {
            this.unfreezeSound = Sound.valueOf(unfreezeString);
        } catch (IllegalArgumentException e) {
            if (unfreezeString.startsWith("BLOCK_") && unfreezeString.length() > 6) {
                this.unfreezeSound = Sound.valueOf(unfreezeString.substring(6));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid unfreeze sound: &c" + unfreezeString));
            }
        }

        this.freezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound.volume");
        this.freezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound.pitch");

        this.unfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound.volume");
        this.unfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound.pitch");
    }

    public void reset() {
        String freezeString = this.plugin.getConfig().getString("freeze-sound.sound");
        try {
            this.freezeSound = Sound.valueOf(this.plugin.getConfig().getString("freeze-sound.sound"));
        } catch (IllegalArgumentException e) {
            if (freezeString.startsWith("BLOCK_") && freezeString.length() > 6) {
                this.freezeSound = Sound.valueOf(freezeString.substring(6));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid freeze sound: &c" + freezeString));
            }
        }

        String unfreezeString = this.plugin.getConfig().getString("unfreeze-sound.sound");
        try {
            this.unfreezeSound = Sound.valueOf(unfreezeString);
        } catch (IllegalArgumentException e) {
            if (unfreezeString.startsWith("BLOCK_") && unfreezeString.length() > 6) {
                this.unfreezeSound = Sound.valueOf(unfreezeString.substring(6));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders("[SimpleFreeze] &c&lERROR: &7Invalid unfreeze sound: &c" + unfreezeString));
            }
        }

        this.freezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound.volume");
        this.freezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound.pitch");

        this.unfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound.volume");
        this.unfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound.pitch");
    }


    public void playFreezeSound(Player p) {
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
            this.freezeSound = Sound.valueOf(soundString);
        } catch (IllegalArgumentException e) {
            if (soundString.startsWith("BLOCK_") && soundString.length() > 6) {
                this.freezeSound = Sound.valueOf(soundString.substring(6));
            }
            return false;
        }
        return true;
    }

    public boolean setUnfreezeSound(String soundString) {
        try {
            this.unfreezeSound = Sound.valueOf(soundString);
        } catch (IllegalArgumentException e) {
            if (soundString.startsWith("BLOCK_") && soundString.length() > 6) {
                this.unfreezeSound = Sound.valueOf(soundString.substring(6));
            }
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
