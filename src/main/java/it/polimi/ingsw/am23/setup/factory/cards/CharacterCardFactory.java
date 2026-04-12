package it.polimi.ingsw.am23.setup.factory.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.creator.cards.CharacterCardCreator;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CharacterCardFactory {

    private final Map<CharacterType, CharacterCardCreator> creators;

    public CharacterCardFactory(List<CharacterCardCreator> creators) {
        Objects.requireNonNull(creators, "creators cannot be null");

        this.creators = creators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        CharacterCardCreator::supportedType,
                        Function.identity()
                ));
    }

    public CharacterCard create(CharacterCardDefinition definition) {
        Objects.requireNonNull(definition, "definition cannot be null");

        CharacterType characterType = definition.getCharacterType();
        CharacterCardCreator creator = creators.get(characterType);

        if (creator == null) {
            throw new IllegalArgumentException("Unsupported character type: " + characterType);
        }

        return creator.create(definition);
    }
}
