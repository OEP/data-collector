
public class Relation {
	private String key;
	private int code;
	private int displayType = 0;

	Relation(int i, String s) {
		code = i;
		key = s;
	}
	
	public String toString() {
		switch(displayType) {
		case FULL: return new String(code + " - " + key);
		default: return key;
		}
	}
	
	public int getCode() {
		return code;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setDisplayType(int i) { displayType = i; }
	
	public static final int TEXT_ONLY = 0;
	public static final int FULL = 1;
}
