package data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GlobalData {
    private static String shopName = "";
    private static String shopAddress = "";
    private static String shopTelNo = "";
    private static String GSTNo = "";
    private static String dbUrl = "";
    private static final Path FILEPATH = Path.of(System.getProperty("user.dir"), "data.txt");

    public static String getShopAddress(){
        return shopAddress;
    }
    public static String getShopTelNo(){
        return shopTelNo;
    }
    public static String getGSTNo(){
        return GSTNo;
    }
    public static String getShopName(){
        return shopName;
    }
    public static String getDbUrl() {
        return dbUrl;
    }

    public static void loadShopDetails(){
        try {
            List<String> lines = Files.readAllLines(FILEPATH, StandardCharsets.UTF_8);
            shopName = lines.get(0).strip();
            shopAddress = lines.get(1).strip();
            shopTelNo = lines.get(2).strip();
            GSTNo = lines.get(3).strip();
            dbUrl = lines.get(4).strip();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static boolean shopDataFileExists() {
        return Files.exists(FILEPATH);
    }

    public static void createShopDetails(String shopAddress, String shopTelNo, String GSTNo, String shopName, String dbUrl) throws IOException {
        GlobalData.shopTelNo = shopTelNo;
        GlobalData.GSTNo = GSTNo;
        GlobalData.shopName = shopName;
        GlobalData.shopAddress = shopAddress;
        GlobalData.dbUrl = dbUrl;
        String text = String.format("%s\n%s\n%s\n%s\n%s",shopName, shopAddress, shopTelNo, GSTNo, dbUrl);
        File dataFile = new File(FILEPATH.toString());
        if (!dataFile.createNewFile()) {
            if (!(dataFile.delete())) {
                throw new IOException("Error deleting old data file.");
            }

            if (!dataFile.createNewFile()) {
                throw new IOException("Error creating data file.");
            }
        }

        try (FileWriter fw = new FileWriter(dataFile)){
            fw.write(text);
        }
    }
}
