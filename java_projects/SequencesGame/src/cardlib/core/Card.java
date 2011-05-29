package cardlib.core;

import java.io.Serializable;

/**
 * Represents a Card entity, in any gameplay mechanics context.
 */
public abstract class Card implements Serializable, Comparable<Card>
{    
    public enum Suits
    {
        HEARTS("Hearts"),
        SPADES("Spades"),
        DIAMONDS("Diamonds"),
        CLUBS("Clubs");

        private String desc;

        private Suits(String desc)
        {
            this.desc = desc;
        }

        @Override
        public String toString()
        {
            return this.desc;
        }
    }

    protected Enum<?> value; /* an enumerator constant of a given enum type */
    protected Suits suit; /* represents a card suit */
    protected Deck deck; /* the deck which it belongs to */

    /**
     * Creates a standard Card entity, associated
     * with a given enum constant of a given enum type
     * and a given suit.
     *
     * @param value an enum constant of a given enum type
     * @param suit a card suit
     */
    public Card(Enum<?> value, Suits suit)
    {
        this.value = value;
        this.suit = suit;
    }

    /**
     * Creates a Card entity with a given enum constant
     * of a given enum type.
     *
     * @param value an enum constant of a given enum type
     */
    public Card(Enum<?> value)
    {
        this(value, null);
    }

    /**
     * Creates a Card entity with a given suit.
     *
     * @param suit a card suit
     */
    public Card(Suits suit)
    {
        this(null, suit);
    }

    public Enum<?> getValue()
    {
        return this.value;
    }

    public Suits getSuit()
    {
        return this.suit;
    }

    public Deck getDeck()
    {
        return this.deck;
    }

    public Deck setDeck(Deck d)
    {
        return this.deck = d;
    }

    @Override
    public String toString()
    {
        return "Value " + getValue() + ", Suit: " + getSuit();
    }
}
