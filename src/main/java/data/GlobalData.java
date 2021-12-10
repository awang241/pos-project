package data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class GlobalData {
    private static String shopName;
    private static String shopAddress;
    private static String shopTelNo;
    private static String GSTNo;
    private static final Path FILEPATH = Path.of(System.getProperty("user.dir"), "data.txt");

    public static void init(){
        getShopDetails();
    }

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

    public static void getShopDetails(){
        try {
            List<String> lines = Files.readAllLines(FILEPATH, StandardCharsets.UTF_8);
            shopName = lines.get(0).strip();
            shopAddress = lines.get(1).strip();
            shopTelNo = lines.get(2).strip();
            GSTNo = lines.get(3).strip();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static boolean shopDataFileExists() {
        return Files.exists(FILEPATH);
    }

    public static void createShopDetails(String shopAddress, String shopTelNo, String GSTNo, String shopName) throws IOException {
        String text = String.format("%s\n%s\n%s\n%s\n", shopAddress, shopTelNo, GSTNo, shopName);
        if (shopDataFileExists()) {
            File fOld = new File(FILEPATH.toString());
            if (!(fOld.delete())) {
                throw new IOException("A");
            }
        }
        File dataFile = new File(FILEPATH.toString());
        dataFile.createNewFile();
        try (FileWriter fw = new FileWriter(dataFile)){
            fw.write(text);
        }
    }
}
