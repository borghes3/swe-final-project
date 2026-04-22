package it.polimi.ingsw.am23.jsonParsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.am23.jsonParsing.deserializers.*;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.setup.Setup;

import java.io.IOException;
import java.util.List;

public class Parser {

    public Setup parse(List<PlayerConnectionInfo> playersConnectionInfo) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BuildingCard.class, new BuildingsDeserializer())
                .registerTypeAdapter(EventCard.class, new EventsDeserializer())
                .registerTypeAdapter(CharacterCard.class, new CharactersDeserializer())
                .registerTypeAdapter(OfferTile.class, new OfferTilesDeserializer())
                .registerTypeAdapter(TurnOrderTile.class, new TurnOrderDeserializer())
                .create();

        List<BuildingCard> buildingCards = ResourceLoader.loadListFromResource(gson, "/setup/buildings.json", BuildingCard.class);
        List<EventCard> eventCards = ResourceLoader.loadListFromResource(gson, "/setup/events.json", EventCard.class);
        List<CharacterCard> characterCards = ResourceLoader.loadListFromResource(gson, "/setup/characters.json", CharacterCard.class);
        List<OfferTile> offerTiles = ResourceLoader.loadListFromResource(gson, "/setup/offer_tiles.json", OfferTile.class);
        List<TurnOrderTile> turnOrderTiles = ResourceLoader.loadListFromResource(gson, "/setup/turn_order.json", TurnOrderTile.class);

        return new Setup(playersConnectionInfo, buildingCards, eventCards, characterCards, offerTiles, turnOrderTiles);
    }
}
