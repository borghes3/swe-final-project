package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.Card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Single deck used to refill the card market top row. Holds a shuffled
 * sequence of tribe and event cards.
 */
public class TribeDeck {

    private final Deque<Card> cards;

    /**
     * Builds a new tribe deck.
     *
     * @param cards initial deck content in draw order (first to be drawn comes first)
     */
    public TribeDeck(List<Card> cards){
        this.cards = new ArrayDeque<>(cards);
    }

    /**
     * Draws the next card from the deck.
     *
     * @return the drawn card
     * @throws IllegalStateException if the deck is empty
     */
    public Card draw(){
        if(isEmpty()){
            throw new IllegalStateException("The deck is empty");
        }
        return cards.removeFirst();
    }

    /**
     * Peeks the next card without drawing it.
     *
     * @return the next card
     * @throws IllegalStateException if the deck is empty
     */
    public Card peekTop(){
        if(isEmpty()){
            throw new IllegalStateException("The deck is empty");
        }
        return cards.peekFirst();
    }

    /** @return {@code true} if the deck is empty */
    public boolean isEmpty(){
        return cards.isEmpty();
    }

    /** @return the number of cards left in the deck */
    public int size(){
        return cards.size();
    }

    /** @return an unmodifiable snapshot of the cards still in the deck */
    public List<Card> getCards(){
        return List.copyOf(new ArrayList<>(cards));
    }


}
