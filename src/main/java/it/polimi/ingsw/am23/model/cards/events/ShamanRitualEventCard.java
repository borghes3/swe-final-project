package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ShamanRitualEventCard extends EventCard {

    private final int winPoints;
    private final int losePoints;

    public ShamanRitualEventCard(String id, Era era, int points, boolean isFinal, int winPoints, int losePoints) {
        super(id, era, points, isFinal);
        this.winPoints = winPoints;
        this.losePoints = losePoints;
    }

    public int getWinPoints() {
        return winPoints;
    }

    public int getLosePoints() {
        return losePoints;
    }

    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();
        List<ShamanResult> results = new ArrayList<>();

        for (Player player : players) {
            ShamanRitualEffectData data = new ShamanRitualEffectData();

            for (BuildingCard building : player.getTribe().getBuildings()) {
                building.getEffect().applyShamanRitual(game, player, data);
            }

            int totalstars = player.getTribe().totalShamanStars() + data.getBonusStars();
            results.add(new ShamanResult(player, totalstars, data));
        }
        int maxStars = getMaxStars(results);
        int minStars = getMinStars(results);

        for (ShamanResult result : results) {
            result.setWinner(result.getTotalStars() == maxStars);
            result.setLast(result.getTotalStars() == minStars);
        }

        for (ShamanResult result : results) {
            applyResult(result);
        }
    }

    private int getMaxStars(List<ShamanResult> results) {
        int max = Integer.MIN_VALUE;
        for (ShamanResult result : results) {
            max = Math.max(max, result.getTotalStars());
        }
        return max;
    }

    private int getMinStars(List<ShamanResult> results) {
        int min = Integer.MAX_VALUE;
        for (ShamanResult result : results) {
            min = Math.min(min, result.getTotalStars());
        }
        return min;
    }

    private void applyResult(ShamanResult result) {
        Player player = result.getPlayer();
        ShamanRitualEffectData data = result.getData();

        if (result.isWinner()) {
            int finalPoints = winPoints;
            if (data.doubleWin()) {
                finalPoints *= 2;
            }
            player.addPrestigePoints(finalPoints);
        }
        if (result.isLast()) {
            if (!data.ignoreLoss()) {
                player.losePrestigePoints(losePoints);
            }
        }
    }

    private static class ShamanResult {
        private final Player player;
        private final int totalStars;
        private final ShamanRitualEffectData data;
        private boolean winner;
        private boolean last;

        public ShamanResult(Player player, int totalStars, ShamanRitualEffectData data) {
            this.player = player;
            this.totalStars = totalStars;
            this.data = data;
        }

        public Player getPlayer() {
            return player;
        }

        public int getTotalStars() {
            return totalStars;
        }

        public ShamanRitualEffectData getData() {
            return data;
        }

        public boolean isWinner() {
            return winner;
        }

        public boolean isLast() {
            return last;
        }

        public void setWinner(boolean winner) {
            this.winner = winner;
        }

        public void setLast(boolean last) {
            this.last = last;
        }
    }
}


