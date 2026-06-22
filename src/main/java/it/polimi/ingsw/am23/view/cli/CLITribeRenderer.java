package it.polimi.ingsw.am23.view.cli;

import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;
import it.polimi.ingsw.am23.model.state.PlayerState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static it.polimi.ingsw.am23.view.cli.CLIColors.*;

public class CLITribeRenderer {
    private static final int TERM_W = 120;

    void render(List<PlayerState> players, String localId) {
        if (players == null || players.isEmpty()) return;

        System.out.println();
        System.out.println(paint(DIM, rule(TERM_W)));

        for (PlayerState p : players) {     // determinare se è you
            boolean isLocal = Objects.equals(p.playerId(), localId);
            printPlayerTribe(p, isLocal);
        }
    }

    private void printPlayerTribe(PlayerState p, boolean isLocal) {
        String nameColor = totemColor(p.totemColor());
        String tag = isLocal ? " (you)" : "";

        int totChars = p.characters().size();
        System.out.println(
                paintBold(nameColor, p.nickname() + tag)
                        + "    " + p.food() + " food"
                        + "  " + p.prestigePoints() + " PP"
                        + "  " + " TOT: " + totChars);


        int countInv = 0, countSha = 0, countBld = 0, countHnt = 0, countGth = 0, countArt = 0;
        int totalStars = 0, totalDiscount = 0;
        Set<String> iconSet = new LinkedHashSet<>();

        for (CardState c : p.characters()) {
            if (!(c instanceof CharacterCardState cc)) continue;
            switch (cc.getCharacterType()) {
                case INVENTOR -> {
                    countInv++;
                    InventionIcon icon = cc.getInventionIcon();
                    if (icon != null) iconSet.add(icon.toString());
                }
                case SHAMAN -> {
                    countSha++;
                    if (cc.getStars() != null) totalStars += cc.getStars();
                }
                case BUILDER -> {
                    countBld++;
                    if (cc.getDiscount() != null) totalDiscount += cc.getDiscount();
                }
                case HUNTER -> countHnt++;
                case GATHERER -> countGth++;
                case ARTIST -> countArt++;
            }
        }


        System.out.println(
                "  INV " + countInv
                        + "  SHA " + countSha
                        + "  BLD " + countBld
                        + "  HNT " + countHnt
                        + "  GTH " + countGth
                        + "  ART " + countArt
        );

        System.out.println(
                "  SHA stars: " + totalStars
                        + "  INV icons: " + iconSet.size()
                        + "  BLD disc: " + "-" + totalDiscount + "f"
        );


        if (p.buildings().isEmpty()) {
            System.out.println("  BUILDINGS (0)");
        } else {
            String ids = p.buildings().stream()
                    .map(CardState::getCardId)
                    .collect(Collectors.joining("  "));
            System.out.println("  BUILDINGS (" + p.buildings().size() + "):  " + ids);
        }

        System.out.println(paint(DIM, rule(TERM_W)));
    }
}
