public class Receipt
{
    private Produkt produkt;
    private int antal;

    public Receipt()
    {
        produkt = new Produkt("", 0);
        antal = 0;
    }
    public Receipt(Produkt produkt, int antal)
    {
        this.produkt = produkt;
        this.antal = antal;
    }

    public Produkt getProdukt()
    {
        return produkt;
    }

    public int getAntal()
    {
        return antal;
    }
}