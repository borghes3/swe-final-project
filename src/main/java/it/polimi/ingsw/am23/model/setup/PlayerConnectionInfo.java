package it.polimi.ingsw.am23.model.setup;

public class PlayerConnectionInfo {
    private String id;
    private String nickname;

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
