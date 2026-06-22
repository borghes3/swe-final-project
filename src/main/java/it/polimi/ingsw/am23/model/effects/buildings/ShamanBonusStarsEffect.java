package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Shaman ritual building effect that contributes a configurable amount of
 * bonus stars to the owning player's total during the ritual ranking.
 */
public class ShamanBonusStarsEffect implements BuildingEffect {

    private final int bonusStars;

    /**
     * Builds a new effect.
     *
     * @param bonusStars stars contributed to the ritual ranking
     */
    public ShamanBonusStarsEffect(int bonusStars) {
        this.bonusStars = bonusStars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data) {
        data.setBonusStars(bonusStars);
    }
}
