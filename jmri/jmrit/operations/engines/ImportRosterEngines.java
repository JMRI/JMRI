// ImportEngines.java

package jmri.jmrit.operations.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

public class ImportRosterEngines extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");
	
	private static boolean fileValid = false;		// used to flag status messages
	EngineManager manager = EngineManager.instance();
	private static String defaultEngineLength = "50";
	
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textId = new javax.swing.JLabel();

	// we use a thread so the status frame will work!
	public void run() {
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Import engines from roster");
	   	fstatus.setLocationRelativeTo(null);
	   	fstatus.setSize (200,100);

	   	ps.add (textEngine);
	   	ps.add(textId);
	   	fstatus.getContentPane().add (ps);
		textEngine.setText("Add engine: ");
        textEngine.setVisible(true);
        textId.setVisible(true);
		fstatus.setVisible (true);

		// Now get engines from the JMRI roster 
		boolean importOkay = true;
		int enginesAdded = 0;
		
		List engines = Roster.instance().matchingList(null, null, null, null, null, null, null);
		
		for (int i=0; i<engines.size(); i++){
			RosterEntry re = (RosterEntry)engines.get(i);
			// add engines that have a road name and number
			if (!re.getRoadName().equals("") && !re.getRoadNumber().equals("") ){
				textId.setText(re.getRoadName()+" "+re.getRoadNumber());
				Engine engine = manager.getEngineByRoadAndNumber(re.getRoadName(), re.getRoadNumber());
				if (engine == null){
					engine = manager.newEngine(re.getRoadName(), re.getRoadNumber());
					engine.setModel(re.getModel());
					engine.setLength(defaultEngineLength);
					engine.setOwner(re.getOwner());
					enginesAdded++;
				} else{
					log.info("Can not add, engine number ("+re.getRoadNumber()+") road ("+re.getRoadName()+ ") already exists");
				}
			}
		}

		// kill status panel
		fstatus.setVisible (false);

		if (enginesAdded>0) {
			JOptionPane.showMessageDialog(null, enginesAdded+" engines added to operations roster",
					"Successful import!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					enginesAdded+" engines added to operations roster",
					"Import failed", JOptionPane.ERROR_MESSAGE);
		}
	}




	
	private class textFilter extends javax.swing.filechooser.FileFilter {
		
		public boolean accept(File f){
			if (f.isDirectory())
			return true;
			String name = f.getName();
			if (name.matches(".*\\.txt"))
				return true;
			else
				return false;
		}
		
		public String getDescription() {
			return "Text Documents (*.txt)";
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(ImportRosterEngines.class.getName());
}

