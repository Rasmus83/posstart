import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
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
    private Receipt receipt;
    private ArrayList<JButton> buttons = new ArrayList<JButton>();
    private JTextArea inputProductName;
    private JTextArea inputCount;
    private JButton addToReceiptButton;
    private JButton payButton;

    private ArrayList<Produkt> produkter = new ArrayList<Produkt>();

    private Map<String, Float> produktPrisHashMap = new HashMap<String, Float>();
    private Map<String, Integer> produktMomsHashMap = new HashMap<String, Integer>();

    private ArrayList<TillagdProdukt> tillagdaProdukter = new ArrayList<TillagdProdukt>();

    private Produkt senastValdProdukt = new Produkt();

    private int föredettaAntal = 0;

    private double totalSumma = 0;
    private double utSkrivenTotalSumma = 0;

    private Timer timer = new Timer(0, this);

    private FileAttributes type = FileAttributes.None;

    private enum FileAttributes
    {
        None,
        Products,
        ReceiptNumber
    };

    public CashRegister()
    {
        frame = new JFrame("IOT24 POS");

        createReceiptArea();

        try {
            loadCashRegisterXml();
        }
        catch (FileNotFoundException e) {
            System.out.println("Filen går inte att hitta");
        }

        for(Produkt i : produkter)
        {
            produktPrisHashMap.put(i.getNamn(), i.getPris());
        }
        for(Produkt i : produkter)
        {
            produktMomsHashMap.put(i.getNamn(), i.getMoms());
        }

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
        Scanner scanner = new Scanner(new File("CashRegisterCommands/CashRegister.xml"));
        while(scanner.hasNextLine())
        {
            try
            {
                String line = scanner.nextLine();
                if(line.contains("<CashRegister>"))
                {
                    while(!line.contains("</CashRegister>"))
                    {
                        line = scanner.nextLine();
                        if(!line.isEmpty())
                            line = line.trim();

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
                            boolean momsSet = false;
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
                                            String name = "";
                                            try
                                            {
                                                name = arrOfStr[1];
                                            }
                                            catch(ArrayIndexOutOfBoundsException e)
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }

                                            name = name.trim();
                                            name = name.replace("\"", "");
                                            name = name.replace("/>", "");
                                            if(!line.contains("\"" + name + "\"") || !line.contains("/>"))
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            prod.setNamn(name);
                                            nameSet = true;
                                        }
                                        else if(line.contains("<Price"))
                                        {
                                            arrOfStr = line.split("=");
                                            String price = "";
                                            try
                                            {
                                                price = arrOfStr[1];
                                            }
                                            catch(ArrayIndexOutOfBoundsException e)
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            price = price.trim();
                                            price = price.replace("\"", "");
                                            price = price.replace("/>", "");
                                            if(!line.contains("\"" + price + "\"") || !line.contains("/>"))
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            try
                                            {
                                                prod.setPris(Float.parseFloat(price));
                                                if(prod.getPris() <= 0)
                                                    throw new NumberFormatException();
                                            }
                                            catch(NumberFormatException e)
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            priceSet = true;
                                        }
                                        else if(line.contains("<Moms"))
                                        {
                                            arrOfStr = line.split("=");
                                            String moms = "";
                                            try
                                            {
                                                moms = arrOfStr[1];
                                            }
                                            catch(ArrayIndexOutOfBoundsException e)
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            moms = moms.trim();
                                            moms = moms.replace("\"", "");
                                            moms = moms.replace("/>", "");
                                            if(!line.contains("\"" + moms + "\"") || !line.contains("/>"))
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            try
                                            {
                                                prod.setMoms(Integer.parseInt(moms));
                                                if(prod.getMoms() <= 0)
                                                    throw new NumberFormatException();
                                            }
                                            catch(NumberFormatException e)
                                            {
                                                System.out.println("Filen har felaktig data");
                                                return;
                                            }
                                            momsSet = true;
                                        }
                                
                                        if(nameSet && priceSet && momsSet)
                                        {
                                            produkter.add(new Produkt(prod.getNamn(), prod.getPris(), prod.getMoms()));
                                            nameSet = false;
                                            priceSet = false;
                                            momsSet = false;
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
                            receipt.setReceiptNumber(Integer.parseInt(receiptNumber));
                            type = FileAttributes.None;
                        }
                    }
                }
            }
            catch (NoSuchElementException e)
            {
                System.out.println("Finns inga produkter");
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
        receipt = new Receipt();

        JScrollPane scroll = new JScrollPane (receipt.getRecipt());
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(600, 0, 400, 1000);

        frame.add(scroll);    
    }

    private void betala()
    {
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

            float produktBruttoPris = Float.valueOf(produkt.getPris() + (produkt.getPris() * (produkt.getMoms() / 100.0f)));
            float senastValdProduktBruttoPris = Float.valueOf(senastValdProdukt.getPris() + (senastValdProdukt.getPris() 
                    * (senastValdProdukt.getMoms() / 100.0f)));

            NumberFormat numberFormat = NumberFormat.getInstance(new Locale("en"));
            numberFormat.setMaximumFractionDigits(2);

            if(receipt.getText().contains(senastValdProdukt.getNamn()))
                receipt.setText(receipt.getText().replace(senastValdProdukt.getNamn() + "           " + föredettaAntal + " *     " 
                        + senastValdProduktBruttoPris + "    =   "  + numberFormat.format(senastValdProduktBruttoPris * föredettaAntal),

                        senastValdProdukt.getNamn() + "           " + antal + " *     " 
                        + senastValdProduktBruttoPris + "    =   "  + numberFormat.format(senastValdProduktBruttoPris * antal)));

            else
            {
                receipt.append(produkt.getNamn() + "           " + antal + " *     " + produktBruttoPris + "    =   "  
                        + numberFormat.format(produktBruttoPris * antal) + "  \n");
                receipt.append("Moms% " + produkt.getMoms() + "    Moms " + produkt.getPris() * (produkt.getMoms() / 100.0f) 
                        + "    Netto " + produkt.getPris() + "    Brutto " 
                        + produktBruttoPris + "\n\n");
            }


            receipt.setText(receipt.getText().replace("Total                                        ------\n" + 
                    "                                             " + numberFormat.format(utSkrivenTotalSumma) + "\n", ""));
            receipt.append("Total                                        ------\n");
            receipt.append("                                             " + numberFormat.format(totalSumma) + "\n");

            utSkrivenTotalSumma = totalSumma;
        }
        else
        {
            if(!timer.isRunning())
            {
                receipt.setCurrentDate();

                receipt.append("                     STEFANS SUPERSHOP\n");
                receipt.append("----------------------------------------------------\n");
                receipt.append("\n");
                receipt.append("Kvittonummer: " + receipt.getReceiptNumber() + "        Datum: " + receipt.getDate() + "\n");
                receipt.append("----------------------------------------------------\n");
            }
        }
    }

    private void addProdukt()
    {
        try
        {
            senastValdProdukt.setPris(produktPrisHashMap.get(inputProductName.getText()));
            senastValdProdukt.setMoms(produktMomsHashMap.get(inputProductName.getText()));
        }
        catch(NullPointerException e)
        {
            return;
        }
        senastValdProdukt.setNamn(inputProductName.getText());

        try
        {
            if(Integer.parseInt(inputCount.getText()) <= 0)
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
                    tillagdaProdukter.add(new TillagdProdukt(new Produkt(senastValdProdukt.getNamn(), senastValdProdukt.getPris(), senastValdProdukt.getMoms()), 
                            Integer.parseInt(inputCount.getText())));
                    break;
                }
            }
            if(tillagdaProdukter.isEmpty())
                tillagdaProdukter.add(new TillagdProdukt(new Produkt(senastValdProdukt.getNamn(), senastValdProdukt.getPris(), senastValdProdukt.getMoms()), 
                        Integer.parseInt(inputCount.getText())));
        }
        catch(NumberFormatException e)
        {
            return;
        }
        int antal = Integer.parseInt(inputCount.getText());
        float pris = senastValdProdukt.getPris();
        float moms = senastValdProdukt.getMoms() / 100.0f;
        totalSumma += (pris + (pris * moms)) * antal;
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
                Scanner scanner = new Scanner(new File("CashRegisterCommands/CashRegister.xml"));
                Scanner scanner2 = new Scanner(Paths.get("CashRegisterCommands/CashRegister.xml"), StandardCharsets.UTF_8.name());

                String file = scanner2.useDelimiter("\\A").next();
                String line = "";

                while(scanner.hasNextLine())
                {
                    if(line.contains("ReceiptNumber"))
                        break;
                    else
                        line = scanner.nextLine();
                }
                file = file.replace(line, line.replace(Integer.toString(receipt.getReceiptNumber()), 
                        Integer.toString(receipt.getReceiptNumber() + 1)));
                Files.write(Paths.get("CashRegisterCommands/CashRegister.xml"), file.getBytes());

                scanner.close();
                scanner2.close();
            }
            catch(IOException e1)
            {
                System.out.println("Filen går inte att hitta");
            }
            try
            {
                loadCashRegisterXml();
            } catch (FileNotFoundException e1)
            {
                System.out.println("Filen går inte att hitta");
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
