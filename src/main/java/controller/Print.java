package controller;

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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import data.GlobalData;
import model.Product;
import model.Transaction;
import model.TransactionItem;

public class Print implements Printable {

    private static final String MONEY_FORMAT = "%.2f";
    private static Transaction transaction;
    private static Set<TransactionItem> items;
    private static final String dashLine = "-".repeat(70);


    /**
     * Sends signal to open the printer-attached till.
     */
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

    /**
     * ??
     * @param trans     Transaction to be printed
     */
    public static void printSheet(Transaction trans) {
        int X_OFFSET = 10;
        int Y_OFFSET = 10;
        int PAGE_WIDTH = 200;
        int PAGE_BASE_HEIGHT = 200;
        int PAGE_ROW_HEIGHT = 10;
        transaction = trans;
        items = trans.getItems();
        Book book = new Book();
        // set to vertical
        PageFormat pf = new PageFormat();
        pf.setOrientation (PageFormat.PORTRAIT);

        Paper p = new Paper();
        int height = PAGE_BASE_HEIGHT + PAGE_ROW_HEIGHT * items.size();

        p.setSize (PAGE_WIDTH, height);
        p.setImageableArea (X_OFFSET, Y_OFFSET, PAGE_WIDTH - X_OFFSET, height - Y_OFFSET);
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

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        try {
            // Convert to Graphics2D
            Graphics2D g2d = (Graphics2D) graphics;
            // Set the printing color to black
            g2d.setColor(Color.black);
            // Print starting point coordinates
            if (pageIndex == 0) {
                float x = (float) pageFormat.getImageableX();
                float y = (float) (pageFormat.getImageableY() + 10);
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
                g2d.drawString(dashLine, (float) x, (float) y);
                y += fontContent.getSize2D() + 4;
                BigDecimal SubTotal = BigDecimal.ZERO;

                for (TransactionItem item: items) {
                    g2d.drawString( item.getProductName(), x, y);
                    BigDecimal lineTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    SubTotal = SubTotal.add(lineTotal);
                    g2d.drawString("" + item.getQuantity(), x + 110, (float) y);
                    g2d.drawString(String.format(MONEY_FORMAT, item.getPrice()), x + 130, y);
                    g2d.drawString(String.format(MONEY_FORMAT, lineTotal), x + 165, y);
                    y += fontContent.getSize2D() + 2;
                }
                y += fontContent.getSize2D() + 8;
                g2d.drawString("Balance Due:", (float) x, (float) y);
                g2d.drawString(String.format(MONEY_FORMAT, SubTotal), (float) x+158, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Payment:", (float) x, (float) y);
                g2d.drawString(String.format(MONEY_FORMAT, transaction.getPayment()), (float) x+158, (float) y);
                y += fontContent.getSize2D() + 4;
                g2d.drawString("Change:", (float) x, (float) y);
                g2d.drawString(String.format(MONEY_FORMAT, transaction.getPayment().subtract(SubTotal)), (float) x+158, (float) y);
                y += fontContent.getSize2D() + 2;

                g2d.drawString(dashLine, (float) x, (float) y);
                y += fontContent.getSize2D() + 2;

                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formatDateTime = transaction.getDateTime().format(format);
                g2d.drawString( formatDateTime, (float) x, (float) y);
                y += fontContent.getSize2D() + 4;

                g2d.drawString(transaction.getType().toString(), x, y);
                y += fontContent.getSize2D() + 4;

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
