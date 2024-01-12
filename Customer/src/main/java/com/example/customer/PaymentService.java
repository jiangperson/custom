package com.example.customer;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PaymentService {
    private static Connection connection;

    public PaymentService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/enterprise", "root", "000000");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Payment> searchPayments(String payment_id, String order_id , LocalDate payment_date, String payment_method,String amount_paid) {
        List<Payment> payments = new ArrayList<>();
        try {
            String query = "SELECT * FROM payments WHERE 1=1";

            if (payment_id != null && !payment_id.isEmpty()) {
                query += " AND payment_id LIKE ?";
            }
            if (order_id != null && !order_id.isEmpty()) {
                query += " AND payment_name LIKE ?";
            }
            if (payment_date != null) {
                query += " AND payment_date = ?";
            }
            if (payment_method != null && !payment_method.isEmpty()) {
                query += " AND payment_method LIKE?";
            }
            if (amount_paid != null && !amount_paid.isEmpty()) {
                query += " AND amount_paid LIKE ?";
            }
            PreparedStatement statement = connection.prepareStatement(query);
            int parameterIndex = 1;
            if (payment_id != null && !payment_id.isEmpty()) {
                statement.setString(parameterIndex, "%" + payment_id + "%");
                parameterIndex++;
            }
            if (order_id != null && !order_id.isEmpty()) {
                statement.setString(parameterIndex, "%" + order_id + "%");
                parameterIndex++;
            }
            if (payment_date != null) {
                statement.setDate(parameterIndex, java.sql.Date.valueOf(payment_date));
                parameterIndex++;
            }
            if (payment_method != null && !payment_method.isEmpty()) {
                statement.setString(parameterIndex, "%" + payment_method + "%");
                parameterIndex++;
            }
            if (amount_paid != null && !amount_paid.isEmpty()) {
                statement.setString(parameterIndex, "%" + amount_paid + "%");
                parameterIndex++;
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String payid = resultSet.getString("payment_id");
                String  orid= resultSet.getString("order_id");
                LocalDate paydate = resultSet.getDate("payment_date").toLocalDate();
                String method = resultSet.getString("payment_method");
                String paid=resultSet.getString("amount_paid");
                Payment payment = new Payment(payid, orid, paydate, method, paid);
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public Date getDate(Payment payment) {
        if (payment.getPaymentDate() == null)
            return null;
        return Date.valueOf(payment.getPaymentDate());
    }

    public void savePayment(Payment payment) {
        try {
            String query = "INSERT INTO payments (payment_id,order_id,payment_date,payment_method,amount_paid) VALUES ( ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, payment.getPaymentId());
            statement.setString(2, payment.getOrderId());
            statement.setDate(3, getDate(payment));
            statement.setString(4, payment.getPaymentMethod());
            statement.setString(5, payment.getAmountPaid());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePayment(Payment payment) {
        try {
            String query = "DELETE FROM payments WHERE payment_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, String.valueOf(payment.getPaymentId()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePayment(Payment oldPayment, Payment payment) {
        try {
            String query = "UPDATE payments SET payment_id=?,order_id=?,payment_date=?,payment_method=?,amount_paid=? WHERE payment_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, payment.getPaymentId());
            statement.setString(2, payment.getOrderId());
            statement.setDate(3, getDate(payment));
            statement.setString(4, payment.getPaymentMethod());
            statement.setString(5, payment.getAmountPaid());
            statement.setString(6, oldPayment.getPaymentId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        try {
            String query = "SELECT * FROM payments";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String payid = resultSet.getString("payment_id");
                String  orid= resultSet.getString("order_id");
                LocalDate paydate = resultSet.getDate("payment_date").toLocalDate();
                String method = resultSet.getString("payment_method");
                String paid=resultSet.getString("amount_paid");
                Payment payment = new Payment(payid, orid, paydate, method, paid);
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public int getTotalPages(int pageSize) {
        int totalPages = 0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM payments")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int totalRecords = resultSet.getInt(1);
                totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalPages;
    }

    protected Connection getConnection() {
        return this.connection;
    }

    //Excel的导入导出
    public void exportPaymentsToExcel(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Payments");
        Row headerRow = sheet.createRow(0);

        // 假设您的Payment类有name, contact, 等属性
        headerRow.createCell(0).setCellValue("paymentId");
        headerRow.createCell(1).setCellValue("orderId");
        headerRow.createCell(2).setCellValue("paymentDate");
        headerRow.createCell(3).setCellValue("paymentMethod");
        headerRow.createCell(4).setCellValue("amountPaid");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        int rowNum = 1;
        for (Payment payment : getAllPayments()) { // 这里假设getAllPayments()返回所有客户的List
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(payment.getPaymentId());
            dataRow.createCell(1).setCellValue(payment.getOrderId());
            dataRow.createCell(2).setCellValue(dateFormatter.format(payment.getPaymentDate()));
            dataRow.createCell(3).setCellValue(payment.getPaymentMethod());
            dataRow.createCell(4).setCellValue(payment.getAmountPaid());
        }

        try (FileOutputStream outputStream = new FileOutputStream(new File(filePath))) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    /**
     * 从Excel文件导入客户数据.
     *
     * @param workbook
     * @param filePath 要导入的Excel文件路径
     * @throws IOException 如果文件读取或解析过程中发生错误
     */
    public void importPaymentsFromExcel(Workbook workbook, String filePath) throws IOException {
        Sheet sheet = workbook.getSheetAt(0); // 假设数据在第一个工作表上

        Iterator<Row> iterator = sheet.iterator();
        if (iterator.hasNext()) {
            iterator.next(); // 跳过标题行
        }

        while (iterator.hasNext()) {
            Row row = iterator.next();
            String payid = row.getCell(0).getStringCellValue();
            String orderid = row.getCell(1).getStringCellValue();
            LocalDate paymentDate = toLocalDate((Date) row.getCell(2).getDateCellValue());
            String paymentMethod = row.getCell(3).getStringCellValue();
            String amountPaid = row.getCell(4).getStringCellValue();

            Cell registrationDateCell = row.getCell(5);
            LocalDate registrationDate;
            if (registrationDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(registrationDateCell)) {
                Date excelDate = (Date) registrationDateCell.getDateCellValue();
                registrationDate = toLocalDate(excelDate);
            } else if (registrationDateCell.getCellType() == CellType.STRING) {
                String rawDateString = registrationDateCell.getStringCellValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"); // 根据实际日期格式调整
                try {
                    registrationDate = LocalDate.parse(rawDateString, formatter);
                } catch (DateTimeParseException e) {
                    // 如果解析失败，可以记录错误并跳过当前行，或者使用默认日期等
                    System.err.println("无法解析日期字符串: " + rawDateString);
                    continue;
                }
            } else {
                throw new RuntimeException("无效的日期单元格类型");
            }
            savePayment(new Payment(payid,orderid,paymentDate,paymentMethod,amountPaid));
        }
        workbook.close();
    }

    // 将Java Util Date转换为LocalDate
    private static LocalDate toLocalDate(Date excelDate) {
        return excelDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}