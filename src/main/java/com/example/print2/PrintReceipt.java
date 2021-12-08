package com.example.print2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrintReceipt {

    public static void main(String[] args) {
        List<Items> items = new ArrayList<>();
        Transaction trans = new Transaction(1234,LocalDateTime.now(),(float)5000,"EFTPOS");
        Items item = new Items ("12345678901234567890", 10, (float) 32.10);
        for(int i=0;i<10;i++) {
            items.add(item);
        }
        GlobalData.init();

        //Print.printSheet(trans, items);
        Print.Kick();
    }

}
