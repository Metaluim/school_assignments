package cardlib.core;

import java.io.Serializable;

/**
 * Represents a play from a player. Subclassing
 * this may be useful for loggin and/or persistance.
 */
public class Play implements Serializable, Comparable<Play>
{
    public enum PlayStatus
    {
        VALID("Valid play"),
        INVALID("Invalid play");

        protected String desc;

        private PlayStatus(String desc)
        {
            this.desc = desc;
        }

        @Override
        public String toString()
        {
            return this.desc;
        }
    }

    protected Card cardPlayed; /* the card of this play */
    protected PlayerDelegate sourcePlayer; /* the player which played it */

    /**
     * Constructs a play with a given card from
     * a given player.
     *
     * @param cardPlayed the card of this play
     * @param sourcePlayer the player which played it
     */
    public Play(Card cardPlayed, PlayerDelegate sourcePlayer)
    {
        this.cardPlayed = cardPlayed;
        this.sourcePlayer = sourcePlayer;
    }

    public Card getCardPlayed()
    {
        return this.cardPlayed;
    }

    public PlayerDelegate getSourcePlayer()
    {
        return this.sourcePlayer;
    }

    @Override
    public int compareTo(Play play)
    {
        int
        n1 = play.getCardPlayed().compareTo(this.getCardPlayed()),
        n2 = play.getSourcePlayer().compareTo(this.getSourcePlayer());

        if (n1 == 0 && n2 == 0)
            return 0;
        else if (n1 < 0 || n2 < 0)
            return -1;
        else
            return 1;
    }

    @Override
    public String toString()
    {
        return "Card: " + this.getCardPlayed() + ", Player: " +
                this.getSourcePlayer();
    }
}
