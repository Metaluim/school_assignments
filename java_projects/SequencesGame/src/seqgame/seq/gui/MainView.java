package seq.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import seq.entities.GameModelObserver;
import seq.entities.SeqCard;
import seq.entities.SeqDeck;
import seq.entities.SeqPlay;
import seq.entities.Utils;

/**
 *
 */
public class MainView extends BaseView implements GameModelObserver
{
    private JPanel headerFrame;
    private JPanel middleFrame;
    private JPanel leftMiddleFrame;
    private JPanel upperRightMiddleFrame;
    private JPanel downRightMiddleFrame;
    private JPanel footerFrame;
    private JLabel deckSpr;
    private JLabel currCardSpr;
    private List<JLabel> seqSprs;
    private List<JLabel> seqNextLbls;
    private List<Stack<JLabel>> auxStacks;
    private List<JPanel> auxPanels;
    private JLayeredPane topLayer;
    private JLabel timerLbl;
    private JLabel points;
    private JLabel numPlays;

    // MISC
    transient private Map<String, BufferedImage> cachedImages;
    public static final String EMPTY_IMAGE = "empty.png";
    private static final String NEXT_DESC = "Próximo: ";
    private static final String DONE = "Completa";
    private static final String POINTS = "Pontos: ";
    private static final String NUM_PLAYS = "Número de jogadas: ";
    private long time = System.currentTimeMillis();
    private int numSeqs;

    private final Timer timer = new Timer(1000, new ActionListener()
    {
        public void actionPerformed(ActionEvent ae)
        {
            if (timerLbl != null)
            {
                long dt = getElapsedTime();

                int h = (int) (dt / (1000*60*60));
                int r = (int) (dt % 60);

                int m = r / (1000*60);
                r %= 1000*60;

                int s = r / 1000;
                r %= 1000;
                
                timerLbl.setText(h + ":" + m + ":" + s);
            }
        }
    });

    // CONSTS
    private static final int HEADER_H = 0;
    private static final int FOOTER_H = 32;

    public MainView(JFrame mainComponent, MainController controller, int numSeqs)
    {
        super(mainComponent, controller);
        this.numSeqs = numSeqs;
    }

    public void cacheSprites()
    {
        if (cachedImages == null)
            cachedImages = new HashMap<String, BufferedImage>();

        if (!cachedImages.isEmpty())
            cachedImages.clear();

        File[] dir = new File("./sprites").listFiles();

        for (File file : dir)
        {
            try
            {
                cachedImages.put(file.getName(), ImageIO.read(file));
            }
            catch (IOException ex)
            {
                Utils.log(String.format("Error loading image %20s:\n%s", file.getName(), ex.getMessage()),
                        Level.WARNING);
            }
        }
    }

    public void reloadAllListeners(SeqCard cardOnTable)
    {
        MainController mainController = (MainController) super.controller;

        deckSpr.removeMouseListener(mainController.getDeckDealHandler());
        deckSpr.addMouseListener(mainController.getDeckDealHandler());

        removeListeners(currCardSpr);
        if (cardOnTable != null)
        {
            addListeners(currCardSpr);
        }

        for (Stack<JLabel> auxStack : auxStacks)
        {
            removeListeners(auxStack.peek());

            if (hasCardsInAuxStack(auxStack))
            {
                addListeners(auxStack.peek());
            }
        }
        
        buildMenu();

        mainComponent.pack();
        mainComponent.validate();
    }

