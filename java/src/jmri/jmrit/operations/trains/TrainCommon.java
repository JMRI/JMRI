// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.List;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Common routines for trains
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010, 2011, 2012
 * @version             $Revision: 1 $
 */
public class TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String LENGTHABV = Setup.LENGTHABV;
	protected static final String TAB = "    ";
	protected static final String NEW_LINE = "\n";
	protected static final String SPACE = " ";
	private static final boolean pickup = true;
	private static final boolean local = true;
	
	CarManager carManager = CarManager.instance();
	EngineManager engineManager = EngineManager.instance();
	
	protected void pickupEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl, String orientation){
		for (int i =0; i < engineList.size(); i++){
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(""))
				pickupEngine(fileOut, engine, orientation);
		}
	}
	
	protected void dropEngines(PrintWriter fileOut, List<String> engineList, RouteLocation rl, String orientation){
		for (int i =0; i < engineList.size(); i++){
			Engine engine = engineManager.getById(engineList.get(i));
			if (engine.getRouteDestination() == rl)
				dropEngine(fileOut, engine, orientation);
		}
	}
	
	
	protected void pickupEngine(PrintWriter file, Engine engine, String orientation){
		StringBuffer buf = new StringBuffer(Setup.getPickupEnginePrefix());
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			String s = getEngineAttribute(engine, format[i], pickup);
			if (buf.length()+s.length()>lineLength(orientation)){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}
	
	protected void dropEngine(PrintWriter file, Engine engine, String orientation){
		StringBuffer buf = new StringBuffer(Setup.getDropEnginePrefix());
		String[] format = Setup.getDropEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			String s = getEngineAttribute(engine, format[i], !pickup);
			if (buf.length()+s.length()>lineLength(orientation)){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		addLine(file, buf.toString());
	}
	
	
	/*
	 * Adds the car's pick up string to the output file using the manifest format
	 */
	protected void pickUpCar(PrintWriter file, Car car){
		pickUpCar(file, car, new StringBuffer(Setup.getPickupCarPrefix()), Setup.getPickupCarMessageFormat(), Setup.getManifestOrientation());
	}
	
	/*
	 * Adds the car's pick up string to the output file using the switch list format
	 */
	protected void switchListPickUpCar(PrintWriter file, Car car){
		pickUpCar(file, car, new StringBuffer(Setup.getSwitchListPickupCarPrefix()), Setup.getSwitchListPickupCarMessageFormat(), Setup.getSwitchListOrientation());
	}
	
	protected void pickUpCar(PrintWriter file, Car car, StringBuffer buf, String[] format, String orientation){
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName())))
			return; // print nothing local move, see dropCar
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], pickup, !local);
			if (buf.length()+s.length()>lineLength(orientation)){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (!s.equals(TAB))
			addLine(file, s);
	} 
	
	protected String pickupCar(Car car){
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getPickupCarMessageFormat();
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], pickup, !local);
			buf.append(s);
		}
		return buf.toString();
	}
	
	/*
	 * Adds the car's set out string to the output file using the manifest format
	 */	
	protected void dropCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(Setup.getDropCarPrefix());
		String[] format = Setup.getDropCarMessageFormat();
		boolean local = false;
		// local move?
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName())) && car.getTrack()!=null){
			buf = new StringBuffer(Setup.getLocalPrefix());
			format = Setup.getLocalMessageFormat();
			local = true;
		}
		dropCar(file, car, buf, format, local, Setup.getManifestOrientation());
	}
	
	/*
	 * Adds the car's set out string to the output file using the truncated manifest format.
	 * Does not print out local moves
	 */	
	protected void truncatedDropCar(PrintWriter file, Car car){
		// local move?
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName())) && car.getTrack()!=null)
			return; 	// yes
		dropCar(file, car, new StringBuffer(Setup.getDropCarPrefix()), Setup.getTruncatedSetoutManifestMessageFormat(), false, Setup.getManifestOrientation());
	}
	
	/*
	 * Adds the car's set out string to the output file using the switch list format
	 */	
	protected void switchListDropCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(Setup.getSwitchListDropCarPrefix());
		String[] format = Setup.getSwitchListDropCarMessageFormat();
		boolean local = false;
		// local move?
		if (splitString(car.getRouteLocation().getName()).equals(splitString(car.getRouteDestination().getName())) && car.getTrack()!=null){
			buf = new StringBuffer(Setup.getSwitchListLocalPrefix());
			format = Setup.getSwitchListLocalMessageFormat();
			local = true;
		}
		dropCar(file, car, buf, format, local, Setup.getSwitchListOrientation());
	}
	
	protected void dropCar(PrintWriter file, Car car, StringBuffer buf, String[] format, boolean local, String orientation){
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], !pickup, local);
			if (buf.length()+s.length()>lineLength(orientation)){
				addLine(file, buf.toString());
				buf = new StringBuffer(TAB);
			}
			buf.append(s);
		}
		String s = buf.toString();
		if (!s.equals(TAB))
			addLine(file, s);
	}
	
	protected String dropCar(Car car){
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getDropCarMessageFormat();
		for (int i=0; i<format.length; i++){
			//TODO the Setup.Location doesn't work correctly for the conductor window
			// therefore we use the local true to disable it.
			String s = getCarAttribute(car, format[i], !pickup, local);
			buf.append(s);
		}
		return buf.toString();
	}
	
	protected String moveCar(Car car){
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getLocalMessageFormat();
		for (int i=0; i<format.length; i++){
			String s = getCarAttribute(car, format[i], !pickup, local);
			buf.append(s);
		}
		return buf.toString();
	}
	
	// writes string with level to console and file
	protected void addLine (PrintWriter file, String level, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null){
			int lineLengthMax = lineLength(Setup.PORTRAIT);
			if (string.length() > lineLengthMax){
				log.debug("String is too long for "+Setup.PORTRAIT);
				String[] s = string.split(SPACE);
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<s.length; i++){
					if (sb.length() + s[i].length() < lineLengthMax){
						sb.append(s[i]+SPACE);
					} else {
						file.println(level +"- " + sb.toString());
						sb = new StringBuffer(s[i]+SPACE);
					}
				}
				string = sb.toString();
			}
			String[] msg = string.split(NEW_LINE);
			for (int i=0; i<msg.length; i++)
				file.println(level +"- " + msg[i]);
		}
	}
	
	// writes string to console and file
	protected void addLine (PrintWriter file, String string){
		if(log.isDebugEnabled()){
			log.debug(string);
		}
		if (file != null)
			file.println(string);
	}
	
	protected void newLine (PrintWriter file, String string, String orientation){
		int lineLengthMax = lineLength(orientation);
		if (string.length() > lineLengthMax){
			log.debug("String is too long for "+orientation);
			String[] s = string.split(SPACE);
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<s.length; i++){
				if (sb.length() + s[i].length() < lineLengthMax){
					sb.append(s[i]+SPACE);
				} else {
					addLine(file, sb.toString());
					sb = new StringBuffer(s[i]+SPACE);
				}
			}
			addLine(file, sb.toString());
			return;
		}
		addLine(file, string);
	}
	
	protected void newLine (PrintWriter file){
		file.println(" ");
	}
	
	/**
	 * Splits a string (example-number) as long as the second part of
	 * the string is an integer.
	 * @param name
	 * @return First half the string.
	 */
	protected static String splitString(String name){
		String[] fullname = name.split("-");
		String parsedName = fullname[0].trim();
		// is the hyphen followed by a number?
		if (fullname.length>1){
			try{
				Integer.parseInt(fullname[1]);
			}
			catch (NumberFormatException e){
				// no return full name
				parsedName = name;
			}
		}
		return parsedName;
	}
	
	protected void getCarsLocationUnknown(PrintWriter file){
		CarManager cManager = CarManager.instance();
		List<String> cars = cManager.getCarsLocationUnknown();
		if (cars.size() == 0)
			return;	// no cars to search for!
		newLine(file);
		addLine(file, Setup.getMiaComment());
		for (int i=0; i<cars.size(); i++){
			Car car = cManager.getById(cars.get(i));
			searchForCar(file, car);
		}
	}
	
	protected void searchForCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer();
		String[] format = Setup.getMissingCarMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getCarAttribute(car, format[i], false, false));
		}
		addLine(file, buf.toString());
	}
	

	// @param pickup true when rolling stock is being picked up 	
	protected String getEngineAttribute(Engine engine, String attribute, boolean pickup){
		if (attribute.equals(Setup.MODEL))
			return " "+ engine.getModel();
		if (attribute.equals(Setup.CONSIST))
			return " "+ engine.getConsistName();
		return getRollingStockAttribute(engine, attribute, pickup, false);
	}
	
	protected String getCarAttribute(Car car, String attribute, boolean pickup, boolean local){
		if (attribute.equals(Setup.LOAD))
			return (car.isCaboose() || car.isPassenger())? tabString("", CarLoads.instance().getCurMaxNameLength()+1) 
					: " "+tabString(car.getLoad(), CarLoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.HAZARDOUS))
			return (car.isHazardous()? " "+Setup.getHazardousMsg() : "");
		else if (attribute.equals(Setup.DROP_COMMENT))
			return " "+car.getDropComment();
		else if (attribute.equals(Setup.PICKUP_COMMENT))
			return " "+car.getPickupComment();
		else if (attribute.equals(Setup.KERNEL))
			return " "+tabString(car.getKernelName(), Control.max_len_string_attibute);
		else if (attribute.equals(Setup.RWE)){
			if (!car.getReturnWhenEmptyDestName().equals(""))
				return " "+rb.getString("RWE")+" "+splitString(car.getReturnWhenEmptyDestinationName())
						+" ("+splitString(car.getReturnWhenEmptyDestTrackName())+")";
			return "";
		}
		return getRollingStockAttribute(car, attribute, pickup, local);
	}

	protected String getRollingStockAttribute(RollingStock rs, String attribute, boolean pickup, boolean local){
		if (attribute.equals(Setup.NUMBER))
			return " "+tabString(splitString(rs.getNumber()), Control.max_len_string_road_number-4);
		else if (attribute.equals(Setup.ROAD))
			return " "+tabString(rs.getRoad(), CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.TYPE)){
			String[] type = rs.getType().split("-");	// second half of string can be anything
			return " "+tabString(type[0], CarTypes.instance().getCurMaxNameLength());
		}
		else if (attribute.equals(Setup.LENGTH))
			return " "+tabString(rs.getLength()+ LENGTHABV, CarLengths.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COLOR))
			return " "+tabString(rs.getColor(), CarColors.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.LOCATION) && (pickup || local)){
			if (rs.getTrack() != null)
				return " "+rb.getString("from")+ " "+splitString(rs.getTrackName());
			return "";
		}
		else if (attribute.equals(Setup.LOCATION) && !pickup && !local)
			return " "+rb.getString("from")+ " "+splitString(rs.getLocationName());
		else if (attribute.equals(Setup.DESTINATION) && pickup){
			if (Setup.isTabEnabled())
				return " "+rb.getString("dest")+ " "+splitString(rs.getDestinationName());
			else
				return " "+rb.getString("destination")+ " "+splitString(rs.getDestinationName());
		}
		else if (attribute.equals(Setup.DESTINATION) && !pickup)
			return " "+rb.getString("to")+ " "+splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.DEST_TRACK))
			return " "+rb.getString("dest")+ " "+splitString(rs.getDestinationName())
					+ ", "+splitString(rs.getDestinationTrackName());
		else if (attribute.equals(Setup.OWNER))
			return " "+tabString(rs.getOwner(), CarOwners.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.COMMENT))
			return " "+rs.getComment();
		else if (attribute.equals(Setup.NONE))
			return "";
		// the three utility attributes that don't get printed but need to be tabbed out
		else if (attribute.equals(Setup.NO_NUMBER))
			return " "+tabString("", Control.max_len_string_road_number-7);	// (-4 -3) for utility quantity field
		else if (attribute.equals(Setup.NO_ROAD))
			return " "+tabString("", CarRoads.instance().getCurMaxNameLength());
		else if (attribute.equals(Setup.NO_COLOR))
			return " "+tabString("", CarColors.instance().getCurMaxNameLength());
		// the three truncated manifest attributes
		else if (attribute.equals(Setup.NO_DESTINATION) || attribute.equals(Setup.NO_DEST_TRACK) || attribute.equals(Setup.NO_LOCATION))
			return "";
		return " ("+rb.getString("ErrorPrintOptions")+") ";	// maybe user changed locale
	}
	
	protected static String getDate(){
		Calendar calendar = Calendar.getInstance();
		
		String year = Setup.getYearModeled();
		if (year.equals(""))
			year = Integer.toString(calendar.get(Calendar.YEAR));
		year = year.trim();
		
		// Use 24 hour clock
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		if (Setup.is12hrFormatEnabled()){
			hour = calendar.get(Calendar.HOUR);
			if (hour == 0)
				hour = 12;
		}
		
		String h  = Integer.toString(hour);
		if (hour <10)
			h = "0"+ Integer.toString(hour);
		
		int minute = calendar.get(Calendar.MINUTE);
		String m = Integer.toString(minute);
		if (minute <10)
			m = "0"+ Integer.toString(minute);
					
		//AM_PM field
		String AM_PM = "";
		if (Setup.is12hrFormatEnabled()){
			AM_PM = (calendar.get(Calendar.AM_PM)== Calendar.AM)? "AM":"PM";
		}
		
		// Java 1.6 methods calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()
		// Java 1.6 methods calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault())
		String date = calendar.get(Calendar.MONTH)+1
				+ "/"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "/" + year + " "
				+ h + ":" + m + " " 
				+ AM_PM;
		return date;
	}
	
	public static String tabString(String s, int fieldSize){
		if (!Setup.isTabEnabled())
			return s;
		StringBuffer buf = new StringBuffer(s);
		while (buf.length() < fieldSize){
			buf.append(" ");
		}
		return buf.toString();
	}
	
	protected int lineLength(String orientation){
		// page size has been adjusted to account for margins of .5
		// Dimension pagesize = new Dimension(612,792);
		Dimension pagesize = new Dimension(540,792);	// Portrait
		if (orientation.equals(Setup.LANDSCAPE))
			pagesize = new Dimension(720,612);
		if (orientation.equals(Setup.HANDHELD))
			pagesize = new Dimension(206,792);
		// Metrics don't always work for the various font names, so use Monospaced
		Font font = new Font("Monospaced", Font.PLAIN, Setup.getFontSize());
		Frame frame = new Frame();
		FontMetrics metrics = frame.getFontMetrics(font);

		int charwidth = metrics.charWidth('m');

		// compute lines and columns within margins
		return pagesize.width / charwidth;
	}
	
	// all of this for the utility car print option
	String[] pickupUtilityMessageFormat = Setup.getPickupUtilityCarMessageFormat();
	String[] setoutUtilityMessageFormat = Setup.getSetoutUtilityCarMessageFormat();
	String[] localUtilityMessageFormat = Setup.getLocalUtilityCarMessageFormat();
	
	List<String> utilityCarTypes = new ArrayList<String>();
	
	protected void pickupCars(PrintWriter fileOut, List<String> carList, Car car, RouteLocation rl, RouteLocation rld){
		// list utility cars by type, track, length, and load
		boolean showLength = showUtilityCarLength(pickupUtilityMessageFormat);
		boolean showLoad = showUtilityCarLoad(pickupUtilityMessageFormat);
		String[] carType = car.getType().split("-");
		String carAttributes = carType[0] + splitString(car.getTrackName());
		if (showLength)
			carAttributes = carAttributes + car.getLength();
		if (showLoad)
			carAttributes = carAttributes + car.getLoad();
		if (!utilityCarTypes.contains(carAttributes)) {
			// first we need the quantity
			int count = 0;
			for (int i = 0; i < carList.size(); i++) {
				Car c = carManager.getById(carList.get(i));
				String[] cType = c.getType().split("-");
				if (c.getRouteLocation() == rl
						&& c.getRouteDestination() == rld
						&& c.isUtility()
						&& cType[0].equals(carType[0])
						&& splitString(c.getTrackName()).equals(splitString(car.getTrackName()))
						&& (!showLength || c.getLength().equals(car.getLength()))
						&& (!showLoad || c.getLoad().equals(car.getLoad()))) {
					count++;
				}
			}
			//log.debug("Car ("+car.toString()+ ") type ("+car.getType()+") length ("+car.getLength()+") load ("+car.getLoad()+") track ("+ car.getTrackName()+")");
			pickUpCar(fileOut, car, new StringBuffer(Setup.getPickupCarPrefix() +" "+tabString(Integer.toString(count), 2)), pickupUtilityMessageFormat, Setup.getManifestOrientation());
			utilityCarTypes.add(carAttributes);	// don't do this type again
		}
	}

	protected void setoutCars(PrintWriter fileOut, List<String> carList, Car car, RouteLocation rl, boolean local){
		boolean showLength = showUtilityCarLength(setoutUtilityMessageFormat);
		boolean showLoad = showUtilityCarLoad(setoutUtilityMessageFormat);
		StringBuffer buf = new StringBuffer(Setup.getDropCarPrefix());
		String[] format = setoutUtilityMessageFormat;
		if (local) {
			showLength = showUtilityCarLength(localUtilityMessageFormat);
			showLoad = showUtilityCarLoad(setoutUtilityMessageFormat);
			buf = new StringBuffer(Setup.getLocalPrefix());
			format = Setup.getLocalUtilityCarMessageFormat();
		}
		// list utility cars by type, track, length, and load
		String[] carType = car.getType().split("-");
		String carAttributes = carType[0] + splitString(car.getDestinationTrackName()) + car.getRouteDestinationId();
		if (showLength)
			carAttributes = carAttributes + car.getLength();
		if (showLoad)
			carAttributes = carAttributes + car.getLoad();
		if (!utilityCarTypes.contains(carAttributes)) {
			// first we need the quantity
			int count = 0;
			for (int i = 0; i < carList.size(); i++) {
				Car c = carManager.getById(carList.get(i));
				String[] cType = c.getType().split("-");
				if (c.getRouteDestination() == rl
						&& c.isUtility()
						&& splitString(c.getDestinationTrackName()).equals(splitString(car.getDestinationTrackName()))
						&& c.getRouteDestination().equals(car.getRouteDestination())					
						&& cType[0].equals(carType[0])
						&& (!showLength || c.getLength().equals(car.getLength()))
						&& (!showLoad || c.getLoad().equals(car.getLoad()))) {
					count++;
				}
			}
			buf.append(" "+tabString(Integer.toString(count), 2));
			//log.debug("Car ("+car.toString()+ ") type ("+car.getType()+") length ("+car.getLength()+") load ("+car.getLoad()+") track ("+ car.getTrackName()+")");
			dropCar(fileOut, car, buf, format, local, Setup.getManifestOrientation());
			utilityCarTypes.add(carAttributes);	// don't do this type again
		}
	}
	
	protected boolean showUtilityCarLength(String[] mFormat){
		for (int i=0; i<mFormat.length; i++){
			if (mFormat[i].equals(Setup.LENGTH))
				return true;
		}
		return false;
	}
	
	protected boolean showUtilityCarLoad(String[] mFormat){
		for (int i=0; i<mFormat.length; i++){
			if (mFormat[i].equals(Setup.LOAD))
				return true;
		}
		return false;
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCommon.class.getName());
}
