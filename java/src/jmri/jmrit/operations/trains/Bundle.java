// Bundle.java

package jmri.jmrit.operations.trains;

import java.util.ResourceBundle;

public class Bundle {
	
	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle")
				.getString(key);
	}

}
