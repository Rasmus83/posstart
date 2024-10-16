import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class App
{
    public static void main(String[] args) throws Exception
    {
        while(true)
        {
            System.out.println("1. Registrera ny produkt\n2. Lista alla produkter\n3. Ta bort produkt\n4. Återställ kvittonummer" + 
                "\n5. Avsluta");

            byte option = Byte.parseByte(System.console().readLine());

            switch(option)
            {
                case 1:
                    RegistreraProdukt();
                    break;
                case 2:
                    ListaProdukter();
                    break;
                case 3:
                    AvregistreraProdukt();
                    break;
                case 4:
                    ÅterställKvittonummer();
                    break;
                case 5:
                    return;
            }
        }
    }

    private static void RegistreraProdukt()
    {
        System.out.print("Namn på produkten: ");
        String namn = System.console().readLine();
        System.out.print("Priset på produkten: ");
        float pris = 0;

        while(true)
        {
            try
            {
                pris = Float.parseFloat(System.console().readLine());
                if(pris < 0)
                    throw new NumberFormatException();
            }
            catch(NumberFormatException e)
            {
                System.out.print("Priset på produkten: ");
                continue;
            }
        break;
        }

        try
        {
            Scanner scanner = new Scanner(new File("CashRegister.xml"));
            Scanner scanner2 = new Scanner(Paths.get("CashRegister.xml"), StandardCharsets.UTF_8.name());

            String file = scanner2.useDelimiter("\\A").next();
            String line = "";

            while(scanner.hasNextLine())
            {
                if(line.contains("</Products>"))
                    break;
                else
                    line = scanner.nextLine();
            }
            file = file.replace(line, line.replace(line, "\r\n    <Product>\r\n        <Name = \"" + namn + 
                    "\"/>\r\n        <Price = \"" + pris + "\"/>\r\n    </Product>\r\n</Products>"));
            Files.write(Paths.get("CashRegister.xml"), file.getBytes());

            scanner.close();
            scanner2.close();
        }
        catch(IOException e1)
        {
            System.out.println("Filen går inte att hitta");
        }
    }

    private static void ListaProdukter()
    {
        try
        {
            ArrayList<Produkt> produkter = new ArrayList<Produkt>();

            Scanner scanner = new Scanner(new File("CashRegister.xml"));

            String line = "";

            String namn = "";
            String pris = "";

            while(scanner.hasNextLine())
            {
                if(line.contains("<Products>"))
                {
                    while(!line.contains("</Products>"))
                    {
                        line = scanner.nextLine();
                        if(line.contains("<Product>"))
                        {
                            boolean namnSet = false;
                            boolean prisSet = false;
                            while(!line.contains("</Product>"))
                            {
                                line = scanner.nextLine();
                                if(line.contains("<Name"))
                                {
                                    String[] arrOfStr = line.split("=");
                                    namn = arrOfStr[1];
                                    namn = namn.trim();
                                    namn = namn.replace("\"", "");
                                    namn = namn.replace("/>", "");
                                    namnSet = true;
                                }
                                else if(line.contains("<Price"))
                                {
                                    String[] arrOfStr = line.split("=");
                                    pris = arrOfStr[1];
                                    pris = pris.trim();
                                    pris = pris.replace("\"", "");
                                    pris = pris.replace("/>", "");
                                    prisSet = true;
                                }

                                if(namnSet && prisSet)
                                {
                                    produkter.add(new Produkt(namn, Float.parseFloat(pris)));
                                    namnSet = false;
                                    prisSet = false;
                                    line = scanner.nextLine();
                                }
                            }
                        }
                    }
                    break;
                }
                else
                    line = scanner.nextLine();
            }
            
            for(Produkt i : produkter)
            {
                System.out.println("Produkt: " + i.getNamn() + "\nPris: " + i.getPris() + "\n");
            }

            scanner.close();
        }
        catch(IOException e1)
        {
            System.out.println("Filen går inte att hitta");
        }
    }

    private static void AvregistreraProdukt()
    {
        System.out.print("Namn på produkten att avregistera ");
        String namn = System.console().readLine();

        try
        {
            Scanner scanner = new Scanner(new File("CashRegister.xml"));
            Scanner scanner2 = new Scanner(Paths.get("CashRegister.xml"), StandardCharsets.UTF_8.name());

            String file = scanner2.useDelimiter("\\A").next();
            String line = "";
            String produkt = "";

            boolean rättProdukt = false;

            while(scanner.hasNextLine())
            {
                if(line.contains("<Product>"))
                {
                    while(!line.contains("</Product>"))
                    {
                        produkt = produkt.concat(line + "\r\n");
                        line = scanner.nextLine();
                        if(line.contains("<Name"))
                        {
                            if(line.contains("\"" + namn + "\""))
                            {
                                produkt = produkt.concat(line + "\r\n");
                                rättProdukt = true;
                                line = scanner.nextLine();
                            }
                            else
                            {
                                produkt = "";
                                break;
                            }
                        }
                    }
                    if(rättProdukt)
                    {
                        produkt = produkt.concat(line);
                        file = file.replace(produkt, "");
                        Files.write(Paths.get("CashRegister.xml"), file.getBytes());

                        scanner.close();
                        scanner2.close();
                        return;
                    }
                }
                else
                    line = scanner.nextLine();
            }

            System.out.println("Produkten går inte att hitta");

            scanner.close();
            scanner2.close();
        }
        catch(IOException e1)
        {
            System.out.println("Filen går inte att hitta");
        }
    }

    private static void ÅterställKvittonummer()
    {
        try
        {
            Scanner scanner = new Scanner(new File("CashRegister.xml"));
            Scanner scanner2 = new Scanner(Paths.get("CashRegister.xml"), StandardCharsets.UTF_8.name());

            String file = scanner2.useDelimiter("\\A").next();
            String line = "";

            while(scanner.hasNextLine())
            {
                if(line.contains("ReceiptNumber"))
                    break;
                else
                    line = scanner.nextLine();
            }
            String[] arrOfStr = line.split("=");
            String kvittoNummer = arrOfStr[1];
            kvittoNummer = kvittoNummer.trim();
            kvittoNummer = kvittoNummer.replace("\"", "");
            kvittoNummer = kvittoNummer.replace("/>", "");
            file = file.replace(line, line.replace(kvittoNummer, "0"));
            Files.write(Paths.get("CashRegister.xml"), file.getBytes());

            scanner.close();
            scanner2.close();
        }
        catch(IOException e1)
        {
            System.out.println("Filen går inte att hitta");
        }
    }
}
