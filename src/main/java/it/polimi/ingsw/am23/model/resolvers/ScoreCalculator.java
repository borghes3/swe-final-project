package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.player.Tribe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes the end-of-game scores for every player in the {@link Game}.
 * Aggregates per-category points (builders, inventors, artists, buildings
 * including their effects) on top of the prestige points accumulated
 * during the match.
 */
public class ScoreCalculator {

    private final Game game;
    private final List<ScoreResult> scoreboard = new ArrayList<>();

    /**
     * Builds a new score calculator bound to the supplied game.
     *
     * @param game game whose players will be scored
     */
    public ScoreCalculator(Game game) {
        this.game = game;
    }

    /**
     * Computes and orders the final scoreboard for the bound game.
     *
     * @return the scoreboard, sorted by prestige points (and food points as tie breaker)
     */
    public List<ScoreResult> calculateFinalScores() {
        for (Player player : game.getPlayers()) {
            int foodPoints = player.getFood();
            int prePoints = player.getPrestigePoints();
            int postPoints = calculateBuildersPoints(player)
                    + calculateInventorsPoints(player)
                    + calculateArtistsPoints(player)
                    + calculateBuildingsPoints(player, game);

            int totalPoints = prePoints + postPoints;
            scoreboard.add(new ScoreResult(player, foodPoints, totalPoints));
        }
        // Sort by prestige points, then by food points as tie breaker
        scoreboard.sort(
                Comparator
                        .comparingInt((ScoreResult s) -> s.PP)
                        .thenComparingInt(s -> s.foodPoints).reversed()
        );
        return scoreboard;
    }

    /**
     * Returns the winner(s) of the match. When multiple players share the
     * top prestige and food points, all of them are returned.
     *
     * @return the list of winners (always non empty after {@link #calculateFinalScores()})
     * @throws IllegalStateException if scores have not been computed yet
     */
    public List<Player> getWinner() {
        if (scoreboard.isEmpty()) {
            throw new IllegalStateException("Scores have not been calculated yet");
        }
        List<Player> winners = new ArrayList<>();
        ScoreResult firstWinner = scoreboard.get(0);
        winners.add(firstWinner.player);

        // Collect all players tied with the top score
        for (int i = 1; i < scoreboard.size(); i++) {
            ScoreResult current = scoreboard.get(i);
            if (current.PP == firstWinner.PP && current.foodPoints == firstWinner.foodPoints) {
                winners.add(current.player);
            } else {
                break;
            }
        }
        return winners;
    }

    private int calculateBuildersPoints(Player p) {
        List<CharacterCard> builders = p.getTribe().getCharacters().stream().filter(c -> c.getCharacterType() == CharacterType.BUILDER).toList();
        int points = 0;
        for (CharacterCard b : builders) {
            points += b.getPoints();
        }
        return points;
    }

    private int calculateInventorsPoints(Player p) {
        Tribe tribe = p.getTribe();
        int inventorsCount = tribe.getCharacters().stream().filter(c -> c.getCharacterType() == CharacterType.INVENTOR).toList().size();
        int distinctIconsCount = tribe.getDistinctInventionIcons();
        return inventorsCount * distinctIconsCount;
    }

    private int calculateArtistsPoints(Player p) {
        int artistsCount = p.getTribe().getCharacters().stream().filter(c -> c.getCharacterType() == CharacterType.ARTIST).toList().size();
        return 10 * (artistsCount / 2);
    }

    private int calculateBuildingsPoints(Player p, Game g) {
        List<BuildingCard> buildings = p.getTribe().getBuildings();
        int points = 0;
        for (BuildingCard b : buildings) {
            points += b.getPoints();
            points += b.getEffect().getEndGamePoints(g, p);
        }
        return points;
    }

}
