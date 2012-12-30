// Bundle.java

package jmri.jmrit.operations.setup;

import java.util.ResourceBundle;

public class Bundle {
	
	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle")
				.getString(key);
	}

}
