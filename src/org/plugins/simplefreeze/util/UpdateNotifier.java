package org.plugins.simplefreeze.util;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateNotifier {

    public final static String VERSION = "3.0.2";

    public static String getLatestVersion() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + "18044").getBytes("UTF-8"));
            String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            if (version.contains(" ")) {
                return version.substring(0, version.indexOf(" "));
            } else {
                return version;
            }
        } catch (Exception ex) {
            Bukkit.getLogger().info("Failed to check for a update on spigot.");
            return "";
        }
    }

    public static String getCurrentVersion() {
        return UpdateNotifier.VERSION;
    }

}
