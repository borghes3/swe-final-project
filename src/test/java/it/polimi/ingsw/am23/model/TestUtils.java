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
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.payloads.*;
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

    // Test helper: forces the Game phase via reflection so tests can wire up
    // an already-running game without simulating the full setup/placing flow.
    public static void setPhase(Game game, GamePhase phase) {
        try {
            java.lang.reflect.Field f = Game.class.getDeclaredField("phase");
            f.setAccessible(true);
            f.set(game, phase);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // Observer di test che conta le notifiche ricevute dal Game.
    // Usato in qualunque test che voglia verificare il flusso di notifiche del model.
    public static class RecordingObserver implements ModelObserver {
        public int gameStartedCount;
        public int stateChangedCount;
        public int totemPlacedCount;
        public int endPlacingCount;
        public int cardsTakenCount;
        public int extraDrawCount;
        public int extraCardTakenCount;
        public int eventResolvedCount;
        public int marketRefreshedCount;
        public int eraProgressionCount;
        public int scoresCount;
        public int gameOverCount;

        @Override
        public void onGameStarted(GameStartedPayload payload) {
            gameStartedCount++;
        }

        @Override
        public void onTotemPlaced(TotemPlacedPayload payload) {
            totemPlacedCount++;
        }

        @Override
        public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) {
            endPlacingCount++;
        }

        @Override
        public void onCardsTaken(CardsTakenPayload payload) {
            cardsTakenCount++;
        }

        @Override
        public void onExtraDrawRequest(ExtraDrawRequestPayload payload) {
            extraDrawCount++;
        }

        @Override
        public void onExtraCardTaken(ExtraCardTakenPayload payload) {
            extraCardTakenCount++;
        }

        @Override
        public void onEventResolved(EventResolvedPayload payload) {
            eventResolvedCount++;
        }

        @Override
        public void onMarketRefreshed(MarketRefresherPayload payload) {
            marketRefreshedCount++;
        }

        @Override
        public void onEraProgression(EraProgressionPayload payload) {
            eraProgressionCount++;
        }

        @Override
        public void onGameOver() {
            gameOverCount++;
        }

        @Override
        public void onScoreboardAvailable(ScoreBoardPayload payload) {
            scoresCount++;
        }
    }
}
