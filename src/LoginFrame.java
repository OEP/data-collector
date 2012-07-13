import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
public class LoginFrame extends JFrame implements KeyListener {
	private GridBagLayout gb;
	private GridBagConstraints gbc;
	private JPanel content;
	private JTextField host;
	private JTextField database;
	private JTextField username;
	private JTextField password;
	private JTextField table;
	private JButton login;
	private JButton exit;
	private boolean alive = true;
	
	LoginFrame(String name, String dbname, String tbname) {
		gb = new GridBagLayout();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.setLayout(gb);
		this.setTitle(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.content = (JPanel) this.getContentPane();
		this.host = new JTextField(20);
		this.database = new JTextField(20);
		this.username = new JTextField(20);
		this.password = new JPasswordField(20);
		this.table = new JTextField(20);
		this.login = new JButton("Login");
		this.exit = new JButton("Exit");
		
		this.database.setText(dbname);
		this.database.setEditable(false);
		this.table.setText(tbname);
		this.table.setEditable(false);
		this.addFields();
		this.requestFocus();
		this.username.requestFocusInWindow();
		this.pack();
		this.setLocationRelativeTo( null );
	}
	
	LoginFrame(String name, String dbhost, String dbname, String tbname) {
		gb = new GridBagLayout();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.setLayout(gb);
		this.setTitle(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.content = (JPanel) this.getContentPane();
		this.host = new JTextField(20);
		this.database = new JTextField(20);
		this.username = new JTextField(20);
		this.password = new JPasswordField(20);
		this.table = new JTextField(20);
		this.login = new JButton("Login");
		this.exit = new JButton("Exit");
		
		this.host.setText(dbhost);
		this.host.setEditable(false);
		this.database.setText(dbname);
		this.database.setEditable(false);
		this.table.setText(tbname);
		this.table.setEditable(false);
		this.addFields();
		this.requestFocus();
		this.username.requestFocusInWindow();
		this.pack();
		this.setLocationRelativeTo( null );
	}
	
	LoginFrame(String name) {
		gb = new GridBagLayout();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.setLayout(gb);
		this.setTitle(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.content = (JPanel) this.getContentPane();
		this.host = new JTextField(20);
		this.database = new JTextField(20);
		this.username = new JTextField(20);
		this.password = new JPasswordField(20);
		this.table = new JTextField(20);
		this.addFields();
		this.setLocationRelativeTo( null );
	}
	
	public void keyPressed(KeyEvent e) {
//		System.out.println("Key Pressed: " + e);
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			login.doClick();
			
	}
	
	public void keyReleased(KeyEvent e) {
//		System.out.println("Key Release: " + e);
	}
	
	public void keyTyped(KeyEvent e) {
//		System.out.println("Key Type: " + e);
	}
	
	public void addFields() {
//		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 20;
		content.add(new JLabel("Host", JLabel.CENTER),gbc); gbc.gridy++;
		content.add(new JLabel("Database", JLabel.CENTER),gbc); gbc.gridy++;
		content.add(new JLabel("Table", JLabel.CENTER),gbc); gbc.gridy++;
		content.add(new JLabel("Username", JLabel.CENTER),gbc); gbc.gridy++;
		content.add(new JLabel("Password", JLabel.CENTER),gbc); gbc.gridy = 0; gbc.gridx = 1;
		content.add(host,gbc); gbc.gridy++;
		content.add(database,gbc); gbc.gridy++;
		content.add(table,gbc); gbc.gridy++;
		content.add(username,gbc); gbc.gridy++;
		content.add(password,gbc); gbc.gridy++;
		login.setMnemonic(KeyEvent.VK_L);
		
		ActionListener act = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == login) {
					if(username.getText().length() > 0)
						alive = false;
					else {
						showMessage("Username required", "At least enter a username :)");
					}
				}
				if(e.getSource() == exit) {
					System.exit(0);
				}
			}
		};
		
		login.addActionListener(act);
		exit.addActionListener(act);
		JPanel p = new JPanel(new FlowLayout());
		p.add(login); p.add(exit);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		content.add(p, gbc);
		
		username.addKeyListener(this);
		password.addKeyListener(this);
		login.addKeyListener(this);
	}
	
	public boolean alive() { return alive; }
	public void die() { alive = false; }
	public void revive() { alive = true; }
	
	public String [] getFields() {
		return new String [] {host.getText(), database.getText(), table.getText(), username.getText(),  password.getText()};
	}
	
	public static void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

}
