package seq.entities;

import cardlib.core.Mediator;
import cardlib.core.Play;

/**
 * Represents a Mediator in a sequence game.
 */
public class SeqMediator implements Mediator
{
    private static final SeqMediator instance = new SeqMediator();

    /**
     * Obtains the singleton instance of a sequence
     * game mediator
     *
     * @return the singleton instance
     */
    public static SeqMediator instance()
    {
        return instance;
    }

    public Play.PlayStatus evalPlay(Play play)
    {
        SeqPlay p = (SeqPlay) play;
        // if the card that is played, has the value of the next value
        // of the sequence, then it is a valid play.

        if (!p.getDestDeck().isAuxiliar())
        {
            if ((((SeqCard) p.getCardPlayed()).value()+1) == p.getDestDeck().next())
            {                
                p.setPlayState(Play.PlayStatus.VALID);
                return p.getPlayState();
            }
        }
        else
        {
            if (!((SeqDeck) p.getCardPlayed().getDeck()).isAuxiliar() ||
                    ((SeqDeck) p.getCardPlayed().getDeck()).getOrder() == p.getDestDeck().getOrder())
            {
                p.setPlayState(Play.PlayStatus.VALID);
                return p.getPlayState();
            }

            p.setPlayState(Play.PlayStatus.INVALID);
            return p.getPlayState();
        }

        p.setPlayState(Play.PlayStatus.INVALID);
        return p.getPlayState();
    }
}