package com.example.print2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class GlobalData {
    private static String shopName;
    private static String shopAddress;
    private static String shopTelNo;
    private static String GSTNo;

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
    private static void getShopDetails(){
        String filePath = "C:\\Users\\Alan\\IdeaProjects\\pos-project\\src\\main\\resources\\data.txt";
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("@"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        //System.out.println(contentBuilder.toString().length());
        String[] str=contentBuilder.toString().split("@");
        shopName = str[0];
        shopAddress=str[1];
        shopTelNo=str[2];
        GSTNo=str[3];
    }
}
