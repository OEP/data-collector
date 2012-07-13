import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
public class DataCollector {
	private SQLHandler handle;
	
	public static void main(String [] args) {
		while(true) {
			LoginFrame login = new LoginFrame("Please Login...", "localhost", "clinic_analysis", "retrospective_charts");
//			LoginFrame login = new LoginFrame("Please Login...", "pk-fire.com", "clinic_analysis", "retrospective_charts");
//			LoginFrame login = new LoginFrame("Please Login...", "clinic_analysis", "retrospective_charts");

			login.setVisible(true);
			
			while(login.alive()) {
				try { Thread.sleep(300); } catch(Exception e) { }
			}
			
			String [] fields = login.getFields();
			String host = fields[0]; String db = fields[1]; String table = fields[2]; String user = fields[3]; String password = fields[4];

			MessageFrame msg = new MessageFrame("Authenticating...", "Verifying username and password...");
			
			login.setVisible(false);
			msg.setVisible(true);
			login.dispose();
			
			SQLHandler handle;
			try {
				handle = new SQLHandler(host, db, table, user, password);
			} catch (SQLException e) {
				msg.dispose();
				reportSQLError(e);
				continue;
			}
			
			BrowseFrame frame = new BrowseFrame(handle, "chart_id", "patient_name");
			msg.setVisible(false);
			frame.setVisible(true);
			msg.dispose();
			
			while(frame.alive()) {
				try { Thread.sleep(300); } catch(Exception e) { }
			}
			showMessage("Queries used", handle.getQueryCount() + " queries used this session");
			frame.setVisible(false);
			frame.dispose();
		}
	}
	
	public static void reportSQLError(SQLException e) {
		JOptionPane.showMessageDialog(null, "SQL said: " + e.getMessage(), "Error code: " + e.getErrorCode(), JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
}


