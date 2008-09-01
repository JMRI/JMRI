// ImportEngines.java

package jmri.jmrit.operations.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;

public class ImportEngines extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");
	
	private static boolean fileValid = false;		// used to flag status messages
	EngineManager manager = EngineManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();

	// we use a thread so the status frame will work!
	public void run() {
		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
		fc.addChoosableFileFilter(new textFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // cancelled
		if (fc.getSelectedFile() == null)
			return; // cancelled
		File f = fc.getSelectedFile();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return;
		}
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Import engines");
	   	fstatus.setLocationRelativeTo(null);
	   	fstatus.setSize (200,100);

	   	ps.add (textLine);
	   	ps.add(lineNumber);
	   	fstatus.getContentPane().add (ps);
		textLine.setText("Line number: ");
        textLine.setVisible(true);
        lineNumber.setVisible(true);
		fstatus.setVisible (true);

		// Now read the input file 
		boolean importOkay = false;
		int lineNum = 0;
		int enginesAdded = 0;
		String line = " ";
		String engineNumber;
		String engineRoad;
		String engineModel;
		String engineLength;
		String engineOwner ="";
		String engineBuilt ="";
		String engineLocation ="";
		String engineSecondary ="";

		while (line != null) {
			lineNumber.setText(Integer.toString(++lineNum));
			try {
				line = in.readLine();
			} catch (IOException e) {
				break;
			}

			if (line == null){
				importOkay = true;
				break;
			}

			line.trim();
			if (log.isDebugEnabled()) {
				log.debug("Import: " + line);
			}

			String[] inputLine = line.split("\\s+");
			int base = 0;
			if (!inputLine[0].equals("")){
				base--;		// skip over any spaces at start of line
			}

			if (inputLine.length > base+4){
				engineNumber = inputLine[base+1];
				engineRoad = inputLine[base+2];
				engineModel = inputLine[base+3];
				engineLength = inputLine[base+4];
				engineOwner ="";
				engineBuilt ="";
				engineLocation ="";
				engineSecondary ="";

				log.debug("Checking engine number ("+engineNumber+") road ("+engineRoad+ ") type ("+engineModel+ ") length ("+engineLength+")");
				if (engineNumber.length() > 10){
					JOptionPane.showMessageDialog(null, rb.getString("engineRoadNum"),
							"Engine ("+engineRoad+" "+engineNumber+") road number ("+engineNumber+") too long!",
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineRoad.length() > 12){
					JOptionPane.showMessageDialog(null, rb.getString("engineAttribute"),
							"Engine ("+engineRoad+" "+engineNumber+") road name ("+engineRoad+") too long!",
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineModel.length() > 12){
					JOptionPane.showMessageDialog(null, rb.getString("engineAttribute"),
							"Engine ("+engineRoad+" "+engineNumber+") type ("+engineModel+") too long!",
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineLength.length() > 4){
					JOptionPane.showMessageDialog(null, rb.getString("engineAttribute5"),
							"Engine ("+engineRoad+" "+engineNumber+") length ("+engineLength+") too long!",
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				Engine e = manager.getEngineByRoadAndNumber(engineRoad, engineNumber);
				if (e != null){
					log.info("Can not add, engine number ("+engineNumber+") road ("+engineRoad+ ") already exists");
				} else {

					if(inputLine.length > base+5){
						engineOwner = inputLine[base+5];
						if (engineOwner.length() > 12){
							JOptionPane.showMessageDialog(null, rb.getString("engineAttribute"),
									"Engine ("+engineRoad+" "+engineNumber+") owner ("+engineOwner+") too long!",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+6){
						engineBuilt = inputLine[base+6];
						if (engineBuilt.length() > 4){
							JOptionPane.showMessageDialog(null, rb.getString("engineAttribute5"),
									"Engine ("+engineRoad+" "+engineNumber+") built ("+engineBuilt+") too long!",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+7){
						engineLocation = inputLine[base+7];

					}
					// Location name can be one to three words 
					if(inputLine.length > base+8){
						if (!inputLine[8].equals("-")){
							engineLocation = engineLocation + " " +inputLine[base+8];
							if(inputLine.length > base+9){
								if (!inputLine[base+9].equals("-"))
									engineLocation = engineLocation + " " +inputLine[base+9];
							}
							// create secondary location if there's one
						}
						boolean foundDash = false;
						for (int i=base+8; i<inputLine.length; i++){
							if(inputLine[i].equals("-")){
								foundDash = true;
								if (inputLine.length > i)
									engineSecondary = inputLine[++i];
							} else if (foundDash)
								engineSecondary = engineSecondary + " " +inputLine[i];
						}
						log.debug("Engine ("+engineRoad+" "+engineNumber+") has secondary location ("+engineSecondary+")");
					}

					if (engineLocation.length() > 25){
						JOptionPane.showMessageDialog(null, rb.getString("engineAttribute"),
								"Engine ("+engineRoad+" "+engineNumber+") location ("+engineLocation+") too long!",
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (engineSecondary.length() > 25){
						JOptionPane.showMessageDialog(null, rb.getString("engineAttribute"),
								"Engine ("+engineRoad+" "+engineNumber+") secondary location ("+engineSecondary+") too long!",
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(engineLocation);
					SecondaryLocation sl = null;
					if (l == null && !engineLocation.equals("")){
						JOptionPane.showMessageDialog(null, "Engine ("+engineRoad+" "+engineNumber+") location ("+engineLocation+") does not exist",
								rb.getString("engineLocation"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (!engineSecondary.equals("")){
						sl = l.getSecondaryLocationByName(engineSecondary, SecondaryLocation.YARD);
						if (sl == null){
							sl = l.getSecondaryLocationByName(engineSecondary, SecondaryLocation.SIDING);
						}
						if (sl == null){
							sl = l.getSecondaryLocationByName(engineSecondary, SecondaryLocation.STAGING);
						}
						if (sl == null){
							JOptionPane.showMessageDialog(null, "Engine ("+engineRoad+" "+engineNumber+") secondary location ("+engineLocation+", "+engineSecondary+") does not exist",
									rb.getString("engineLocation"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					log.debug("Add engine ("+engineRoad+" "+engineNumber+") owner ("+engineOwner+") built ("+engineBuilt+") location ("+engineLocation+", "+engineSecondary+")");
					Engine engine = manager.newEngine(engineRoad, engineNumber);
					engine.setModel(engineModel);
					engine.setLength(engineLength);
					engine.setOwner(engineOwner);
					engine.setBuilt(engineBuilt);
					enginesAdded++;

					if (l != null){
						String status = engine.setLocation(l,sl);
						if (!status.equals(Engine.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(null,
									"Can't set engine ("+engineRoad+" "+engineNumber+") type ("+engineModel+") because of location ("+engineLocation+", "+engineSecondary+ ") "+ status,
									"Can not update engine location",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}else{
//						log.debug("No location for engine ("+engineRoad+" "+engineNumber+")");
					}
				}
			}else{
				log.debug("Import missing one of four required engine attributes");
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// kill status panel
		fstatus.setVisible (false);

		if (importOkay) {
			JOptionPane.showMessageDialog(null, enginesAdded+" engines added to roster",
					"Successful import!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					enginesAdded+" engines added to roster",
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
			.getInstance(ImportEngines.class.getName());
}

