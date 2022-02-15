package data;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GlobalData {
    public enum Key {
        ADDRESS("shopAddress"),
        TEL_NO("shopTelNo"),
        GST_NO("GSTNo"),
        NAME("shopName"),
        DB_FILEPATH("dbFilepath"),
        DB_USERNAME("dbUsername"),
        DB_PASSWORD("dbPassword");
        private final String key;

        Key(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private static final Properties properties = new Properties();
    private static final String APP_PROPERTIES = "application.properties";
    private static final URL JAR_PROPERTIES = GlobalData.class.getResource("application.properties");

    private static String encode(String string) {
        if (string == null) return null;
        byte[] a = string.getBytes(StandardCharsets.UTF_8);
        byte[] b = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = (byte) ((a[i] * 9) + 87);
        }
        return Arrays.toString(b);
    }

    private static String decode(String byteArray) {
        if (byteArray == null || byteArray.equals("[]") || byteArray.equals("")) return null;
        String[] byteStrings = byteArray.replaceAll("[\\[\\] \"]", "" ).split("\\s*,\\s*");
        byte[] bytes = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            byte b = Byte.parseByte(byteStrings[i]);
            bytes[i] = (byte) ((b - 87) * 57);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void createPropertiesFileIfNotExisting() throws IOException {
        File filepath = new File(APP_PROPERTIES);
        filepath.createNewFile();
    }

    public static void loadProperties() throws IOException {
        String filepath;
        if (Files.exists(Path.of("../", APP_PROPERTIES))) {
            filepath = "../" + APP_PROPERTIES;
        } else if (Files.exists(Path.of(APP_PROPERTIES))){
            filepath = APP_PROPERTIES;
        } else {
            throw new FileNotFoundException("Could not find application.properties file.");
        }

        try (InputStream stream = new FileInputStream(filepath)) {
            properties.load(stream);
        }
    }

    public static String getProperty(Key key) {
        String value = properties.getProperty(key.toString());
        return key.equals(Key.DB_PASSWORD) ? decode(value): value;
    }

    /**
     * Sets all given key-value pairs as properties and saves them to the application.properties file. If the given
     * value is null, that property will be deleted instead.
     * @param newProperties
     * @throws IOException
     */
    public static void setProperties(Map<Key, String> newProperties) throws IOException{
        for (Map.Entry<Key, String> property: newProperties.entrySet()) {
            String value = property.getValue();
            Key key = property.getKey();
            if (value == null) {
                properties.remove(key.toString());
            } else {
                properties.setProperty(key.toString(), key.equals(Key.DB_PASSWORD) ? encode(value): value);
            }
        }
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        properties.store(new FileWriter(APP_PROPERTIES), null);
    }

    public static boolean shopDataDoesNotExist() {
        return getProperty(Key.NAME) == null;
    }
}
