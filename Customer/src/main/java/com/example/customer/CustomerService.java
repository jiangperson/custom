package com.example.customer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomerService {
    private static Connection connection;

    public CustomerService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/enterprise", "root", "000000");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Customer> searchCustomers(String customerId, String customerName, String contactPerson, String email, String phoneNumber, String companyName, String industry, LocalDate startDate, String status) {
        List<Customer> customers = new ArrayList<>();
        try {
            String query = "SELECT * FROM customers WHERE 1=1";

            if (customerId != null && !customerId.isEmpty()) {
                query += " AND customer_id LIKE ?";
            }
            if (customerName != null && !customerName.isEmpty()) {
                query += " AND customer_name LIKE ?";
            }
            if (contactPerson != null && !contactPerson.isEmpty()) {
                query += " AND contact_person LIKE ?";
            }
            if (email != null && !email.isEmpty()) {
                query += " AND email LIKE ?";
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                query += " AND phone_number LIKE ?";
            }
            if (companyName != null && !companyName.isEmpty()) {
                query += " AND company_name LIKE ?";
            }
            if (industry != null && !industry.isEmpty()) {
                query += " AND industry LIKE ?";
            }
            if (startDate != null) {
                query += " AND registration_date = ?";
            }
            if (status != null && !status.isEmpty()) {
                query += " AND status LIKE?";
            }
            PreparedStatement statement = connection.prepareStatement(query);
            int parameterIndex = 1;
            if (customerId != null && !customerId.isEmpty()) {
                statement.setString(parameterIndex, "%" + customerId + "%");
                parameterIndex++;
            }
            if (customerName != null && !customerName.isEmpty()) {
                statement.setString(parameterIndex, "%" + customerName + "%");
                parameterIndex++;
            }
            if (contactPerson != null && !contactPerson.isEmpty()) {
                statement.setString(parameterIndex, "%" + contactPerson + "%");
                parameterIndex++;
            }
            if (email != null && !email.isEmpty()) {
                statement.setString(parameterIndex, "%" + email + "%");
                parameterIndex++;
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                statement.setString(parameterIndex, "%" + phoneNumber + "%");
                parameterIndex++;
            }
            if (companyName != null && !companyName.isEmpty()) {
                statement.setString(parameterIndex, "%" + companyName + "%");
                parameterIndex++;
            }
            if (industry != null && !industry.isEmpty()) {
                statement.setString(parameterIndex, "%" + industry + "%");
                parameterIndex++;
            }
            if (startDate != null) {
                statement.setDate(parameterIndex, java.sql.Date.valueOf(startDate));
                parameterIndex++;
            }
            if (status != null && !status.isEmpty()) {
                statement.setString(parameterIndex, "%" + status + "%");
                parameterIndex++;
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("customer_id");
                String name = resultSet.getString("customer_name");
                String contact = resultSet.getString("contact_person");
                String Email = resultSet.getString("email");
                String phone = resultSet.getString("phone_number");
                LocalDate registrationDate = resultSet.getDate("registration_date").toLocalDate();
                String company = resultSet.getString("company_name");
                String Industry = resultSet.getString("industry");
                String Status = resultSet.getString("status");

                Customer customer = new Customer(id, name, contact, Email, phone, registrationDate, company, Industry, Status);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public Date getDate(Customer customer) {
        if (customer.getRegistrationDate() == null)
            return null;
        return Date.valueOf(customer.getRegistrationDate());
    }

    public void saveCustomer(Customer customer) {
        try {
            String query = "INSERT INTO customers (customer_id, customer_name, contact_person, email, phone_number, registration_date, company_name, industry, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, customer.getId());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getContact());
            statement.setString(4, customer.getEmail());
            statement.setString(5, customer.getPhoneNumber());
            statement.setDate(6, getDate(customer));
            statement.setString(7, customer.getCompanyName());
            statement.setString(8, customer.getIndustry());
            statement.setString(9, customer.getStatus());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCustomer(Customer customer) {
        try {
            String query = "DELETE FROM customers WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, String.valueOf(customer.getId()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(Customer oldCustomer, Customer newCustomer) {
        try {
            String query = "UPDATE customers SET customer_id = ?, customer_name = ?, contact_person = ?, email = ?, phone_number = ?, registration_date = ?, company_name = ?, industry = ?, status = ? WHERE customer_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newCustomer.getId());
            statement.setString(2, newCustomer.getName());
            statement.setString(3, newCustomer.getContact());
            statement.setString(4, newCustomer.getEmail());
            statement.setString(5, newCustomer.getPhoneNumber());
            statement.setDate(6, Date.valueOf(newCustomer.getRegistrationDate()));
            statement.setString(7, newCustomer.getCompanyName());
            statement.setString(8, newCustomer.getIndustry());
            statement.setString(9, newCustomer.getStatus());
            statement.setString(10, oldCustomer.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            String query = "SELECT * FROM customers";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("customer_id");
                String name = resultSet.getString("customer_name");
                String contact = resultSet.getString("contact_person");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone_number");
                LocalDate registrationDate = resultSet.getDate("registration_date") == null ? null : resultSet.getDate("registration_date").toLocalDate();
                String company = resultSet.getString("company_name");
                String industry = resultSet.getString("industry");
                String status = resultSet.getString("status");
                Customer customer = new Customer(id, name, contact, email, phone, registrationDate, company, industry, status);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public int getTotalPages(int pageSize) {
        int totalPages = 0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM customers")) {
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

    //Excel的导入导出
    public void exportCustomersToExcel(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");
        Row headerRow = sheet.createRow(0);

        // 假设您的Customer类有name, contact, email等属性
        headerRow.createCell(0).setCellValue("Id");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("Contact");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Phone");
        headerRow.createCell(5).setCellValue("Registration Date");
        headerRow.createCell(6).setCellValue("Company Name");
        headerRow.createCell(7).setCellValue("Industry");
        headerRow.createCell(8).setCellValue("Status");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"); // 根据实际日期格式调整
        int rowNum = 1;
        for (Customer customer : getAllCustomers()) { // 这里假设getAllCustomers()返回所有客户的List
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(customer.getId());
            dataRow.createCell(1).setCellValue(customer.getName());
            dataRow.createCell(2).setCellValue(customer.getContact());
            dataRow.createCell(3).setCellValue(customer.getEmail());
            dataRow.createCell(4).setCellValue(customer.getPhoneNumber());
            dataRow.createCell(5).setCellValue(customer.getRegistrationDate());
            dataRow.createCell(6).setCellValue(customer.getCompanyName());
            dataRow.createCell(7).setCellValue(customer.getIndustry());
            dataRow.createCell(8).setCellValue(customer.getStatus());
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
    public void importCustomersFromExcel(Workbook workbook, String filePath) throws IOException {
        Sheet sheet = workbook.getSheetAt(0); // 假设数据在第一个工作表上

        Iterator<Row> iterator = sheet.iterator();
        if (iterator.hasNext()) {
            iterator.next(); // 跳过标题行
        }

        while (iterator.hasNext()) {
            Row row = iterator.next();
            String id = row.getCell(0).getStringCellValue();
            String name = row.getCell(1).getStringCellValue();
            String contact = row.getCell(2).getStringCellValue();
            String email = row.getCell(3).getStringCellValue().trim();
            String phone = row.getCell(4).getStringCellValue();

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

            String companyName = row.getCell(6).getStringCellValue();
            String industry = row.getCell(7).getStringCellValue();
            String status = row.getCell(8).getStringCellValue();

            saveCustomer(new Customer(id, name, contact, email, phone, registrationDate, companyName, industry, status));
        }
        workbook.close();
    }

    // 将Java Util Date转换为LocalDate
    private static LocalDate toLocalDate(Date excelDate) {
        return excelDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}