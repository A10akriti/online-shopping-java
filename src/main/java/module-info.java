module com.aakrititiwari.onlineshopping {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires servlet.api;
    requires java.sql;
    requires org.json;
    requires jakarta.json;
    opens com.aakrititiwari.onlineshopping to javafx.fxml;
    exports com.aakrititiwari.onlineshopping;
    opens com.aakrititiwari.onlineshopping.models to javafx.base;
    exports com.aakrititiwari.onlineshopping.models;
}
