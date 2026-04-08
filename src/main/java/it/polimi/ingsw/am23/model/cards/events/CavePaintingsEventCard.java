package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.effects.CavePaintingsEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

public class CavePaintingsEventCard extends EventCard {

    private final int minArtists;
    private final int pointsToRemove;
    private final int pointsFactor;

    public CavePaintingsEventCard(String id, Era era, int points, boolean isFinal, int minArtists, int pointsToRemove, int pointsFactor) {
        super(id, era, points, isFinal);
        this.minArtists = minArtists;
        this.pointsToRemove = pointsToRemove;
        this.pointsFactor = pointsFactor;

    }

    public int getMinArtists() {
        return minArtists;
    }

    public int getPointsToRemove() {
        return pointsToRemove;
    }

    public int getPointsFactor() {
        return pointsFactor;
    }

    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            int basePoints;
            if (player.getTribe().getCharacters().size() >= minArtists) {
                player.addPrestigePoints(player.getTribe().count(CharacterType.ARTIST) * pointsFactor);
            } else {
                player.losePrestigePoints(pointsToRemove);
            }
            CavePaintingsEffectData data = new CavePaintingsEffectData();
            for (BuildingCard building : player.getTribe().getBuildings()) {
                building.getEffect().applyCavePaintings(game, player, data);
            }
            player.addFood(data.getExtraFood());
        }
    }
}
