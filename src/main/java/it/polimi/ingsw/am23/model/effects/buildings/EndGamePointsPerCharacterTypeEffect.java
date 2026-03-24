package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

public class EndGamePointsPerCharacterTypeEffect implements BuildingEffect{
    private final CharacterType characterType;
    private final int pointsPerCharacter;

    public EndGamePointsPerCharacterTypeEffect(CharacterType characterType, int pointsPerCharacter){
        this.characterType = characterType;
        this.pointsPerCharacter = pointsPerCharacter;
    }

    @Override
    public int getEndGamePoints(Game game, Player player){
        return player.getTribe().count(characterType)*pointsPerCharacter;
    }
}
