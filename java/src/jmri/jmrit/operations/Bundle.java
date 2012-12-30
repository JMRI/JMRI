// Bundle.java

package jmri.jmrit.operations;

import java.util.ResourceBundle;

public class Bundle {
	
	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle")
				.getString(key);
	}

}
