package seq.entities;

import cardlib.core.Deck;
import java.io.Serializable;
import java.util.Collections;
import java.util.Stack;

/**
 * Represents a deck in a Sequence game.
 */
public class SeqDeck extends Deck implements Serializable
{
    private int max; /* maximum amount of cards per sequence */
    private int order; /* the order of the sequence, i.e. starting number */
    private int curr; /* the current value of the sequene */
    private boolean auxiliar = false;
    private boolean complete = false;

    /**
     * Constructs a deck with a stack of
     * SeqCards.
     *
     * @param cards a stack of SeqCards
     */
    public SeqDeck(Stack<SeqCard> cards)
    {
        super(cards);
    }

    /**
     * Constructs a deck with a stack of
     * SeqCards, order and maximum number of cards
     * per sequence.
     *
     * @param cards a stack of SeqCards
     * @param order the order of the sequence of this deck
     * @param max the max amount of cards in a sequence
     */
    public SeqDeck(Stack<SeqCard> cards, int order, int max)
    {
        this(cards);
        this.order = order;
        this.max = max;
        this.curr = order;
    }

    public SeqDeck(Stack<SeqCard> cards, int order, boolean auxiliar)
    {
        this(cards);
        this.order = order;
        this.auxiliar = auxiliar;
    }

    /**
     * Convenience method for dealing a card.
     *
     * @return the card on the top of the stack or null if it is empty
     */
    public SeqCard dealCard()
    {
        Stack<SeqCard> stack = (Stack<SeqCard>) super.getCards();

        if (!stack.empty())
        {
            return stack.pop();
        }

        return null;
    }

    public void clear()
    {
        getCards().clear();
    }

    public int getMax()
    {
        return this.max;
    }

    public int getOrder()
    {
        return this.order;
    }

    public int getCurr()
    {
        return this.curr;
    }

    public void setCurr(int curr)
    {
        this.curr = curr;
    }

    public boolean isAuxiliar()
    {
        return this.auxiliar;
    }

    public void isComplete(boolean b)
    {
        this.complete = b;
    }

    public boolean isComplete()
    {
        return this.complete;
    }

    /**
     * Calculates the next value for the
     * sequence.
     *
     * @return the next value of the sequence
     */
    public int next()
    {
        int r = (getCurr() + getOrder()) % getMax();
        return r == 0 ? getMax() : r;
    }

    public void shuffle()
    {
        Collections.shuffle((Stack<?>) this.cards);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Stack<SeqCard> stack = (Stack<SeqCard>) super.getCards();

        for (SeqCard c : stack)
        {
            sb.append(c).append("\n");
        }

        return sb.toString();
    }
}
