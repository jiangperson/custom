package com.example.customer;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PaymentsController {
    @FXML
    private TextField tfPaymentId, tfOrderId, tfAmountPaid, tfPaymentMethod;
    @FXML
    private DatePicker dpPaymentDate;
    @FXML
    private Button query, savePay, cancelPaymentButton, savePaymentButton;
    @FXML
    private TableView<Payment> PaytableView;
    @FXML
    private TableColumn<Payment, Integer> tcPaymentId;
    @FXML
    private TableColumn<Payment, String> tcOrderId, tcPaymentMethod, tcAmountPaid, phoneColumn, companyColumn, industryColumn, statusColumn;
    @FXML
    private TableColumn<Payment, LocalDate> tcPaymentDate;
    @FXML
    private Text Count;
    @FXML
    private Pagination page;
    private final int PageSize = 10;
    private ObservableList<Payment> payments = FXCollections.observableArrayList();
    private boolean isEditing;
    private Payment originalPayment;
    private final PaymentService PaymentService = new PaymentService();

    @FXML
    public void initialize() {
        initColumns();
        loadAllPayments();

        cancelPaymentButton.setVisible(false);
        savePaymentButton.setVisible(false);

        PaytableView.setRowFactory(tv -> {
            TableRow<Payment> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("删除");
            MenuItem editItem = new MenuItem("编辑");
            MenuItem noSelectionItem = new MenuItem("请先选中数据进行操作");

            deleteItem.setOnAction(e -> onDelete(row.getItem()));
            editItem.setOnAction(e -> onEdit(row.getItem()));
            cancelPaymentButton.setOnAction(e -> cancelEdit());
            savePaymentButton.setOnAction(e -> saveEdit());
            row.itemProperty().addListener((obs, oldValue, newValue) -> {
                deleteItem.setVisible(newValue != null);
                editItem.setVisible(newValue != null);
                noSelectionItem.setVisible(newValue == null);
                contextMenu.getItems().remove(noSelectionItem);
                if (newValue == null) contextMenu.getItems().add(noSelectionItem);
            });

            contextMenu.getItems().addAll(deleteItem, editItem);

            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty()))
                    .then(contextMenu)
                    .otherwise((ContextMenu) null));
            return row;
        });
        page.setPageCount(PaymentService.getTotalPages(PageSize));
        page.currentPageIndexProperty().addListener((obs, oldValue, newValue) -> onPageChange(newValue.intValue()));
        Count.setText(String.valueOf(payments.size()));
    }


    private void onPageChange(int newValue) {
        int currentPage = newValue;
        ObservableList<Payment> pageData = Page(currentPage, PageSize);
        PaytableView.setItems(pageData);
    }

    private ObservableList<Payment> Page(int pageIndex, int pageSize) {
        int i = 0;
        ObservableList<Payment> list = FXCollections.observableArrayList();
        for (Payment payment : payments) {
            if (i >= pageIndex * pageSize && i < pageSize + pageIndex * pageSize) {
                list.add(payment);
            }
            i++;
        }
        return list;
    }

    private int totalPage() {
        return (payments.size() + PageSize - 1) / PageSize;
    }

    private void initColumns() {
        tcPaymentId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        tcOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        tcPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        tcAmountPaid.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        tcPaymentDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
    }
    

    @FXML
    public void search() {
        List<Payment> searchedPayments = PaymentService.searchPayments(
                tfPaymentId.getText(),
                tfOrderId.getText(),
                dpPaymentDate.getValue(),
                tfPaymentMethod.getText(),
                tfAmountPaid.getText()
        );
        payments.clear();
        payments.addAll(searchedPayments);
        page.setPageCount(totalPage());
        int currentPage = 0;
        onPageChange(currentPage);
        Count.setText(String.valueOf(payments.size()));
    }

    @FXML
    public void save(ActionEvent event) {
        if (isFormValid()) {
            Payment newPayment = buildPaymentFromForm();
            if (isEditing) {
                PaymentService.updatePayment(originalPayment, newPayment);
                PaytableView.getItems().set(PaytableView.getSelectionModel().getSelectedIndex(), newPayment);
            } else {
                PaymentService.savePayment(newPayment);
                payments.add(newPayment);
            }
            clearForm();
            loadAllPayments();
            toggleEditMode(false);
        } else {
        }
    }

    @FXML
    public void onCancelEdit() {
        populateFormWithPayment(originalPayment);
        toggleEditMode(false);
    }

    private boolean isFormValid() {
        if (tfPaymentId.getText().isEmpty() || tfOrderId.getText().isEmpty() || tfAmountPaid.getText().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            if (tfPaymentId.getText().isEmpty())
                a.setHeaderText("id不能为空");
            else if (tfOrderId.getText().isEmpty())
                a.setHeaderText("订单号不能为空");
            a.show();
            return false;
        }
        return true;
    }

    private void onEdit(Payment payment) {
        originalPayment = payment;
        populateFormWithPayment(payment);
        toggleEditMode(true);
    }

    private void onDelete(Payment payment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("是否确定要删除该客户？");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            PaymentService.deletePayment(payment);
            payments.remove(payment);
            loadAllPayments();
            PaytableView.refresh();
        }
    }

    private void toggleEditMode(boolean isEditing) {
        this.isEditing = isEditing;
        cancelPaymentButton.setVisible(isEditing);
        savePaymentButton.setVisible(isEditing);
        savePay.setVisible(!isEditing);
    }

    private void clearForm() {
        tfPaymentId.clear();
        tfOrderId.clear();
        tfAmountPaid.clear();
        tfPaymentMethod.clear();
        dpPaymentDate.setValue(null);
    }

    private void populateFormWithPayment(Payment payment) {
        tfPaymentId.setText(payment.getPaymentId());
        tfOrderId.setText(payment.getOrderId());
        tfAmountPaid.setText(payment.getPaymentMethod());
        tfPaymentMethod.setText(payment.getPaymentMethod());
        dpPaymentDate.setValue(payment.getPaymentDate());
    }

    private Payment buildPaymentFromForm() {
        return new Payment(
                tfPaymentId.getText(),
                tfOrderId.getText(),
                dpPaymentDate.getValue(),
                tfPaymentMethod.getText(),
                tfAmountPaid.getText()
        );
    }

    private void loadAllPayments() {
        payments.clear();
        payments.addAll(PaymentService.getAllPayments());
        page.setPageCount(PaymentService.getTotalPages(PageSize));
        int currentPage = 0;
        onPageChange(currentPage);
    }

    @FXML
    public void cancelEdit() {
        toggleEditMode(false);
        clearForm();
    }

    public void saveEdit() {
        if (isFormValid()) {
            Payment updatedPayment = buildPaymentFromForm();
            PaymentService.updatePayment(originalPayment, updatedPayment);
            payments.set(payments.indexOf(originalPayment), updatedPayment);
            clearForm();
            toggleEditMode(false);
            PaytableView.refresh();

            // 编辑成功的弹窗
            Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
            alertSuccess.setTitle("编辑成功");
            alertSuccess.setHeaderText(null);
            alertSuccess.setContentText("支付记录已成功更新！");
            alertSuccess.showAndWait();

            loadAllPayments();
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
        File selectedFile = fileChooser.showOpenDialog(PaytableView.getScene().getWindow());

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

                PaymentService.importPaymentsFromExcel(workbook, selectedFile.getAbsolutePath());

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
        File selectedFile = fileChooser.showSaveDialog(PaytableView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                PaymentService.exportPaymentsToExcel(selectedFile.getAbsolutePath());

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