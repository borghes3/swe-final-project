package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.board.Board;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.player.Totem;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TestUtils {

    private TestUtils() {
    }

    public static Player player(String id, int food, int prestigePoints) {
        return new Player(id, id + "-nick", food, prestigePoints, new Totem(id, "color-" + id));
    }

    public static ArtistCard artist(String id, Era era) {
        return new ArtistCard(id, era, 0, 2);
    }

    public static BuildingCard building(String id, Era era, int points, int foodCost, BuildingEffect effect) {
        return new BuildingCard(id, era, points, foodCost, effect);
    }

    public static Game game(List<Player> players,
                            List<OfferTile> offerTiles,
                            List<TurnOrderSlot> turnOrderSlots,
                            List<Card> topRow,
                            List<Card> bottomRow,
                            List<BuildingCard> topBuildings,
                            Era currentEra,
                            int currentRound) {
        TurnOrderTile turnOrderTile = new TurnOrderTile(new ArrayList<>(turnOrderSlots));
        Board board = new Board(new ArrayList<>(offerTiles), turnOrderTile);
        CardMarket cardMarket = new CardMarket(new ArrayList<>(topRow), new ArrayList<>(bottomRow), new ArrayList<>(topBuildings));

        TribeDeck tribeDeck = new TribeDeck(List.of());
        BuildingDeck buildingDeck = new BuildingDeck(Map.of());

        return new Game(players, board, tribeDeck, buildingDeck, new EventResolver(), cardMarket, currentEra, currentRound);
    }
}
