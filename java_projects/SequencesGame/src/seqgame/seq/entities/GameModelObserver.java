package seq.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an observer which is informed of updates in
 * the game model.
 */
public interface GameModelObserver extends Serializable
{
    public void turnJustEnded(SeqPlay play);

    public void cardDealt(SeqCard card);

    public void roundHasBegun(List<SeqDeck> seqDecks);

    public void roundHasEnded(SeqPlay.Outcomes outcome);

    public void sequenceFinished(SeqDeck seqDeck);
}
