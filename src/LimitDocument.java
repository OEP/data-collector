import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitDocument extends PlainDocument
{
	 int limit;
	 
	 public LimitDocument(int limit)
	 {
	  this.limit = limit;
	 }
	 
	 public void insertString(int offset, String s, AttributeSet a) throws BadLocationException 
	 {
	  if (offset + s.length() <= limit)
	   super.insertString(offset,s,a);
	  else
	   Toolkit.getDefaultToolkit().beep();
	 } 
}