package it.polimi.ingsw.am23.setup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.setup.creator.cards.*;
import it.polimi.ingsw.am23.setup.creator.effects.*;
import it.polimi.ingsw.am23.setup.definition.board.OfferTileDefinition;
import it.polimi.ingsw.am23.setup.definition.cards.BuildingCardDefinition;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;
import it.polimi.ingsw.am23.setup.definition.cards.EventCardDefinition;
import it.polimi.ingsw.am23.setup.factory.board.OfferTileFactory;
import it.polimi.ingsw.am23.setup.factory.cards.BuildingCardFactory;
import it.polimi.ingsw.am23.setup.factory.cards.CharacterCardFactory;
import it.polimi.ingsw.am23.setup.factory.cards.EventCardFactory;
import it.polimi.ingsw.am23.setup.factory.effects.BuildingEffectFactory;
import it.polimi.ingsw.am23.setup.loader.DefinitionLoader;
import it.polimi.ingsw.am23.setup.loader.JsonDefinitionLoader;

import java.util.List;
import java.util.Objects;

public class JsonSetupCatalogLoader {

    private final DefinitionLoader<CharacterCardDefinition> characterLoader;
    private final DefinitionLoader<BuildingCardDefinition> buildingLoader;
    private final DefinitionLoader<EventCardDefinition> eventLoader;
    private final DefinitionLoader<OfferTileDefinition> tileLoader;

    private final CharacterCardFactory characterFactory;
    private final BuildingCardFactory buildingFactory;
    private final EventCardFactory eventFactory;
    private final OfferTileFactory offerTileFactory;

    private final String charactersPath;
    private final String buildingsPath;
    private final String eventsPath;
    private final String tilesPath;

    public JsonSetupCatalogLoader() {
        this(
                new ObjectMapper(),
                "/setup/characters.json",
                "/setup/buildings.json",
                "/setup/events.json",
                "/setup/tiles.json"
        );
    }

    public JsonSetupCatalogLoader(ObjectMapper objectMapper,
                                  String charactersPath,
                                  String buildingsPath,
                                  String eventsPath,
                                  String tilesPath) {
        Objects.requireNonNull(objectMapper, "objectMapper cannot be null");

        this.characterLoader = new JsonDefinitionLoader<>(objectMapper, CharacterCardDefinition[].class);
        this.buildingLoader = new JsonDefinitionLoader<>(objectMapper, BuildingCardDefinition[].class);
        this.eventLoader = new JsonDefinitionLoader<>(objectMapper, EventCardDefinition[].class);
        this.tileLoader = new JsonDefinitionLoader<>(objectMapper, OfferTileDefinition[].class);

        this.characterFactory = new CharacterCardFactory(List.of(
                new ArtistCardCreator(),
                new BuilderCardCreator(),
                new GathererCardCreator(),
                new HunterCardCreator(),
                new InventorCardCreator(),
                new ShamanCardCreator()
        ));

        this.buildingFactory = new BuildingCardFactory(
                new BuildingEffectFactory(List.of(
                        new FoodFromTurnOrderBonusEffectCreator(),
                        new FoodPerCompletedSetEffectCreator(),
                        new FoodPerInventorPairEffectCreator(),
                        new NoLossIfLastShamanEffectCreator(),
                        new SustenanceDiscountPerTypeEffectCreator(),
                        new CavePaintingsFoodPerArtistEffectCreator(),
                        new DoubleBuilderEndGameEffectCreator(),
                        new DoubleShamanWinEffectCreator(),
                        new EndGamePointsPerCompleteSetEffectCreator(),
                        new HuntingRewardPerHunterEffectCreator(),
                        new ShamanBonusStarsEffectCreator(),
                        new EndGamePointsPerCharacterTypeEffectCreator(),
                        new ExtraDrawEffectCreator(),
                        new FlatEndGamePointsEffectCreator()
                ))
        );

        this.eventFactory = new EventCardFactory();

        this.offerTileFactory = new OfferTileFactory();

        this.charactersPath = Objects.requireNonNull(charactersPath, "charactersPath cannot be null");
        this.buildingsPath = Objects.requireNonNull(buildingsPath, "buildingsPath cannot be null");
        this.eventsPath = Objects.requireNonNull(eventsPath, "eventsPath cannot be null");
        this.tilesPath = Objects.requireNonNull(tilesPath, "tilesPath cannot be null");
    }

    public SetupCatalog load() {
        List<CharacterCard> characterCards = characterLoader.loadAll(charactersPath).stream()
                .map(characterFactory::create)
                .toList();

        List<BuildingCard> buildingCards = buildingLoader.loadAll(buildingsPath).stream()
                .map(buildingFactory::create)
                .toList();

        List<EventCard> eventCards = eventLoader.loadAll(eventsPath).stream()
                .map(eventFactory::create)
                .toList();

        List<OfferTile> offerTiles = tileLoader.loadAll(tilesPath).stream()
                .map(offerTileFactory::create)
                .toList();

        return new SetupCatalog(
                buildingCards,
                eventCards,
                characterCards,
                offerTiles
        );
    }
}