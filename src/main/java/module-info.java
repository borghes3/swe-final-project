module it.polimi.ingsw.am23 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires java.desktop;
    requires java.rmi;
    requires java.sql;
    requires com.googlecode.lanterna;

    // JSON PARSING
    requires com.google.gson;
    requires java.logging;
    requires org.jline;
    requires consoleui;
    requires jline;
    opens it.polimi.ingsw.am23.model.cards to com.google.gson;


    exports it.polimi.ingsw.am23.network;
    exports it.polimi.ingsw.am23.network.rmi.client;
    exports it.polimi.ingsw.am23.network.rmi.server;
    exports it.polimi.ingsw.am23.model;
    exports it.polimi.ingsw.am23.model.board;
    exports it.polimi.ingsw.am23.model.enums;
    exports it.polimi.ingsw.am23.model.player;
    exports it.polimi.ingsw.am23.model.deck;
    exports it.polimi.ingsw.am23.model.resolvers;
    exports it.polimi.ingsw.am23.model.cards;
    exports it.polimi.ingsw.am23.model.state;
    exports it.polimi.ingsw.am23.model.effects;
    exports it.polimi.ingsw.am23.model.cards.turnorder;
    exports it.polimi.ingsw.am23.model.setup;

    opens it.polimi.ingsw.am23.model.state to javafx.fxml;
    exports it.polimi.ingsw.am23.model.draw;
    opens it.polimi.ingsw.am23.model.draw to com.google.gson;
    exports it.polimi.ingsw.am23.view.cli;

    opens it.polimi.ingsw.am23.view.gui.controllers to javafx.fxml;
    exports it.polimi.ingsw.am23.view.gui;
    exports it.polimi.ingsw.am23.persistence;
    exports it.polimi.ingsw.am23.model.payloads;
}