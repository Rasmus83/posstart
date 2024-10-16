public class Produkt
{
    private String namn;
    private float pris;

    public Produkt()
    {
        this.namn = "";
        this.pris = 0;
    }
    public Produkt(String namn, float pris)
    {
        this.namn = namn;
        this.pris = pris;
    }

    public void setNamn(String namn)
    {
        this.namn = namn;
    }
    public String getNamn()
    {
        return namn;
    }

    public void setPris(float pris)
    {
        this.pris = pris;
    }
    public float getPris()
    {
        return pris;
    }
}