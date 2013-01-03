// AutoSave.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsXml;

/**
 * Auto Save.  When enabled will automatically save operation files.
 * 
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */

public class AutoSave {
	
	static Thread autoSave = null;
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SC_START_IN_CTOR")
	public AutoSave(){
		synchronized (this) {
			if (Setup.isAutoSaveEnabled() && autoSave == null){
				autoSave = new Thread(new Runnable() {
					public void run() {
						saveFiles();
					}
				});
				autoSave.setName("Auto Save"); // NOI18N
				autoSave.start();
			}
		}
	}
	
	private synchronized void saveFiles(){
		while (true){			
			try {
				wait(60000);	// check every minute
			} catch (InterruptedException e) {}
			if (!Setup.isAutoSaveEnabled())
				break;
			if (OperationsXml.areFilesDirty()){
				log.debug("Detected dirty operation files");
				try {
					wait(60000);	// wait another minute before saving
				} catch (InterruptedException e) {}
				if (OperationsXml.areFilesDirty()){
					OperationsXml.save();
					log.info("Operation files automatically saved");
				}
			}
		}
		autoSave = null;	// done
	}	
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoSave.class.getName());
}
