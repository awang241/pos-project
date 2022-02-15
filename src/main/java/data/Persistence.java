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
    public Persistence() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getDBUrl() {
        return "jdbc:ucanaccess://" + GlobalData.getProperty(GlobalData.Key.DB_FILEPATH);
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(getDBUrl(), null, GlobalData.getProperty(GlobalData.Key.DB_PASSWORD));
    }

    public List<Order> findAllOrders() {
        String orderQuery = "SELECT * FROM Orders";
        String itemQuery = "SELECT * FROM OrderItem WHERE OrderID = ?";
        try (Connection conn = createConnection();
                PreparedStatement orderSt = conn.prepareStatement(orderQuery);
                PreparedStatement itemSt = conn.prepareStatement(itemQuery)) {
            ResultSet resultSet = orderSt.executeQuery();
            ResultSet itemSet;
            List<Order> orders = new ArrayList<>();
            while (resultSet.next()) {
                long orderID = resultSet.getLong("OrderID");
                Order order = Order.builder()
                        .id(orderID)
                        .orderDate(resultSet.getDate("OrderDate").toLocalDate())
                        .deliveryDate(resultSet.getDate("DeliverDate").toLocalDate())
                        .paymentDate(resultSet.getDate("PaymentDate").toLocalDate())
                        .supplierCode(resultSet.getString("Distributor"))
                        .build();
                List<OrderItem> items = new ArrayList<>();
                itemSt.setLong(1, orderID);
                itemSet = itemSt.executeQuery();
                while (itemSet.next()) {
                    items.add(new OrderItem(itemSet.getInt("Qty"), orderID, itemSet.getBigDecimal("Price"), itemSet.getString("Product")));
                }
                itemSet.close();
                order.addItems(items);
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveOrder(Order order) {
        String orderQuery = "INSERT INTO Orders (OrderDate, DeliverDate, Distributor, PaymentDate) VALUES (?, ?, ?, ?)";
        String itemQuery = "INSERT INTO OrderItem (OrderID, Product, Qty, Price) VALUES (?, ?, ?, ?)";
        try (Connection conn = createConnection();
                PreparedStatement orderSt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement itemSt = conn.prepareStatement(itemQuery)) {
            conn.setAutoCommit(false);
            orderSt.setDate(1, Date.valueOf(order.getOrderDate()));
            orderSt.setDate(2, Date.valueOf(order.getDeliveryDate()));
            orderSt.setString(3, order.getSupplierCode());
            orderSt.setDate(4, Date.valueOf(order.getPaymentDate()));
            orderSt.executeUpdate();
            ResultSet keys = orderSt.getGeneratedKeys();
            long orderID;
            if (keys.next()) {
                orderID = keys.getLong(1);
            } else {
                throw new SQLException("Could not return Order ID");
            }
            for (OrderItem item: order.getItems()) {
                itemSt.setLong(1, orderID);
                itemSt.setString(2, item.getProduct());
                itemSt.setInt(3, item.getQuantity());
                itemSt.setBigDecimal(4, item.getPrice());
                itemSt.executeUpdate();
            }
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> findAllSupplierCodes() {
        String queryString = "Select SupplierID from Product group by SupplierID";
        Set<String> suppliers = new HashSet<>();
        try (Connection conn = createConnection();
             PreparedStatement statement = conn.prepareStatement(queryString);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String id = resultSet.getString("SupplierID");
                if (id != null) {
                    suppliers.add(resultSet.getString("SupplierID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public Optional<Product> findProductByBarcode(String barcode){
        String queryString = "Select * from Product where BarCode=? or BarCode2=?";
        ResultSet results;
        try (Connection conn = createConnection();
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

    public Set<Product> findProductBySupplier(String supplierCode){
        String queryString;
        if (supplierCode.equals(Order.ALL_SUPPLIERS)) {
            queryString = "Select * from Product";
        } else {
            queryString = "Select * from Product where SupplierID=?";
        }

        ResultSet results;
        Set<Product> products = new HashSet<>();
        try (Connection conn = createConnection();
             PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            if (!supplierCode.equals(Order.ALL_SUPPLIERS)) {
                statement.setString(1, supplierCode);
            }

            results = statement.executeQuery();
            while (results.next()) {
                Product product = Product.builder()
                        .name(results.getString("Product"))
                        .retailPrice(results.getBigDecimal("RP"))
                        .wholesalePrice(results.getBigDecimal("Price"))
                        .unitsPerCarton(results.getInt("Unit"))
                        .currentStock(results.getInt("Stock"))
                        .weeklyNeededStock(results.getInt("StockLevel"))
                        .supplierID(results.getString("SupplierID"))
                        .isCarton(false)
                        .build();
                products.add(product);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public void updateProduct(Product product) throws SQLException {
        String queryString = "UPDATE Product SET Stock = ?, RP = ?, Unit = ?, DRP = ? WHERE Product = ?";
        try (Connection conn = createConnection();
             PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setInt(1, product.getCurrentStock());
            statement.setBigDecimal(2, product.getRetailPrice());
            statement.setInt(3, product.getUnitsPerCarton());
            if (product.getDrp().compareTo(BigDecimal.ZERO) > 0) {
                statement.setBigDecimal(4, product.getDrp());
            } else {
                statement.setNull(4, Types.FLOAT);
            }
            statement.setString(5, product.getName());
            statement.executeUpdate();
        }
    }

    public List<Sales> findSalesBetweenDates(LocalDateTime start, LocalDateTime end) {
        String queryString = "SELECT Product.Product, Product.RP, SUM(TransactionItem.Qty) AS Qty, " +
            "SUM(TransactionItem.Price * Qty) As Sales, Price / Unit AS BuyPrice " +
                "FROM ((TransactionItem " +
                    "INNER JOIN (SELECT * FROM Transactions WHERE Date BETWEEN ? AND ?) As T " +
                    "ON T.ID = TransactionItem.TransactionID) " +
                        "INNER JOIN Product ON Product.Product = TransactionItem.Product) " +
                "GROUP BY Product.Product, Product.RP, BuyPrice";
        try (Connection conn = createConnection();
                PreparedStatement statement = conn.prepareStatement(queryString, Statement.NO_GENERATED_KEYS)) {
            statement.setDate(1, Date.valueOf(start.toLocalDate()));
            statement.setDate(2, Date.valueOf(end.toLocalDate()));
            ResultSet results = statement.executeQuery();

            List<Sales> salesList = new ArrayList<>();
            while (results.next()) {
                salesList.add(Sales.builder().productName(results.getString("Product"))
                        .buyPrice(results.getBigDecimal("BuyPrice"))
                        .quantity(results.getInt("Qty"))
                        .totalSales( results.getBigDecimal("Sales"))
                        .sellPrice(results.getBigDecimal("RP"))
                        .build());
            }
            return salesList;
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
        try (Connection conn = createConnection();
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

    public Optional<Transaction> findLastInsertedTransaction() {
        String queryString = "SELECT id, date, time, payment, paymentMethod " +
                "FROM Transactions WHERE id = (SELECT MAX(id) FROM Transactions)";
        try (Connection conn = createConnection()) {
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

    public void saveTransaction(Transaction transaction) {
        String transactionQuery = "INSERT INTO Transactions (Date, Time, Payment, PaymentMethod, Type) VALUES (?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO TransactionItem (TransactionID, Product, Qty, Price) VALUES (?, ?, ?, ?)";
        try (Connection connection = createConnection();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
