
package seq.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GameSettingsView extends BaseView
{
    private int numSeqs;
    private int maxCardsPerSeq;

    public static final int MIN_SEQS = 2;
    public static final int MAX_SEQS = 4;
    public static final int MIN_CARDS_PER_SEQ = MIN_SEQS*2;
    public static final int MAX_CARDS_PER_SEQ = 9;

    public GameSettingsView()
    {
        JOptionPane gameSettings = new JOptionPane("Opções de jogo");
        /*gameSettings.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameSettings.setResizable(false);
        gameSettings.setAlwaysOnTop(true);*/
        gameSettings.setSize(320, 240);

        JPanel rootContainer = new JPanel();
        rootContainer.setLayout(new GridLayout(2, 2));
        //rootContainer.setLayout(new BoxLayout(rootContainer, BoxLayout.PAGE_AXIS));
        //JPanel inputPanel1 = new JPanel(new BorderLayout());
        JPanel inputPanel1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JPanel inputPanel2 = new JPanel(new BorderLayout());

        numSeqs = 3;
        maxCardsPerSeq = 7;
        JLabel numSeqsLbl = new JLabel("Nº sequências");
        //inputPanel1.add(numSeqsLbl, BorderLayout.WEST);
        final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(3, MIN_SEQS, MAX_SEQS, 1));
        spinner1.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent ce)
            {
                numSeqs = ((Integer) spinner1.getValue()).intValue();
            }
        });
        numSeqsLbl.setLabelFor(spinner1);

        JLabel maxCardsPerSeqLbl = new JLabel("Nº de cartas por sequência:");
        final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(7, MIN_CARDS_PER_SEQ, MAX_CARDS_PER_SEQ, 1));
        spinner2.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent ce)
            {
                maxCardsPerSeq = ((Integer) spinner2.getValue()).intValue();
            }
        });
        maxCardsPerSeqLbl.setLabelFor(spinner2);

        JTextField input1 = new JTextField();
        input1.setText("3");
        numSeqs = 3;
        input1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int n = 0;
                try
                {
                    /*String prop = pce.getPropertyName();

                    if (prop.equals("text"))
                    {
                        n = Integer.parseInt((String) pce.getNewValue());
                    }*/
                    n = Integer.parseInt(((JTextField) ae.getSource()).getText());
                }
                catch (ClassCastException ex)
                {
                    JOptionPane.showMessageDialog(null, "Deve introduzir um número entre 1 e 4");
                    return;
                }

                numSeqs = n;
            }
        });
        inputPanel1.add(input1, BorderLayout.EAST);

        inputPanel2.add(new JLabel("Nº de cartas por sequência"));
        JTextField input2 = new JTextField();
        input2.setText("7");
        maxCardsPerSeq = 7;
        input2.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int n = 0;
                try
                {
                    /*String prop = pce.getPropertyName();

                    if (prop.equals("text"))
                    {
                        n = Integer.parseInt((String) pce.getNewValue());
                    }*/
                    n = Integer.parseInt(((JTextField) ae.getSource()).getText());
                }
                catch (ClassCastException ex)
                {
                    JOptionPane.showMessageDialog(null, "Deve ser introduzido um número entre o número de"+
                            " sequência introduzido e 7");
                    return;
                }

                maxCardsPerSeq = n;
            }
        });
        inputPanel2.add(input2);

        JButton btn = new JButton("OK");
        btn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                ((JFrame) ((JComponent) ae.getSource()).getParent().getParent()).dispose();
            }
        });

        /*rootContainer.add(inputPanel1);
        rootContainer.add(inputPanel2);
        rootContainer.add(btn);*/
        rootContainer.add(numSeqsLbl);
        rootContainer.add(spinner1);
        rootContainer.add(maxCardsPerSeqLbl);
        rootContainer.add(spinner2);
        
        gameSettings.add(rootContainer);
        JDialog dialog = gameSettings.createDialog(null, null);

        dialog.pack();
        dialog.show();
    }

    @Override
    public void init()
    {}

    @Override
    public void restart()
    {}

    @Override
    public void release()
    {}

    public int getNumSeqs()
    {
        return this.numSeqs;
    }

    public int getMaxCardsPerSeq()
    {
        return this.maxCardsPerSeq;
    }
}
