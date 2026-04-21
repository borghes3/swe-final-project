module it.polimi.ingsw.am23 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires java.rmi;
    requires com.googlecode.lanterna;


    opens it.polimi.ingsw.am23 to javafx.fxml;
    exports it.polimi.ingsw.am23.network;
    exports it.polimi.ingsw.am23.network.rmi.client;
    exports it.polimi.ingsw.am23.network.rmi.server;
//    exports it.polimi.ingsw.am23.view.tui;
    exports it.polimi.ingsw.am23.view;
    exports it.polimi.ingsw.am23.model.state;
    exports it.polimi.ingsw.am23.model.setup;
    exports it.polimi.ingsw.am23.model.cards;
    opens it.polimi.ingsw.am23.model.state to javafx.fxml;
    opens it.polimi.ingsw.am23.setup.definition.cards to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.am23.setup.definition.board to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.am23.trash;
    opens it.polimi.ingsw.am23.trash to javafx.fxml;
}