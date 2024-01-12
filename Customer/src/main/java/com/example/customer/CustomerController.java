package com.example.customer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerController {
    @FXML
    private TextField idTextField, nameTextField, contactTextField, emailTextField, phoneTextField, companyTextField, industryTextField;
    @FXML
    private DatePicker registrationDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button searchButton, saveButton, cancelEditButton, saveEditButton;
    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, Integer> idColumn;
    @FXML
    private TableColumn<Customer, String> nameColumn, contactColumn, emailColumn, phoneColumn, companyColumn, industryColumn, statusColumn;
    @FXML
    private TableColumn<Customer, LocalDate> registrationColumn;
    @FXML
    private Text searchResultCount;
    @FXML
    private Pagination pagination;
    private final int PageSize = 10;
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private boolean isEditing;
    private Customer originalCustomer;
    private final CustomerService customerService = new CustomerService();
    public static int theid;

    @FXML
    public void initialize() {
        initColumns();
        loadAllCustomers();

        cancelEditButton.setVisible(false);
        saveEditButton.setVisible(false);

        customerTableView.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("删除");
            MenuItem editItem = new MenuItem("编辑");
            MenuItem orderItem = new MenuItem("显示订单信息");
            MenuItem noSelectionItem = new MenuItem("请先选中数据进行操作");

            deleteItem.setOnAction(e -> onDelete(row.getItem()));
            editItem.setOnAction(e -> onEdit(row.getItem()));
            orderItem.setOnAction(e -> onShowOrders());

            row.itemProperty().addListener((obs, oldValue, newValue) -> {
                deleteItem.setVisible(newValue != null);
                editItem.setVisible(newValue != null);
                orderItem.setVisible(newValue != null);
                noSelectionItem.setVisible(newValue == null);
                contextMenu.getItems().remove(noSelectionItem);
                if (newValue == null) contextMenu.getItems().add(noSelectionItem);
            });

            contextMenu.getItems().addAll(deleteItem, editItem, orderItem);

            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty()))
                    .then(contextMenu)
                    .otherwise((ContextMenu) null));
            return row;
        });
        pagination.setPageCount(customerService.getTotalPages(PageSize));
        pagination.currentPageIndexProperty().addListener((obs, oldValue, newValue) -> onPageChange(newValue.intValue()));
        searchResultCount.setText(String.valueOf(customers.size()));
    }

    private void onShowOrders() {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        theid= Integer.parseInt(selectedCustomer.getId());
        if (selectedCustomer != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("order.fxml"));
                Parent childWindowRoot = loader.load();
                OrderController childController = loader.getController(); // 获取子窗口控制器（如果需要）

                Stage childStage = new Stage();
                childStage.setTitle("订单信息");
                childStage.initModality(Modality.WINDOW_MODAL);
                childStage.setScene(new Scene(childWindowRoot));
                // 将选中的客户传递给OrderController，以便展示相应的订单数据
                childStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "请先选择一个客户！");
            alert.showAndWait();
        }
    }


    private void onPageChange(int newValue) {
        int currentPage = newValue;
        ObservableList<Customer> pageData = Page(currentPage, PageSize);
        customerTableView.setItems(pageData);
    }

    private ObservableList<Customer> Page(int pageIndex, int pageSize) {
        int i = 0;
        ObservableList<Customer> list = FXCollections.observableArrayList();
        for (Customer customer : customers) {
            if (i >= pageIndex * pageSize && i < pageSize + pageIndex * pageSize) {
                list.add(customer);
            }
            i++;
        }
        return list;
    }

    private int totalPage() {
        return (customers.size() + PageSize - 1) / PageSize;
    }

    private void initColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        registrationColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getRegistrationDate()));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        industryColumn.setCellValueFactory(new PropertyValueFactory<>("industry"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public String getStatus() {
        if (statusComboBox.getSelectionModel().getSelectedItem() == "")
            return null;
        return statusComboBox.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void search() {
        List<Customer> searchedCustomers = customerService.searchCustomers(
                idTextField.getText(),
                nameTextField.getText(),
                contactTextField.getText(),
                emailTextField.getText(),
                phoneTextField.getText(),
                companyTextField.getText(),
                industryTextField.getText(),
                registrationDatePicker.getValue(),
                getStatus()
        );
        customers.clear();
        customers.addAll(searchedCustomers);
        pagination.setPageCount(totalPage());
        int currentPage = 0;
        onPageChange(currentPage);
        searchResultCount.setText(String.valueOf(customers.size()));
    }

    @FXML
    public void save(ActionEvent event) {
        if (isFormValid()) {
            Customer newCustomer = buildCustomerFromForm();
            if (isEditing) {
                customerService.updateCustomer(originalCustomer, newCustomer);
                customerTableView.getItems().set(customerTableView.getSelectionModel().getSelectedIndex(), newCustomer);
            } else {
                customerService.saveCustomer(newCustomer);
                customers.add(newCustomer);
            }
            clearForm();
            loadAllCustomers();
            toggleEditMode(false);
        } else {
        }
    }

    @FXML
    public void onCancelEdit(ActionEvent event) {
        populateFormWithCustomer(originalCustomer);
        toggleEditMode(false);
    }

    private boolean isFormValid() {
        if (idTextField.getText().isEmpty() || nameTextField.getText().isEmpty() || contactTextField.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            if (idTextField.getText().isEmpty())
                a.setHeaderText("id不能为空");
            else if (nameTextField.getText().isEmpty())
                a.setHeaderText("姓名不能为空");
            else if (contactTextField.getText().isEmpty())
                a.setHeaderText("联系人不能为空");
            a.show();
        }
        return !idTextField.getText().isEmpty() && !nameTextField.getText().isEmpty() && !contactTextField.getText().isEmpty();
    }

    private void onEdit(Customer customer) {
        originalCustomer = customer;
        populateFormWithCustomer(customer);
        toggleEditMode(true);
    }

    private void onDelete(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("是否确定要删除该客户？");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            customerService.deleteCustomer(customer);
            customers.remove(customer);
            loadAllCustomers();
            customerTableView.refresh();
        }
    }

    private void toggleEditMode(boolean isEditing) {
        this.isEditing = isEditing;
        cancelEditButton.setVisible(isEditing);
        saveEditButton.setVisible(isEditing);
        saveButton.setVisible(!isEditing);
    }

    private void clearForm() {
        idTextField.clear();
        nameTextField.clear();
        contactTextField.clear();
        emailTextField.clear();
        phoneTextField.clear();
        registrationDatePicker.setValue(null);
        companyTextField.clear();
        industryTextField.clear();
        statusComboBox.getSelectionModel().clearSelection();
    }

    private void populateFormWithCustomer(Customer customer) {
        idTextField.setText(String.valueOf(customer.getId()));
        nameTextField.setText(customer.getName());
        contactTextField.setText(customer.getContact());
        emailTextField.setText(customer.getEmail());
        phoneTextField.setText(customer.getPhoneNumber().toString());
        registrationDatePicker.setValue(customer.getRegistrationDate());
        companyTextField.setText(customer.getCompanyName());
        industryTextField.setText(customer.getIndustry());
        statusComboBox.getSelectionModel().select(customer.getStatus());
    }

    private Customer buildCustomerFromForm() {
        return new Customer(
                idTextField.getText(),
                nameTextField.getText(),
                contactTextField.getText(),
                emailTextField.getText(),
                phoneTextField.getText(),
                registrationDatePicker.getValue(),
                companyTextField.getText(),
                industryTextField.getText(),
                statusComboBox.getSelectionModel().getSelectedItem()
        );
    }

    private void loadAllCustomers() {
        customers.clear();
        customers.addAll(customerService.getAllCustomers());
        pagination.setPageCount(customerService.getTotalPages(PageSize));
        int currentPage = 0;
        onPageChange(currentPage);
    }

    @FXML
    public void cancelEdit(ActionEvent actionEvent) {
        toggleEditMode(false);
        clearForm();
    }

    public void saveEdit(ActionEvent actionEvent) {
        if (isFormValid()) {
            Customer updatedCustomer = buildCustomerFromForm();
            customerService.updateCustomer(originalCustomer, updatedCustomer);
            customers.set(customers.indexOf(originalCustomer), updatedCustomer);
            clearForm();
            toggleEditMode(false);
            customerTableView.refresh();

            // 编辑成功的弹窗
            Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
            alertSuccess.setTitle("编辑成功");
            alertSuccess.setHeaderText(null);
            alertSuccess.setContentText("客户信息已成功更新！");
            alertSuccess.showAndWait();

            loadAllCustomers();
        } else {
            // 保存编辑失败的弹窗
            Alert alertFailure = new Alert(Alert.AlertType.ERROR);
            alertFailure.setTitle("保存编辑失败");
            alertFailure.setHeaderText(null);
            alertFailure.setContentText("请检查并修正表单中的错误后再尝试保存！");
            alertFailure.showAndWait();
        }
    }

    @FXML
    public void importTable(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要导入的Excel文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );
        File selectedFile = fileChooser.showOpenDialog(customerTableView.getScene().getWindow());

        if (selectedFile != null) {
            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                String fileName = selectedFile.getName();
                if (fileName.endsWith(".xls")) {
                    workbook = new HSSFWorkbook(fis);
                } else if (fileName.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(fis);
                } else {
                    Alert alertUnsupportedFormat = new Alert(Alert.AlertType.ERROR);
                    alertUnsupportedFormat.setHeaderText(null);
                    alertUnsupportedFormat.setContentText("不支持的文件格式。请选择.xls或.xlsx格式的文件！");
                    alertUnsupportedFormat.showAndWait();
                    return;
                }

                customerService.importCustomersFromExcel(workbook, selectedFile.getAbsolutePath());

                Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
                alertSuccess.setTitle("导入成功");
                alertSuccess.setHeaderText(null);
                alertSuccess.setContentText("客户信息已成功导入！");
                alertSuccess.showAndWait();

            } catch (IOException ex) {
                Alert alertFailure = new Alert(Alert.AlertType.ERROR);
                alertFailure.setTitle("导入失败");
                alertFailure.setHeaderText(null);
                alertFailure.setContentText("导入过程中发生错误：" + ex.getMessage());
                alertFailure.showAndWait();
            }
        }
    }

    @FXML
    public void exportTable(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择导出Excel文件的位置");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File selectedFile = fileChooser.showSaveDialog(customerTableView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                customerService.exportCustomersToExcel(selectedFile.getAbsolutePath());

                Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
                alertSuccess.setTitle("导出成功");
                alertSuccess.setHeaderText(null);
                alertSuccess.setContentText("客户信息已成功导出到 " + selectedFile.getName() + "！");
                alertSuccess.showAndWait();
            } catch (Exception ex) {
                Alert alertFailure = new Alert(Alert.AlertType.ERROR);
                alertFailure.setTitle("导出失败");
                alertFailure.setHeaderText(null);
                alertFailure.setContentText("导出过程中发生错误：" + ex.getMessage());
                alertFailure.showAndWait();
            }
        }
    }
}