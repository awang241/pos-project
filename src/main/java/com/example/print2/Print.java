package com.example.print2;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Print implements Printable {

    private static Transaction transaction;
    private static List<Items> items;
    private static String DashLine;

    private  static void init(Transaction trans,List<Items> itemlist){
        transaction=trans;
        items =itemlist;
        DashLine="";
        for(int i=0;i<70;i++){
            DashLine =DashLine + "-";
        }
    }


     // Used to open till without print
     public static void Kick() {
        byte[] open = {27, 112, 48, 55, 121};
         PrintService pservice = PrintServiceLookup.lookupDefaultPrintService();
         DocPrintJob job = pservice.createPrintJob();
         DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
         Doc doc = new SimpleDoc(open,flavor,null);
         PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
         try {
             job.print(doc, aset);
         } catch (PrintException ex) {
             System.out.println(ex.getMessage());
         }
    }


    public static void printSheet(Transaction trans,List<Items> itemList) {
        init( trans,itemList);
        Book book = new Book();
        // set to vertical
        PageFormat pf = new PageFormat();
        pf.setOrientation (PageFormat.PORTRAIT);

        Paper p = new Paper();
        int length = printSize (itemList);

        p.setSize (200, length);
        p.setImageableArea (10, 10, 190, length);
        pf.setPaper(p);

        book.append(new Print(), pf);

        PrinterJob job = PrinterJob.getPrinterJob();
        // Set the printing class
        job.setPageable(book);
        try {
            job.print();
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    private static Integer printSize(List<Items> itemList) {
        // The value-added parameter is 200, increasing the number of rows requires increasing the height
        int height = 200;
        if (itemList.size() > 0) {
            height += itemList.size()*10;
        }
		return height;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        try {
            // Convert to Graphics2D
            Graphics2D g2d = (Graphics2D) graphics;
            // Set the printing color to black
            g2d.setColor(Color.black);
            // Print starting point coordinates
            if (pageIndex == 0) {
                double x = pageFormat.getImageableX();
                double y = pageFormat.getImageableY() + 10;
                // Set the printing font (font name, style and dot size) (the font name can be a physical or logical name)
                Font fontTitle = new Font("   ", Font.BOLD, 16);
                g2d.setFont(fontTitle); // Set font
                // print title
                g2d.drawString(GlobalData.getShopName(), (float) x , (float) y+2);
                y += fontTitle.getSize2D() + 4;

                Font fontContent = new Font("   ", Font.PLAIN, 9);
                g2d.setFont(fontContent); // Set font
                // print order number
                g2d.drawString( GlobalData.getShopAddress(), (float) x, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Telephone: " + GlobalData.getShopTelNo(), (float) x, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Tax invoice" , (float) x, (float) y);
                g2d.drawString("GST Reg No." + GlobalData.getGSTNo(), (float) x+85, (float) y);
                y += fontContent.getSize2D() + 10;
                g2d.drawString("Product", (float) x, (float) y);
                g2d.drawString("Qty", (float) x + 110, (float) y);
                g2d.drawString("Price", (float) x + 130, (float) y);
                g2d.drawString("$", (float) x + 165, (float) y);
                y += fontContent.getSize2D() + 1;
                g2d.drawString(DashLine, (float) x, (float) y);
                y += fontContent.getSize2D() + 4;
                double SubTotal = 0.0;

                for (int i = 0; i < items.size(); i++) {
                    g2d.drawString( items.get(i).getProduct(), (float) x, (float) y);
                    double lineTotal=0.0;
                    lineTotal=items.get(i).getCount()*items.get(i).getPrice();
                    SubTotal+=lineTotal;
                    g2d.drawString(""+(items.get(i).getCount()), (float) x + 110, (float) y);
                    g2d.drawString(""+items.get(i).getPrice(), (float) x + 130, (float) y);
                    g2d.drawString(""+lineTotal, (float) x + 165, (float) y);
                    y += fontContent.getSize2D() + 2;
                }
                y += fontContent.getSize2D() + 8;
                g2d.drawString("Balance Due:", (float) x, (float) y);
                g2d.drawString("$"+SubTotal, (float) x+158, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Payment:", (float) x, (float) y);
                g2d.drawString("$"+transaction.getPayment(), (float) x+158, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Change:", (float) x, (float) y);
                g2d.drawString("$"+(transaction.getPayment()-SubTotal), (float) x+158, (float) y);
                y += fontContent.getSize2D() + 2;

                g2d.drawString(DashLine, (float) x, (float) y);
                y += fontContent.getSize2D() + 2;
                g2d.drawString(transaction.getID() + "", (float) x, (float) y);

                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formatDateTime = transaction.getDateTime().format(format);

                g2d.drawString( formatDateTime, (float) x+100, (float) y);

                y += fontContent.getSize2D() + 8;
                g2d.drawString ("GST Included", (float) x, (float) y);

                y += fontContent.getSize2D() + 10;
                g2d.drawString(" ", (float) x, (float) y);

                return PAGE_EXISTS;
            }
            return NO_SUCH_PAGE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
