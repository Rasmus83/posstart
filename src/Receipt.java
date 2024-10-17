import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;

public class Receipt
{
    private JTextArea receipt;
    private int receiptNumber;
    private String date;

    Receipt()
    {
        receipt = new JTextArea();
        receipt.setSize(400,400); 
        receipt.setLineWrap(true);
        receipt.setEditable(false);
        receipt.setVisible(true);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        receiptNumber = 0;
    }

    public void setText(String text)
    {
        receipt.setText(text);
    }
    public String getText()
    {
        return receipt.getText();
    }

    public void setReceiptNumber(int receiptNumber)
    {
        this.receiptNumber = receiptNumber;
    }
    public int getReceiptNumber()
    {
        return receiptNumber;
    }

    public void append(String text)
    {
        receipt.append(text);
    }

    public void setCurrentDate()
    {
        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    public String getDate()
    {
        return date;
    }

    public JTextArea getRecipt()
    {
        return receipt;
    }
}
