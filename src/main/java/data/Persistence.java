package data;

import model.Product;
import model.Transaction;

import java.sql.*;
import java.util.Optional;

public class Persistence {
    private static String dbUrl = "jdbc:ucanaccess:////home/alan/VirtualBox VMs/VM Shared Files/POS/Pos.mdb";

    public Persistence() {}

    public Optional<Product> findProductByBarcode(String barcode) throws SQLException{
        String queryString = String.format("Select * from Product where BarCode='%1$s' or BarCode2='%1$s'", barcode);
        ResultSet results;
        try {
            results = query(queryString);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("");
        }

        if (!results.next()) {
            return Optional.empty();
        }
        return Optional.of(new Product(results.getString("Product"), results.getFloat("RP"),
                results.getFloat("DRP"), results.getInt("Unit"), results.getInt("Stock"),
                barcode.equals(results.getString("BarCode2"))));
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

    public int saveTransaction(Transaction transaction) {
        String transactionQuery = "INSERT INTO Transactions (Date, Payment, PaymentMethod, Type) VALUES (?, ?, ?, ?)";
        String itemQuery = "INSERT INTO TransactionItem (TransactionID, Product, Qty, Price) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl);
                PreparedStatement transactionSt = connection.prepareStatement(transactionQuery, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement itemSt = connection.prepareStatement(itemQuery)){
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            transactionSt.setObject(1, transaction.getDateTime());
            transactionSt.setFloat(2, transaction.getTotal());
            transactionSt.setString(3, transaction.getType().toString());
            transactionSt.setString(4, transaction.isComplete() ? "S": "N");
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

    private ResultSet query(String queryString) throws SQLException, ClassNotFoundException {
        ResultSet data;
        try (Connection connection = DriverManager.getConnection(dbUrl); Statement st = connection.createStatement()){
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            data = st.executeQuery(queryString);
        }

        return data;

    }


}
