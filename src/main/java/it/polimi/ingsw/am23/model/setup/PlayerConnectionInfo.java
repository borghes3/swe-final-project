package it.polimi.ingsw.am23.model.setup;

import java.io.Serializable;

public class PlayerConnectionInfo implements Serializable {
    private String id;
    private String nickname;
    // TODO: Aggiungere colore scelto dal controller
    
    public PlayerConnectionInfo(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }
}
