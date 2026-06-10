package it.polimi.ingsw.am23.view.cli;

import it.polimi.ingsw.am23.model.state.*;

import java.util.*;
import java.util.stream.Collectors;

import static it.polimi.ingsw.am23.view.cli.CLIColors.*;

final class CLIBoardRenderer {

    // Card
    private static final int CARD_INNER = 12;
    private static final int CARD_W = CARD_INNER + 2;
    private static final int GAP = 1;

    // Tile
    private static final int TO_INNER = 9;
    private static final int TO_W = TO_INNER + 2;

    private static final int TILE_LINES = 8;
    private static final int TILE_INNER = 7;

    // Terminal Width
    private static final int TERMINAL_W = 120;
    private static final int MAX_CARDS_PER_ROW = 7;


    private String cardColor(String type) {
        if (type == null) return BR_WHITE;
        return switch (type.toUpperCase()) {
            case "BUILDING" -> BR_WHITE;
            case "EVENT"    -> BR_RED;
            default         -> BR_CYAN;
        };
    }

    // ENTRY POINT
    void render(GameState gameState, String statusMessage, String localPlayerId) {
        if (gameState == null) {
            System.out.println(paint(BR_RED,"Game state unavailable."));
            return;
        }

        Map<String, PlayerState> byId = gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId() != null)
                .collect(Collectors.toMap(
                        PlayerState::getPlayerId, p -> p,
                        (a, b) -> a, LinkedHashMap::new));

        BoardState board = gameState.getBoard();

