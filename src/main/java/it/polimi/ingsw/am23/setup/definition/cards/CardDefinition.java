package it.polimi.ingsw.am23.setup.definition.cards;

import it.polimi.ingsw.am23.model.enums.Era;

public abstract class CardDefinition {
    private String id;
    private Era era;
    private int points;

    public String getId() { return id; }
    public Era getEra() { return era; }
    public int getPoints() { return points; }
}
