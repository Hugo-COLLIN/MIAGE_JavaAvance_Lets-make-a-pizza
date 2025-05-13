module idmc.letsmakeapizzajfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.paho.client.mqttv3;

    opens com.pizza to javafx.fxml;
    exports com.pizza;
    exports com.pizza.controllers;
    opens com.pizza.controllers to javafx.fxml;
    exports com.pizza.model;
    opens com.pizza.model to javafx.fxml;
}
