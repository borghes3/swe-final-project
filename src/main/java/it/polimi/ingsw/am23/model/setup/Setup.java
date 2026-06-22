package it.polimi.ingsw.am23.model.setup;

import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.exceptions.UnmatchedGameCriteriaException;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.board.Board;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.TotemColors;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.player.Totem;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Builder that turns the raw, JSON-loaded game material into a fully
 * configured {@link Game} ready to start a match.
 * Encapsulates the entire setup sequence: player count filtering, deck
 * shuffling, initial draw, totem placement and starting food distribution.
 */
public class Setup {
    // Constructor inputs (not yet processed)
    private final int numberOfPlayers;
    private final List<PlayerConnectionInfo> playersInfo;
    private final List<EventCard> eventCards;
    private final List<CharacterCard> characterCards;
    private final List<OfferTile> offerTiles;
    private final List<TurnOrderTile> turnOrderTiles;
    private final List<BuildingCard> buildingCards;

    /**
     * Builds a new setup descriptor from the JSON-loaded game material.
     *
     * @param connectedPlayersInfo connected players (the size determines the player count)
     * @param buildingCards        all available building cards
     * @param eventCards           all available event cards
     * @param characterCards       all available character cards
     * @param offerTiles           all available offer tiles
     * @param turnOrderTiles       all available turn order tile variants
     */
    public Setup(List<PlayerConnectionInfo> connectedPlayersInfo, List<BuildingCard> buildingCards, List<EventCard> eventCards, List<CharacterCard> characterCards, List<OfferTile> offerTiles, List<TurnOrderTile> turnOrderTiles) {
        this.numberOfPlayers = connectedPlayersInfo.size();
        this.playersInfo = connectedPlayersInfo;
        this.eventCards = eventCards;
        this.characterCards = characterCards;
        this.offerTiles = offerTiles;
        this.buildingCards = buildingCards;
        this.turnOrderTiles = turnOrderTiles;
    }

    /**
     * Runs the full setup sequence and returns the resulting game.
     *
     * @return the freshly built {@link Game}
     * @throws UnmatchedGameCriteriaException if no offer tiles or turn order tile matches the player count
     * @throws PlayerNotFoundException        if the initial food distribution fails to locate a player
     */
    public Game make() {

        // Build every dependency needed to instantiate the game components
        List<OfferTile> filteredOfferTiles = createOfferTrack(offerTiles);
        TurnOrderTile selectedTurnOrderTile = selectTurnOrderTile(turnOrderTiles);
        TribeDeck sortedTribeDeck = buildTribeDeck(eventCards, characterCards);
        DrawResult drawResult = drawCards(sortedTribeDeck);                                                     // fields: upperRow, lowerRow, tribeDeck (reduced to the remaining cards)
        BuildingDrawResult buildingsResult = drawBuildings(buildingCards);                                      // fields: era1Buildings, buildingDeck (era 2 + era 3)
        List<Player> players = createPlayersAndTotems(playersInfo);
        randomlyPlaceTotems(selectedTurnOrderTile, players);
        dealFood(selectedTurnOrderTile, players);

        CardMarket cardMarket = new CardMarket(drawResult.upperRow(), drawResult.lowerRow(), buildingsResult.era1Buildings());
        Board board = new Board(filteredOfferTiles, selectedTurnOrderTile);

        return new Game(players, board, drawResult.tribeDeck(), buildingsResult.buildingDeck(), new EventResolver(), cardMarket, Era.ERA_1, 1);
    }

    /**
     * Selects the offer tiles available for the current player count and
     * orders them by their letter id.
     *
     * @param offerTiles all available offer tiles
     * @return the ordered list of offer tiles in play
     * @throws UnmatchedGameCriteriaException if no offer tile matches the player count
     */
    private List<OfferTile> createOfferTrack(List<OfferTile> offerTiles) {
        // Filter by player count
        List<OfferTile> filteredTiles = new ArrayList<>(offerTiles.stream().filter(t -> t.getMinPlayers() <= numberOfPlayers).toList());
        // Sort by letter id
        filteredTiles.sort(Comparator.comparing(OfferTile::getId));
        if (filteredTiles.isEmpty()) {
            throw new UnmatchedGameCriteriaException("No Offer Tiles matched the game criteria.");
        }
        return filteredTiles;
    }

