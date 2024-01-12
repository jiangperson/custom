package com.example.customer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class Sys {

    @FXML
    private Button paymentsButton; // 假设这是从FXML加载的订单按钮

    @FXML
    private Button customerButton; // 假设这是从FXML加载的客户按钮

    public void initialize() {
        // 为订单按钮添加事件监听器
        paymentsButton.setOnAction(this::showPaymentsWindow);

        // 为客户按钮添加事件监听器
        customerButton.setOnAction(this::showCustomerWindow);
    }

    private void showPaymentsWindow(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("payments.fxml"));
            Parent childWindowRoot = loader.load();
            PaymentsController childController = loader.getController(); // 获取子窗口控制器（如果需要）

            Stage childStage = new Stage();
            childStage.setTitle("订单信息");
            childStage.initModality(Modality.WINDOW_MODAL);
            childStage.initOwner(paymentsButton.getScene().getWindow());
            childStage.setScene(new Scene(childWindowRoot));
            childStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCustomerWindow(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer.fxml"));
            Parent childWindowRoot = loader.load();
            CustomerController childController = loader.getController(); // 获取子窗口控制器（如果需要）

            Stage childStage = new Stage();
            childStage.setTitle("客户信息");
            childStage.initModality(Modality.WINDOW_MODAL);
            childStage.initOwner(customerButton.getScene().getWindow());
            childStage.setScene(new Scene(childWindowRoot));
            childStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}