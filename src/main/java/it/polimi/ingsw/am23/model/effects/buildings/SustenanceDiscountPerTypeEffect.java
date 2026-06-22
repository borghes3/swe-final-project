package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Sustenance building effect that reduces the cost paid during the
 * Sustenance event by a configurable amount per character of the supplied
 * type owned by the player.
 */
public class SustenanceDiscountPerTypeEffect implements BuildingEffect {

    private final CharacterType characterType;
    private final int discount;

    /**
     * Builds a new effect.
     *
     * @param characterType character archetype to count
     * @param discount      cost reduction per character of the supplied type
     */
    public SustenanceDiscountPerTypeEffect(CharacterType characterType, int discount) {
        this.characterType = characterType;
        this.discount = discount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int modifySustenanceCost(Game game, Player player, int currentCost) {
        int count = player.getTribe().count(characterType);
        return currentCost - (count * discount);
    }
}
