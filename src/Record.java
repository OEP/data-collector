import java.util.*;

import javax.swing.*;

import java.sql.*;
public class Record {
	private Hashtable data = new Hashtable();
	private Hashtable types = new Hashtable();
	private SQLHandler handle;
	private String LOGTABLE = "entry_log";
	
	Record(SQLHandler handle) {
		this.handle = handle;
	}

	public void insert() throws SQLException, Exception {
		Set keys = data.keySet();
		Iterator it = keys.iterator();
		String part1 = new String("("), part2 = new String("(");
		String showVal = new String();
		while(it.hasNext()) {
			String key = (String) it.next();
			Object c = data.get(key);
			String type = (String) types.get(key);
			String val = this.extractValue(c, type);
			
			if(key.compareTo("chart_id") == 0 || key.compareTo("patient_name") == 0) {
				showVal += val + " ";
			}
			
			if(val.length() == 0) {
				val = "NULL";
			}
			else
				val = "'" + val + "'";
			
			part1 += '`' + key + "`,";
			part2 += val + ",";
		}
		part1 = part1.substring(0, part1.length() - 1) + ")";
		part2 = part2.substring(0, part2.length() - 1) + ")";
		String sql = "INSERT INTO `" + handle.getDatabase() + "`.`" + handle.getTable() + "` " + part1 + " VALUES " + part2;
		handle.query(sql);
		//this.addLogEntry(handle.getUser() + " inserted record " + showVal);
	}
	
	public void update(String key, String id) throws SQLException, Exception {
		Set fields = data.keySet();
		Iterator it = fields.iterator();
		String values = new String();
		while(it.hasNext()) {
			String field = (String) it.next();
			Object c = data.get(field);
			String type = (String) types.get(field);
			String val = this.extractValue(c, type);
			
			if(val.length() == 0) {
				val = "NULL";
			}
			else
				val = "'" + val + "'";
			
			values += "`" + field +"` = " + val + ",";
		}
		values = values.substring(0, values.length() - 1); // take off trailing comma of SQL
		
		String sql = "UPDATE `" + handle.getDatabase() + "`.`" + handle.getTable() + "` SET " + values
			+ " WHERE `" + key + "` = '" + id + "'";
		handle.query(sql);
		//this.addLogEntry(handle.getUser() + " updated record " + id);
	}
	
	public void delete(String key, String id) throws SQLException {
		String sql = "DELETE FROM `" + handle.getDatabase() + "`.`" + handle.getTable()
			+ "` WHERE `" + key + "` = '" + id + "'";
		handle.query(sql);
		//this.addLogEntry(handle.getUser() + " deleted record " + id);
	}
	
	public void setValue(String key, Object value) {
		if(data.containsKey(key))
			data.remove(key);
		data.put(key, value);
	}
	
	public void setDataTable(Hashtable table) {
		this.data = table;
	}
	
	public void setDataTypes(Hashtable types) {
		this.types = types;
	}
	
	public void setFieldsTable(Hashtable table) {
		types = table;
	}
	
	public String sanitize(String in) {
		return in.replaceAll("[\"'\\]", "");
	}
		

	public boolean verifyData() throws Exception {
		Enumeration keys = data.keys();
		Enumeration dataTypes = types.elements();
		Enumeration components = data.elements();
		Vector<String> problems = new Vector<String>();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String type = (String) dataTypes.nextElement();
			Object c = components.nextElement();
			if(!validData(c, type)) {
				problems.add(key);
			}
			
		}
		if(problems.size() > 0) {
			String msg = new String();
			if(problems.size() <= 5) {
				msg = "The following fields are missing: " + problems + ". Continue anyway?"; 
			}
			else
				msg = "You have " + problems.size() + " empty fields. Continue anyway?";
			
			int selection = JOptionPane.showConfirmDialog(
					null,
					msg,
					"You have " + problems.size() + " null values.",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		
			return (selection == JOptionPane.YES_OPTION);

		}
		return true;
		
	}
	
	public boolean validData(Object c, String type) throws Exception {
		if(c == null) {
			return false;
		}
		
		String val = extractValue(c, type);
		if(val.length() > 0) return true;
		else return false;
	}
	
	public String extractValue(Object c, String type) throws Exception {
		if(type.compareTo("Boolean") == 0) {
			JComboBox b = (JComboBox) c;
			Relation r = (Relation) b.getSelectedItem();
			switch(r.getCode()) {
				case 0: return "0";
				case 1: return "1";
				default: return "";
			}
		}
		if(type.compareTo("NonNullBoolean") == 0) {
			JCheckBox check = (JCheckBox) c;
			if(check.isSelected())
				return "1";
			else
				return "0";
		}
		if(type.compareTo("JTextField") == 0) {
			JTextField t = (JTextField) c;
			if(t == null || t.getText().length() == 0) return "";
			else return t.getText();
		}
		if(type.compareTo("CheckboxArray") == 0) {
			CheckboxArray ca = (CheckboxArray) c;
			return ca.getString();
		}
		if(type.compareTo("RJComboBox") == 0) {
			JComboBox b = (JComboBox) c;
			Relation r = (Relation) b.getSelectedItem();
			if(r.getCode() == -1) return "";

			String out = new String();
			out += r.getCode();
			return out;
		}
		if(type.compareTo("MaleFemale") == 0) {
			JComboBox b = (JComboBox) c;
			String str = (String) b.getSelectedItem();
			if(str.compareTo("?") == 0) return "";
			else return str;
		}

			
		throw new Exception("Data type '" + type + "' not implemented yet for extractValue()");
	}

	/* May decide not to do this
	public void addLogEntry(String msg) throws SQLException {
		String sql = "INSERT INTO `" + handle.getDatabase() + "`.`" + LOGTABLE + "` (`id`,`time`,`msg`) "
			+ "VALUES (NULL, NULL, '" + msg + "')";
		handle.query(sql);
	}*/

}
