package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.CavePaintingsEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect granting bonus food during the resolution of the Cave
 * Paintings event, proportional to the number of Artist cards owned.
 */
public class CavePaintingsFoodPerArtistEffect implements BuildingEffect {

    private int foodPerArtist;

    /**
     * Builds a new effect.
     *
     * @param foodPerArtist food granted per Artist owned
     */
    public CavePaintingsFoodPerArtistEffect(int foodPerArtist){
        this.foodPerArtist = foodPerArtist;
    }

    /** {@inheritDoc} */
    @Override
    public void applyCavePaintings(Game game, Player player, CavePaintingsEffectData data){
        int count = player.getTribe().count(CharacterType.ARTIST);
        data.addExtraFood(foodPerArtist*count);
    }

}
