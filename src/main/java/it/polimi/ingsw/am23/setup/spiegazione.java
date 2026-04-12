package it.polimi.ingsw.am23.setup;

public class spiegazione {
}
/*
 * SETUP JSON-DRIVEN: panoramica architetturale
 *
 * Questa parte del progetto serve a costruire i componenti statici del gioco
 * (carte personaggio, carte evento, carte edificio, offer tiles) a partire dai
 * file JSON presenti in src/main/resources/setup.
 *
 * L’idea principale è separare bene:
 *
 * 1) definition
 *    classi semplici che rappresentano il contenuto dei JSON
 *    (es. CharacterCardDefinition, EventCardDefinition, ...)
 *
 * 2) loader
 *    classi che leggono i JSON e li deserializzano in liste di definition
 *    (JsonDefinitionLoader)
 *
 * 3) creator / factory
 *    classi che trasformano le definition in veri oggetti del model
 *    (CharacterCardFactory, EventCardFactory, BuildingCardFactory, OfferTileFactory, ...)
 *
 * 4) service
 *    classi che orchestrano tutto il caricamento e restituiscono un catalogo
 *    pronto da usare per costruire il Setup del gioco
 *    (JsonSetupCatalogLoader, ResourceSetupFactory)
 *
 * Perché questa struttura:
 *
 * - il model runtime non conosce JSON né parsing
 * - la logica di costruzione è centralizzata e non sparsa
 * - evitiamo mega-switch / instanceof nel dominio
 * - se dobbiamo cambiare un file JSON, interveniamo nel layer setup
 *   senza sporcare il model
 *
 * Scelte progettuali importanti:
 *
 * - I JSON contengono solo dati di configurazione del gioco.
 * - I parametri fissi del gioco che non cambieranno mai possono restare hardcoded
 *   nei creator/factory, invece di essere forzati nel JSON.
 * - Per gli edifici usiamo effectType semantici e puliti, non nomi di classi Java.
 * - Le carte finali evento usano isFinal, che poi viene gestito nella costruzione
 *   del tribe deck.
 *
 * Flusso completo:
 *
 * JSON -> Definition -> Factory/Creator -> Oggetti del model -> Setup -> Game
 *
 * Nota:
 * questa parte è pensata per restare indipendente sia dalla view sia dalla rete.
 * Si occupa solo di bootstrap/configurazione iniziale del gioco.
 */

//TODO:ricordarsi di toglierlo