    private void initMiscResources()
    {
        this.seqSprs = new ArrayList<JLabel>(numSeqs);
        this.seqNextLbls = new ArrayList<JLabel>(numSeqs);
        this.auxStacks = new ArrayList<Stack<JLabel>>();
        this.auxPanels = new ArrayList<JPanel>();

        for (int i = 0; i < numSeqs; i++)
        {
            auxStacks.add(new Stack<JLabel>());
            auxPanels.add(new JPanel());
        }

        // FRAME ALLOCATION
        topLayer = new JLayeredPane();
        headerFrame = new JPanel(new FlowLayout());
        leftMiddleFrame = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 40));
        upperRightMiddleFrame = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 40));
        //upperRightMiddleFrame = new JPanel();
        //upperRightMiddleFrame.setLayout(new GridLayout(2, numSeqs, 20, 2));
        downRightMiddleFrame = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 40));
        //middleFrame = new JPanel(new FlowLayout());
        middleFrame = new JPanel(new BorderLayout());
        footerFrame = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        
        cacheSprites();
    }

    private JPanel composeSeqCard(JLabel card, JLabel seqNextLbl)
    {
        JPanel panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        BorderLayout layout = new BorderLayout();
        layout.setHgap(8);
        panel.setLayout(layout);
        panel.add(card, BorderLayout.CENTER);
        panel.add(seqNextLbl, BorderLayout.SOUTH);
        return panel;
    }

    private void composeView()
    {
        deckSpr = new JLabel(new ImageIcon(obtainCardSprite("costas.png")));
        getDeckSpr().addMouseListener(((MainController) super.controller).getDeckDealHandler());

        currCardSpr = new JLabel();
        currCardSpr.setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));

        for (int i = 0; i < numSeqs; i++)
        {
            seqSprs.add(new JLabel());
            seqNextLbls.add(new JLabel());
            auxStacks.get(i).push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
            
            upperRightMiddleFrame.add(composeSeqCard(seqSprs.get(i), seqNextLbls.get(i)));

            auxPanels.get(i).setLayout(new GridLayout(21, 1, 20, -78));
            auxPanels.get(i).add(auxStacks.get(i).peek(), 0);

            downRightMiddleFrame.add(auxPanels.get(i));
        }

        leftMiddleFrame.add(getDeckSpr());
        leftMiddleFrame.add(getCurrCardSpr());
        leftMiddleFrame.setPreferredSize(new Dimension(super.mainComponent.getWidth()/2,
                super.mainComponent.getHeight() - (HEADER_H + FOOTER_H)));
        
        JPanel rightMiddleFrame = new JPanel();
        GridLayout rightMiddleLM = new GridLayout(2, 1);
        rightMiddleLM.setHgap(0);
        rightMiddleFrame.setLayout(rightMiddleLM);
        rightMiddleFrame.add(getUpperRightMiddleFrame());
        rightMiddleFrame.add(getDownRightMiddleFrame());
        rightMiddleFrame.revalidate();
        rightMiddleFrame.setPreferredSize(new Dimension(super.mainComponent.getWidth()/2 + (numSeqs*20)+(numSeqs*deckSpr.getWidth()),
                super.mainComponent.getHeight() - (HEADER_H + FOOTER_H)));

        middleFrame.add(leftMiddleFrame, BorderLayout.WEST);
        //middleFrame.add(new JSeparator());
        middleFrame.add(rightMiddleFrame, BorderLayout.EAST);
        
        points = new JLabel(POINTS + "0");
        timerLbl = new JLabel();
        numPlays = new JLabel(NUM_PLAYS + "0");
        footerFrame.setBackground(Color.WHITE);
        footerFrame.add(timerLbl);
        footerFrame.add(points);
        footerFrame.add(numPlays);
        timer.start();
        footerFrame.validate();

        topLayer.setLayout(new BoxLayout(topLayer, BoxLayout.Y_AXIS));
        topLayer.add(middleFrame, JLayeredPane.DEFAULT_LAYER);
        topLayer.add(footerFrame, JLayeredPane.DEFAULT_LAYER);
        
        super.mainComponent.add(topLayer, BorderLayout.CENTER);
    }

    public void buildMenu()
    {
        super.mainComponent.setJMenuBar(null);

        // MENU
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Ficheiro");
        JMenu viewMenu = new JMenu("Ver");
        JMenu helpMenu = new JMenu("Ajuda");

        fileMenu.setMnemonic(KeyEvent.VK_F);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        helpMenu.setMnemonic(KeyEvent.VK_A);

        MainController mainController = (MainController) super.controller;

        // NEW GAME
        JMenuItem newGameOpt = Utils.buildMenuItem("Novo jogo", KeyEvent.VK_N,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        newGameOpt.addActionListener(mainController.getNewGameCallBack());

        // SAVE GAME
        JMenuItem saveGameOpt = Utils.buildMenuItem("Guardar jogo", KeyEvent.VK_S,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        saveGameOpt.addActionListener(mainController.getSaveGameCallBack());

        // LOAD GAME
        JMenuItem loadGameOpt = Utils.buildMenuItem("Carregar jogo", KeyEvent.VK_A,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
        loadGameOpt.addActionListener(mainController.getLoadGameCallBack());

        // EXIT GAME
        JMenuItem exitGameOpt = Utils.buildMenuItem("Sair do jogo", KeyEvent.VK_J);
        exitGameOpt.addActionListener(mainController.getExitGameCallBack());

        // VIEW PLAYS
        JMenuItem viewPlaysOpt = Utils.buildMenuItem("Ver jogadas", KeyEvent.VK_V);
        viewPlaysOpt.addActionListener(mainController.getViewPlaysCallBack());

        // HELP
        JMenuItem aboutOpt = Utils.buildMenuItem("Sobre", KeyEvent.VK_S);
        aboutOpt.addActionListener(mainController.getAboutCallBack());

        // FILE MENU
        fileMenu.add(newGameOpt);
        fileMenu.add(saveGameOpt);
        fileMenu.add(loadGameOpt);
        fileMenu.add(exitGameOpt);

        // VIEW MENU
        viewMenu.add(viewPlaysOpt);

        // HELP MENU
        helpMenu.add(aboutOpt);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        super.mainComponent.setJMenuBar(menuBar);
    }

    @Override
    public void init()
    {
        initMiscResources();

        composeView();
        buildMenu();

        super.mainComponent.setResizable(true);
        super.mainComponent.setVisible(true);

        middleFrame.revalidate();
        super.mainComponent.pack();
        super.mainComponent.validate();
    }

    @Override
    public void release()
    {
        super.mainComponent.dispose();
    }

    private BufferedImage obtainCardSprite(String name)
    {
        BufferedImage spr = cachedImages.get(name);

        if (spr == null)
        {
            Random rand = new Random(System.currentTimeMillis());
            // TODO: return default not found spr
            spr = new BufferedImage(75, 99, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < spr.getWidth(); i++)
                for (int j = 0; j < spr.getHeight(); j++)
                    spr.setRGB(i, j, rand.nextInt(Integer.MAX_VALUE));

        }

        return spr;
    }

    private ImageIcon obtainCardIcon(SeqCard card)
    {
        return new ImageIcon(obtainCardSprite(obtainCardFilename(card)));
    }

    private static String obtainCardFilename(SeqCard card)
    {
        return card.getColor().toString().toLowerCase() + "_" +
                (card.value()+1) + ".png";
    }

    private JLabel buildAuxSpr(ImageIcon spr, boolean registerListeners)
    {
        JLabel s = new JLabel(spr);

        if (registerListeners)
        {
            addListeners(s);
        }

        return s;
    }

    private void removeListeners(Component comp)
    {
        MainController mainController = (MainController) super.controller;
        comp.removeMouseListener(mainController.getCardClickedHandler());
        comp.removeMouseMotionListener(mainController.getCardMoveHandler());
    }

    private void addListeners(Component comp)
    {
        MainController mainController = (MainController) super.controller;
        comp.addMouseListener(mainController.getCardClickedHandler());
        comp.addMouseMotionListener(mainController.getCardMoveHandler());
    }

    private void addTopAuxStackListeners(Stack<JLabel> auxStack)
    {
        if (!auxStack.isEmpty())
        {
            addListeners(auxStack.peek());
        }
    }

    private void removeTopAuxStackListeners(Stack<JLabel> auxStack)
    {
        if (!auxStack.isEmpty())
        {
            removeListeners(auxStack.peek());
        }
    }

    public void reloadSprites(final List<SeqDeck> seqDecks, final List<SeqDeck> auxDecks, SeqCard cardOnTable,
            int points, int numPlays, long time)
    {

        Thread loadSeqsJob = new Thread()
        {
            @Override
            public void run()
            {
                if (seqSprs.size() >= seqDecks.size())
                {
                    int i = seqSprs.size()-1;

                    while (seqSprs.size() > seqDecks.size())
                    {
                        JPanel parent = (JPanel) seqSprs.get(seqSprs.size()-1).getParent();
                        parent.removeAll();
                        /*upperRightMiddleFrame.remove(seqSprs.get(seqSprs.size()-1));
                        upperRightMiddleFrame.remove(seqNextLbls.get(seqNextLbls.size()-1));*/
                        upperRightMiddleFrame.remove(parent);
                        seqSprs.remove(seqSprs.size()-1);
                        seqNextLbls.remove(seqNextLbls.size()-1);
                        i--;
                    }

                    upperRightMiddleFrame.revalidate();
                }
                else
                {
                    while (seqSprs.size() < seqDecks.size())
                    {
                        seqSprs.add(new JLabel());
                        seqNextLbls.add(new JLabel());

                        upperRightMiddleFrame.add(composeSeqCard(seqSprs.get(seqSprs.size()-1), seqNextLbls.get(seqNextLbls.size()-1)));
                        upperRightMiddleFrame.revalidate();
                    }
                }

                for (int i = 0; i < seqDecks.size(); i++)
                {
                    JLabel seqSpr = seqSprs.get(i);

                    /*if (seqSprs.size() < i)
                    {
                        seqSpr = seqSprs.get(i);
                    }
                    else
                    {
                        seqSprs.add(new JLabel());
                        seqNextLbls.add(new JLabel());

                        upperRightMiddleFrame.add(composeSeqCard(seqSprs.get(i), seqNextLbls.get(i)));
                        upperRightMiddleFrame.revalidate();

                        seqSpr = seqSprs.get(i);
                    }*/

                    SeqDeck seqDeck = seqDecks.get(i);

                    seqSpr.setIcon(obtainCardIcon(((Stack<SeqCard>) seqDeck.getCards()).peek()));

                    if (!seqDeck.isComplete())
                    {
                        seqNextLbls.get(i).setText(NEXT_DESC + seqDeck.next());
                    }
                    else
                    {
                        seqNextLbls.get(i).setText(DONE);
                    }
                }
            }        
        };

        Thread loadAuxStacksJob = new Thread()
        {
            @Override
            public void run()
            {
                if (auxStacks.size() >= auxDecks.size())
                {
                    int i = auxStacks.size()-1;

                    while (auxStacks.size() > auxDecks.size())
                    {
                        auxPanels.get(auxPanels.size()-1).removeAll();
                        downRightMiddleFrame.remove(auxPanels.get(auxPanels.size()-1));
                        auxPanels.remove(auxPanels.size()-1);

                        auxStacks.get(auxStacks.size()-1).clear();
                        auxStacks.remove(auxStacks.size()-1);

                        downRightMiddleFrame.revalidate();

                        i--;
                    }
                }
                else
                {
                    while (auxStacks.size() < auxDecks.size())
                    {
                        auxStacks.add(new Stack<JLabel>());
                        auxStacks.get(auxStacks.size()-1).push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
                        auxPanels.add(new JPanel());
                        auxPanels.get(auxPanels.size()-1).setLayout(new GridLayout(21, 1, 20, -78));
                        auxPanels.get(auxPanels.size()-1).add(auxStacks.get(auxStacks.size()-1).peek());
                        downRightMiddleFrame.add(auxPanels.get(auxPanels.size()-1));
                        downRightMiddleFrame.revalidate();
                    }
                }

                for (int i = 0; i < auxDecks.size(); i++)
                {
                    Stack<JLabel> auxStack = auxStacks.get(i);

                    /*if (auxStacks.size() < i)
                    {
                        auxStack = auxStacks.get(i);
                    }
                    else
                    {
                        auxStacks.add(new Stack<JLabel>());
                        auxStacks.get(i).push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
                        auxPanels.add(new JPanel());
                        auxPanels.get(i).setLayout(new GridLayout(21, 1, 20, -78));
                        auxPanels.get(i).add(auxStacks.get(i).peek());
                        downRightMiddleFrame.add(auxPanels.get(i));
                        downRightMiddleFrame.revalidate();
                        auxStack = auxStacks.get(i);
                    }*/

                    if (!auxDecks.get(i).getCards().isEmpty())
                    {
                        JPanel auxPanel = auxPanels.get(i);
                        auxStack.clear();
                        auxPanel.removeAll();

                        Stack<SeqCard> cardStack = (Stack<SeqCard>) auxDecks.get(i).getCards();
                        for (int j = 0; j < cardStack.size(); j++)
                        {
                            auxStack.add(buildAuxSpr(obtainCardIcon(cardStack.get(j)), false));
                            auxPanel.add(auxStack.peek(), 0);
                        }
                    }
                    else
                    {
                        auxStack.peek().setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));
                    }
                    removeListeners(auxStack.peek());
                    addListeners(auxStack.peek());
                }
            }
        };
        
        loadSeqsJob.start();
        loadAuxStacksJob.start();

        try
        {
            loadSeqsJob.join();
            loadAuxStacksJob.join();
        }
        catch (InterruptedException ex)
        {
            Utils.logStackTrace(ex);
            ((MainController) controller).destroy();
        }

        if (cardOnTable != null)
        {
            currCardSpr.setIcon(obtainCardIcon(cardOnTable));
        }
        else
        {
            currCardSpr.setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));
            removeListeners(currCardSpr);
        }

        this.points.setText(POINTS + points);
        this.numPlays.setText(NUM_PLAYS + numPlays);

        this.time = time;
        timer.restart();
    }

    public void turnJustEnded(SeqPlay play)
    {
        // este método é chamado quando há mudança no estado
        // do jogo, por exemplo, quando é adicionado uma carta à
        // pilha de cartas auxiliar.

        // if the play state is valid, then it entered a sequence stack...

        ImageIcon spr = obtainCardIcon((SeqCard) play.getCardPlayed());
        
        if (!play.getDestDeck().isAuxiliar())
        {
            JLabel seqSpr = seqSprs.get(play.getDestDeck().getOrder()-1);

            seqSpr.setIcon(spr);

            if (!play.getDestDeck().isComplete())
                seqNextLbls.get(play.getDestDeck().getOrder()-1).setText(NEXT_DESC + play.getDestDeck().next());


            if (play.getSrcDeck().isAuxiliar())
            {
                Stack<JLabel> auxStack = auxStacks.get(play.getSrcDeck().getOrder()-1);

                auxStack.pop();

                if (auxStack.isEmpty())
                {
                    JPanel auxPanel = auxPanels.get(play.getSrcDeck().getOrder()-1);

                    auxStack.push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
                    auxPanel.add(auxStack.peek());
                    auxPanel.setComponentZOrder(auxStack.lastElement(), 0);
                }
                else
                {
                    addTopAuxStackListeners(auxStack);
                }
            }
        }
        else
        {
            Stack<JLabel> auxStack = auxStacks.get(play.getDestDeck().getOrder()-1);
            JPanel auxPanel = auxPanels.get(play.getDestDeck().getOrder()-1);

            if (!hasCardsInAuxStack(auxStack))
            {
                auxStack.clear();
                auxPanel.removeAll();
            }

            removeTopAuxStackListeners(auxStack);
            auxStack.push(buildAuxSpr(spr, true));
            auxPanel.add(auxStack.peek());
            auxPanel.setComponentZOrder(auxStack.peek(), 0);
            auxPanel.revalidate();
        }

        String s = points.getText();
        Integer n = Integer.parseInt(s.substring(POINTS.length()));
        points.setText(POINTS + (n + play.getPoints()));

        s = numPlays.getText();
        n = Integer.parseInt(s.substring(NUM_PLAYS.length()));
        numPlays.setText(NUM_PLAYS + (++n));

        downRightMiddleFrame.revalidate();
        downRightMiddleFrame.repaint();

        if (!play.getSrcDeck().isAuxiliar())
        {
            currCardSpr.setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));
            removeListeners(currCardSpr);
        }

        currCardSpr.setVisible(true);
        currCardSpr.invalidate();
        super.mainComponent.validate();
    }

    public void sequenceFinished(SeqDeck seqDeck)
    {
        JLabel seqNextLbl = seqNextLbls.get(seqDeck.getOrder()-1);

        seqNextLbl.setText(DONE);
        seqNextLbl.revalidate();
        seqNextLbl.paintImmediately(seqNextLbl.getVisibleRect());

        upperRightMiddleFrame.revalidate();
        upperRightMiddleFrame.repaint();
        super.mainComponent.validate();
    }

    public void roundHasBegun(List<SeqDeck> seqDecks)
    {
        // este método é chamado sempre que começa um jogo novo.
        for (int i = 0; i < seqDecks.size(); i++)
        {
            Stack<SeqCard> tmpDeck = (Stack<SeqCard>) seqDecks.get(i).getCards();
            seqSprs.get(i).setIcon(obtainCardIcon(tmpDeck.get(0)));
            seqNextLbls.get(i).setText(NEXT_DESC + seqDecks.get(i).next());
        }

        super.mainComponent.validate();
    }

    public void roundHasEnded(SeqPlay.Outcomes outcome)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(outcome.equals(SeqPlay.Outcomes.VICTORY) ? "Ganhastes! \n\t:)" : "Perdestes...\n\t:(")
                .append('\n').append("Pretendes jogar outra vez?");

        int r = JOptionPane.showConfirmDialog(super.mainComponent, sb.toString(), "O jogo acabou", JOptionPane.YES_NO_OPTION);
        
        if (r == JOptionPane.YES_OPTION)
        {
            int r2 = JOptionPane.showConfirmDialog(super.mainComponent, "Pretendes fazer replay do mesmo jogo?",
                    "Replay do jogo", JOptionPane.YES_NO_OPTION);

            super.controller.restart(r2 == JOptionPane.YES_OPTION);
        }
        else
        {
            super.controller.destroy();
        }
    }

    @Override
    public void restart()
    {
        //upperRightMiddleFrame.setLayout(new GridLayout(2, numSeqs, 20, 2));

        if (auxStacks.size() >= numSeqs)
        {
            int i = auxStacks.size()-1;

            while (auxStacks.size() > numSeqs)
            {
                auxPanels.get(auxPanels.size()-1).removeAll();
                downRightMiddleFrame.remove(auxPanels.get(auxPanels.size()-1));
                auxPanels.remove(auxPanels.size()-1);

                auxStacks.get(auxStacks.size()-1).clear();
                auxStacks.remove(auxStacks.size()-1);

                downRightMiddleFrame.revalidate();

                i--;
            }
        }

        if (seqSprs.size() >= numSeqs)
        {
            int i = seqSprs.size()-1;

            while (seqSprs.size() > numSeqs)
            {
                JPanel parent = (JPanel) seqSprs.get(seqSprs.size()-1).getParent();
                parent.removeAll();
                /*upperRightMiddleFrame.remove(seqSprs.get(seqSprs.size()-1));
                upperRightMiddleFrame.remove(seqNextLbls.get(seqNextLbls.size()-1));*/
                upperRightMiddleFrame.remove(parent);
                seqSprs.remove(seqSprs.size()-1);
                seqNextLbls.remove(seqNextLbls.size()-1);
                i--;
            }

            upperRightMiddleFrame.revalidate();
        }

        for (int i = 0; i < numSeqs; i++)
        {
            Stack<JLabel> auxStack;

            if (auxStacks.size() > i)
            {
                auxStack = auxStacks.get(i);
            }
            else
            {
                auxStacks.add(new Stack<JLabel>());
                auxStacks.get(i).push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
                auxPanels.add(new JPanel());
                auxPanels.get(i).setLayout(new GridLayout(21, 1, 20, -78));
                auxPanels.get(i).add(auxStacks.get(i).peek());
                downRightMiddleFrame.add(auxPanels.get(i));
                downRightMiddleFrame.revalidate();
                auxStack = auxStacks.get(i);
            }
            JPanel auxPanel = auxPanels.get(i);

            JLabel seqSpr;
            if (seqSprs.size() > i)
            {
                seqSpr = seqSprs.get(i);
            }
            else
            {
                seqSprs.add(new JLabel());
                seqNextLbls.add(new JLabel());

                upperRightMiddleFrame.add(composeSeqCard(seqSprs.get(i), seqNextLbls.get(i)));
                upperRightMiddleFrame.revalidate();
                
                seqSpr = seqSprs.get(i);
            }

            auxStack.clear();
            auxPanel.removeAll();

            auxStack.push(buildAuxSpr(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)), false));
            auxPanel.add(auxStack.peek());

            seqSpr.setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));
        }

        removeListeners(currCardSpr);
        currCardSpr.setIcon(new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));

        timer.restart();
        points.setText(POINTS + "0");
        numPlays.setText(NUM_PLAYS + "0");

        super.mainComponent.repaint();
    }

    public void cardDealt(SeqCard card)
    {
        // este método é chamado quando o jogador tira uma carta do baralho
        currCardSpr.setIcon(card != null ? obtainCardIcon(card) : new ImageIcon(obtainCardSprite(EMPTY_IMAGE)));
        currCardSpr.addMouseMotionListener(((MainController) super.controller).getCardMoveHandler());
        currCardSpr.addMouseListener(((MainController) super.controller).getCardClickedHandler());

        currCardSpr.setVisible(true);
        currCardSpr.revalidate();
        currCardSpr.repaint();

        super.mainComponent.validate();
    }

    public JPanel getHeaderFrame() {
        return headerFrame;
    }

    public JPanel getMiddleFrame()
    {
        return this.middleFrame;
    }

    public JPanel getLeftMiddleFrame() {
        return leftMiddleFrame;
    }

    public JPanel getUpperRightMiddleFrame() {
        return upperRightMiddleFrame;
    }

    public JPanel getDownRightMiddleFrame() {
        return downRightMiddleFrame;
    }

    public JPanel getFooterFrame() {
        return footerFrame;
    }

    public JLabel getDeckSpr() {
        return deckSpr;
    }

    public JLabel getCurrCardSpr() {
        return currCardSpr;
    }

    public Graphics getGraphics()
    {
        return super.mainComponent.getGraphics();
    }

    public JFrame getMainComponent()
    {
        return super.mainComponent;
    }

    public JLayeredPane getTopLayer()
    {
        return this.topLayer;
    }

    public Map<String, BufferedImage> getCachedImages()
    {
        return this.cachedImages;
    }

    private boolean hasCardsInAuxStack(Stack<JLabel> auxStack)
    {
        BufferedImage img = (BufferedImage) ((ImageIcon) auxStack.peek().getIcon()).getImage();
        return img != obtainCardSprite(EMPTY_IMAGE);
    }

    public long getElapsedTime()
    {
        return System.currentTimeMillis() - time;
    }

    public List<Stack<JLabel>> getAuxStacks()
    {
        return this.auxStacks;
    }

    public void setNumSeqs(int numSeqs)
    {
        this.numSeqs = numSeqs;
    }
}
