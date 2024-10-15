import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private ArrayList<JButton> buttons = new ArrayList<JButton>();
    private JTextArea inputProductName;
    private JTextArea inputCount;
    private JButton addToReceiptButton;
    private JButton payButton;

    private ArrayList<Produkt> produkter = new ArrayList<Produkt>();

    private Map<String, Float> produktHashMap = new HashMap<String, Float>();

    private ArrayList<Receipt> tillagdaProdukter = new ArrayList<Receipt>();

    private Produkt senastValdProdukt = new Produkt();

    private int föredettaAntal = 0;

    private int kvittoNummer = 0;

    private double totalSumma = 0;

    private Timer timer = new Timer(0, this);

    FileAttributes type = FileAttributes.None;

    private enum FileAttributes
    {
        None,
        Products,
        ReceiptNumber
    };

    public CashRegister()
    {
        frame = new JFrame("IOT24 POS");

        try {
            loadCashRegisterXml();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(Produkt i : produkter)
        {
            produktHashMap.put(i.getNamn(), i.getPris());
        }

        createReceiptArea();
        createQuickButtonsArea();
        createAddArea();

        frame.getContentPane().setBackground(Color.BLACK);
        frame.setSize(1000,800);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);
    
        frame.setVisible(true);
    }

    private void loadCashRegisterXml() throws FileNotFoundException
    {
        Scanner scanner = new Scanner(new File("CashRegister.xml"));
        String line;
        while(scanner.hasNextLine())
        {
            line = scanner.nextLine();
            if(line.contains("<CashRegister>"))
            {
                while(!line.contains("</CashRegister>"))
                {
                    line = scanner.nextLine();
                    if(!line.isEmpty())
                        line.trim();

                    if(line.contains("<Products>"))
                        type = FileAttributes.Products;
                    else if(line.contains("<ReceiptNumber"))
                        type = FileAttributes.ReceiptNumber;

                    String[] arrOfStr = null;
                    if(type == FileAttributes.Products)
                    {
                        Produkt prod = new Produkt();
                        boolean nameSet = false;
                        boolean priceSet = false;
                        while(!line.contains("</Products>"))
                        {
                            line = scanner.nextLine();
                            if(line.contains("<Product>"))
                            {
                                while(!line.contains("</Product>"))
                                {
                                    line = scanner.nextLine();
                                    if(line.contains("<Name"))
                                    {
                                        arrOfStr = line.split("=");
                                        String name = arrOfStr[1];
                                        name = name.trim();
                                        name = name.replace("\"", "");
                                        name = name.replace("/>", "");
                                        prod.setNamn(name);
                                        nameSet = true;
                                    }
                                    else if(line.contains("<Price"))
                                    {
                                        arrOfStr = line.split("=");
                                        String price = arrOfStr[1];
                                        price = price.trim();
                                        price = price.replace("\"", "");
                                        price = price.replace("/>", "");
                                        prod.setPris(Float.parseFloat(price));
                                        priceSet = true;
                                    }
                                
                                    if(nameSet && priceSet)
                                    {
                                        produkter.add(new Produkt(prod.getNamn(), prod.getPris()));
                                        nameSet = false;
                                        priceSet = false;
                                        type = FileAttributes.None;
                                        line = scanner.nextLine();
                                    }
                                }
                            }
                        }
                    }
                    else if(type == FileAttributes.ReceiptNumber)
                    {
                        arrOfStr = line.split("=");
                        String receiptNumber = arrOfStr[1];
                        receiptNumber = receiptNumber.trim();
                        receiptNumber = receiptNumber.replace("\"", "");
                        receiptNumber = receiptNumber.replace("/>", "");
                        kvittoNummer = Integer.parseInt(receiptNumber);
                        type = FileAttributes.None;
                    }
                }
            }
        }
        scanner.close();
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

        for(Produkt i : produkter)
        {
            buttons.add(new JButton(i.getNamn()));
            buttons.get(buttons.size() - 1).addActionListener(this);
            panel.add( buttons.get(buttons.size() - 1));
        }

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

        inputProductName.setText("");
        inputCount.setText("");

        timer.setInitialDelay(5*1000);
        timer.start();
    }

    public void run()
    {
        if(!tillagdaProdukter.isEmpty())
        {
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
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                receipt.append("Kvittonummer: " + kvittoNummer + "        Datum: " + currentDate + "\n");
                receipt.append("----------------------------------------------------\n");
            }
        }
    }            

    private void addProdukt()
    {
        try
        {
            senastValdProdukt.setPris(produktHashMap.get(inputProductName.getText()));
        }
        catch(NullPointerException e)
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
        catch(NumberFormatException e)
        {
            return;
        }
        totalSumma += (senastValdProdukt.getPris() * Integer.parseInt(inputCount.getText()));
        inputProductName.setText("");
        inputCount.setText("");
        run();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == timer)
        {
            receipt.setText("");
            timer.stop();
            try
            {
                Scanner scanner = new Scanner(Paths.get("CashRegister.xml"), StandardCharsets.UTF_8.name());
                String line = scanner.useDelimiter("\\A").next();
                line = line.replace("ReceiptNumber = \"" + Integer.toString(kvittoNummer) + "\"", 
                        "ReceiptNumber = \"" + Integer.toString(kvittoNummer + 1) + "\"");
                Files.write(Paths.get("CashRegister.xml"), line.getBytes());
                scanner.close();
            }
            catch(IOException e1)
            {
                e1.printStackTrace();
            }
            try {
                loadCashRegisterXml();
            } catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            run();
        }

        if(!timer.isRunning())
        {
            for(int i = 0; i < buttons.size(); i++)
            {
                if(e.getSource() == buttons.get(i))
                {
                    inputProductName.setText(buttons.get(i).getText());
                    break;
                }
            }

            if(e.getSource() == addToReceiptButton)
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
