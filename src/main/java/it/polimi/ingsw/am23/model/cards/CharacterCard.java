package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

import java.util.Objects;

public abstract class CharacterCard extends Card {
    private final CharacterType characterType;
    private final int minPlayers;

    protected CharacterCard(String id, Era era, int points, CharacterType characterType, int minPlayers) {
        super(id, era, points);
        this.characterType = Objects.requireNonNull(characterType);
        this.minPlayers = minPlayers;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public boolean canBeTaken() {
        return true;
    }

    @Override
    public void onTaken(Game game, Player player) { //la logica di togliere il cibo al player la lascerei al game e non alla carta(player.canAfford -> player.spendFood -> building.onTaken)
        Objects.requireNonNull(game);
        Objects.requireNonNull(player);

        player.getTribe().addCharacter(this);
        onAddedToTribe(game, player);
    }

    protected void onAddedToTribe(Game game, Player player) {
    } //di default vuoto

    @Override
    public CardState toState(){
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                null,
                null,
                null,
                null
        );
    }
}
