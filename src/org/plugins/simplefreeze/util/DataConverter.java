package org.plugins.simplefreeze.util;

import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.FrozenPlayer;
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
            }
            catch (FileNotFoundException e) {
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
            }
            catch (FileNotFoundException e) {
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
            }
            catch (IOException e) {
                return;
            }
        }

    }
}
