package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;


public class ShamanCard extends CharacterCard{

    private final int stars;

    public ShamanCard(String id, Era era, int points, int stars, int  minPlayers) {
        super(id, era, points, CharacterType.SHAMAN,  minPlayers);
        this.stars = stars;
    }

    public int getStars() {
        return stars;
    }

    @Override
    public void onAddedToTribe(Game game, Player player){
        player.getTribe().addShamanStars(getStars());
    }

    @Override
    public CardState toState(){
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                null,
                stars,
                null,
                null
        );
    }
}
