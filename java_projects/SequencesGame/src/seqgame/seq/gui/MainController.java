package seq.gui;

import cardlib.core.Play;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import seq.entities.GameModel;
import seq.entities.SeqCard;
import seq.entities.SeqDeck;
import seq.entities.SeqMediator;
import seq.entities.SeqPlay;
import seq.entities.Utils;

/**
 *
 */
public class MainController extends BaseController
{
    private int numSeqs;
    private int maxCardsPerSeq;
    private SeqDeck initDeck;

    private static final String FILE_EXTENSION = ".dat";

    private final FileFilter seqDataFileFilter = new FileFilter()
    {
        @Override
        public boolean accept(File file)
        {
            return file.getName().endsWith(FILE_EXTENSION);
        }

        @Override
        public String getDescription()
        {
            return "Sequence game data file";
        }
    };

    private final MouseListener deckDealHandler = new MouseListener()
    {
        public void mouseClicked(MouseEvent me)
        {
            // deals a card...
            if (me.getSource() instanceof JLabel)
            {
                if (model.getCardOnTable() == null)
                    model.setCardOnTable(model.getMainDeck().dealCard());
            }
        }

        public void mousePressed(MouseEvent me) {}

        public void mouseReleased(MouseEvent me) {}

        public void mouseEntered(MouseEvent me) {}

        public void mouseExited(MouseEvent me) {}
    };

    private final MouseListener cardClickedHandler = new MouseListener()
    {
        private Container container;
        private Point originalLocation;

        public void mouseClicked(MouseEvent me) {}

        public void mousePressed(MouseEvent me)
        {
            if (me.getSource() instanceof JLabel)
            {
                MainView mainView = (MainView) view;
                JLabel label = (JLabel) me.getSource();

                container = label.getParent();

                originalLocation = label.getLocation();

                label.setLocation((label.getX() - (label.getWidth()/2)) + me.getX(),
                        (label.getY() - (label.getHeight()/2) + me.getY()));

                container.remove(label);
                container.invalidate();
                container.validate();

                mainView.getTopLayer().remove(mainView.getFooterFrame());
                mainView.getTopLayer().add(label, JLayeredPane.DRAG_LAYER);
            }
        }

        public void mouseReleased(MouseEvent me)
        {
            if (me.getSource() instanceof JLabel)
            {
                System.out.println("Mouse released...");

                MainView mainView = (MainView) view;
                JLabel label = (JLabel) me.getSource();

                mainView.getTopLayer().remove(label);
                mainView.getTopLayer().setLayer(label, JLayeredPane.DEFAULT_LAYER);
                
                Component comp = SwingUtilities.getDeepestComponentAt(mainView.getMainComponent(),
                        label.getX()+(label.getWidth()/2), label.getY()+(label.getHeight()/2));
                
                boolean modifyZOrder = false;

                SeqCard cardPlayed = obtainCardBySprite(label);
                if (comp != null && comp != mainView.getDeckSpr())
                {
                    if (comp instanceof JLabel)
                    {
                        JLabel dest = (JLabel) comp;
                        
                        SeqCard destCard = obtainCardBySprite(dest);

                        if (destCard != null)
                        {
                            if (destCard.getDeck() != null && cardPlayed != null && !((SeqDeck) destCard.getDeck()).isComplete())
                            {
                                SeqPlay play = new SeqPlay(cardPlayed, null,
                                        (SeqDeck) destCard.getDeck(), null);
                                SeqMediator.instance().evalPlay(play);

                                if (play.getPlayState().equals(Play.PlayStatus.INVALID))
                                {
                                    // return card to original deck
                                    JOptionPane.showMessageDialog(mainView.getMainComponent(), "Jogada inválida!");
                                }
                                else
                                {
                                    label.setVisible(false);
                                    model.update(play);

                                    // force to maintain z order if the card
                                    // comes from an auxiliar stack and goes
                                    // to the same stack
                                    if (play.getSrcDeck().isAuxiliar() && ((SeqDeck) destCard.getDeck()).isAuxiliar())
                                        cardPlayed = null;
                                }
                            }
                        }
                    }

                    if (cardPlayed != null)
                        modifyZOrder = ((SeqDeck) cardPlayed.getDeck()).isAuxiliar() && container != mainView.getLeftMiddleFrame();
                }

                mainView.getTopLayer().add(mainView.getFooterFrame());
                container.add(label);
                if (modifyZOrder) container.setComponentZOrder(label, 0);
                container.invalidate();
                container.validate();
                label.setLocation(originalLocation);

                mainView.getTopLayer().repaint();
                mainView.getMainComponent().validate();
            }
        }

        public void mouseEntered(MouseEvent me) {}

        public void mouseExited(MouseEvent me) {}
        
    };

