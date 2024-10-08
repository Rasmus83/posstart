import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.TextArea;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.event.*;

public class CashRegister implements ActionListener
{
    private JFrame frame;
    private JTextArea receipt;
    private JButton kaffeButton;
    private JButton nalleButton;
    private JButton muggButton;
    private JButton chipsButton;
    private JButton vaniljYoghurtButton;
    private JButton daimButton;
    private JTextArea inputProductName;
    private JTextArea inputCount;
    private JButton addToReceiptButton;
    private JButton payButton;

    private Produkt kaffe;
    private Produkt nalle;
    private Produkt mugg;
    private Produkt chips;
    private Produkt yoghurt;
    private Produkt daim;

    private Map<String, Float> produktHashMap;

    private ArrayList<Receipt> tillagdaProdukter;

    private Produkt senastValdProdukt;

    private int föredettaAntal;

    private double totalSumma;

    private boolean kvittoUtskrivet;

    private Timer timer;

    public CashRegister()
    {
        frame = new JFrame("IOT24 POS");

        kaffe = new Produkt("Kaffe", 51);
        nalle = new Produkt("Nalle", 110);
        mugg = new Produkt("Mugg", 10);
        chips = new Produkt("Chips", 23);
        yoghurt = new Produkt("Yoghurt", 37);
        daim = new Produkt("Daim", 16);

        produktHashMap = new HashMap<String, Float>(6);
        produktHashMap.put(kaffe.getNamn(), kaffe.getPris());
        produktHashMap.put(nalle.getNamn(), nalle.getPris());
        produktHashMap.put(mugg.getNamn(), mugg.getPris());
        produktHashMap.put(chips.getNamn(), chips.getPris());
        produktHashMap.put(yoghurt.getNamn(), yoghurt.getPris());
        produktHashMap.put(daim.getNamn(), daim.getPris());

        tillagdaProdukter = new ArrayList<Receipt>();

        senastValdProdukt = new Produkt();

        föredettaAntal = 0;

        totalSumma = 0;

        kvittoUtskrivet = false;

        timer = new Timer(0, this);

        createReceiptArea();
        createQuickButtonsArea();
        createAddArea();

        frame.getContentPane().setBackground(Color.BLACK);
        frame.setSize(1000,800);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);
    
