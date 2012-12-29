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
 * @version $Revision$
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
				continue;
			}
			// use comma as delimiter if found otherwise use spaces
			if (comma)
				inputLine = parseCommaLine(line, 9);
			else
				inputLine = line.split("\\s+");
			
			if (inputLine.length < 1 || line.equals("")){
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
				if (engineNumber.length() > Control.max_len_string_road_number){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("EngineRoadNumberTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineNumber}),
							rb.getString("engineRoadNum"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineRoad.length() > Control.max_len_string_attibute){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("EngineRoadNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineRoad}),
							MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_attibute}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (engineModel.length() > Control.max_len_string_attibute){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("EngineModelNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineModel}),
							MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_attibute}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (!EngineModels.instance().containsName(engineModel)){
					int results = JOptionPane.showConfirmDialog(null,
							rb.getString("Engine")+" ("+engineRoad+" "+engineNumber+") \n"+MessageFormat.format(rb.getString("modelNameNotExist"),new Object[]{engineModel}),
							rb.getString("engineAddModel"),
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (results == JOptionPane.YES_OPTION)
						EngineModels.instance().addName(engineModel);
					else if (results == JOptionPane.CANCEL_OPTION)
						break;		
				}
				if (engineLength.length() > Control.max_len_string_length_name){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("EngineLengthNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineLength}),
							MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_length_name}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				try {
					Integer.parseInt(engineLength);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							MessageFormat.format(rb.getString("EngineLengthNameNotNumber"),new Object[]{(engineRoad+" "+engineNumber), engineLength}), rb.getString("EngineLengthMissing"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				Engine e = manager.getByRoadAndNumber(engineRoad, engineNumber);
				if (e != null){
					log.info("Can not add, engine number ("+engineNumber+") road ("+engineRoad+ ") already exists");
				} else {

					if(inputLine.length > base+5){
						engineOwner = inputLine[base+5];
						if (engineOwner.length() > Control.max_len_string_attibute){
							JOptionPane.showMessageDialog(null, 
									MessageFormat.format(rb.getString("EngineOwnerNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineOwner}),
									MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_attibute}),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+6){
						engineBuilt = inputLine[base+6];
						if (engineBuilt.length() > Control.max_len_string_built_name){
							JOptionPane.showMessageDialog(null, 
									MessageFormat.format(rb.getString("EngineBuiltDateTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineBuilt}),
									MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_built_name}),
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

					if (engineLocation.length() > Control.max_len_string_location_name){
						JOptionPane.showMessageDialog(null, 
								MessageFormat.format(rb.getString("EngineLocationNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineLocation}),
								MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_location_name}),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (engineTrack.length() > Control.max_len_string_track_name){
						JOptionPane.showMessageDialog(null, 
								MessageFormat.format(rb.getString("EngineTrackNameTooLong"),new Object[]{(engineRoad+" "+engineNumber),engineTrack}),
								MessageFormat.format(rb.getString("engineAttribute"),new Object[]{Control.max_len_string_track_name}),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(engineLocation);
					Track sl = null;
					if (l == null && !engineLocation.equals("")){
						JOptionPane.showMessageDialog(null, MessageFormat.format(rb.getString("EngineLocationDoesNotExist"),new Object[]{(engineRoad+" "+engineNumber),engineLocation}),
								rb.getString("engineLocation"),
								JOptionPane.ERROR_MESSAGE);
						int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToCreateLoc"),new Object[]{engineLocation}),
								rb.getString("engineLocation"),
								JOptionPane.YES_NO_OPTION);
						if (results == JOptionPane.YES_OPTION){
							log.debug("Create location ("+engineLocation+")");
							l = LocationManager.instance().newLocation(engineLocation);
						} else {
							break;
						}
					}
					if (l != null && !engineTrack.equals("")){
						sl = l.getTrackByName(engineTrack, null);
						if (sl == null){
							JOptionPane.showMessageDialog(null, MessageFormat.format(rb.getString("EngineTrackDoesNotExist"),new Object[]{(engineRoad+" "+engineNumber),engineTrack, engineLocation}),
									rb.getString("engineTrack"),
									JOptionPane.ERROR_MESSAGE);
							int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToCreateTrack"),new Object[]{engineTrack, engineLocation}),
									rb.getString("engineTrack"),
									JOptionPane.YES_NO_OPTION);
							if (results == JOptionPane.YES_OPTION){
								if (l.getLocationOps() == Location.NORMAL){
									log.debug("Create 1000 foot yard track ("+engineTrack+")");
									sl = l.addTrack(engineTrack, Track.YARD);
								} else {
									log.debug("Create 1000 foot staging track ("+engineTrack+")");
									sl = l.addTrack(engineTrack, Track.STAGING);	
								}
								sl.setLength(1000);
							} else {
								break;
							}
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

					if (l != null && sl != null){
						String status = engine.setLocation(l,sl);
						if (!status.equals(Track.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(null,
									MessageFormat.format(rb.getString("CanNotSetEngineAtLocation"),new Object[]{(engineRoad+" "+engineNumber),engineModel,engineLocation,engineTrack,status}),
									rb.getString("rsCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
							if (status.equals(Track.TYPE)){
								int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToAllowService"),new Object[]{engineLocation, engineTrack, (engineRoad+" "+engineNumber), engine.getType()}),
										rb.getString("ServiceEngineType"),
										JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION){
									l.addTypeName(engine.getType());
									sl.addTypeName(engine.getType());
									status = engine.setLocation(l,sl);
								} else {
									break;
								}						
							}
							if (status.equals(Track.LENGTH)){
								int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantIncreaseLength"),new Object[]{engineTrack}),
										rb.getString("TrackLength"),
										JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION){
									sl.setLength(sl.getLength()+1000);
									status = engine.setLocation(l,sl);
								} else {
									break;
								}						
							}
							if (!status.equals(Track.OKAY)){
								int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToForceEngine"),new Object[]{(engineRoad+" "+engineNumber), engineLocation, engineTrack}),
										rb.getString("OverRide"),
										JOptionPane.YES_NO_OPTION);
								if (results == JOptionPane.YES_OPTION){
									engine.setLocation(l,sl,true);	// force engine
								} else {
									break;
								}
							}
						}
					}else{
//						log.debug("No location for engine ("+engineRoad+" "+engineNumber+")");
					}
				}
			} else if (!line.equals("")){
				log.info("Engine import line "+lineNum+" missing attributes: "+line);
				JOptionPane.showMessageDialog(null, 
						MessageFormat.format(rb.getString("ImportMissingAttributes"),new Object[]{lineNum}),
						rb.getString("EngineAttributeMissing"),
						JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// kill status panel
		fstatus.dispose();

		if (importOkay) {
			JOptionPane.showMessageDialog(null, 
					MessageFormat.format(rb.getString("ImportEnginesAdded"),new Object[]{enginesAdded}),
					rb.getString("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					MessageFormat.format(rb.getString("ImportEnginesAdded"),new Object[]{enginesAdded}),
					rb.getString("ImportFailed"), JOptionPane.ERROR_MESSAGE);
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

