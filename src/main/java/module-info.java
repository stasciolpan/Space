module org.example.space {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.space to javafx.fxml;
    exports org.example.space;
}