package it.polimi.ingsw.am23.model.effects;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.player.Player;

public interface BuildingEffect {

    default int modifyTurnOrderFood(Game game, Player player, int currentFood){ //il cibo è fisso????
        return currentFood;
    } //quando player metter totem in TurnOrder

    default void onCardTaken(Game game, Player player, Card card){

    }// sarebbe quello per i set/coppie di inventori ma è da sistemare
    // se usare come ora mettere notifyCardTakenToBuildings in game

    default int modifySustenanceCost(Game game, Player player, int currentCost){
        return currentCost;
    }// per effetti legati all'evento sostentamento

    default void applyHunting(Game game, Player player, HuntingEffectData data){

    }// per effetti legati all'evento caccia

    default void applyCavePaintings(Game game, Player player, CavePaintingsEffectData data){

    }// per effetti legati all'evento pitture rupestri

    default void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data){
    } // per effetti legati all'evento rituale sciamanico

    default void onAfterAllActions(Game game, Player player){

    }// per gestire la pesca aggiuntiva

    default int getEndGamePoints(Game game, Player player){
        return 0;
    } // per calcolo finale dei punti

    default void onBuildingAdded(Player player){}
}
