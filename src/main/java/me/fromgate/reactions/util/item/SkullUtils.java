package me.fromgate.reactions.util.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SkullUtils {
    static private final JsonParser parser = new JsonParser();
    static private final String API_PROFILE_LINK = "https://sessionserver.mojang.com/session/minecraft/profile/";
    static private final String API_USERS_LINK = "https://api.mojang.com/users/profiles/minecraft/";
    static private final Map<String, String> skinCache = new HashMap<>();

    public static String getSkinUrlByName(String name){
        if (skinCache.containsKey(name)) return skinCache.get(name);

        String json = getContent(API_USERS_LINK + name);
        JsonObject o = parser.parse(json).getAsJsonObject();
        String uuid = o.get("id").getAsString();
        String skinUrl = getSkinUrlByUUID(uuid);

        skinCache.put(name, skinUrl);

        return skinUrl;
    }

    public static String getSkinUrlByUUID(String uuid){
        String json = getContent(API_PROFILE_LINK + uuid);
        JsonObject o = parser.parse(json).getAsJsonObject();
        String jsonBase64 = o.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
        return jsonBase64;
    }

    private static String getContent(String link){
        try {
            URL url = new URL(link);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder inputLine = new StringBuilder();
            while (br.ready()) {
                inputLine.append(br.readLine());
            }
            br.close();
            return inputLine.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
