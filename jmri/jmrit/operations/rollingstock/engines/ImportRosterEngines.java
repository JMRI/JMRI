// ImportRosterEngines.java

package jmri.jmrit.operations.rollingstock.engines;

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
import jmri.jmrit.operations.locations.Track;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Import engines from the jmri Roster
 * @author Daniel Boudreau Copyright (C) 2008
 *
 */
public class ImportRosterEngines extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	private static boolean fileValid = false;		// used to flag status messages
	EngineManager manager = EngineManager.instance();
	private static String defaultEngineLength = rb.getString("engineDefaultLength");
	private static String defaultEngineType = rb.getString("engineDefaultType");
	
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textId = new javax.swing.JLabel();

	// we use a thread so the status frame will work!
	public void run() {
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame(rb.getString("TitleImportEngines"));
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
				String road = re.getRoadName();
				if (road.length() > 12)
					road = road.substring(0, 12);
				textId.setText(road+" "+re.getRoadNumber());
				Engine engine = manager.getEngineByRoadAndNumber(road, re.getRoadNumber());
				if (engine == null){
					engine = manager.newEngine(road, re.getRoadNumber());
					String model = re.getModel();
					if (model.length() > 12)
						model = model.substring(0, 12);
					engine.setModel(model);
					// does this model already have a length?
					if (engine.getLength().equals(""))
						engine.setLength(defaultEngineLength);
					// does this model already have a type?
					if (engine.getType().equals(""))
						engine.setType(defaultEngineType);
					engine.setOwner(re.getOwner());
					enginesAdded++;
				} else{
					log.info("Can not add, engine number ("+re.getRoadNumber()+") road ("+re.getRoadName()+ ") already exists");
				}
			}
		}

		// kill status panel
		fstatus.dispose();

		if (enginesAdded>0) {
			JOptionPane.showMessageDialog(null, enginesAdded+" "+rb.getString("ImportEnginesAdded"),
					rb.getString("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					enginesAdded+" "+rb.getString("ImportEnginesAdded"),
					rb.getString("ImportFailed"), JOptionPane.ERROR_MESSAGE);
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

