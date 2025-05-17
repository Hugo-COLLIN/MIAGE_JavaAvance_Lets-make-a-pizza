module idmc.letsmakeapizzajfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.paho.client.mqttv3;
    requires idmc.common;

    opens com.pizza to javafx.fxml;
    exports com.pizza;
    exports com.pizza.controllers;
    opens com.pizza.controllers to javafx.fxml;
}