    /**
     * Selects the turn order tile variant whose slot count matches the
     * player count.
     *
     * @param turnOrderTiles available variants
     * @return the matching variant
     * @throws UnmatchedGameCriteriaException if no variant matches
     */
    private TurnOrderTile selectTurnOrderTile(List<TurnOrderTile> turnOrderTiles) {
        return turnOrderTiles.stream()
                .filter(t -> t.getSlotsCount() == numberOfPlayers)
                .findFirst()
                .orElseThrow(() -> new UnmatchedGameCriteriaException("No Turn Order Tile matched the game criteria"));
    }

    /**
     * Builds the tribe deck applying the canonical era ordering. Filters
     * the character cards by player count, splits everything into three
     * era piles plus a final events pile, shuffles each pile and stacks
     * them as Era I &gt; Era II &gt; Era III &gt; final events.
     *
     * @param events     all available event cards
     * @param characters all available character cards
     * @return the resulting tribe deck
     */
    private TribeDeck buildTribeDeck(List<EventCard> events, List<CharacterCard> characters) {
        // Character cards are filtered by player count; events have no min-players field
        List<CharacterCard> filteredCharacters = characters.stream().filter(c -> c.getMinPlayers() <= numberOfPlayers).toList();
        // Split by era
        List<Card> era1 = new ArrayList<>();
        era1.addAll(filteredCharacters.stream().filter(c -> c.getEra() == Era.ERA_1).toList());
        era1.addAll(events.stream().filter(e -> e.getEra() == Era.ERA_1).toList());
        Collections.shuffle(era1);

        List<Card> era2 = new ArrayList<>();
        era2.addAll(filteredCharacters.stream().filter(c -> c.getEra() == Era.ERA_2).toList());
        era2.addAll(events.stream().filter(e -> e.getEra() == Era.ERA_2).toList());
        Collections.shuffle(era2);

        List<Card> era3 = new ArrayList<>();
        era3.addAll(filteredCharacters.stream().filter(c -> c.getEra() == Era.ERA_3).toList());
        era3.addAll(events.stream().filter(e -> e.getEra() == Era.ERA_3 && !e.isFinal()).toList());
        Collections.shuffle(era3);

        List<EventCard> finalEvents = new ArrayList<>(events.stream().filter(EventCard::isFinal).toList());
        Collections.shuffle(finalEvents);

        // Build the final stack
        List<Card> cardsByEra = Stream.of(era1, era2, era3, finalEvents).flatMap(List::stream).collect(Collectors.toList());
        return new TribeDeck(cardsByEra);
    }

    /**
     * Performs the initial card draw populating both market rows. Events
     * drawn while filling the bottom row are diverted to the top row, as
     * per the game rules.
     *
     * @param tribeDeck the deck to draw from
     * @return the resulting {@link DrawResult}
     */
    private DrawResult drawCards(TribeDeck tribeDeck) {
        List<Card> upperRow = new ArrayList<>(), lowerRow = new ArrayList<>();

        // Fill the lower row; events get diverted to the upper row
        while (lowerRow.size() < numberOfPlayers + 1) {
            Card drawn = tribeDeck.draw();
            if (drawn instanceof EventCard) {
                upperRow.add(0, drawn);
            } else {
                lowerRow.add(0, drawn);
            }
        }

        // Top up the upper row to numberOfPlayers + 4
        while (upperRow.size() < numberOfPlayers + 4) {
            Card drawn = tribeDeck.draw();
            upperRow.add(0, drawn);
        }

        return new DrawResult(upperRow, lowerRow, tribeDeck);
    }

