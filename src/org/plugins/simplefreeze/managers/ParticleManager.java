package org.plugins.simplefreeze.managers;

import org.plugins.simplefreeze.SimpleFreezeMain;

/**
 * Created by Rory on 1/30/2017.
 */
public class ParticleManager {

    private SimpleFreezeMain plugin;

    private String effectName;

    public ParticleManager (SimpleFreezeMain plugin) {
        this.plugin = plugin;
    }

    public void setEffectName(String effectName) {
        this.effectName = effectName;
    }

    public void startTask() {

    }

    public void endTask() {

    }

}
