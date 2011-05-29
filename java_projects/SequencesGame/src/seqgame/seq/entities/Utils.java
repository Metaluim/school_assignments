package seq.entities;

import cardlib.core.Play.PlayStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 */
public class Utils
{
    public static SeqDeck buildMainDeck(int numSeqs, int max)
    {
        Stack<SeqCard> stack = new Stack<SeqCard>();
        SeqDeck d = new SeqDeck(stack);

        for (int i = 0; i < numSeqs; i++)
        {
            SeqCard.Colors color = SeqCard.Colors.values()[i];
            for (int j = 0; j < max; j++)
            {
                SeqCard.Values value = SeqCard.Values.values()[j];
                stack.push(new SeqCard(value, color, d));
            }
        }

        return d;
    }

    public static GameModel buildDefaultModel(int numSeqs, int max)
    {
        SeqDeck mainDeck = buildMainDeck(numSeqs, max);
        
        List<SeqDeck> seqDecks = new ArrayList<SeqDeck>(numSeqs);
        List<SeqDeck> auxDecks = new ArrayList<SeqDeck>(numSeqs);

        for (int i = 0; i < numSeqs; i++)
        {
            seqDecks.add(new SeqDeck(new Stack<SeqCard>(), i+1, max));
            auxDecks.add(new SeqDeck(new Stack<SeqCard>(), i+1, true));
        }

        return new GameModel(mainDeck, seqDecks, auxDecks);
    }

    public static JMenuItem buildMenuItem(String desc, int mnemonic)
    {
        JMenuItem item = new JMenuItem(desc);
        item.setMnemonic(mnemonic);
        return item;
    }

    public static JMenuItem buildMenuItem(String desc, int mnemonic, KeyStroke accelarator)
    {
        JMenuItem item = buildMenuItem(desc, mnemonic);
        item.setAccelerator(accelarator);
        return item;
    }

    public static SeqPlay makeDummyPlay(SeqCard cardPlayed, SeqDeck destDeck)
    {
        return new SeqPlay(cardPlayed, null, destDeck, PlayStatus.INVALID);
    }

    public static boolean compareCardState(SeqCard c, String color, int value)
    {
        return c == null ? false : c.getColor().toString().equals(color) &&
                c.value()+1 == value;
    }

    public static void log(String s, Level level)
    {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(new LogRecord(level, s));
    }

    public static void logStackTrace(Throwable t)
    {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] stackTrace = t.getStackTrace();
        for (StackTraceElement trace : stackTrace)
        {
            sb.append(trace.toString()).append('\n');
        }

        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(new LogRecord(Level.WARNING, sb.toString()));
    }
}