        printHeader(gameState, byId);
        printMainArea(board, byId, localPlayerId, gameState.getPlayers().size());
        printStatus(statusMessage);
        printCommands();
    }

    // HEADER
    // Phase | Era | Round
    private void printHeader(GameState gs, Map<String, PlayerState> byId) {
        String phase = paintBold(BR_WHITE, gs.getPhase().name().replace('_', ' '));
        String era   = paintBold(BR_WHITE,  "Era " + eraLabel(gs.getCurrentEra()));
        String round = paintBold(BR_WHITE,   "Round " + gs.getCurrentRound());
        System.out.println();
        System.out.println(phase + "  " + paint(DIM, "|") + "  " + era
                + "  " + paint(DIM, "|") + "  " + round);
        System.out.println(paint(DIM, rule(TERMINAL_W)));
        System.out.println();
    }

    // MAIN AREA
    private void printMainArea(BoardState board, Map<String, PlayerState> byId, String localId, int numPlayers) {
        if (board == null) {
            System.out.println(paint(DIM, "  [board unavailable]"));
            return;
        }

        // sx e dx
        String[] toLines = buildTurnOrderLines(board.getTurnOrderSlots(), byId, numPlayers);
        String[] otLines = buildOfferTrackLines(board.getOfferTiles(), byId);

        int cardsWidth = TERMINAL_W - TO_W - GAP - 1;
        int indent     = TO_W + GAP + 1;

        // Top Row (characters, buildings)
        printCardBand("top", board.getTopRow(), board.getTopBuildings(), indent, cardsWidth);

        // Offer Track (turn-order, track)
        System.out.println();
        int midH = Math.max(toLines.length, otLines.length);
        for (int i = 0; i < midH; i++) {
            String toCol = i < toLines.length ? toLines[i] : blankWidth(TO_W);
            String otCol = i < otLines.length ? otLines[i] : "";
            System.out.println(toCol + " " + otCol);
        }
        System.out.println();

        // Bottom Row (characters, buildings)
        printCardBand("bottom", board.getBottomRow(), board.getBottomBuildings(), indent, cardsWidth);
    }

    // CARD BAND
    // characters, buildings
    private void printCardBand(String label, List<CardState> chars, List<CardState> buildings, int leftIndent, int areaWidth) {
        String pad = " ".repeat(leftIndent);

        List<CardState> all = new ArrayList<>(chars); // all = characters + buildings
        List<Boolean> isBuilding = new ArrayList<>(Collections.nCopies(chars.size(), false));
        all.addAll(buildings);
        isBuilding.addAll(Collections.nCopies(buildings.size(), true));

        if (all.isEmpty()) {
            System.out.println(pad + paint(DIM, "[" + label + ": empty]"));
            return;
        }

        System.out.println(pad + paint(DIM, "-- " + label + " --"));

        int cardsPerRow = Math.min(
                MAX_CARDS_PER_ROW,
                Math.max(1, (areaWidth + GAP) / (CARD_W + GAP)));

        // Partition into sub-rows
        List<List<String[]>> rowGroups = new ArrayList<>();
        List<String[]> cur = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            cur.add(buildCardLines(all.get(i), isBuilding.get(i)));
            if (cur.size() == cardsPerRow || i == all.size() - 1) {
                rowGroups.add(new ArrayList<>(cur));
                cur.clear();
            }
        }

        for (List<String[]> group : rowGroups) {
            for (int line = 0; line < group.get(0).length; line++) {
                System.out.print(pad);
                for (int c = 0; c < group.size(); c++) {
                    System.out.print(group.get(c)[line]); // line : 5 righe per ogni card
                    if (c < group.size() - 1) System.out.print(" ".repeat(GAP));
                }
                System.out.println();
            }
        }
    }

    // Card box
    private String[] buildCardLines(CardState card, boolean isBuilding) {
        String type  = isBuilding ? "BUILDING" : resolveCharType(card);
        String color = cardColor(type);
        String info  = essentialInfo(card, isBuilding);
        String name  = resolveDisplayName(card);

        String[] out = new String[5];
        //  +------------+
        //  |id          |
        //  |type        |
        //  |info        |
        //  +------------+

        out[0] = paint(color, "+" + "-".repeat(CARD_INNER) + "+");
        out[1] = paint(color, "|") + padVis(paint(color, truncate(name, CARD_INNER)), CARD_INNER) + paint(color, "|");

        String typeLine = paint(color, type.toUpperCase());
        String infoStr  = info.isEmpty() ? "" : paint(DIM, info);
        out[2] = paint(color, "|") + padVis(typeLine, CARD_INNER) + paint(color, "|");
        out[3] = paint(color, "|") + padVis(infoStr, CARD_INNER) + paint(color, "|");
        out[4] = paint(color, "+" + "-".repeat(CARD_INNER) + "+");

        return out;
    }

    // ESSENTIAL INFO
    private String essentialInfo(CardState card, boolean isBuilding) {
        if (isBuilding && card instanceof BuildingCardState b)
            return b.getFoodCost() + "f";

        if (!(card instanceof CharacterCardState cc)) return "";

        return switch (cc.getCharacterType()) {
            case INVENTOR -> cc.getInventionIcon() != null ? cc.getInventionIcon().toString() : "";
            case SHAMAN   -> cc.getStars() != null ? cc.getStars()    + "*"  : "";
            case BUILDER  -> cc.getDiscount() != null ? "-" + cc.getDiscount() + "f" : "";
            case HUNTER   -> Boolean.TRUE.equals(cc.getHasFoodSymbol()) ? "[o-]" : "";
            default       -> "";
        };
    }


    // TURN ORDER TILE
    private String[] buildTurnOrderLines(List<TurnOrderSlotState> slots, Map<String, PlayerState> byId, int numPlayers) {
        List<TurnOrderSlotState> ordered = new ArrayList<>(slots);
        ordered.sort(Comparator.comparingInt(TurnOrderSlotState::getPositionIndex));

        // as many slots as the players
        while (ordered.size() > numPlayers)
            ordered.remove(ordered.size() - 1);
        while (ordered.size() < numPlayers)
            ordered.add(new TurnOrderSlotState(ordered.size(), 0, null));


        int height = 2 + numPlayers + 1;  // bordi + players + scritta ORDER
        String[] out = new String[height];

        out[0] = paint(DIM, "+" + "-".repeat(TO_INNER) + "+");
        out[1] = paint(DIM, "|") + centerVis(paint(DIM, "ORDER"), TO_INNER) + paint(DIM, "|");

        for (int i = 0; i < numPlayers; i++) {
            TurnOrderSlotState s = ordered.get(i);
            String pid = s.getOccupiedByPlayerId();
            String cell;
            if (pid == null) {
                cell = paint(DIM, (i + 1) + " ---");
            } else {

                String nick = truncate(nickOf(pid, byId), TO_INNER - 2);
                String tc = totemColor(byId.containsKey(pid) ? byId.get(pid).getTotemColor() : null);
                cell = paint(DIM, (i + 1) + " ") + paint(tc, nick);
            }
            out[2 + i] = paint(DIM, "|") + padVis(cell, TO_INNER) + paint(DIM, "|");
        }

        out[height - 1] = paint(DIM, "+" + "-".repeat(TO_INNER) + "+");
        return out;
    }

    // OFFER TRACK
    private String[] buildOfferTrackLines(List<OfferTileState> tiles, Map<String, PlayerState> byId) {
        List<OfferTileState> sorted = new ArrayList<>(tiles);
        sorted.sort(Comparator.comparing(OfferTileState::getTileId));
        if (sorted.isEmpty()) return new String[]{ paint(DIM, "[offer track empty]") };

        List<String[]> tileBoxes = new ArrayList<>();
        for (OfferTileState t : sorted) {
            tileBoxes.add(buildTileBox(t, byId));   // 8 righe
        }

        String[] out = new String[TILE_LINES];
        for (int line = 0; line < TILE_LINES; line++) {
            StringBuilder sb = new StringBuilder();
            for (String[] box : tileBoxes) {
                sb.append(box[line]);
            }
            out[line] = sb.toString();
        }
        return out;
    }

    private String[] buildTileBox(OfferTileState t, Map<String, PlayerState> byId) {
        String[] out = new String[TILE_LINES];

        List<String> content = new ArrayList<>();

        content.add(padVis(paintBold(BR_WHITE, "   " + t.getTileId()), TILE_INNER));

        content.add(" ".repeat(TILE_INNER));

        if (t.getFoodReward() > 0)
            content.add(padVis(paint(BR_WHITE, "+" + t.getFoodReward() + "f"), TILE_INNER));

        if (t.getTopDrawCount() > 0 && t.getBottomDrawCount() > 0) {
            content.add(padVis(paint(BR_WHITE, "^" + t.getTopDrawCount() + " v" + t.getBottomDrawCount()), TILE_INNER));
        } else if (t.getTopDrawCount() > 0) {
            content.add(padVis(paint(BR_WHITE, "^" + t.getTopDrawCount()), TILE_INNER));
        } else if (t.getBottomDrawCount() > 0) {
            content.add(padVis(paint(BR_WHITE, "v" + t.getBottomDrawCount()), TILE_INNER));
        }

        content.add(" ".repeat(TILE_INNER));

        // occupant player
        String pid = t.getOccupiedByPlayerId();
        String occ;
        if (pid == null) {
            occ = paint(DIM, "---");
        } else {
            String nick = truncate(nickOf(pid, byId), TILE_INNER - 1);
            String tc   = totemColor(byId.containsKey(pid) ? byId.get(pid).getTotemColor() : null);
            occ = paint(tc, nick);
        }
        content.add(padVis(occ, TILE_INNER));


        int contentSlots = TILE_LINES - 2;   // 6
        out[0] = paint(DIM, "+" + "-".repeat(TILE_INNER) + "+");
        for (int i = 0; i < contentSlots; i++) {
            String cell = i < content.size() ? content.get(i) : " ".repeat(TILE_INNER);
            out[1 + i] = paint(DIM, "|") + cell + paint(DIM, "|");
        }
        out[TILE_LINES - 1] = paint(DIM, "+" + "-".repeat(TILE_INNER) + "+");

        return out;
    }

    // STATUS
    private void printStatus(String statusMessage) {
        if (statusMessage == null || statusMessage.isBlank()) return;
        System.out.println();
        System.out.println();
        System.out.println(paintBold(BR_GREEN, statusMessage));
        System.out.println();
    }

    // COMMANDS
    private void printCommands() {
        System.out.println();
        System.out.println(paint(DIM, rule(TERMINAL_W)));
        System.out.println(paint(DIM, "place <A-G>  |  take <top|bottom> <idx> <card|building>  |  extra <idx> [building]  |  skip  |  peek  |  state  |  exit "));
    }


    // HELPERS
    private String resolveDisplayName(CardState card) {
        if (card instanceof EventCardState) {
            String id = card.getCardId();
            if (id != null) {
                if (id.startsWith("ECP")) return "CAVE PAINT";
                if (id.startsWith("EHU")) return "HUNT";
                if (id.startsWith("ESH")) return "SHAMANIC";
                if (id.startsWith("ESU")) return "SUSTENANCE";
            }
            return "EVENT";
        }
        return card.getCardId();
    }

    private String resolveCharType(CardState card) {
        if (card instanceof CharacterCardState cc) return cc.getCharacterType().toString().toUpperCase();
        if (card instanceof EventCardState)        return "EVENT";
        if (card instanceof BuildingCardState)     return "BUILDING";
        return "UNKNOWN";
    }

    // UTILITIES
    private String padVis(String text, int width) {
        int pad = Math.max(0, width - visLen(text));
        return text + " ".repeat(pad);
    }

    private String centerVis(String text, int width) {
        int pad  = Math.max(0, width - visLen(text));
        int left = pad / 2;
        return " ".repeat(left) + text + " ".repeat(pad - left);
    }

    private int visLen(String s) {
        if (s == null) return 0;
        return s.replaceAll("\u001B\\[[\\d;]*m", "").length();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s == null ? "" : s;
        return s.substring(0, max - 2) + "..";
    }

    private String nickOf(String pid, Map<String, PlayerState> byId) {
        if (pid == null) return "free";
        PlayerState p = byId.get(pid);
        return p != null ? p.getNickname() : pid;
    }

    private String eraLabel(Object era) {
        if (era == null) return "?";
        String s = era.toString().toUpperCase();
        if (s.contains("3") || s.contains("III")) return "III";
        if (s.contains("2") || s.contains("II"))  return "II";
        if (s.contains("1") || s.contains("I"))   return "I";
        return s;
    }
}
