package data;

import enums.PaymentType;
import model.Product;
import model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Persistence {
    public static String dbUrl = "jdbc:ucanaccess://" + System.getProperty("user.dir") + "\\Pos.mdb";

    public Persistence() {}

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
            return Optional.of(new Product(results.getString("Product"), results.getFloat("RP"),
                    results.getFloat("DRP"), results.getInt("Unit"), results.getInt("Stock"),
                    barcode.equals(results.getString("BarCode2"))));
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String queryString = "UPDATE Product SET Stock = ?, RP = ?, Unit = ?, DRP = ? WHERE Product = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setInt(1, product.getStockLevel());
            statement.setFloat(2, product.getPrice());
            statement.setInt(3, product.getUnit());
            if (product.getDrp() > 0) {
                statement.setFloat(4, product.getDrp());
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
            PreparedStatement statement = conn.prepareStatement(queryString);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return Optional.empty();
            }
            int transactionId = results.getInt("id");
            LocalDate date = results.getDate("date").toLocalDate();
            LocalTime time = results.getTime("time").toLocalTime();
            LocalDateTime dateTime = date.atTime(time);
            float payment = results.getFloat("payment");
            String typeString = results.getString("paymentMethod");
            if (typeString == null) {
                throw new IllegalArgumentException();
            }
            PaymentType type = PaymentType.fromString(typeString);
            statement.close();

            queryString = "SELECT Product.Product, RP, Unit, BarCode, DRP, Stock, BarCode2, Qty FROM Product INNER JOIN" +
                    " (SELECT * FROM TransactionItem WHERE TransactionID = ?) T ON T.Product = Product.Product";
            statement = conn.prepareStatement(queryString);
            statement.setInt(1, transactionId);
            results = statement.executeQuery();
            Map<Product, Integer> items = new HashMap<>();
            while (results.next()) {
                Product product = new Product(results.getString("Product"), results.getFloat("RP"),
                        results.getFloat("DRP"), results.getInt("Unit"),
                        results.getInt("Stock"),false);
                int quantity = results.getInt("Qty");
                items.put(product, quantity);
            }
            statement.close();

            return Optional.of(new Transaction(items, dateTime, type, payment));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int saveTransaction(Transaction transaction) {
        String transactionQuery = "INSERT INTO Transactions (Date, Time, Payment, PaymentMethod, Type) VALUES (?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO TransactionItem (TransactionID, Product, Qty, Price) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl);
                PreparedStatement transactionSt = connection.prepareStatement(transactionQuery, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement itemSt = connection.prepareStatement(itemQuery)){
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            transactionSt.setDate(1, Date.valueOf(transaction.getDateTime().toLocalDate()));
            transactionSt.setTime(2, Time.valueOf(transaction.getDateTime().toLocalTime()));
            transactionSt.setFloat(3, transaction.getPayment());
            transactionSt.setString(4, transaction.getType().toString());
            transactionSt.setString(5, transaction.isComplete() ? "S": "N");
            transactionSt.executeUpdate();
            ResultSet set = transactionSt.getGeneratedKeys();
            if (!set.next()) {
                throw new IllegalArgumentException("Error inserting transaction");
            }
            int transactionId = set.getInt(1);
            set.close();
            for (Product product: transaction.getItems().keySet()) {
                itemSt.setInt(1, transactionId);
                itemSt.setString(2, product.getName());
                itemSt.setInt(3, transaction.getItems().get(product));
                itemSt.setFloat(4, product.getPrice());
                itemSt.executeUpdate();
            }
            return 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
