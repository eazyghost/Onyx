package net.onyx.client.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.onyx.client.OnyxClient;
import net.onyx.client.modules.Module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Persistence {
    public static final String CONFIG_PATH = "walksy-config.json";
    Persistence() { }

    /**
     * Read the config file into a string
     * @param path the path to the config file
     * @return the config file as a string
     * @throws FileNotFoundException if the file is not found
     */
    private static String readConfig(String path) throws FileNotFoundException {
        // Create the string builder
        StringBuilder sb = new StringBuilder();

        // Create the scanner
        Scanner sc = new Scanner(new File(path));

        // Read the file
        while (sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }

        // Close the scanner
        sc.close();

        // Return the string
        return sb.toString();
    }

    private static void writeConfig(String data, String path) throws IOException {
        File file = new File(path);
        FileWriter writer = new FileWriter(file);

        writer.write(data);

        writer.close();
    }

    public static boolean loadConfig() {
        String data;
        try {
            data = readConfig(CONFIG_PATH);
        } catch (FileNotFoundException e) {
            OnyxClient.log("No config file found... creating one...");
            saveConfig();

            return false;
        }

        // Load all of the flattened states
        HashMap<String, HashMap<String, String>> flat = new Gson().fromJson(data, new TypeToken<HashMap<String, HashMap<String, String>>>() {}.getType());
        for (String name : flat.keySet()) {
            Module module = OnyxClient.getInstance().getModules().get(name);
            
            // Check that we can get the module
            if (module == null) {
                OnyxClient.log(String.format("Unable to find module '%s,' ignoring...", name));
                continue;
            }

            module.lift(flat.get(name));
        }

        return true;
    }

    public static String makeConfig() {
        Gson gson = new Gson();

        // Save all of the modules
        HashMap<String, HashMap<String, String>> flat = new HashMap<String, HashMap<String, String>>();
        for (String name : OnyxClient.getInstance().getModules().keySet()) {
            Module module = OnyxClient.getInstance().getModules().get(name);

            flat.put(name, module.flatten());
        }

        // TODO Save friends list

        return gson.toJson(flat);
    }

    public static void saveConfig() {
        String json = makeConfig();
        try {
            writeConfig(json, CONFIG_PATH);
        } catch (IOException e) {
            OnyxClient.log(e);
        }
    }
}
