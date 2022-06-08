import com.google.gson.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Main {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final String[] dependencyUrls = new String[] {
            "https://cdn.azura.best/download/library/azura/eventbus/1.2.0/azura-event-bus-1.2.0.jar",
            "https://cdn.azura.best/download/library/viaversion/1.30/ViaSnakeYaml-1.30.jar",
            "https://cdn.azura.best/download/library/viaversion/2.0.3/ViaRewind-2.0.3-SNAPSHOT.jar",
            "https://cdn.azura.best/download/library/viaversion/4.3.2/ViaVersion-4.3.2-SNAPSHOT.jar",
            "https://cdn.azura.best/download/library/viaversion/4.3.1/ViaBackwards-4.3.1-SNAPSHOT.jar",
            "https://cdn.azura.best/download/library/kingalts/kinggen/1.4.2/KingGen-1.4.2.jar",
            "https://cdn.azura.best/download/library/discordrpc/1.0.0/discord-rpc.jar",
            "https://cdn.azura.best/download/library/openauth/1.1.2/openauth-1.1.2.jar",
            "https://cdn.azura.best/download/library/jna/5.10.0/jna-5.10.0.jar",
            "https://cdn.azura.best/download/library/jna/5.10.0/jna-platform-5.10.0.jar",
            "https://cdn.azura.best/download/library/log4j/api/2.17.0/log4j-api-2.17.0.jar",
            "https://cdn.azura.best/download/library/log4j/core/2.17.0/log4j-core-2.17.0.jar",
            "https://cdn.azura.best/download/library/oshi/core/6.1.3/oshi-core-6.1.3.jar",
            "https://cdn.azura.best/download/library/slf4j/api/1.7.36/slf4j-api-1.7.36.jar",
            "https://cdn.azura.best/download/library/slf4j/simple/1.7.36/slf4j-simple-1.7.36.jar",
    };

    private static final ArrayList<JsonObject> jsonObjectArrayList = new ArrayList<>();

    public static void main(String[] args) {
        try {
            System.out.println("Downloading Dependencies.");

            for (String url : dependencyUrls) {
                System.out.println("Downloading " + url);

                JsonObject jsonObject = new JsonObject();
                String[] splits = url.split("/");
                String fileName = splits[splits.length - 1];
                String name = fileName.replace(".jar", "");
                String version = splits[splits.length - 2];

                InputStream in = new URL(url).openStream();
                Path libsPath = Path.of("libs/", fileName);
                Files.copy(in, libsPath, StandardCopyOption.REPLACE_EXISTING);
                File actualFile = new File("libs/", fileName);

                System.out.println("Downloaded " + fileName);

                String sha1 = createSha1(actualFile).toLowerCase();

                System.out.println("Created SHA-1 Hash " + sha1);

                long size = Files.size(libsPath);

                System.out.println("Received size " + size);

                JsonObject artifact = new JsonObject();
                artifact.addProperty("path", "azura/" + fileName);
                artifact.addProperty("sha1", sha1);
                artifact.addProperty("size", size);
                artifact.addProperty("url", url);

                JsonObject downloads = new JsonObject();

                downloads.add("artifact", artifact);

                jsonObject.addProperty("name", "best.azura:" + name + ":" + version);
                jsonObject.add("downloads", downloads);

                System.out.println("Created JsonObject " + fileName);
                jsonObjectArrayList.add(jsonObject);
            }

            JsonElement jsonElement = JsonParser.parseString(Files.readString(new File("version.json").toPath()));

            System.out.println("Loading current version.json.");

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                System.out.println("Valid base JSON.");

                if (jsonObject.has("libraries") && jsonObject.get("libraries").isJsonArray()) {
                    System.out.println("Valid libraries JsonArray.");
                    JsonArray jsonArray = jsonObject.getAsJsonArray("libraries");
                    for (JsonObject libs : jsonObjectArrayList) {
                        System.out.println("Adding Library to JsonArray.");
                        jsonArray.add(libs);
                    }
                    jsonObject.add("libraries", jsonArray);
                    System.out.println("Printing.");
                    try (PrintWriter printWriter = new PrintWriter("out.json")) {
                        printWriter.println(gson.toJson(jsonObject));
                        System.out.println("Finished.");
                    } catch (Exception exception) {
                        System.out.println("Couldn't write into out.json!\nException: " + exception.getMessage());
                    }
                } else {
                    System.out.println("Got an Invalid JSON.");
                }
            } else {
                System.out.println("Got an Invalid JSON.");
            }
        } catch (Exception exception) {
            System.out.println("Error!\nException: " + exception.getMessage());
        }
    }

    public static String createSha1(File file)  {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream fis = new FileInputStream(file)) {
                int n = 0;
                byte[] buffer = new byte[8192];
                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        digest.update(buffer, 0, n);
                    }
                }
                return new HexBinaryAdapter().marshal(digest.digest());
            } catch (Exception ignore) {
            }
        } catch (Exception ignore) {}

        return "";
    }
}
