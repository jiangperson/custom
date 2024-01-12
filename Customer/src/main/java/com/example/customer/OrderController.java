package com.example.customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

public class OrderController {
    @FXML
    private TableView<Order> tableView;
    @FXML
    private TableColumn<Order, String> idColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, LocalDate> orderDateColumn;
    @FXML
    private TableColumn<Order, String> productNameColumn;
    @FXML
    private TableColumn<Order, Integer> quantityColumn;
    @FXML
    private TableColumn<Order, Double> amountColumn;
    @FXML
    private Button importButton;
    @FXML
    private Button exportButton;
    private String customerId;
    private ObservableList<Order> observableOrders = FXCollections.observableArrayList();
    private OrderService orderService = new OrderService();
    private ObservableList<Order> orders;

    @FXML
    public void initialize() {
        orders=FXCollections.observableArrayList();
        orders.addAll(orderService.getAllOrdersByCustomerId(CustomerController.theid));
        initColumns();
        importButton.setOnAction(this::handleImport);
        exportButton.setOnAction(this::handleExport);
    }
    private void initColumns() {
        tableView.setItems(orders);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }


    // 导入和导出Excel功能实现：

    private void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(tableView.getScene().getWindow());

        if (selectedFile != null) {
            try (InputStream fis = new FileInputStream(selectedFile)) {
                Workbook workbook = WorkbookFactory.create(fis);
                Sheet sheet = workbook.getSheetAt(0); // 假设数据在第一个工作表

                for (Row row : sheet) {
                    // 解析每一行数据并创建Order对象，然后添加到数据库...
                    // 这里仅提供示例框架，具体解析逻辑需根据Excel文件内容自行编写
                    Order order = parseOrderFromExcelRow(row);
                    if (orderService.doesCustomerExist(Integer.parseInt(order.getCustomerId()))) {
                        orderService.saveOrder(order);
                    } else {
                        System.err.println("Error: Customer ID " + order.getCustomerId() + " does not exist in the database.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error importing Excel file: " + e.getMessage());
            }
        }
    }
    private boolean doesCustomerExist(int customerId) {
        // 这个方法需要在OrderService中实现，用于检查数据库中是否存在指定的客户ID。
        return false; // 这里仅作占位符，请在OrderService类中实现具体逻辑
    }

    private void handleExport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Orders to Excel");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel Files", "*.xlsx"));

        File selectedFile = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (selectedFile != null) {
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(selectedFile)) {
                Sheet sheet = workbook.createSheet("Orders");
                int rowNum = 0;
                List<Order> allOrders = orderService.getAllOrdersByCustomerId(Integer.parseInt(customerId));
                for (Order order : observableOrders) {
                    Row excelRow = sheet.createRow(rowNum++);
                    writeOrderToExcelRow(excelRow, order);
                }
                workbook.write(fos);
                workbook.close();
            } catch (IOException e) {
                System.err.println("Error exporting orders to Excel: " + e.getMessage());
            }
        }
    }
    private Order parseOrderFromExcelRow(Row row) {
        Cell idCell = row.getCell(0);
        Cell customerCell = row.getCell(1);
        Cell orderDateCell = row.getCell(2);
        Cell productNameCell = row.getCell(3);
        Cell quantityCell = row.getCell(4);
        Cell amountCell = row.getCell(5);

        if (idCell == null || customerCell == null || orderDateCell == null || productNameCell == null ||
                quantityCell == null || amountCell == null) {
            return null; // 或者抛出异常，表示行数据不完整
        }

        String id = idCell.getStringCellValue();
        String customerId = customerCell.getStringCellValue();
        LocalDate orderDate = LocalDate.parse(orderDateCell.getStringCellValue());
        String productName = productNameCell.getStringCellValue();
        int quantity = Integer.parseInt(quantityCell.getStringCellValue());
        double amount = Double.parseDouble(amountCell.getStringCellValue());

        return new Order(id, customerId, orderDate, productName, quantity, amount);
    }

    private void writeOrderToExcelRow(Row excelRow, Order order) {
        Cell cellId = excelRow.createCell(0);
        cellId.setCellValue(order.getId());

        Cell cellCustomerName = excelRow.createCell(1);
        cellCustomerName.setCellValue(order.getCustomerId());

        Cell cellOrderDate = excelRow.createCell(2);
        cellOrderDate.setCellValue(order.getOrderDate());

        Cell cellProductName = excelRow.createCell(3);
        cellProductName.setCellValue(order.getProductName());

        Cell cellQuantity = excelRow.createCell(4);
        cellQuantity.setCellValue(order.getQuantity());

        Cell cellAmount = excelRow.createCell(5);
        cellAmount.setCellValue(order.getAmount());
    }
}

class OrderService {

    private Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/enterprise", "root", "000000");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error connecting to database: " + e.getMessage(), e);
        }
    }

    public List<Order> getAllOrdersByCustomerId(int customerId) {
        List<Order> result = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT * FROM orders " +
                             "WHERE customer_id = ?")) {
            statement.setString(1, String.valueOf(customerId));
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getString("order_id"),
                        rs.getString("customer_id"),
                        rs.getDate("order_date") == null ? null : rs.getDate("order_date").toLocalDate(),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("amount")
                );
                result.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders from database: " + e.getMessage());
        }
        return result;
    }
    public void saveOrder(Order order) {
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "INSERT INTO orders(order_id, customer_id, order_date, product_name, quantity, amount) VALUES (?, ?, ?, ?, ?, ?)")) {
            // 假设customer_id已经在order对象中设置好
            statement.setString(1, order.getId());
            statement.setString(2, order.getCustomerId());
            statement.setObject(3, order.getOrderDate(), java.sql.Types.DATE);
            statement.setString(4, order.getProductName());
            statement.setInt(5, order.getQuantity());
            statement.setDouble(6, order.getAmount());

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving order to database: " + e.getMessage());
        }
    }
    public boolean doesCustomerExist(int customerId) {
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM customers WHERE customer_id = ?")) {
            statement.setInt(1, customerId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if customer exists: " + e.getMessage());
        }
        return false;
    }
}