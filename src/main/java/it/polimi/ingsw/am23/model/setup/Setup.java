package it.polimi.ingsw.am23.model.setup;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.exceptions.UnmatchedGameCriteriaException;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.board.Board;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.board.RoundManager;
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


public class Setup {
    // Attributi forniti dal costruttore (NON ELABORATI)
    private final int numberOfPlayers;
    private final List<PlayerConnectionInfo> playersInfo;
    private final List<EventCard> eventCards;
    private final List<CharacterCard> characterCards;
    private final List<OfferTile> offerTiles;
    private final List<TurnOrderTile> turnOrderTiles;
    private final List<BuildingCard> buildingCards;

    public Setup(List<PlayerConnectionInfo> connectedPlayersInfo, List<BuildingCard> buildingCards, List<EventCard> eventCards, List<CharacterCard> characterCards, List<OfferTile> offerTiles, List<TurnOrderTile> turnOrderTiles) {
        this.numberOfPlayers = connectedPlayersInfo.size();
        this.playersInfo = connectedPlayersInfo;
        this.eventCards = eventCards;
        this.characterCards = characterCards;
        this.offerTiles = offerTiles;
        this.buildingCards = buildingCards;
        this.turnOrderTiles = turnOrderTiles;
    }

    // Crea effettivamente il game e lo restituisce
    public Game make() {

        // Creo tutti gli elementi necessari per la creazione delle componenti del game
        List<OfferTile> filteredOfferTiles = createOfferTrack(offerTiles);
        TurnOrderTile selectedTurnOrderTile = selectTurnOrderTile(turnOrderTiles);
        TribeDeck sortedTribeDeck = buildTribeDeck(eventCards, characterCards);
        DrawResult drawResult = drawCards(sortedTribeDeck);                                                     // Campi: upperRow, lowerRow, tribeDeck (ridotto alle carte pescate)
        BuildingDrawResult buildingsResult = drawBuildings(buildingCards);                                      // Campi: era1Buildings, buildingDeck(era 2 + era 3)
        List<Player> players = createPlayersAndTotems(playersInfo);
        List<String> orderedPlayersId = randomlyPlaceTotems(selectedTurnOrderTile, players);
        dealFood(selectedTurnOrderTile, players);

        CardMarket cardMarket = new CardMarket(drawResult.upperRow(), drawResult.lowerRow(), buildingsResult.era1Buildings(), null);
        Board board = new Board(cardMarket, filteredOfferTiles, selectedTurnOrderTile);
        RoundManager roundManager = new RoundManager(orderedPlayersId);

        return new Game(players, board, drawResult.tribeDeck(), buildingsResult.buildingDeck(), roundManager, new EventResolver(), cardMarket, Era.ERA_1, 0);
    }

    // ----------------------------------------------
    // Moduli per la creazione del game
    // ----------------------------------------------

    // Crea il tracciato delle offerte selezionando le tiles relative al corretto numero di giocatori e mettendole in ordine
    private List<OfferTile> createOfferTrack(List<OfferTile> offerTiles) {
        // Filtro per numero di giocatori
        List<OfferTile> filteredTiles = new ArrayList<>(offerTiles.stream().filter(t -> t.getMinPlayers() <= numberOfPlayers).toList());
        // Ordino per lettera alfabetica
        filteredTiles.sort(Comparator.comparing(OfferTile::getId));
        if (filteredTiles.isEmpty()) {
            throw new UnmatchedGameCriteriaException("No Offer Tiles matched the game criteria.");
        }
        return filteredTiles;
    }

    // Seleziono la tessera ordine di turno con il numero corretto di slot (e quindi di giocatori)
    private TurnOrderTile selectTurnOrderTile(List<TurnOrderTile> turnOrderTiles) {
        TurnOrderTile filteredTurnOrderTile = turnOrderTiles.stream().filter(t -> t.getSlotsCount() == numberOfPlayers).toList().getFirst();
        if (filteredTurnOrderTile == null) {
            throw new UnmatchedGameCriteriaException("No Turn Order Tile matched the game criteria");
        }
        return filteredTurnOrderTile;
    }

    // Filtra per numero di giocatori se presente, Divide in base a era, Mischia i 3 mazzi separatamente, Mette in ordine: (ERA I > ERA II > ERA III > EVENTI_FINALI)
    private TribeDeck buildTribeDeck(List<EventCard> events, List<CharacterCard> characters) {
        // Filtro per numero di giocatori (solo personaggi, gli eventi non hanno min. Giocatori)
        List<CharacterCard> filteredCharacters = characters.stream().filter(c -> c.getMinPlayers() <= numberOfPlayers).toList();
        // Divido in base a era
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
        era3.addAll(events.stream().filter(e -> e.getEra() == Era.ERA_3).toList());
        Collections.shuffle(era3);

        List<EventCard> finalEvents = new ArrayList<>(events.stream().filter(EventCard::isFinal).toList());
        Collections.shuffle(finalEvents);

        // Creo la lista finale
        List<Card> cardsByEra = Stream.of(era1, era2, era3, finalEvents).flatMap(List::stream).collect(Collectors.toList());
        return new TribeDeck(cardsByEra);
    }

