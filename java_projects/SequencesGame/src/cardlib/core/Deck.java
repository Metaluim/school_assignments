package cardlib.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * Represents a Deck entity, in any gameplay mechanics context.
 */
public abstract class Deck implements Serializable
{
    protected Collection<? extends Card> cards; /* a container of cards */
    
    /**
     * Constructs a deck entity, which encapsulates
     * a given collection of cards.
     * 
     * @param cards a collection of cards
     */
    public Deck(Collection<? extends Card> cards)
    {
        this.cards = cards;
    }

    /**
     * Shuffles the deck of cards.
     */
    public abstract void shuffle();

    /**
     * Obtains the generic collection which aggregates the cards in a deck
     * @return the collection of the cards in the deck
     */
    public Collection<? extends Card> getCards()
    {
        return this.cards;
    }
}
