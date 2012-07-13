import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.swing.*;



/**
 * Create a dynamic JFrame which mirrors the structure of a given
 * database's table.
 * @author pkilgo
 *
 */
class DataFrame extends JFrame {
	private GridBagLayout gb;
	private GridBagConstraints gbc;
	private Hashtable fields;
	private Hashtable types;
	private JPanel content;
	private JScrollPane scroller;
	private SQLHandler handle;
	private final JButton submit;
	private final JButton cancel;
	private BrowseFrame parent;
	private boolean alive = true;
	private boolean editing = false;
	private String primary_key = "chart_id";
	private String editKey;
	private String prototype = "wwwwwwwwwwwwwww";
	
	private Vector<String> originalData;
	
	private String dates [] = new String [] { "first_review_date", "second_review_date", "patient_iov", "patient_last_kept_visit", "patient_dob"};
	
	DataFrame(BrowseFrame parent, SQLHandler handle) {
		this.parent = parent;
		this.setTitle(handle.getWindowTitle() + " :: New record");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.fields = new Hashtable();
		this.types = new Hashtable();
		this.handle = handle;
		this.submit = new JButton("Submit");
		this.cancel = new JButton("Cancel");
		this.editKey = new String();
		this.scroller = new JScrollPane(content);
		this.populate();
		this.setLocationRelativeTo( null );
	}
	
	DataFrame(BrowseFrame parent, SQLHandler handle, String key) {
		this.parent = parent;
		this.setTitle(handle.getWindowTitle() + " :: Editing record");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.fields = new Hashtable();
		this.types = new Hashtable();
		this.handle = handle;
		this.submit = new JButton("Update");
		this.cancel = new JButton("Cancel");
		this.editing = true;
		this.editKey = key;
		this.populate();
		this.setLocationRelativeTo( null );
	}

