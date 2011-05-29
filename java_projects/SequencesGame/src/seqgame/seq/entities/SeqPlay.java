package seq.entities;

import cardlib.core.Play;
import cardlib.core.PlayerDelegate;
import java.io.Serializable;

/**
 * Encapsulates play data on the sequence game.
 */
public class SeqPlay extends Play implements Serializable
{
    private SeqDeck destDeck; /* the destination deck of the card */
    private SeqDeck srcDeck;
    private Play.PlayStatus playState; /* the state of the play */
    private int points;

    public enum Outcomes
    {
        VICTORY,
        DEFEAT
    }

    /**
     * Constructs a play with the card played, the player
     * who played it and the destination deck of the card.
     *
     * @param cardPlayed the card played
     * @param sourcePlayer the player who played it
     * @param destDeck the destination deck
     */
    public SeqPlay(SeqCard cardPlayed, PlayerDelegate sourcePlayer,
            SeqDeck destDeck, Play.PlayStatus playState)
    {
        super(cardPlayed, sourcePlayer);
        this.destDeck = destDeck;
        this.srcDeck = (SeqDeck) cardPlayed.getDeck();
        this.points = 0;
    }

    public SeqDeck getDestDeck()
    {
        return this.destDeck;
    }

    public Play.PlayStatus getPlayState()
    {
        return this.playState;
    }

    public SeqDeck getSrcDeck()
    {
        return this.srcDeck;
    }

    public int getPoints() {
        return points;
    }

    public void setPlayState(Play.PlayStatus state)
    {
        this.playState = state;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString()
    {
        return super.toString() + "\nDeck: \n" + this.getDestDeck() +
                "Play state: " + this.getPlayState() + "\n";
    }
}
