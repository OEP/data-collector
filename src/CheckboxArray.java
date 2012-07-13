import java.util.Vector;
import javax.swing.*;

public class CheckboxArray {
	private Vector<JCheckBox> checks = new Vector<JCheckBox>();
	
	public void add(JCheckBox c) {
		checks.add(c);
	}
	
	public String getString() {
		String out = new String();
		for(int i = 0; i < checks.size(); i++) {
			JCheckBox b = checks.elementAt(i);
			if(b.isSelected()) out += '1';
			else out+='0';
		}
		return out;
	}
	
	public void setCheckboxes(String val) throws Exception {
		if(val.length() != checks.size()) {
			throw new Exception("Invalid checkbox string size for '" + val + "'");
		}
		
		for(int i = 0; i < checks.size(); i++) {
			JCheckBox b = checks.elementAt(i);
			if(val.charAt(i) == '1') {
				b.setSelected(true);
			}
			else
				b.setSelected(false);
		}
	}
}
