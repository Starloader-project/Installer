package de.geolykt.starloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Utils {

    /**
     * The constructor.
     * DO NOT CALL THE CONSTRUCTOR.
     */
    private Utils() {
        // Do not construct classes for absolutely no reason at all
        throw new RuntimeException("Didn't the javadoc tell you to NOT call the constructor of this class?");
    }

    public static final int STEAM_GALIMULATOR_APPID = 808100;
    public static final String STEAM_GALIMULATOR_APPNAME = "Galimulator";

    public static final String OPERATING_SYSTEM = System.getProperty("os.name");

    public static final String STEAM_WINDOWS_REGISTRY_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam";
    public static final String STEAM_WINDOWS_REGISTRY_INSTALL_DIR_KEY = "InstallPath";

    public static byte[] streamReadAllBytes(InputStream stream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        try {
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return buffer.toByteArray();
        }
        return buffer.toByteArray();
    }

    /**
     * Stupid little hack.
     *
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     * @author Oleg Ryaboy, based on work by Miguel Enriquez; Made blocking and fixed by Geolykt
     */
    public static final String readWindowsRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            Process process = Runtime.getRuntime().exec("reg query " +
                    '"'+ location + "\" /v " + key);

            process.waitFor();
            @SuppressWarnings("resource")
            InputStream is = process.getInputStream();
            String output = new String(streamReadAllBytes(is), StandardCharsets.UTF_8);
            is.close();

            if(!output.contains(location) ||!output.contains(key)){
                return null;
            }

            // Parse out the value
            // For me this results in:
            // [, HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Valve\Steam, InstallPath, REG_SZ, D:\Programmes\Steam]
            String[] parsed = output.split("\\s+");
            return parsed[parsed.length-1];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final File getSteamExecutableDir() {
        if (OPERATING_SYSTEM.toLowerCase(Locale.ROOT).startsWith("win")) {
            String val = readWindowsRegistry(STEAM_WINDOWS_REGISTRY_KEY, STEAM_WINDOWS_REGISTRY_INSTALL_DIR_KEY);
            System.out.println(val);
            if (val == null) {
                return null;
            }
            return new File(val);
        } else {
            // Assuming UNIX, though for real we should check other OSes
            String homeDir = System.getProperty("user.home");
            File usrHome = new File(homeDir);
            File steamHome = new File(usrHome, ".steam");
            return new File(steamHome, "steam");
        }
    }

    public static final File getOneOfExistingFiles(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static File getGameDir(String game) {
        File steamExec = null;
        try {
            steamExec = getSteamExecutableDir();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (steamExec == null || !steamExec.exists()) {
            if (OPERATING_SYSTEM.toLowerCase(Locale.ROOT).startsWith("win")) {
                steamExec = getOneOfExistingFiles("C:\\Steam\\", "C:\\Program Files (x86)\\Steam\\",
                        "C:\\Program Files\\Steam\\", "D:\\Steam\\", "C:\\Programmes\\Steam\\", "D:\\Programmes\\Steam\\");
            } else {
                // Assuming my install
                String homeDir = System.getProperty("user.home");
                File usrHome = new File(homeDir);
                File steamHome = new File(usrHome, ".steam");
                steamExec = new File(steamHome, "steam");
                if (!steamExec.exists()) {
                    // some have their steam installs in ~/Steam as they do not like hidden directories
                    steamHome = new File(usrHome, "Steam");
                    steamExec = new File(steamHome, "steam");
                    if (!steamExec.exists()) {
                        if (!steamHome.exists()) {
                            return null;
                        } else {
                            steamExec = steamHome;
                        }
                    }
                }
            }
            if (steamExec == null) {
                return null;
            }
        }
        if (!steamExec.isDirectory()) {
            throw new Error("Space and time just collapsed");
        }
        File appdata = new File(steamExec, "steamapps");
        File common = new File(appdata, "common");
        return new File(common, game);
    }

    public static File getCurrentDir() {
        return new File(".");
    }
}
