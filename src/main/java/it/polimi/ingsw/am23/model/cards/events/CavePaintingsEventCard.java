package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.effects.CavePaintingsEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

/**
 * Event card implementing the Cave Paintings scoring rule.
 * Players who own at least {@code minArtists} Artist characters are awarded
 * prestige points proportional to the number of Artists owned; players that
 * miss the threshold lose a flat amount of prestige points.
 */
public class CavePaintingsEventCard extends EventCard {

    private final int minArtists;
    private final int pointsToRemove;
    private final int pointsFactor;

    /**
     * Builds a new Cave Paintings event card.
     *
     * @param id             unique identifier of the card
     * @param era            era the card belongs to
     * @param points         printed victory points
     * @param isFinal        whether the card is resolved only at end of match
     * @param minArtists     minimum number of Artists required to score positively
     * @param pointsToRemove flat prestige loss for players below the threshold
     * @param pointsFactor   prestige points awarded per Artist when above the threshold
     */
    public CavePaintingsEventCard(String id, Era era, int points, boolean isFinal, int minArtists, int pointsToRemove, int pointsFactor) {
        super(id, era, points, isFinal);
        this.minArtists = minArtists;
        this.pointsToRemove = pointsToRemove;
        this.pointsFactor = pointsFactor;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            if (player.getTribe().count(CharacterType.ARTIST) >= minArtists) {
                player.addPrestigePoints(player.getTribe().count(CharacterType.ARTIST) * pointsFactor);
            } else {
                player.spendPrestigePoints(pointsToRemove);
            }
            CavePaintingsEffectData data = new CavePaintingsEffectData();
            for (BuildingCard building : player.getTribe().getBuildings()) {
                building.getEffect().applyCavePaintings(game, player, data);
            }
            player.applyFoodDelta(data.getExtraFood());
        }
    }
}
