import javax.swing.*;
import java.awt.*;

public class MessageFrame extends JFrame {
	private JLabel message;
	private JPanel content;
	
	MessageFrame(String title, String msg) {
		this.setTitle(title);
		this.message = new JLabel(msg, JLabel.CENTER);
		this.content = (JPanel) this.getContentPane();
		this.content.setLayout( new FlowLayout() );
		this.content.add(this.message);
		this.pack();
		this.setLocationRelativeTo( null );
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
}
