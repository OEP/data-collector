import java.awt.event.KeyEvent;
import javax.swing.JTextField;


public class ImprovedText extends JTextField {

	private boolean normal = true;
    final static String badchars 
       = "-`~!@#$%^&*()_+=\\|\"':;?/>.<, ";

    public void processKeyEvent(KeyEvent ev) {
    	if(normal) {
    		super.processKeyEvent(ev);
    		return;
    	}

        char c = ev.getKeyChar();

        if((Character.isLetter(c) && !ev.isAltDown()) 
           || badchars.indexOf(c) > -1) {
            ev.consume();
            return;
        }

        else super.processKeyEvent(ev);

    }
    
    public void setNormal(boolean b) { normal = b; }
}
