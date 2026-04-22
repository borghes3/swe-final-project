module it.polimi.ingsw.am23 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires java.desktop;
    requires java.rmi;
    requires com.googlecode.lanterna;

//    JSON PARSING
    requires com.google.gson;
    opens it.polimi.ingsw.am23.model.cards to com.google.gson;
    exports it.polimi.ingsw.am23.model.enums;


    opens it.polimi.ingsw.am23 to javafx.fxml;
    exports it.polimi.ingsw.am23.network;
    exports it.polimi.ingsw.am23.network.rmi.client;
    exports it.polimi.ingsw.am23.network.rmi.server;
    exports it.polimi.ingsw.am23.view;
    exports it.polimi.ingsw.am23.trash;
    exports it.polimi.ingsw.am23.model;
    exports it.polimi.ingsw.am23.model.board;
//    exports it.polimi.ingsw.am23.view.tui;

    opens it.polimi.ingsw.am23.model.state to javafx.fxml;
    opens it.polimi.ingsw.am23.trash to javafx.fxml;
}