        frame.setVisible(true);
    }

    private void createAddArea()
    {
        inputProductName = new JTextArea();
        inputProductName.setBounds(20,650,300,30);
        inputProductName.setFont(new Font("Arial Black", Font.BOLD, 18));
        frame.add(inputProductName);

        JLabel label = new JLabel("Antal");
        label.setBounds(340,625,300,30);
        label.setForeground(Color.WHITE);
        frame.add(label);

        inputCount = new JTextArea();
        inputCount.setBounds(330,650,50,30);
        inputCount.setFont(new Font("Arial Black", Font.BOLD, 18));
        frame.add(inputCount);

        addToReceiptButton = new JButton("Add");
        addToReceiptButton.setBounds(400,640,70,50);
        addToReceiptButton.setFont(new Font("Arial Black", Font.PLAIN, 14));
        addToReceiptButton.addActionListener(this);
        frame.add(addToReceiptButton);

        payButton = new JButton("Pay");
        payButton.setBounds(480,640,70,50);
        payButton.setFont(new Font("Arial Black", Font.PLAIN, 14));
        payButton.addActionListener(this);
        frame.add(payButton);
    }

    private void createQuickButtonsArea()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(Color.green);
        panel.setPreferredSize(new Dimension(600, 600));

        kaffeButton = new JButton(kaffe.getNamn());
        kaffeButton.addActionListener(this);
        panel.add(kaffeButton);

        nalleButton = new JButton(nalle.getNamn());
        nalleButton.addActionListener(this);
        panel.add(nalleButton);

        muggButton = new JButton(mugg.getNamn());
        muggButton.addActionListener(this);
        panel.add(muggButton);

        chipsButton = new JButton(chips.getNamn());
        chipsButton.addActionListener(this);
        panel.add(chipsButton);

        vaniljYoghurtButton = new JButton(yoghurt.getNamn());
        vaniljYoghurtButton.addActionListener(this);
        panel.add(vaniljYoghurtButton);
        
        daimButton = new JButton(daim.getNamn());
        daimButton.addActionListener(this);
        panel.add(daimButton);

        panel.setBounds(0, 0, 600, 600);

        frame.add(panel);
    }

    private void createReceiptArea()
    {
        receipt = new JTextArea();
        receipt.setSize(400,400); 
        receipt.setLineWrap(true);
        receipt.setEditable(false);
        receipt.setVisible(true);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane (receipt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(600, 0, 400, 1000);

        frame.add(scroll);    
    }

    private void betala()
    {
        receipt.append("Total                                        ------\n");
        receipt.append("                                             " + totalSumma + "\n");
        receipt.append("TACK FÖR DITT KÖP\n");

        tillagdaProdukter.clear();

        totalSumma = 0;

        kvittoUtskrivet = false;

        timer.setInitialDelay(5*1000);
        timer.start();
    }

    public void run()
    {
        if(!tillagdaProdukter.isEmpty())
        {
            if(!kvittoUtskrivet)
            {
                int kvittoNummer = new Random().nextInt(999) + 1;
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                receipt.append("Kvittonummer: " + kvittoNummer + "        Datum: " + currentDate + "\n");
                receipt.append("----------------------------------------------------\n");
                kvittoUtskrivet = true;
            }
            Produkt produkt = senastValdProdukt;
            int antal = tillagdaProdukter.get(tillagdaProdukter.size() - 1).getAntal();
            for(int i = 0; i < tillagdaProdukter.size(); i++)
            {
                if(tillagdaProdukter.get(i).getProdukt().getNamn().equals(senastValdProdukt.getNamn()))
                {
                    antal = tillagdaProdukter.get(i).getAntal();
                    break;
                }
            }

            if(receipt.getText().contains(senastValdProdukt.getNamn()))
                receipt.setText(receipt.getText().replace(senastValdProdukt.getNamn() + "           " + föredettaAntal + " *     " 
                        + senastValdProdukt.getPris() + "    =   "  + senastValdProdukt.getPris() * föredettaAntal, 
                        senastValdProdukt.getNamn() + "           " + antal + " *     " 
                        + senastValdProdukt.getPris() + "    =   "  + senastValdProdukt.getPris() * antal));

            else
                receipt.append(produkt.getNamn() + "           " + antal + " *     " 
                        + produkt.getPris() + "    =   "  + produkt.getPris() * antal + "  \n\n");
        }
        else
        {
            if(!timer.isRunning())
            {
                receipt.append("                     STEFANS SUPERSHOP\n");
                receipt.append("----------------------------------------------------\n");
                receipt.append("\n");
            }
        }
    }            

    private void addProdukt()
    {
        try
        {
            senastValdProdukt.setPris(produktHashMap.get(inputProductName.getText()));
        }
        catch(NullPointerException exception)
        {
            return;
        }
        senastValdProdukt.setNamn(inputProductName.getText());

        try
        {
            if(Integer.parseInt(inputCount.getText()) == 0)
                return;
            for(int i = 0; i < tillagdaProdukter.size(); i++)
            {
                if(tillagdaProdukter.get(i).getProdukt().getNamn().equals(senastValdProdukt.getNamn()))
                {
                    föredettaAntal = tillagdaProdukter.get(i).getAntal();
                    tillagdaProdukter.get(i).setAntal(tillagdaProdukter.get(i).getAntal() + Integer.parseInt(inputCount.getText()));
                    break;
                }
                else if(i == tillagdaProdukter.size() - 1)
                {
                    tillagdaProdukter.add(new Receipt(new Produkt(senastValdProdukt.getNamn(), senastValdProdukt.getPris()), 
                            Integer.parseInt(inputCount.getText())));
                    break;
                }
            }
            if(tillagdaProdukter.isEmpty())
                tillagdaProdukter.add(new Receipt(new Produkt(senastValdProdukt.getNamn(), senastValdProdukt.getPris()), 
                        Integer.parseInt(inputCount.getText())));
        }
        catch(NumberFormatException exception)
        {
            return;
        }
        totalSumma += (senastValdProdukt.getPris() * Integer.parseInt(inputCount.getText()));
        run();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == timer)
        {
            receipt.setText("");
            timer.stop();
            run();
        }

        if(!timer.isRunning())
        {
            if(e.getSource() == kaffeButton)
                inputProductName.setText(kaffe.getNamn());
    
            else if(e.getSource() == nalleButton)
                inputProductName.setText(nalle.getNamn());
    
            else if(e.getSource() == muggButton)
                inputProductName.setText(mugg.getNamn());
    
            else if(e.getSource() == chipsButton)
                inputProductName.setText(chips.getNamn());
    
            else if(e.getSource() == vaniljYoghurtButton)
                inputProductName.setText(yoghurt.getNamn());
    
            else if(e.getSource() == daimButton)
                inputProductName.setText(daim.getNamn());
    
            else if(e.getSource() == addToReceiptButton)
            {
                addProdukt();
            }
    
            else if(e.getSource() == payButton)
            {
                if(!tillagdaProdukter.isEmpty())
                {
                    betala();
                }
            }
        }
    }
}
