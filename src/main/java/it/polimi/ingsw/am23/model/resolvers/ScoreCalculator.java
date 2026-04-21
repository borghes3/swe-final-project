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

public class ScoreCalculator {

    private final Game game;
    private final List<ScoreResult> scoreboard = new ArrayList<>();

    public ScoreCalculator(Game game) {
        this.game = game;
    }

    // Calcolo punteggio
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
        // Ordino scoreboard
        scoreboard.sort(
                Comparator
                        .comparingInt((ScoreResult s) -> s.PP)                          // sort prima per PP
                        .thenComparingInt(s -> s.foodPoints).reversed()      // poi per foodPoints
        );
        return scoreboard;
    }

    // Print vincitore(i)
    public List<Player> getWinner() {
        if (scoreboard.isEmpty()) {
            throw new IllegalStateException("Scores have not been calculated yet");
        }
        List<Player> winners = new ArrayList<>();
        ScoreResult firstWinner = scoreboard.get(0);
        winners.add(firstWinner.player);

        // Verifico equivalenze di punti / cibo
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

    //Metodi dedicati al calcolo dei punteggi per le singole carte che conseguono al punteggio finale

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
