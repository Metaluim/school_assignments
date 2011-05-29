package seq.entities;

import cardlib.core.Play;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Encapsulates the game state and updates it.
 */
public class GameModel implements Serializable
{
    private SeqDeck mainDeck;
    private List<SeqDeck> seqDecks;
    private List<SeqDeck> auxDecks;
    private Stack<SeqPlay> playStack;
    transient private List<GameModelObserver> observerList;
    private SeqCard cardOnTable;
    private int points;

    private static final int POINTS_VALID_PLAY = 10;
    private static final int POINTS_SEQ_COMPLETED = POINTS_VALID_PLAY * 2;
    private static final int POINTS_WON = POINTS_SEQ_COMPLETED * 3;

    public GameModel(SeqDeck mainDeck, List<SeqDeck> seqDecks,
            List<SeqDeck> auxDecks)
    {
        this.mainDeck = mainDeck;
        this.seqDecks = seqDecks;
        this.auxDecks = auxDecks;

        this.playStack = new Stack<SeqPlay>();
    }

    public void registerObserver(GameModelObserver observer)
    {
        getObservers().add(observer);
        //this.observer = observer;
    }

    public SeqDeck getMainDeck()
    {
        return this.mainDeck;
    }

    public List<SeqDeck> getSeqDecks()
    {
        return this.seqDecks;
    }

    public List<SeqDeck> getAuxDecks()
    {
        return this.auxDecks;
    }

    public Stack<SeqPlay> getPlayStack()
    {
        return this.playStack;
    }

    public SeqCard getCardOnTable()
    {
        return this.cardOnTable;
    }

    public List<GameModelObserver> getObservers()
    {
        if (this.observerList == null)
            this.observerList = new ArrayList<GameModelObserver>();

        return this.observerList;
        //return this.observer;
    }

    public int getPoints()
    {
        return this.points;
    }

    public void setCardOnTable(SeqCard card)
    {
        for (GameModelObserver observer : getObservers())
        {
            if (observer != null)
                observer.cardDealt(card);
        }        
        
        this.cardOnTable = card;
    }

    private void clearSeqDeck(List<SeqDeck> seqDeck)
    {
        for (SeqDeck d : seqDecks)
        {
            d.clear();
        }
    }

    public void restart()
    {
        clearSeqDeck(seqDecks);
        clearSeqDeck(auxDecks);
        playStack.clear();
        cardOnTable = null;
        //mainDeck = Utils.buildMainDeck();
    }

    public void update(SeqPlay p)
    {
        playStack.push(p);

        // update the decks
        if (p.getPlayState().equals(Play.PlayStatus.VALID))
        {
            Stack<SeqCard> deckStack = null;

            if (!p.getDestDeck().isAuxiliar())
            {
                deckStack = (Stack<SeqCard>) seqDecks.get(p.getDestDeck().getOrder()-1).getCards();

                if (p.getDestDeck().getMax() == ((SeqCard) p.getCardPlayed()).value()+1)
                {
                    p.setPoints(p.getPoints() + POINTS_SEQ_COMPLETED);
                    p.getDestDeck().isComplete(true);

                    for (GameModelObserver observer : getObservers())
                    {
                        if (observer != null)
                            observer.sequenceFinished(p.getDestDeck());
                    }
                    
                }
                else
                {
                    p.setPoints(p.getPoints() + POINTS_VALID_PLAY);
                    p.getDestDeck().setCurr(p.getDestDeck().next());
                }
            }
            else
            {
                deckStack = (Stack<SeqCard>) auxDecks.get(p.getDestDeck().getOrder()-1).getCards();
            }

            deckStack.add((SeqCard) p.getCardPlayed());
            ((SeqCard) p.getCardPlayed()).getDeck().getCards().remove((SeqCard) p.getCardPlayed());
            ((SeqCard) deckStack.lastElement()).setDeck(p.getDestDeck());

        }

        if (p.getCardPlayed() == cardOnTable)
            this.cardOnTable = null;

        for (GameModelObserver observer : getObservers())
        {
            if (observer != null)
            {
                observer.turnJustEnded(p);

                SeqPlay.Outcomes outcome = canPlayNextTurn();
                if (outcome != null)
                {
                    if (outcome == SeqPlay.Outcomes.VICTORY)
                        p.setPoints(p.getPoints() + POINTS_WON);

                    observer.roundHasEnded(outcome);
                }
            }
        }

        points += p.getPoints();
    }

    private SeqPlay.Outcomes canPlayNextTurn()
    {
        if (mainDeck.getCards().size() > 0) return null;

        int cnt = 0;
        for (SeqDeck d : seqDecks)
        {
            if (d.isComplete())
                cnt++;
        }

        if (cnt < seqDecks.size())
        {
            // if the top of the aux stacks can fullfill any
            // of the uncompleted sequence, then a next turn is possible

            int possible = 0;

            if (cardOnTable != null)
            {
                for (int i = 0; i < seqDecks.size(); i++)
                {
                    SeqDeck seqDeck = seqDecks.get(i);
                    if (SeqMediator.instance().evalPlay(Utils.makeDummyPlay(cardOnTable, seqDeck)).equals(Play.PlayStatus.VALID))
                    {
                        possible++;
                        i = seqDecks.size();
                    }
                }
            }

            for (SeqDeck auxDeck : auxDecks)
            {
                Stack<SeqCard> cardStack = (Stack<SeqCard>) auxDeck.getCards();
                if (!cardStack.isEmpty())
                {
                    SeqCard auxCard = cardStack.peek();
                    for (int i = 0; i < seqDecks.size(); i++)
                    {
                        SeqDeck seqDeck = seqDecks.get(i);
                        if (SeqMediator.instance().evalPlay(Utils.makeDummyPlay(auxCard, seqDeck)).equals(Play.PlayStatus.VALID))
                        {
                            possible++;
                            i = seqDecks.size();
                        }
                    }
                }
            }

            return (possible < (seqDecks.size() - cnt)) ? SeqPlay.Outcomes.DEFEAT : null;
        }

        return SeqPlay.Outcomes.VICTORY;
    }

    public void release()
    {
        // ...
    }

    public void beginRound()
    {
        this.mainDeck.shuffle();

        Stack<SeqCard> cards = (Stack<SeqCard>) this.mainDeck.getCards();

        // deal the first three cards
        for (int cnt = 0; cnt < seqDecks.size(); cnt++)
        {
            for (int i = 0; i < cards.size(); i++)
            {
                SeqCard c = cards.get(i);

                if (c.value() == cnt)
                {
                    this.mainDeck.getCards().remove(c);
                    ((Stack<SeqCard>) this.seqDecks.get(cnt).getCards()).push(c);
                    c.setDeck(this.seqDecks.get(cnt));

                    i = cards.size();
                }
            }
        }
        
        for (GameModelObserver observer : getObservers())
        {
            if (observer != null)
                 observer.roundHasBegun(seqDecks);
        }
    }
}
