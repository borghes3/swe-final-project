package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.enums.Era;

public class RefillResult {

    private boolean eraAdvanced;
    private Era newEra;

    public boolean isEraAdvanced() {
        return eraAdvanced;
    }
    public Era getNewEra() {
        return newEra;
    }
    public void registerEraAdvance(Era era) {
        if(!eraAdvanced || era.ordinal() > newEra.ordinal()){
            eraAdvanced = true;
            newEra = era;
        }
    }
}
