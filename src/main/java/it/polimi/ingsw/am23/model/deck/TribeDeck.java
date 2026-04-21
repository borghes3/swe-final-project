package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.Card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TribeDeck {

    private final Deque<Card> cards;

    public TribeDeck(List<Card> cards){
        this.cards = new ArrayDeque<>(cards);
    }

    public Card draw(){
        if(isEmpty()){
            throw new IllegalStateException("The deck is empty");
        }
        return cards.removeFirst();
    }

    public Card peekTop(){
        if(isEmpty()){
            throw new IllegalStateException("The deck is empty");
        }
        return cards.peekFirst();
    }

    public boolean isEmpty(){
        return cards.isEmpty();
    }

    public int size(){
        return cards.size();
    }

    public List<Card> getCards(){
        return List.copyOf(new ArrayList<>(cards));
    }


}