    /**
     * Selects the buildings for the current player count, splitting them
     * into the Era 1 starting row and a {@link BuildingDeck} holding the
     * remaining Era 2 and Era 3 buildings.
     *
     * @param buildings all available building cards
     * @return the resulting {@link BuildingDrawResult}
     */
    private BuildingDrawResult drawBuildings(List<BuildingCard> buildings) {
        int[][] buildingsByNumberOfPlayers = {
                {1, 2, 3},      // 2 players
                {2, 2, 4},      // 3 players
                {2, 3, 4},      // 4 players
                {2, 3, 5},      // 5 players
        };
        // Create three pools: Era 1, Era 2 and Era 3 buildings
        List<BuildingCard> era1Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_1).toList());
        Collections.shuffle(era1Buildings);
        era1Buildings = era1Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][0]);

        List<BuildingCard> era2Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_2).toList());
        Collections.shuffle(era2Buildings);
        era2Buildings = era2Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][1]);

        List<BuildingCard> era3Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_3).toList());
        Collections.shuffle(era3Buildings);
        era3Buildings = era3Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][2]);

        // BuildingDeck holds only Era 2 and Era 3 buildings: the Era 1 ones are returned separately to seed the game
        BuildingDeck buildingDeck = new BuildingDeck(Map.of(
                Era.ERA_2, era2Buildings,
                Era.ERA_3, era3Buildings
        ));
        return new BuildingDrawResult(era1Buildings, buildingDeck);
    }

    /**
     * Creates the {@link Player} instances and pairs them with a totem,
     * assigning colors in the declaration order of {@link TotemColors}.
     *
     * @param playersInfo connected players
     * @return the resulting list of players
     */
    private List<Player> createPlayersAndTotems(List<PlayerConnectionInfo> playersInfo) {
        List<Player> players = new ArrayList<>();
        TotemColors[] colors = TotemColors.values();
        for (int i = 0; i < numberOfPlayers; i++) {
            String pId = playersInfo.get(i).id();
            String pNickname = playersInfo.get(i).nickname();

            Totem t = new Totem(pId, colors[i].getColor());
            players.add(new Player(pId, pNickname, 0, 0, t));
        }
        return players;
    }

    /**
     * Shuffles the players and places their totems on the turn order
     * slots in the resulting order.
     *
     * @param tile    the turn order tile to populate
     * @param players the players to place
     */
    private void randomlyPlaceTotems(TurnOrderTile tile, List<Player> players) {
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        for (int i = 0; i < numberOfPlayers; i++) {
            Player p = shuffledPlayers.get(i);
            tile.getSlot(i).placeTotem(p.getId());
        }
    }

    /**
     * Distributes the starting food to the players based on their
     * position on the turn order tile.
     *
     * @param tile    the turn order tile with placed totems
     * @param players the players in the match
     */
    private void dealFood(TurnOrderTile tile, List<Player> players) {
        int[] foodByIndex = {2, 3, 3, 4, 4};
        for (int i = 0; i < numberOfPlayers; i++) {
            TurnOrderSlot slot = tile.getSlot(i);
            Player player = players.stream()
                    .filter(p -> Objects.equals(p.getId(), slot.getPlayerId()))
                    .findFirst()
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found in Turn Order Slot, food can not be dealt!"));
            player.applyFoodDelta(foodByIndex[i]);
        }
    }

    /**
     * Result of the initial card draw used to populate the card market.
     *
     * @param upperRow  cards placed on the top row
     * @param lowerRow  cards placed on the bottom row
     * @param tribeDeck the tribe deck after the draws
     */
    private record DrawResult(List<Card> upperRow, List<Card> lowerRow, TribeDeck tribeDeck) {
    }

    /**
     * Result of the initial building draw.
     *
     * @param era1Buildings buildings revealed at setup for Era 1
     * @param buildingDeck  the deck holding Era 2 and Era 3 buildings
     */
    private record BuildingDrawResult(List<BuildingCard> era1Buildings, BuildingDeck buildingDeck) {
    }

}
