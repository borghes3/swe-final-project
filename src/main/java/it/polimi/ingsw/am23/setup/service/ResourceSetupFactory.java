package it.polimi.ingsw.am23.setup.service;

import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.setup.Setup;

import java.util.List;
import java.util.Objects;

public class ResourceSetupFactory {

    private final JsonSetupCatalogLoader catalogLoader;

    public ResourceSetupFactory() {
        this.catalogLoader = new JsonSetupCatalogLoader();
    }

    public ResourceSetupFactory(JsonSetupCatalogLoader catalogLoader) {
        this.catalogLoader = Objects.requireNonNull(catalogLoader, "catalogLoader cannot be null");
    }

    public Setup createSetup(List<PlayerConnectionInfo> playersInfo,
                             List<TurnOrderTile> turnOrderTiles) {
        Objects.requireNonNull(playersInfo, "playersInfo cannot be null");
        Objects.requireNonNull(turnOrderTiles, "turnOrderTiles cannot be null");

        SetupCatalog catalog = catalogLoader.load();

        return new Setup(
                playersInfo,
                catalog.getBuildingCards(),
                catalog.getEventCards(),
                catalog.getCharacterCards(),
                catalog.getOfferTiles(),
                turnOrderTiles
        );
    }
}

//facciata usata da tutto il resto del progetto, non si vedono definition, json, factory o creator