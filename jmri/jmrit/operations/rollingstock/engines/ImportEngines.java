// ImportEngines.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;

/**
 * This routine will import engines into the operation database.
 * 
 * Each field is space or comma delimited.  Field order:
 * Number Road Type Length Owner Year Location
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.15 $
 */
public class ImportEngines extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	private static String defaultEngineType = rb.getString("engineDefaultType");
	private static String defaultEngineHp = rb.getString("engineDefaultHp");

	EngineManager manager = EngineManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();

	// we use a thread so the status frame will work!
	public void run() {
		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
		fc.addChoosableFileFilter(new ImportFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // Canceled
		if (fc.getSelectedFile() == null)
			return; // Canceled
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
		boolean comma = false;
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
		String engineTrack ="";
		String[] inputLine;
		
		// does the file name end with .csv?
		if (f.getAbsolutePath().endsWith(".csv")){
			log.info("Using comma as delimiter for import cars");
			comma = true;
		}

		while (true) {
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
			
			// has user canceled import?
			if (!fstatus.isShowing())
				break;
			
			line = line.trim();
			if (log.isDebugEnabled()) {
				log.debug("Import: " + line);
			}
			if (line.equalsIgnoreCase("comma")){
				log.info("Using comma as delimiter for import engines");
				comma = true;
			}
			// use comma as delimiter if found otherwise use spaces
			if (comma)
				inputLine = parseCommaLine(line, 9);
			else
				inputLine = line.split("\\s+");
			
			if (inputLine.length < 1){
				log.debug("Skipping blank line");
				continue;
			}
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
				engineTrack ="";

				log.debug("Checking engine number ("+engineNumber+") road ("+engineRoad+ ") model ("+engineModel+ ") length ("+engineLength+")");
				if (engineNumber.length() > Control.MAX_LEN_STRING_ROAD_NUMBER){
					JOptionPane.showMessageDialog(null, 
							"Engine ("+engineRoad+" "+engineNumber+") road number ("+engineNumber+") too long!",
							rb.getString("engineRoadNum"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineRoad.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							"Engine ("+engineRoad+" "+engineNumber+") road name ("+engineRoad+") too long!",
							MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineModel.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							"Engine ("+engineRoad+" "+engineNumber+") model ("+engineModel+") too long!",
							MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (!EngineModels.instance().containsName(engineModel)){
					int results = JOptionPane.showConfirmDialog(null,
							"Engine ("+engineRoad+" "+engineNumber+") \n"+MessageFormat.format(rb.getString("modelNameNotExist"),new Object[]{engineModel}),
							rb.getString("engineAddModel"),
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (results == JOptionPane.YES_OPTION)
						EngineModels.instance().addName(engineModel);
					else if (results == JOptionPane.CANCEL_OPTION)
						break;		
				}
				if (engineLength.length() > Control.MAX_LEN_STRING_LENGTH_NAME){
					JOptionPane.showMessageDialog(null, 
							"Engine ("+engineRoad+" "+engineNumber+") length ("+engineLength+") too long!",
							rb.getString("engineAttribute5"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				Engine e = manager.getByRoadAndNumber(engineRoad, engineNumber);
				if (e != null){
					log.info("Can not add, engine number ("+engineNumber+") road ("+engineRoad+ ") already exists");
				} else {

					if(inputLine.length > base+5){
						engineOwner = inputLine[base+5];
						if (engineOwner.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
							JOptionPane.showMessageDialog(null, 
									"Engine ("+engineRoad+" "+engineNumber+") owner ("+engineOwner+") too long!",
									rb.getString("engineAttribute"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+6){
						engineBuilt = inputLine[base+6];
						if (engineBuilt.length() > Control.MAX_LEN_STRING_BUILT_NAME){
							JOptionPane.showMessageDialog(null, 
									"Engine ("+engineRoad+" "+engineNumber+") built ("+engineBuilt+") too long!",
									rb.getString("engineAttribute5"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+7){
						engineLocation = inputLine[base+7];
					}
					// Location name can be one to three words 
					if(inputLine.length > base+8){
						if (!inputLine[base+8].equals("-")){
							engineLocation = engineLocation + " " +inputLine[base+8];
							if(inputLine.length > base+9){
								if (!inputLine[base+9].equals("-"))
									engineLocation = engineLocation + " " +inputLine[base+9];
							}
							// create track location if there's one
						}
						boolean foundDash = false;
						for (int i=base+8; i<inputLine.length; i++){
							if(inputLine[i].equals("-")){
								foundDash = true;
								if (inputLine.length > i+1)
									engineTrack = inputLine[++i];
							} else if (foundDash)
								engineTrack = engineTrack + " " +inputLine[i];
						}
						if (engineTrack == null)
							engineTrack = "";
						log.debug("Engine ("+engineRoad+" "+engineNumber+") has track ("+engineTrack+")");
					}

					if (engineLocation.length() > Control.MAX_LEN_STRING_LOCATION_NAME){
						JOptionPane.showMessageDialog(null, 
								"Engine ("+engineRoad+" "+engineNumber+") location ("+engineLocation+") too long!",
								rb.getString("engineAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (engineTrack.length() > Control.MAX_LEN_STRING_TRACK_NAME){
						JOptionPane.showMessageDialog(null, 
								"Engine ("+engineRoad+" "+engineNumber+") track ("+engineTrack+") too long!",
								rb.getString("engineAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(engineLocation);
					Track sl = null;
					if (l == null && !engineLocation.equals("")){
						JOptionPane.showMessageDialog(null, "Engine ("+engineRoad+" "+engineNumber+") location ("+engineLocation+") does not exist",
								rb.getString("engineLocation"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (l != null && !engineTrack.equals("")){
						sl = l.getTrackByName(engineTrack, null);
						if (sl == null){
							JOptionPane.showMessageDialog(null, "Engine ("+engineRoad+" "+engineNumber+") track location ("+engineLocation+", "+engineTrack+") does not exist",
									rb.getString("engineTrack"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					log.debug("Add engine ("+engineRoad+" "+engineNumber+") owner ("+engineOwner+") built ("+engineBuilt+") location ("+engineLocation+", "+engineTrack+")");
					Engine engine = manager.newEngine(engineRoad, engineNumber);
					engine.setModel(engineModel);
					engine.setLength(engineLength);
					// does this model already have a type?
					if (engine.getType().equals(""))
						engine.setType(defaultEngineType);
					// does this model already have a hp?
					if (engine.getHp().equals(""))
						engine.setHp(defaultEngineHp);
					engine.setOwner(engineOwner);
					engine.setBuilt(engineBuilt);
					enginesAdded++;

					if (l != null){
						String status = engine.setLocation(l,sl);
						if (!status.equals(Engine.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(null,
									"Can't set engine ("+engineRoad+" "+engineNumber+") type ("+engineModel+") because of location ("+engineLocation+", "+engineTrack+ ") "+ status,
									"Can not update engine location",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}else{
//						log.debug("No location for engine ("+engineRoad+" "+engineNumber+")");
					}
				}
			}else{
				log.info("Import line number " + lineNum + " missing one of four required engine attributes");
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// kill status panel
		fstatus.dispose();

		if (importOkay) {
			JOptionPane.showMessageDialog(null, enginesAdded+" engines added to roster",
					"Successful import!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					enginesAdded+" engines added to roster",
					"Import failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected String[] parseCommaLine(String line, int arraySize) {
		String[] outLine = new String[arraySize];
		if (line.contains("\"")) {
			// log.debug("line number "+lineNum+" has escape char \"");
			String[] parseLine = line.split(",");
			int j = 0;
			for (int i = 0; i < parseLine.length; i++) {
				if (parseLine[i].contains("\"")) {
					StringBuilder sb = new StringBuilder(parseLine[i++]);
					sb.deleteCharAt(0); // delete the "
					outLine[j] = sb.toString();
					while (i < parseLine.length) {
						if (parseLine[i].contains("\"")) {
							sb = new StringBuilder(parseLine[i]);
							sb.deleteCharAt(sb.length() - 1); // delete the "
							outLine[j] = outLine[j] + "," + sb.toString();
							// log.debug("generated string: "+outLine[j]);
							j++;
							break; // done!
						} else {
							outLine[j] = outLine[j] + "," + parseLine[i++];
						}
					}

				} else {
					// log.debug("outLine: "+parseLine[i]);
					outLine[j++] = parseLine[i];
				}
			}
		} else {
			outLine = line.split(",");
		}
		return outLine;
	}



	
	protected static class ImportFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f){
			if (f.isDirectory())
				return true;
			String name = f.getName();
			if (name.matches(".*\\.txt"))
				return true;
			if (name.matches(".*\\.csv"))
				return true;
			else
				return false;
		}

		public String getDescription() {
			return "Text & CSV Documents (*.txt, *.csv)";
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ImportEngines.class.getName());
}

