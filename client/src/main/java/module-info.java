module idmc.letsmakeapizzajfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.pizza to javafx.fxml;
    exports com.pizza;
}