    // Metodo unico perché le rows vengono modificate contemporaneamente (nel caso in cui si peschi un building per la lower row)
    // Pesca il numero corretto di carte e costruisce fila sopra e fila sotto
    private record DrawResult(List<Card> upperRow, List<Card> lowerRow, TribeDeck tribeDeck) {
    }

    private DrawResult drawCards(TribeDeck tribeDeck) {
        List<Card> upperRow = new ArrayList<>(), lowerRow = new ArrayList<>();

        // Pesco per creare fila inferiore, se becco eventi, li metto in quella superiore
        while (lowerRow.size() < numberOfPlayers + 1) {
            Card drawn = tribeDeck.draw();
            // Se é un evento va nella fila sopra
            if (drawn instanceof EventCard) {
                upperRow.add(0, drawn);
            } else {
                lowerRow.add(0, drawn);
            }
        }

        // Pesco per creare la fila superiore, completandola a #players + 4
        while (upperRow.size() < numberOfPlayers + 4) {
            Card drawn = tribeDeck.draw();
            upperRow.add(0, drawn);
        }

        return new DrawResult(upperRow, lowerRow, tribeDeck);
    }

    // Restituisce tre mazzi separati di Era1, Era 2 ed Era 3
    private record BuildingDrawResult(List<BuildingCard> era1Buildings, BuildingDeck buildingDeck) {
    }

    private BuildingDrawResult drawBuildings(List<BuildingCard> buildings) {
        int[][] buildingsByNumberOfPlayers = {
                {1, 2, 3},      // 2 players
                {2, 2, 4},      // 3 players
                {2, 3, 4},      // 4 players
                {2, 3, 5},      // 5 players
        };
        // Creo tre mazzetti Era1, Era 2, Era 3 buildings
        List<BuildingCard> era1Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_1).toList());
        Collections.shuffle(era1Buildings);
        era1Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][0]);
        List<BuildingCard> era2Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_2).toList());
        Collections.shuffle(era2Buildings);
        era2Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][1]);
        List<BuildingCard> era3Buildings = new ArrayList<>(buildings.stream().filter(b -> b.getEra() == Era.ERA_3).toList());
        Collections.shuffle(era3Buildings);
        era3Buildings.subList(0, buildingsByNumberOfPlayers[numberOfPlayers - 2][2]);

        // Costruisco BuildingDeck con le carte di era 2 ed era 3 (le carte di era 1 vengono ritornate separatamente per istanziare il game, vedi constructor game)
        BuildingDeck buildingDeck = new BuildingDeck(Map.of(
                Era.ERA_2, era2Buildings,
                Era.ERA_3, era3Buildings
        ));
        return new BuildingDrawResult(era1Buildings, buildingDeck);
    }

    // Sceglie colori in base all'ordine dell'enum TotemColors e li assegna ai player
    // TODO: Implementare scelta del colore da parte dell'utente (NEL CONTROLLER)
    private List<Player> createPlayersAndTotems(List<PlayerConnectionInfo> playersInfo) {
        List<Player> players = new ArrayList<>();
        TotemColors[] colors = TotemColors.values();
        for (int i = 0; i < numberOfPlayers; i++) {
            String pId = playersInfo.get(i).getId();
            String pNickname = playersInfo.get(i).getNickname();

            Totem t = new Totem(pId, colors[i].getColor());
            players.add(new Player(pId, pNickname, 0, 0, t));
        }
        return players;
    }

    // Prima mescola i players e successivamente li piazza negli slot della Turn Order tile
    private List<String> randomlyPlaceTotems(TurnOrderTile tile, List<Player> players) {
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        for (int i = 0; i < numberOfPlayers; i++) {
            Player p = shuffledPlayers.get(i);
            tile.getSlot(i).placeTotem(p.getId());
        }
        return shuffledPlayers.stream().map(Player::getId).collect(Collectors.toList());    // Restituisce playerId con lo stesso ordine con cui li ho piazzati
    }

    // Dá cibo in base a posizione
    private void dealFood(TurnOrderTile tile, List<Player> players) {
        int[] foodByIndex = {2, 3, 3, 4, 4};
        for (int i = 0; i < numberOfPlayers; i++) {
            TurnOrderSlot slot = tile.getSlot(i);
            Player player = players.stream()
                    .filter(p -> Objects.equals(p.getId(), slot.getPlayerId()))
                    .findFirst()
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found in Turn Order Slot, food can not be dealt!"));
            player.addFood(foodByIndex[i]);
        }
    }

}
