package it.polimi.ingsw.am23.setup.service;

import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.EventCard;

import java.util.List;
import java.util.Objects;

public final class SetupCatalog {

    private final List<BuildingCard> buildingCards;
    private final List<EventCard> eventCards;
    private final List<CharacterCard> characterCards;
    private final List<OfferTile> offerTiles;

    public SetupCatalog(List<BuildingCard> buildingCards,
                        List<EventCard> eventCards,
                        List<CharacterCard> characterCards,
                        List<OfferTile> offerTiles) {
        this.buildingCards = List.copyOf(Objects.requireNonNull(buildingCards, "buildingCards cannot be null"));
        this.eventCards = List.copyOf(Objects.requireNonNull(eventCards, "eventCards cannot be null"));
        this.characterCards = List.copyOf(Objects.requireNonNull(characterCards, "characterCards cannot be null"));
        this.offerTiles = List.copyOf(Objects.requireNonNull(offerTiles, "offerTiles cannot be null"));
    }

    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    public List<EventCard> getEventCards() {
        return eventCards;
    }

    public List<CharacterCard> getCharacterCards() {
        return characterCards;
    }

    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }
}