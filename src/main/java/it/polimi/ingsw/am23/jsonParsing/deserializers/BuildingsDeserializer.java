package it.polimi.ingsw.am23.jsonParsing.deserializers;

import com.google.gson.*;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.*;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

import java.lang.reflect.Type;

public class BuildingsDeserializer implements JsonDeserializer<BuildingCard> {

    public BuildingCard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();

        String id = obj.get("id").getAsString();
        Era era = Era.values()[obj.get("era").getAsInt() - 1];
        int points = obj.get("points").getAsInt();
        int foodCost = obj.get("foodCost").getAsInt();

        String effectType = obj.get("effectType").getAsString();

        JsonObject params = obj.has("effectParams")
                ? obj.getAsJsonObject("effectParams")
                : null;

        BuildingEffect effect;
        String characterTypeString;
        CharacterType characterType;

        switch (effectType) {
            case "FOOD_FROM_TURN_ORDER_BONUS":
                effect = new FoodFromTurnOrderBonusEffect(1);
                break;
            case "FOOD_PER_COMPLETED_SET":
                effect = new FoodPerCompletedSetEffect();
                break;
            case "FOOD_PER_INVENTOR_PAIR":
                effect = new FoodPerInventorPairEffect();
                break;
            case "NO_LOSS_IF_LAST_SHAMAN":
                effect = new NoLossIfLastShamanEffect();
                break;
            case "SUSTENANCE_DISCOUNT_PER_TYPE":    //PARAMS: targetType
                assert params != null;
                characterTypeString = params.get("targetType").getAsString();
                characterType = CharacterType.valueOf(characterTypeString);
                effect = new SustenanceDiscountPerTypeEffect(characterType, 1);
                break;
            case "CAVE_PAINTINGS_FOOD_PER_ARTIST":
                effect = new CavePaintingsFoodPerArtistEffect(1);
                break;
            case "DOUBLE_BUILDER_END_GAME":
                effect = new DoubleBuilderEndGameEffect();
                break;
            case "DOUBLE_SHAMAN_WIN":
                effect = new DoubleShamanWinEffect();
                break;
            case "END_GAME_POINTS_PER_COMPLETE_SET":
                effect = new EndGamePointsPerCompleteSetEffect(6);
                break;
            case "HUNTING_REWARD_PER_HUNTER":
                effect = new HuntingRewardPerHunterEffect();
                break;
            case "SHAMAN_BONUS_STARS":
                effect = new ShamanBonusStarsEffect(3);
                break;
            case "END_GAME_POINTS_PER_CHARACTER_TYPE":  //PARAMS: targetType
                assert params != null;
                characterTypeString = params.get("targetType").getAsString();
                characterType = CharacterType.valueOf(characterTypeString);
                effect = new EndGamePointsPerCharacterTypeEffect(characterType, 3);
                break;
            case "EXTRA_DRAW":
                effect = new ExtraDrawEffect();
                break;
            case "FLAT_END_GAME_POINTS":
                effect = new FlatEndGamePointsEffect(25);
                break;
            default:
                throw new JsonParseException("Unknown building effect type: " + effectType);
        }

        return new BuildingCard(id, era, points, foodCost, effect);
    }
}