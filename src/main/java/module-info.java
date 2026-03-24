module it.polimi.ingsw.am23 {
    requires javafx.controls;
    requires javafx.fxml;
    requires it.polimi.ingsw.am23;


    opens it.polimi.ingsw.am23 to javafx.fxml;
    exports it.polimi.ingsw.am23;
    exports it.polimi.ingsw.am23.model.state;
    opens it.polimi.ingsw.am23.model.state to javafx.fxml;
}