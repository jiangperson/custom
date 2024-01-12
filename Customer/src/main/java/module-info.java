module com.example.customer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.example.customer to javafx.fxml;
    exports com.example.customer;
}