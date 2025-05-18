package io.github.lumine1909.registrylib.common;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {

    public static final String PACKET_HANDLER_NAME = "registrylib_handler";
    public static final Key CHANNEL_INIT_KEY = Key.key("registrylib", PACKET_HANDLER_NAME);
    public static int MC_VER = obtainVersion();

    private static int obtainVersion() {
        try {
            Matcher matcher = Pattern.compile("\\(MC: ([^)]+)\\)").matcher(Bukkit.getVersion());
            if (!matcher.find()) {
                return -1;
            }
            String[] versions = matcher.group(1).split("\\.");
            if (versions.length == 2) {
                return Integer.parseInt(versions[1]) * 100;
            } else if (versions.length == 3) {
                return Integer.parseInt(versions[1]) * 100 + Integer.parseInt(versions[2]);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }
}
