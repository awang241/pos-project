package data;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import enums.PaymentType;
import enums.Period;
import model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class Persistence {
    private static String dbUrl = "jdbc:ucanaccess://" + System.getProperty("user.dir") + "\\Pos.mdb";

    public Persistence(String URL) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        dbUrl = URL;
    }

    public Optional<Product> findProductByBarcode(String barcode) throws SQLException{
        String queryString = "Select * from Product where BarCode=? or BarCode2=?";
        ResultSet results;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setString(1, barcode);
            statement.setString(2, barcode);
            results = statement.executeQuery();
            if (!results.next()) {
                return Optional.empty();
            }
            return Optional.of(new Product(results.getString("Product"), results.getBigDecimal("RP"),
                    results.getBigDecimal("DRP"), results.getInt("Unit"), results.getInt("Stock"),
                    results.getString("DiscountCode"), barcode.equals(results.getString("BarCode2"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Set<Product> findSalesBetweenDates(LocalDateTime start, LocalDateTime end) {
        String queryString = "SELECT Product.Product, Product.RP, SUM(TransactionItem.Qty) AS Qty, SUM(TransactionItem.Price * Qty) As Sales " +
                "FROM ((TransactionItem " +
                    "INNER JOIN (SELECT * FROM Transactions WHERE Date BETWEEN ? AND ?) As T " +
                    "ON T.ID = TransactionItem.TransactionID) " +
                        "INNER JOIN Product ON Product.Product = TransactionItem.Product) " +
                "GROUP BY Product.Product, Product.RP";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setDate(1, Date.valueOf(start.toLocalDate()));
            statement.setDate(2, Date.valueOf(end.toLocalDate()));
            ResultSet results = statement.executeQuery();

            Set<Product> products = new HashSet<>();
            while (results.next()) {
                Product product = new Product(results.getString("Product"), results.getBigDecimal("RP"),
                        results.getBigDecimal("Sales"), 0, results.getInt("Qty"), "",
                        false);
                products.add(product);
            }
            return products;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Table<String, Integer, Integer> findSalesByPeriod(int year, Period period) {
        String queryString = "" +
                "SELECT Product.Product, Iif(Sum(TransactionItem.Qty) is null, 0, Sum(TransactionItem.Qty)) AS Qty, DatePart(%s,[Date]) AS Index " +
                "FROM (TransactionItem " +
                    "INNER JOIN (SELECT * FROM Transactions WHERE DatePart(\"yyyy\", [Date]) = ?) AS T " +
                    "ON TransactionItem.TransactionID = T.ID) " +
                        "INNER JOIN Product ON TransactionItem.Product = Product.Product " +
                "GROUP BY Product.Product, DatePart(%s, [Date]) ";
        String datePart = period.equals(Period.MONTHLY) ? "\"m\"": "\"ww\"";
        queryString = String.format(queryString, datePart, datePart);
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setInt(1, year);
            ResultSet results = statement.executeQuery();
            Table<String, Integer, Integer> products = TreeBasedTable.create();
            while (results.next()) {
                String product = results.getString("Product");
                int index = results.getInt("Index");
                int quantity = results.getInt("Qty");
                products.put(product, index, quantity);
            }
            return products;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProduct(Product product) throws SQLException {
        String queryString = "UPDATE Product SET Stock = ?, RP = ?, Unit = ?, DRP = ? WHERE Product = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setInt(1, product.getStockLevel());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getUnit());
            if (product.getDrp().compareTo(BigDecimal.ZERO) > 0) {
                statement.setBigDecimal(4, product.getDrp());
            } else {
                statement.setNull(4, Types.FLOAT);
            }
            statement.setString(5, product.getName());
            statement.executeUpdate();
        }
    }

    public Optional<Transaction> findLastInsertedTransaction() {
        String queryString = "SELECT id, date, time, payment, paymentMethod " +
                "FROM Transactions WHERE id = (SELECT MAX(id) FROM Transactions)";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            PreparedStatement statement = conn.prepareStatement(queryString);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return Optional.empty();
            }
            int transactionId = results.getInt("id");
            LocalDate date = results.getDate("date").toLocalDate();
            LocalTime time = results.getTime("time").toLocalTime();
            LocalDateTime dateTime = date.atTime(time);
            BigDecimal payment = results.getBigDecimal("payment");
            String typeString = results.getString("paymentMethod");
            if (typeString == null) {
                throw new IllegalArgumentException();
            }
            PaymentType type = PaymentType.fromString(typeString);
            statement.close();

            queryString = "SELECT * FROM TransactionItem WHERE TransactionID = ?";
            statement = conn.prepareStatement(queryString);
            statement.setInt(1, transactionId);
            results = statement.executeQuery();
            Set<TransactionItem> items = new HashSet<>();
            int uncodedIndex = 1;
            while (results.next()) {
                String name = results.getString("Product");
                BigDecimal price = results.getBigDecimal("Price");
                TransactionItem item;

                if (name.matches(TransactionItem.UNCODED + "\\d*")) {
                    item = TransactionItem.createUncodedProduct(uncodedIndex, price);
                    uncodedIndex += 1;
                } else if (name.equals(TransactionItem.CASH_OUT)){
                    item = TransactionItem.createCashProduct(price);
                } else {
                    long id = results.getLong("ID");
                    int quantity = results.getInt("Qty");
                    String code = results.getString("DiscountCode");
                    item = new TransactionItem(id, name, quantity, code, price);
                }
                items.add(item);
            }
            statement.close();

            return Optional.of(new Transaction(items, dateTime, type, payment));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean saveTransaction(Transaction transaction) {
        String transactionQuery = "INSERT INTO Transactions (Date, Time, Payment, PaymentMethod, Type) VALUES (?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO TransactionItem (TransactionID, Product, Qty, Price) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl);
                PreparedStatement transactionSt = connection.prepareStatement(transactionQuery, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement itemSt = connection.prepareStatement(itemQuery)){
            connection.setAutoCommit(false);
            BigDecimal refundMultiplier = new BigDecimal(transaction.getType() == PaymentType.REFUND ? -1: 1);
            transactionSt.setDate(1, Date.valueOf(transaction.getDateTime().toLocalDate()));
            transactionSt.setTime(2, Time.valueOf(transaction.getDateTime().toLocalTime()));
            transactionSt.setBigDecimal(3, transaction.getPayment().multiply(refundMultiplier));
            transactionSt.setString(4, transaction.getType().toString());
            transactionSt.setString(5, transaction.isComplete() ? "S": "N");
            transactionSt.executeUpdate();
            ResultSet set = transactionSt.getGeneratedKeys();
            if (!set.next()) {
                throw new IllegalArgumentException("Error inserting transaction");
            }
            int transactionId = set.getInt(1);
            set.close();
            for (TransactionItem item: transaction.getItems()) {
                itemSt.setInt(1, transactionId);
                itemSt.setString(2, item.getProductName());
                itemSt.setInt(3, item.getQuantity());
                itemSt.setBigDecimal(4, item.getPrice());
                itemSt.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
