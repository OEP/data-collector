import java.sql.*;
import java.util.*;
import java.io.*;

public class SQLHandler {
	private String host;
	private String database;
	private String table;
	private String user;
	private String password;
	private int queryCount = 0;
	private Connection conn;
	private Statement stmt;
	private ResultSetMetaData rsm;
	
	SQLHandler(String host, String database, String table, String user, String password) throws SQLException {
		this.host = host;
		this.database = database;
		this.table = table;
		this.user = user;
		this.password = password;
		this.initialize();
		this.rsm = this.fetchMetaData(this.table);
	}
	
	public String getHost(){ return host; }
	public String getDatabase() { return database; }
	public String getTable() { return table; }
	public String getUser() { return user; }
	public String getPassword() { return password; }
	
	/**
	 * This is for a quick access to a fresh Connection object.
	 * @return Connection
	 * @throws SQLException
	 */
	private void initialize() throws SQLException {
		 this.conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?" + 
                 "user=" + user + "&password=" + password);
		 this.stmt = conn.createStatement();
	}
	/**
	 * Quickly fetch a ResultSet
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	private ResultSet getResultSet(String sql) throws SQLException {
		this.toConsole("Executing: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		queryCount++;
		return rs;
	}
	
	/**
	 * Load ResultSetMetaData into memory
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	private ResultSetMetaData fetchMetaData(String t) throws SQLException {
		ResultSet rs = getResultSet("SELECT * FROM `"+database+"`.`"+t+"`");
		ResultSetMetaData rsm = rs.getMetaData();
		return rsm;
	}
	
	/**
	 * Report something to console
	 * @param msg
	 */
	private void toConsole(String msg) {
		System.out.println("*** SQLHandler: " + msg);
	}
	
	private void toConsole(Exception e) {
		System.out.println("*** SQLHandler Error: " + e);
	}
	
	
	/**
	 * General purpose query execution
	 * @param sql
	 * @return
	 */
	public boolean query(String sql) throws SQLException {
		this.toConsole("Executing: " + sql);
		stmt.execute(sql);
		this.queryCount++;
		return (stmt.getUpdateCount() == 1);
	}
	
	/**
	 * This fetches the column names of a given table.
	 * @param table
	 * @return Vector<String>
	 * @throws SQLException
	 */
	public Vector<String> fetchColumnNames(String table) throws SQLException {
		ResultSetMetaData rsm = fetchMetaData(table);
		
		Vector<String> v = new Vector<String>();
		for(int i = 1; i <= rsm.getColumnCount(); i++) {
			v.add(rsm.getColumnName(i));
		}
		
		return v;
	}
	
	/**
	 * Wrapper to use the default table.
	 * @return
	 * @throws SQLException
	 */
	public Vector<String> fetchColumnNames() throws SQLException {
		Vector<String> v = new Vector<String>();
		for(int i = 1; i <= rsm.getColumnCount(); i++) {
			v.add(rsm.getColumnLabel(i));
		}
		
		return v;
	}	
	
	/**
	 * Get column type for default
	 * @param i
	 * @return
	 * @throws SQLException
	 */
	public String fetchColumnDataType(int i) throws SQLException {
		return rsm.getColumnTypeName(i + 1);
	}
	
	public int fetchColumnSize(int i) throws SQLException {
		return rsm.getColumnDisplaySize(i + 1);
	}
	
	public int fetchColumnCount() throws SQLException {
		return rsm.getColumnCount() - 1;
	}
	
	public String fetchColumnName(int i) throws SQLException {
		return rsm.getColumnName(i + 1);
	}
	
	public Vector<String> fetchColumnComments() throws SQLException, UnsupportedEncodingException {
		Vector<String> v = new Vector<String>();
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet columnResultSet =
			dbMetaData.getColumns(null, user.toUpperCase(), table, null);
		while (columnResultSet.next()) {
			String comment = new String(columnResultSet.getBytes("REMARKS"), "utf-8");
			if(comment.length() == 0) comment = fetchColumnName(v.size());
			v.add(comment);
		}
		return v;
	}
	
	public boolean fetchIsNullable(int i) throws SQLException {
		return (rsm.isNullable(i + 1) == rsm.columnNullable);
	}
	
	
	/**
	 * Fetch a raw result set of a given SQL statement.
	 * @param sql
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ArrayList<Vector<String>> fetchArrayList(String sql) throws SQLException {
		ResultSet rs = getResultSet(sql);
		ResultSetMetaData rsm = rs.getMetaData();
		ArrayList<Vector<String>> set = new ArrayList<Vector<String>>();
		Vector<String> row = new Vector<String>();
		while(rs.next()) {
			for(int i = 1; i <= rsm.getColumnCount(); i++) {
				row.add(rs.getString(i));
			}
			set.add(row);
			row = new Vector<String>();
		}
		return set;
	}
	
	public Vector<String> fetchVector(String sql) throws Exception {
		ResultSet rs = getResultSet(sql);
		ResultSetMetaData rsm = rs.getMetaData();
		rs.next();
		Vector<String> v = new Vector<String>();
		for(int i = 1; i <= rsm.getColumnCount(); i++) {
			v.addElement(rs.getString(i));
		}
		return v;
	}

	
	/**
	 * This returns a String object which is useful to get a quick status bar for
	 * a JFrame, for example.
	 * @return String
	 */
	public String getWindowTitle() {
		return user + "@" + host + " :: " + "`" + database + "`.`" + table + "`";
	}
	
	/**
	 * Return current gross queries
	 * @return int
	 */
	public int getQueryCount() { return queryCount; }
	
}
