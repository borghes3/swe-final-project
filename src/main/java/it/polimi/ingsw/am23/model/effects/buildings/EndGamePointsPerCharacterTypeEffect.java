package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * End-of-game building effect that grants a configurable amount of
 * prestige points per character of a target type owned by the player.
 */
public class EndGamePointsPerCharacterTypeEffect implements BuildingEffect {
    private final CharacterType characterType;
    private final int pointsPerCharacter;

    /**
     * Builds a new effect.
     *
     * @param characterType      character archetype to count
     * @param pointsPerCharacter prestige points awarded per character
     */
    public EndGamePointsPerCharacterTypeEffect(CharacterType characterType, int pointsPerCharacter) {
        this.characterType = characterType;
        this.pointsPerCharacter = pointsPerCharacter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEndGamePoints(Game game, Player player) {
        return player.getTribe().count(characterType) * pointsPerCharacter;
    }
}
