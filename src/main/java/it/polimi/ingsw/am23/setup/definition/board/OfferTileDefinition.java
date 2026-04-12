package it.polimi.ingsw.am23.setup.definition.board;

public final class OfferTileDefinition {
    private char id;
    private int minPlayers;
    private int topDrawCount;
    private int bottomDrawCount;
    private int foodReward;

    public OfferTileDefinition() {
    }

    public char getId() {
        return id;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getTopDrawCount() {
        return topDrawCount;
    }

    public int getBottomDrawCount() {
        return bottomDrawCount;
    }

    public int getFoodReward() {
        return foodReward;
    }
}

//TODO: check se funziona nel modo corretto con OfferTileAction