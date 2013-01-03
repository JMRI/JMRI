// ImportRosterEngines.java

package jmri.jmrit.operations.rollingstock.engines;

import java.text.MessageFormat;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Import engines from the jmri Roster
 * @author Daniel Boudreau Copyright (C) 2008
 *
 */
public class ImportRosterEngines extends Thread {
		
	EngineManager manager = EngineManager.instance();
	private static String defaultEngineLength = Bundle.getMessage("engineDefaultLength");
	private static String defaultEngineType = Bundle.getMessage("engineDefaultType");
	private static String defaultEngineHp = Bundle.getMessage("engineDefaultHp");
	
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textId = new javax.swing.JLabel();

	// we use a thread so the status frame will work!
	public void run() {
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame(Bundle.getMessage("TitleImportEngines"));
	   	fstatus.setLocationRelativeTo(null);
	   	fstatus.setSize (200,100);

	   	ps.add (textEngine);
	   	ps.add(textId);
	   	fstatus.getContentPane().add (ps);
		textEngine.setText(Bundle.getMessage("AddEngine"));
        textEngine.setVisible(true);
        textId.setVisible(true);
		fstatus.setVisible (true);

		// Now get engines from the JMRI roster 
		int enginesAdded = 0;
		
		List<RosterEntry> engines = Roster.instance().matchingList(null, null, null, null, null, null, null);
		
		for (int i=0; i<engines.size(); i++){
			RosterEntry re = engines.get(i);
			// add engines that have a road name and number
			if (!re.getRoadName().equals("") && !re.getRoadNumber().equals("") ){
				String road = re.getRoadName();
				if (road.length() > Control.max_len_string_attibute)
					road = road.substring(0, Control.max_len_string_attibute);
				textId.setText(road+" "+re.getRoadNumber());
				Engine engine = manager.getByRoadAndNumber(road, re.getRoadNumber());
				if (engine == null){
					engine = manager.newEngine(road, re.getRoadNumber());
					String model = re.getModel();
					if (model.length() > Control.max_len_string_attibute)
						model = model.substring(0, Control.max_len_string_attibute);
					engine.setModel(model);
					// does this model already have a length?
					if (engine.getLength().equals(""))
						engine.setLength(defaultEngineLength);
					// does this model already have a type?
					if (engine.getType().equals(""))
						engine.setType(defaultEngineType);
					// does this model already have a hp?
					if (engine.getHp().equals(""))
						engine.setHp(defaultEngineHp);
					String owner = re.getOwner();
					if (owner.length() > Control.max_len_string_attibute)
						owner = owner.substring(0, Control.max_len_string_attibute);
					engine.setOwner(owner);
					enginesAdded++;
				} else{
					log.info("Can not add, engine number ("+re.getRoadNumber()+") road ("+re.getRoadName()+ ") already exists");
				}
			}
		}

		// kill status panel
		fstatus.dispose();

		if (enginesAdded>0) {
			JOptionPane.showMessageDialog(null, 
					MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),new Object[]{enginesAdded}),
					Bundle.getMessage("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"),new Object[]{enginesAdded}),
					Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ImportRosterEngines.class.getName());
}

