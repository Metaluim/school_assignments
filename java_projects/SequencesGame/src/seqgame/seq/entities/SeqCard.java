package seq.entities;

import cardlib.core.Card;
import java.io.Serializable;

/**
 * Implements the logic of a card, in the
 * sequence game.
 */
public class SeqCard extends Card implements Serializable
{
    public enum Colors
    {
        BLUE("Azul"),
        YELLOW("Amarelo"),
        RED("Vermelho"),
        GREEN("Verde");

        private String desc;

        private Colors(String desc)
        {
            this.desc = desc;
        }

        @Override
        public String toString()
        {
            return this.desc.toUpperCase();
        }
    }

    public enum Values
    {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;
    }

    private Colors color; /* the color of this card */

    /**
     * Creates a card with a given value and color.
     *
     * @param value the value
     * @param color the color
     */
    public SeqCard(Values value, Colors color, SeqDeck deck)
    {
        super(value);
        setColor(color);
        super.setDeck(deck);
    }

    /**
     * Convenience method for obtaining the value.
     *
     * @return the value
     */
    public int value()
    {
        return super.getValue().ordinal();
    }

    public Colors getColor()
    {
        return this.color;
    }
    
    public void setColor(Colors color)
    {
        this.color = color;
    }

    public int compareTo(Card t)
    {
        if (t instanceof SeqCard)
        {
            if (((SeqCard) t).value() < this.value())
                return 1;
            else if (((SeqCard) t).value() == this.value())
                return 0;
        }

        return -1;
    }

    @Override
    public String toString()
    {
        return "Value: " + this.value() + ", Color: " + this.getColor();
    }
}