	public void populate() {
		gb = new GridBagLayout();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.setLayout(gb);
		
		content = (JPanel) this.getContentPane();
		this.scroller = new JScrollPane(content);
		this.setContentPane(scroller);
		this.scroller.getVerticalScrollBar().setUnitIncrement(20);
		
		
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		JLabel heading;
		if(editing) {
			heading = new JLabel("Editing record '" + editKey + "'");
		} else {
			heading = new JLabel("New record");
		}
		content.add(heading,gbc);
		gbc.gridwidth = 1;
		gbc.gridy++;
		try {
        	this.addFields();
		} catch (Exception ex) {
		    // handle any errors
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Exception thrown", 0);
			System.out.println(ex);
			this.die();
		}
		
		ActionListener act = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean destruct = false;
				if(e.getSource() == submit) {
					try {
						Record r = new Record(handle);
						r.setDataTypes(types);
						r.setDataTable(fields);
						if(r.verifyData()) {
							if(!editing)
								r.insert();
							else
								r.update(primary_key, editKey);
							destruct = true;
						}
					}
					catch (Exception err) { reportError(err); }
				}
				if(e.getSource() == cancel) {
					destruct = makeYesNoBox("Abandon ship?", "Really discard this record?");
				}
				
				
				if(destruct) {
					parent.setVisible(true);
					parent.refresh();
					setVisible(false);
					die();
					dispose();
				}
			}
		};
		
		submit.addActionListener(act);
		cancel.addActionListener(act);
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new FlowLayout());
		bottom.add(submit);
		bottom.add(cancel);
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		content.add(bottom, gbc);
		this.pack();
		int height = this.getHeight();
		System.out.println(height);
		this.setMinimumSize(new Dimension(520, 600));
		this.setMaximumSize(new Dimension(600, 750));
		this.setSize(new Dimension(520, 600));
	}
	
	/**
	 * This function
	 * @throws Exception
	 */
	public void addFields() throws Exception {
		// These will be the JLabel's for our UI
		Vector<String> names = handle.fetchColumnNames();
		Vector<String> labels = handle.fetchColumnComments();
		
		// A little custom thing to draw a line to seperate certain data
		boolean drawnLine = false;
		
		// If we're editing, we need to fetch the data originally in the DB
		if(editing) {
			originalData = handle.fetchVector("SELECT * FROM `" +
					handle.getDatabase() + "`.`" +
					handle.getTable() + "` " +
					"WHERE `chart_id` = '" + editKey + "' LIMIT 1");
		}
		
		
		// This HUGE loop builds our user interface from top to bottom
		// based on the database structure
		for(int i = 0; i < names.size(); i++) {
			String name = names.elementAt(i);
			String uiname = labels.elementAt(i);
			String type = handle.fetchColumnDataType(i);
			boolean nullable = handle.fetchIsNullable(i);
			int length = handle.fetchColumnSize(i);
			
			boolean date = isDate(name);
			
			if(!drawnLine && name.startsWith("med_preventitive_")) {
				gbc.gridwidth = 3;
				gbc.gridx = 0;
				LinePanel p = new LinePanel(LinePanel.HORIZONTAL);
				p.setSize(250, 30);
				p.repaint();
				content.add(p, gbc);
				gbc.gridwidth = 1;
				gbc.gridy++;
				drawnLine = true;
			}
			
			
			/*
			 * Special label stuff
			 */
			String label = new String();
			
			// Cue the user of special formatting
			if(date) label = uiname + " (MMDDYYYY):";
			else label = uiname + ":";
			
			
			gbc.gridx = 0;
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JLabel(label));
			content.add(p,gbc);
			gbc.gridx = 1;
			
			
			/*
			 * Special exception: username shouldn't be changed
			 */
			
			if(name.compareTo("entered_by") == 0) {
				this.addTextField(i, handle.getUser());
				gbc.gridy++;
				continue;
			}
			
			
			// We decide what type of component setup to use based on the data type
			// in the database and its length, possibly even nullability
			if(type.compareTo("BINARY") == 0 && length == 1 && nullable) { // this is a simple boolean with null
				this.addBool(i);
			}
			else if(type.compareTo("BINARY") == 0 && length == 1 && !nullable) { //simple nullable type
				this.addCheckbox(i);
			}
			else if(type.compareTo("CHAR") == 0 && length == 1) { //ASSUMING: Male/Female
				this.addMaleFemale(i);
			}
			else if(type.compareTo("BINARY") == 0 && length == 6) { // medication workspace
				this.addMedWorkspace(i);
			}
			else if(type.compareTo("INT") == 0 && length == 2) {
				this.addRelationalComboBox(i);
			}
			else if(type.compareTo("INT") == 0) {
				this.addTextField(i, false);
			}
			else {
				this.addTextField(i);
			}
			//rsm.
			gbc.gridy++;
		}
	}
	
	public void addCheckbox(int index) throws SQLException {
		String name = handle.fetchColumnName(index);
		JCheckBox cb = new JCheckBox();
		fields.put(name, cb);
		types.put(name, "NonNullBoolean");
		content.add(cb, gbc);
		
		if(editing) {
			String val = originalData.elementAt(index);
			if(val == null) {
				cb.setSelected(false);
			} else if(val.compareTo("1") == 0) {
				cb.setSelected(true);
			} else {
				cb.setSelected(false);
			}
		}
	}
	
	/**
	 * This adds a little JComboBox with either ?/M/F as its possibilities. This is
	 * really only useful for the one special case of gender. :)
	 * @param index
	 * @throws SQLException
	 */
	public void addMaleFemale(int index) throws SQLException {
		String name = handle.fetchColumnName(index);
		JComboBox c = new JComboBox();
		c.addItem("?");
		c.addItem("M");
		c.addItem("F");
		c.setPrototypeDisplayValue(prototype);
		fields.put(name, c);
		types.put(name, "MaleFemale");
		content.add(c,gbc);
		
		if(editing) {
			String val = originalData.elementAt( index );
			if(val == null) {
				c.setSelectedIndex(0);
				return;
			}
			if(val.compareTo("M") == 0) c.setSelectedIndex(1);
			else if(val.compareTo("F") == 0) c.setSelectedIndex(2);
			else c.setSelectedIndex(0);
		}
	}
	
	/**
	 * This is an array of six checkboxes that forms a binary code based on 
	 * the patient's medication flow. These binary codes have semantic meaning,
	 * grouped by the three. The first three tell about the medications the patient
	 * is taking during their first visit. The second block of three tell what the
	 * patient is on during their last kept visit. In each block of three, the first
	 * indicates if the patient is on that medicine at the time of the appointment.
	 * The second indicates if the doctor ordered the patient to stop taking that 
	 * medicine. The third indicates if the doctor prescribed that type of medicine.
	 * @param index
	 * @throws SQLException
	 * @throws Exception
	 */
	public void addMedWorkspace(int index) throws SQLException, Exception {
		String name = handle.fetchColumnName(index);
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		CheckboxArray ca  = new CheckboxArray();
		
		for(int i = 0; i < 6; i++) {
			if(i == 3) {
				panel.add(new LinePanel(LinePanel.DIAGONAL_BLTR));
			}
			JCheckBox b = new JCheckBox();
			ca.add(b);
			panel.add(b);
		}
		content.add(panel,gbc);
		fields.put(name, ca);
		types.put(name, "CheckboxArray");
		
		if(editing) {
			String val = originalData.elementAt( index );
			
			if(val == null)	return;
			else ca.setCheckboxes(val);
		}
	}
	
	/**
	 * This is a JComboBox that is a human-readable translation of a couple of integer
	 * substitutions. The substitions are keyed in the database table named "key_" +
	 * the column's name.
	 * @param index
	 * @throws SQLException
	 */
	public void addRelationalComboBox(int index) throws SQLException {
		String name = handle.fetchColumnName(index);
		String key_field = "key_" + name;
		String sql = "SELECT * FROM `" + handle.getDatabase() + "`.`" + key_field + "` ORDER BY `id` DESC";
		String val = new String(); if(editing) val = originalData.elementAt(index);
		JComboBox cbox = new JComboBox();
        
        cbox = new JComboBox();
        cbox.addItem(new Relation(-1, "?"));
        cbox.setPrototypeDisplayValue(prototype);
        
        ArrayList<Vector<String>> set = handle.fetchArrayList(sql);
        
        int i = 1;
        boolean found = false;
        if(val == null) found = true;
	    for(Vector<String> v : set) {
	    	cbox.addItem(new Relation(Integer.parseInt(v.elementAt(0)), v.elementAt(1)));
	    	if(editing && !found && v.elementAt(0).compareTo(val) != 0) i++;
	    	else found = true;
	    }
	    
	    
	    content.add(cbox,gbc);
	    fields.put(name, cbox);
	    types.put(name, "RJComboBox");
	    
	    if(!editing || val == null) {
	    	cbox.setSelectedIndex(0);
	    }
	    else {
	    	cbox.setSelectedIndex(i);
	    }
	}
	
	public void addTextField(int index) throws SQLException {
		this.addTextField(index, new String(), true);
	}
	
	public void addTextField(int index, String value) throws SQLException {
		this.addTextField(index, value, true);
	}
	
	public void addTextField(int index, boolean normal) throws SQLException {
		this.addTextField(index, new String(), normal);
	}
	
	/**
	 * This and the above methods handle adding a basic textbox for editable or
	 * non-editable data. The boolean arguement implies the textbox acts like a
	 * "normal" textbox, meaning that all characters are specified. If this is set
	 * to false, it implies that the textbox will only allow numeric characters.
	 * @param index
	 * @param val
	 * @param b
	 * @throws SQLException
	 */
	
	public void addTextField(int index, String val, boolean normal) throws SQLException {
		String name = handle.fetchColumnName(index);
		int size = handle.fetchColumnSize(index);
		ImprovedText t = new ImprovedText();
		t.setDocument(new LimitDocument(size));
		t.setColumns(prototype.length());
		
		if(val.length() > 0) {
			t.setEditable(false);
			t.setText(val);
		}
		
		t.setNormal(normal);
		
		fields.put(name, t);
		types.put(name, "JTextField");
		content.add(t,gbc);
		
		if(editing) {
			String dbValue = originalData.elementAt( index );
			
			if(val == null)	t.setText("");
			else t.setText(dbValue);
		}
	}
	
	/**
	 * This method adds a simple JComboBox with "?", "True", and "False" in it.
	 * @param index
	 * @throws SQLException
	 */
	public void addBool(int index) throws SQLException {
		String name = handle.fetchColumnName(index);
		JComboBox c = new JComboBox();
		c.addItem(new Relation(-1, "?"));
		c.addItem(new Relation(1, "True"));
		c.addItem(new Relation(0, "False"));
		c.setPrototypeDisplayValue(prototype);
		fields.put(name, c);
		types.put(name, "Boolean");
		content.add(c,gbc);
		
		if(editing) {
			String dbValue = originalData.elementAt( index );
			
			if(dbValue == null) {
				c.setSelectedIndex(0);
				return;
			}
			
			if(dbValue.compareTo("1") == 0)
				c.setSelectedIndex(1);
			else
				c.setSelectedIndex(2);
		}
	}

	public boolean isDate(String type) {
		for(int i = 0; i < dates.length; i++)
			if(type.compareToIgnoreCase(dates[i]) == 0) return true;
			//else System.out.println(type + " != " + dates[i]);
		return false;			
	}
	
	public boolean alive() { return alive; }
	public void die() { alive = false; }
	public void revive() { alive = true; }
	
	public void makeMessageBox(String title, String msg) {
		JOptionPane.showMessageDialog(this, msg, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public void reportError(Exception e) {
		String title = e.getClass().getSimpleName();
		JOptionPane.showMessageDialog(this, e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
	}
	
	public void reportSQLError(SQLException e) {
		JOptionPane.showMessageDialog(this, "SQL said: " + e.getMessage(), "Error code: " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
	}
	
	public boolean makeYesNoBox(String title, String msg) {
		return (JOptionPane.YES_OPTION ==
				JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE));
	}
}

/**
 * This is a simple class to draw a line in a panel. The "DIAGONAL" integer values mean
 * draw it from "bottom-left" to "top-right" (BLTR) and "top-left" to "bottom-right" (TLBR).
 * @author pkilgo
 *
 */
class LinePanel extends JPanel {
	int type;
	LinePanel(int type) {
		this.type = type;
	}
	public void paintComponent(Graphics g){
		super.repaint();
		switch(type) {
		case VERTICAL: int xmid = this.getWidth() / 2; g.drawLine(xmid, 0, xmid, this.getHeight()); break;
		case DIAGONAL_BLTR: g.drawLine(0, this.getHeight(), this.getWidth(), 0); break;
		case DIAGONAL_TLBR: g.drawLine(0, 0, this.getWidth(), this.getHeight()); break;
		case HORIZONTAL: int ymid = this.getHeight() / 2; g.drawLine(0, ymid, this.getWidth(), ymid); break;
		}
		
	}
	
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	public static final int DIAGONAL_BLTR = 2;
	public static final int DIAGONAL_TLBR = 3;
}