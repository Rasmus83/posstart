public class Produkt
{
    private String namn;
    private float pris;
    private int moms;

    public Produkt()
    {
        namn = "";
        pris = 0;
        moms = 0;
    }
    public Produkt(String namn, float pris, int moms)
    {
        this.namn = namn;
        this.pris = pris;
        this.moms = moms;
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

    public void setMoms(int moms)
    {
        this.moms = moms;
    }
    public int getMoms()
    {
        return moms;
    }
}
