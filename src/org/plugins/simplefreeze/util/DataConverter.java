package org.plugins.simplefreeze.util;

import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;

import java.io.*;
import java.util.Scanner;
import java.util.UUID;

public class DataConverter {

    private static File file = new File("plugins" + File.separator + "SimpleFreeze" + File.separator, "playerdata.txt");

    private final SimpleFreezeMain plugin;

    public DataConverter(SimpleFreezeMain plugin) {
        this.plugin = plugin;
    }

    public boolean hasLocationsToConvert() {
        return this.plugin.getConfig().isSet("locations");
    }

    public void convertLocationData() {
        for (String location : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
            String lowerCaseLocation = location.toLowerCase();
            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".placeholder")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".placeholder", this.plugin.getConfig().getString("locations." + lowerCaseLocation + ".placeholder"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".worldname")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".worldname", this.plugin.getConfig().getString("locations." + lowerCaseLocation + ".worldname"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".x")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".x", this.plugin.getConfig().getDouble("locations." + lowerCaseLocation + ".x"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".y")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".y", this.plugin.getConfig().getDouble("locations." + lowerCaseLocation + ".y"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".z")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".z", this.plugin.getConfig().getDouble("locations." + lowerCaseLocation + ".z"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".yaw")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".yaw", this.plugin.getConfig().getDouble("locations." + lowerCaseLocation + ".yaw"));
            }

            if (this.plugin.getConfig().isSet("locations." + lowerCaseLocation + ".pitch")) {
                this.plugin.getLocationsConfig().getConfig().set("locations." + lowerCaseLocation + ".pitch", this.plugin.getConfig().getDouble("locations." + lowerCaseLocation + ".pitch"));
            }
        }
        this.plugin.getLocationsConfig().saveConfig();
        this.plugin.getLocationsConfig().reloadConfig();

        File file = new File("plugins" + File.separator + "SimpleFreeze" + File.separator, "config.yml");
        boolean addingText = true;
        String totalText = "";
        if (file.exists()) {
            try {
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    String text = scan.nextLine();
                    if (text.equals("# Defines locations that can be called when freezing a player (ex. /freeze <playername> example-location)")) {
                        addingText = false;
                    } else if (text.equals("# If no location is given in the freeze command this location will be used, (if you want to use this remove the #)")) {
                        addingText = true;
                    }

                    if (addingText) {
                        totalText += text + "\n";
                    }
                }
                totalText = totalText.substring(0, totalText.length() - 2);
                scan.close();
            } catch (FileNotFoundException e) {
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(totalText);
            printWriter.close();
            fileWriter.close();
        } catch (IOException e) {

        }
        this.plugin.reloadConfig();
    }

    public static boolean hasDataToConvert(Player p) {
        if (DataConverter.file.exists()) {
            try {
                Scanner scan = new Scanner(DataConverter.file);
                while (scan.hasNextLine()) {
                    String text = scan.nextLine();
                    String[] fileInfo = text.split(",\\s+");
                    UUID pUUID = UUID.fromString(fileInfo[1]);
                    if (pUUID.equals(p.getUniqueId())) {
                        return true;
                    }
                }
                scan.close();
            } catch (FileNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    public FrozenPlayer convertData(Player p) {
        if (DataConverter.file.exists()) {
            try {
                Scanner scan = new Scanner(DataConverter.file);
                while (scan.hasNextLine()) {
                    String text = scan.nextLine();
                    String[] fileInfo = text.split(",\\s+");
                    if (fileInfo.length > 1) {
                        UUID pUUID = UUID.fromString(fileInfo[1]);
                        if (pUUID.equals(p.getUniqueId())) {
                            SFLocation oldLocation = new SFLocation(p.getWorld(), Double.parseDouble(fileInfo[2]), Double.parseDouble(fileInfo[3]), Double.parseDouble(fileInfo[4]), Float.parseFloat(fileInfo[5]), Float.parseFloat(fileInfo[6]));
                            String uuidStr = pUUID.toString();
                            Long freezeDate = System.currentTimeMillis();
                            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-date", freezeDate);
                            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freezer-uuid", "null");
                            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".original-location", oldLocation.toString());
                            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", "null");
                            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".mysql", false);
                            this.plugin.getPlayerConfig().saveConfig();
                            this.plugin.getPlayerConfig().reloadConfig();
                            this.removeData(p);
                            return new FrozenPlayer(freezeDate, p.getUniqueId(), null, oldLocation, null, plugin.getConfig().getString("default-reason", "None"), false, p.getInventory().getHelmet());

                        }
                    }
                }
                scan.close();
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    private void removeData(Player p) {
        if (DataConverter.file.exists()) {
            String text = "";
            try {
                Scanner scan = new Scanner(DataConverter.file);
                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    String[] fileInfo = text.split(",\\s+");
                    if (fileInfo.length > 1) {
                        UUID pUUID = UUID.fromString(fileInfo[1]);
                        if (!pUUID.equals(p.getUniqueId())) {
                            text += line + "\n";
                        }
                    }
                }
                scan.close();
                if (text.length() > 1) {
                    text = text.substring(0, text.length() - 2);
                }
                PrintWriter clearer = new PrintWriter(DataConverter.file);
                clearer.close();
                FileWriter txtWriter = new FileWriter(DataConverter.file, true);
                PrintWriter pw = new PrintWriter(txtWriter);
                pw.write(text);
                pw.close();
            } catch (IOException e) {
                return;
            }
        }

    }
}