    private final MouseMotionListener cardMoveHandler = new MouseMotionListener()
    {
        public void mouseDragged(MouseEvent me)
        {
            if (me.getSource() instanceof JLabel)
            {
                JLabel label = (JLabel) me.getSource();

                int
                x = (label.getX()-(label.getWidth()/2)) + me.getX(),
                y = (label.getY()-(label.getHeight()/2)) + me.getY();
                
                label.setBounds(x, y, label.getWidth(), label.getHeight());
            }
        }

        public void mouseMoved(MouseEvent me) {}
    };

    private final ActionListener newGameCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            // novo jogo
            if (JOptionPane.showConfirmDialog(null, "O progresso actual no jogo será perdido\n" +
                    "Tem a certeza que pretende começar um novo jogo?", "Novo jogo", JOptionPane.YES_NO_OPTION) == 0)
                restart(false);
        }
    };

    private final ActionListener saveGameCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            // gravar jogo...
            JFileChooser fc = new JFileChooser("./");
            fc.setFileFilter(seqDataFileFilter);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    save(fc.getSelectedFile().getName());
                }
                catch (Exception ex)
                {
                    Utils.logStackTrace(ex);
                    JOptionPane.showMessageDialog(null, "Erro ao gravar o jogo:\n" + ex.getLocalizedMessage());
                }
            }
        }
    };

    private final ActionListener loadGameCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            // carregar jogo...
            JFileChooser fc = new JFileChooser("./");
            fc.setFileFilter(seqDataFileFilter);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fc.showOpenDialog(view.mainComponent) == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    load(fc.getSelectedFile().getName());
                }
                catch (Exception ex)
                {
                    Utils.logStackTrace(ex);
                    JOptionPane.showMessageDialog(null, "Erro ao carregar o jogo:\n" + ex.getLocalizedMessage());
                }
            }
        }
    };

    private final ActionListener exitGameCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            exitGame();
        }
    };

    private final ActionListener viewPlaysCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            JFrame frm = new JFrame();

            JList list = new JList(model.getPlayStack());
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            list.setVisibleRowCount(-1);
            list.setLayoutOrientation(JList.VERTICAL_WRAP);

            JScrollPane scroller = new JScrollPane(list);

            frm.add(scroller, BorderLayout.CENTER);
            frm.setSize(320, 240);
            frm.setResizable(false);
            frm.setAlwaysOnTop(true);
            frm.setVisible(true);
        }
    };

    private final ActionListener aboutCallBack = new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            JOptionPane.showMessageDialog(null, "Jogo da sequência.\nPPROG - 2011");
        }
    };

    private final WindowListener windowCloseCallBack = new WindowListener() {

        public void windowOpened(WindowEvent we)
        {
            JOptionPane.showMessageDialog(null, "Bem vindo ao jogo das sequências!");
        }

        public void windowClosing(WindowEvent we)
        {
            exitGame();
        }

        public void windowClosed(WindowEvent we)
        {}

        public void windowIconified(WindowEvent we)
        {}

        public void windowDeiconified(WindowEvent we)
        {}

        public void windowActivated(WindowEvent we)
        {}

        public void windowDeactivated(WindowEvent we)
        {}
    };

    private void exitGame()
    {
        if (JOptionPane.showConfirmDialog(null, "Tem a certeza que quer sair?",
                    "Sair do jogo", JOptionPane.YES_NO_OPTION) == 0)
            destroy();
    }

    private void launchGameSettings()
    {
        GameSettingsView settingsView = new GameSettingsView();
        this.numSeqs = settingsView.getNumSeqs();
        this.maxCardsPerSeq = settingsView.getMaxCardsPerSeq();
    }

    public MainController(int width, int height)
    {
        
        launchGameSettings();
        //this.numSeqs = numSeqs;
        //this.maxCardsPerSeq = maxCardsPerSequence;

        super.model = Utils.buildDefaultModel(this.numSeqs, this.maxCardsPerSeq);

        JFrame frame = new JFrame("janela");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JFrame.setDefaultLookAndFeelDecorated(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        frame.setLocation((toolkit.getScreenSize().width/2)-(width/2), (toolkit.getScreenSize().height/2)-(height/2));
        frame.addWindowListener(windowCloseCallBack);

        super.view = new MainView(frame, this, this.numSeqs);
    }

    private void initRound()
    {
        super.model.beginRound();
        initDeck = null;
        initDeck = new SeqDeck((Stack<SeqCard>) super.model.getMainDeck().getCards());
    }

    @Override
    public void restart(boolean repeatGame)
    {
        launchGameSettings();
        super.model = Utils.buildDefaultModel(numSeqs, maxCardsPerSeq);
        ((MainView) view).setNumSeqs(numSeqs);
        super.view.restart();
        super.model.registerObserver((MainView) super.view);
        initRound();
    }

    private void save(String fileName) throws Exception
    {
        ObjectOutputStream output = null;
        try
        {
            output = new ObjectOutputStream(new FileOutputStream(fileName));

            // HACK: menu bar can't be serialized on Mac OS X!!!!
            // we must erase it before serializing
            view.mainComponent.setJMenuBar(null);

            output.writeObject(super.model);
            output.writeLong(((MainView) view).getElapsedTime());
            output.writeInt(numSeqs);
            output.writeInt(maxCardsPerSeq);
            output.writeObject(this.initDeck);

            ((MainView) view).buildMenu();
        }
        finally
        {
            output.close();
        }
    }

    private void load(String fileName) throws Exception
    {
        ObjectInputStream input = null;
        try
        {
            super.model.release();
            super.view.release();
            this.initDeck = null;

            input = new ObjectInputStream(new FileInputStream(fileName));
            super.model = (GameModel) input.readObject();
            long dt = input.readLong();
            this.numSeqs = input.readInt();
            this.maxCardsPerSeq = input.readInt();
            this.initDeck = (SeqDeck) input.readObject();

            ((MainView) view).setNumSeqs(numSeqs);
            ((MainView) view).cacheSprites();
            ((MainView) view).reloadAllListeners(super.model.getCardOnTable());
            ((MainView) view).reloadSprites(super.model.getSeqDecks(), super.model.getAuxDecks(), super.model.getCardOnTable(),
                    super.model.getPoints(), super.model.getPlayStack().size(), dt);
            super.model.registerObserver(((MainView) super.view));
            view.mainComponent.setVisible(true);            
        }
        catch (ClassNotFoundException classNotFoundEx)
        {
            Utils.logStackTrace(classNotFoundEx);
            throw new Exception("Bad file");
        }
        finally
        {
            input.close();
        }
    }

    @Override
    public void launch()
    {
        super.view.init();
        super.model.registerObserver((MainView) super.view);
        initRound();
    }

    @Override
    public void destroy()
    {
        super.view.release();
        super.model.release();
        System.exit(0);
    }

    private SeqCard obtainCardBySprite(JLabel spr)
    {
        Set<Entry<String, BufferedImage>> entries = ((MainView) view).getCachedImages().entrySet();

        String key = null;

        if (spr.getIcon() == null) return null;

        for (Entry<String, BufferedImage> entry : entries)
        {
            BufferedImage tmpImg = entry.getValue();
            BufferedImage tmp = (BufferedImage) ((ImageIcon) spr.getIcon()).getImage();

            if (tmpImg.equals(tmp))
                key = entry.getKey();
        }

        if (key == null) return null;

        MainView mainView = (MainView) super.view;
        if (key.equalsIgnoreCase(MainView.EMPTY_IMAGE))
        {
            int i = 0;
            for (Stack<JLabel> auxStack : mainView.getAuxStacks())
            {
                for (JLabel s : auxStack)
                {
                    if (s == spr)
                        return new SeqCard(SeqCard.Values.ONE, SeqCard.Colors.BLUE, model.getAuxDecks().get(i));
                }

                i++;
            }
        }

        String tokens[] = key.split("_");
        String color = tokens[0].toUpperCase();
        int value = Integer.parseInt(tokens[1].substring(0, tokens[1].indexOf(".")));

        // see if it's the card on table
        if (Utils.compareCardState(model.getCardOnTable(), color, value))
            return model.getCardOnTable();
        
        // search the decks
        // TODO: this could be much improved...
        for (SeqCard c : (Stack<SeqCard>) model.getMainDeck().getCards())
        {
            if (Utils.compareCardState(c, color, value))
                return c;
        }

        for (SeqDeck d : model.getSeqDecks())
        {
            for (SeqCard c : (Stack<SeqCard>) d.getCards())
                if (Utils.compareCardState(c, color, value))
                    return c;
        }

        for (SeqDeck d : model.getAuxDecks())
        {
            for (SeqCard c : (Stack<SeqCard>) d.getCards())
                if (Utils.compareCardState(c, color, value))
                    return c;
        }

        return null;
    }

    public MouseListener getDeckDealHandler()
    {
        return this.deckDealHandler;
    }

    public MouseMotionListener getCardMoveHandler()
    {
        return this.cardMoveHandler;
    }

    public MouseListener getCardClickedHandler()
    {
        return this.cardClickedHandler;
    }

    public ActionListener getNewGameCallBack()
    {
        return this.newGameCallBack;
    }

    public ActionListener getSaveGameCallBack()
    {
        return this.saveGameCallBack;
    }

    public ActionListener getLoadGameCallBack()
    {
        return this.loadGameCallBack;
    }

    public ActionListener getExitGameCallBack()
    {
        return this.exitGameCallBack;
    }

    public ActionListener getViewPlaysCallBack()
    {
        return this.viewPlaysCallBack;
    }

    public ActionListener getAboutCallBack()
    {
        return this.aboutCallBack;
    }
}
