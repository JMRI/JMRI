// ImportCars.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * This routine will import cars into the operation database.
 * 
 * Each field is space or comma delimited.  Field order:
 * Number Road Type Length Weight Color Owner Year Location
 * @author Dan Boudreau Copyright (C) 2008 2010
 * @version $Revision: 1.16 $
 */
public class ImportCars extends Thread {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();
	
	private int weightResults = JOptionPane.NO_OPTION;	// Automatically calculate weight for car if weight entry is not found
	private boolean autoCalculate = true;
	
	// we use a thread so the status frame will work!
	public void run() {
		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());
		fc.addChoosableFileFilter(new ImportFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // canceled
		if (fc.getSelectedFile() == null)
			return; // canceled
		File f = fc.getSelectedFile();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return;
		}
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Import cars");
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
		int carsAdded = 0;
		String line = " ";
		String carNumber;
		String carRoad;
		String carType;
		String carLength;
		String carWeight;
		String carColor ="";
		String carOwner ="";
		String carBuilt ="";
		String carLocation ="";
		String carTrack ="";
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
				log.info("Using comma as delimiter for import cars");
				comma = true;
			}
			// use comma as delimiter if found otherwise use spaces
			if (comma)
				inputLine = parseCommaLine(line, 11);
			else
				inputLine = line.split("\\s+");
			
			if (inputLine.length < 1){
				log.debug("Skipping blank line");
				continue;
			}
			int base = 1;
			if (!inputLine[0].equals("")){
				base--;		// skip over any spaces at start of line
			}

			if (inputLine.length > base+5){

				carNumber = inputLine[base+0];
				carRoad = inputLine[base+1];
				carType = inputLine[base+2];
				carLength = inputLine[base+3];
				carWeight = inputLine[base+4];
				carColor = inputLine[base+5];
				carOwner ="";
				carBuilt ="";
				carLocation ="";
				carTrack ="";

				log.debug("Checking car number ("+carNumber+") road ("+carRoad+ ") type ("+carType+ ") length ("+carLength+") weight ("+carWeight+") color ("+carColor+")" );
				if (carNumber.length() > Control.MAX_LEN_STRING_ROAD_NUMBER){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarRoadNumberTooLong"),new Object[]{(carRoad+" "+carNumber),carNumber}),
							rb.getString("carRoadNum"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carRoad.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarRoadNameTooLong"),new Object[]{(carRoad+" "+carNumber),carRoad}),
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carType.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarTypeNameTooLong"),new Object[]{(carRoad+" "+carNumber),carType}),
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (!CarTypes.instance().containsName(carType)){
					int results = JOptionPane.showConfirmDialog(null,
							rb.getString("Car")+" ("+carRoad+" "+carNumber+") \n"+MessageFormat.format(rb.getString("typeNameNotExist"),new Object[]{carType}),
							rb.getString("carAddType"),
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (results == JOptionPane.YES_OPTION)
						CarTypes.instance().addName(carType);
					else if (results == JOptionPane.CANCEL_OPTION){
						break;	
					}
				}
				if (carLength.length() > Control.MAX_LEN_STRING_LENGTH_NAME){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarLengthNameTooLong"),new Object[]{(carRoad+" "+carNumber),carLength}),
							rb.getString("carAttribute5"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carWeight.length() > Control.MAX_LEN_STRING_WEIGHT_NAME){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarWeightNameTooLong"),new Object[]{(carRoad+" "+carNumber),carWeight}),
							rb.getString("carAttribute5"),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (carColor.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
					JOptionPane.showMessageDialog(null, 
							MessageFormat.format(rb.getString("CarColorNameTooLong"),new Object[]{(carRoad+" "+carNumber),carColor}),
							MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				Car c = manager.getByRoadAndNumber(carRoad, carNumber);
				if (c != null){
					log.info("Can not add, car number ("+carNumber+") road ("+carRoad+ ") already exists!");
				} else {

					if(inputLine.length > base+6){
						carOwner = inputLine[base+6];
						if (carOwner.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
							JOptionPane.showMessageDialog(null, 
									MessageFormat.format(rb.getString("CarOwnerNameTooLong"),new Object[]{(carRoad+" "+carNumber),carOwner}),
									MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+7){
						carBuilt = inputLine[base+7];
						if (carBuilt.length() > Control.MAX_LEN_STRING_BUILT_NAME){
							JOptionPane.showMessageDialog(null, 
									MessageFormat.format(rb.getString("CarBuiltNameTooLong"),new Object[]{(carRoad+" "+carNumber),carBuilt}),
									rb.getString("carAttribute5"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					if(inputLine.length > base+8){
						carLocation = inputLine[base+8];

					}
					// Location name can be one to three words 
					if(inputLine.length > base+9){
						if (!inputLine[base+9].equals("-")){
							carLocation = carLocation + " " +inputLine[base+9];
							if(inputLine.length > base+10){
								if (!inputLine[base+10].equals("-"))
									carLocation = carLocation + " " +inputLine[base+10];
							}
							// create track location if there's one
						}
						boolean foundDash = false;
						for (int i=base+9; i<inputLine.length; i++){
							if(inputLine[i].equals("-")){
								foundDash = true;
								if (inputLine.length > i+1)
									carTrack = inputLine[++i];
							} else if (foundDash)
								carTrack = carTrack + " " +inputLine[i];
						}
						if (carTrack == null)
							carTrack = "";
						log.debug("Car ("+carRoad+" "+carNumber+") has track ("+carTrack+")");
					}

					if (carLocation.length() > Control.MAX_LEN_STRING_LOCATION_NAME){
						JOptionPane.showMessageDialog(null, 
								MessageFormat.format(rb.getString("CarLocationNameTooLong"),new Object[]{(carRoad+" "+carNumber),carLocation}),
								rb.getString("carAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (carTrack.length() > Control.MAX_LEN_STRING_TRACK_NAME){
						JOptionPane.showMessageDialog(null, 
								MessageFormat.format(rb.getString("CarTrackNameTooLong"),new Object[]{(carRoad+" "+carNumber),carTrack}),
								rb.getString("carAttribute25"),
								JOptionPane.ERROR_MESSAGE);
						break;
					}
					Location l = LocationManager.instance().getLocationByName(carLocation);
					Track sl = null;
					if (l == null && !carLocation.equals("")){
						JOptionPane.showMessageDialog(null, MessageFormat.format(rb.getString("CarLocationDoesNotExist"),new Object[]{(carRoad+" "+carNumber),carLocation}),
								rb.getString("carLocation"),
								JOptionPane.ERROR_MESSAGE);
						int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToCreateLoc"),new Object[]{carLocation}),
								rb.getString("carLocation"),
								JOptionPane.YES_NO_OPTION);
						if (results == JOptionPane.YES_OPTION){
							log.debug("Create location ("+carLocation+")");
							l = LocationManager.instance().newLocation(carLocation);
						} else {
							break;
						}
					}
					if (l != null && !carTrack.equals("")){
						sl = l.getTrackByName(carTrack, null);
						if (sl == null){
							JOptionPane.showMessageDialog(null, MessageFormat.format(rb.getString("CarTrackDoesNotExist"),new Object[]{(carRoad+" "+carNumber), carTrack, carLocation}),
									rb.getString("carTrack"),
									JOptionPane.ERROR_MESSAGE);
							int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("DoYouWantToCreateTrack"),new Object[]{carTrack, carLocation}),
									rb.getString("carTrack"),
									JOptionPane.YES_NO_OPTION);
							if (results == JOptionPane.YES_OPTION){
								log.debug("Create 1000 foot yard track ("+carTrack+")");
								sl = l.addTrack(carTrack, Track.YARD);
								sl.setLength(1000);
							} else {
								break;
							}
						}
					}

					log.debug("Add car ("+carRoad+" "+carNumber+") owner ("+carOwner+") built ("+carBuilt+") location ("+carLocation+", "+carTrack+")");
					Car car = manager.newCar(carRoad, carNumber);
					car.setType(carType);
					car.setLength(carLength);
					car.setWeight(carWeight);
					car.setColor(carColor);
					car.setOwner(carOwner);
					car.setBuilt(carBuilt);
					carsAdded++;
					
					car.setCaboose(carType.equals("Caboose"));
					
					if (car.getWeight().equals("")){
						log.debug("Car ("+carRoad+" "+carNumber+") weight not specified");
						if (weightResults != JOptionPane.CANCEL_OPTION){
							weightResults = JOptionPane.showOptionDialog(null, 
									MessageFormat.format(rb.getString("CarWeightNotFound"),new Object[]{(carRoad+" "+carNumber)}),
									rb.getString("CarWeightMissing"),
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{rb.getString("ButtonYes"), rb.getString("ButtonNo"), rb.getString("ButtonDontShow")}, autoCalculate?"Yes":"No");
						}
						if (weightResults == JOptionPane.NO_OPTION)
							autoCalculate = false;
						if (weightResults == JOptionPane.YES_OPTION || autoCalculate == true && weightResults == JOptionPane.CANCEL_OPTION){
							autoCalculate = true;
							try {
								double carLen = Double.parseDouble(car.getLength())*12/Setup.getScaleRatio();
								double carWght = (Setup.getInitalWeight() + carLen
										* Setup.getAddWeight()) / 1000;
								NumberFormat nf = NumberFormat.getNumberInstance();
								nf.setMaximumFractionDigits(1);
								car.setWeight(nf.format(carWght));	// car weight in ounces.
								int tons = (int)(carWght*Setup.getScaleTonRatio());
								// adjust weight for caboose
								if (car.isCaboose())
									tons = (int)(Double.parseDouble(car.getLength()) * .9);  //.9 tons/foot
								car.setWeightTons(Integer.toString(tons));
							} catch (NumberFormatException e) {
								JOptionPane.showMessageDialog(null,
										rb.getString("carLengthMustBe"), rb.getString("carWeigthCanNot"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}

					if (l != null){
						String status = car.setLocation(l,sl);
						if (!status.equals(Car.OKAY)){
							log.debug ("Can't set car's location because of "+ status);
							JOptionPane.showMessageDialog(null,
									MessageFormat.format(rb.getString("CanNotSetCarAtLocation"),new Object[]{(carRoad+" "+carNumber),carType,carLocation,carTrack,status}),
									rb.getString("rsCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}else{
//						log.debug("No location for car ("+carRoad+" "+carNumber+")");
					}
				}
			}else{
				log.info("Import line number " + lineNum + " missing one of six required car attributes");
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
					MessageFormat.format(rb.getString("ImportCarsAdded"),new Object[]{carsAdded}),
					rb.getString("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					MessageFormat.format(rb.getString("ImportCarsAdded"),new Object[]{carsAdded}),
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
	.getLogger(ImportCars.class.getName());
}

