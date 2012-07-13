import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class BrowseFrame extends JFrame {
	
	private JList records = new JList();
	private Vector<Relation> items = new Vector<Relation>();
	private boolean alive = true;
	private JPanel content;
	private SQLHandler handle;
	private String key;
	private String auxField;
	
	BrowseFrame(SQLHandler handle, String key, String otherField) {
		this.setTitle(handle.getWindowTitle());
		this.content = (JPanel) this.getContentPane();
		this.content.setLayout(new BorderLayout());
		this.handle = handle;
		this.key = key;
		this.auxField = otherField;
		this.populate();
		this.setPreferredSize(new Dimension(800, 400));
		this.pack();
		this.setLocationRelativeTo( null );
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	public void populate() {
		try {		    
			addItems();
		} catch (SQLException ex) {
		    // handle any errors
			reportSQLError(ex);
			this.die();
		}
	}
	
	public void addItems() throws SQLException {
		readRecords();
		content.add(records, BorderLayout.CENTER);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		
		Dimension d = new Dimension(200, 75);
		final JButton insert = new JButton("New");
		setButtonSize(insert);
		final JButton edit = new JButton("Edit");
		setButtonSize(edit);
		final JButton delete = new JButton("Delete");
		setButtonSize(delete);
		final JButton logout = new JButton("Logout");
		setButtonSize(logout);
		final BrowseFrame parent = this;
		
		ActionListener act = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == insert) {
					DataFrame f = new DataFrame(parent, handle);
					parent.setVisible(false);
					f.setVisible(true);
				}
				if(e.getSource() == delete) {
					int i = records.getSelectedIndex();
					if(i == -1) {
						makeMessageBox("Select a record", "You must select a record first.");
						return;
					}
					Relation rel = (Relation) records.getSelectedValue();
					if(!makeYesNoBox("Delete record?", "Really delete '" + rel.getKey() + "'")) return;
					try {
						Record rec = new Record(handle);
						rec.delete(key, Integer.toString(rel.getCode()));
					} catch (SQLException err) {
						reportSQLError(err);
					}
					refresh();
				}
				if(e.getSource() == edit) {
					int i = records.getSelectedIndex();
					if(i == -1) {
						makeMessageBox("Select a record", "You must select a record first.");
						return;
					}
					Relation r = (Relation) records.getSelectedValue();
					String s = new String(); s += r.getCode();
					DataFrame f = new DataFrame(parent, handle, s);
					parent.setVisible(false);
					f.setVisible(true);
				}
				if(e.getSource() == logout) {
					parent.die();
				}
			}
		};
		
		insert.addActionListener(act);
		delete.addActionListener(act);
		logout.addActionListener(act);
		edit.addActionListener(act);
		
		p.add(insert);
		p.add(edit);
		p.add(delete);
		p.add(logout);
		content.add(p, BorderLayout.EAST);
	}
	
	
	/**
	 * Connects to the database, and initializes the JList
	 * @throws SQLException
	 */
	public void readRecords() throws SQLException {
        String sql = "SELECT `" + key + "`";
        if(auxField.length() > 0) sql += ",`" + auxField +"` ";
        sql += "FROM `" + handle.getDatabase() + "`.`" + handle.getTable() + "` ORDER BY `" + key + "`";
        
        ArrayList<Vector<String>> set = handle.fetchArrayList(sql);
		
		items = new Vector<Relation>();
		for(Vector<String> v : set) {
			try {
				Relation r = new Relation(Integer.parseInt(v.elementAt(0)), v.elementAt(1));
				r.setDisplayType(Relation.FULL);
				items.add(r);
			} catch (Exception e) {
				reportError(e);
			}
		}
		records.setListData(items);
	}
	
	public void refresh() {
		try {
			this.readRecords();
		}
		catch(SQLException e) {
			System.out.println("OOPS! " + e);
		}
	}
	
	public void makeMessageBox(String title, String msg) {
		JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void reportError(Exception e) {
		JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void reportSQLError(SQLException e) {
		JOptionPane.showMessageDialog(this, "SQL said: " + e.getMessage(), "Error code: " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
	}
	
	public boolean makeYesNoBox(String title, String msg) {
		return (JOptionPane.YES_OPTION ==
				JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE));
	}
	
	public void die() { alive = false; }
	public boolean alive() { return alive; }
	
	public static void setButtonSize(JButton button) {
		Dimension d = new Dimension(85,35);
        button.setPreferredSize(d);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
    }
	
}
