// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.List;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.setup.Setup;

/**
 * Common routines for trains
 * @author Daniel Boudreau (C) Copyright 2008, 2009, 2010
 *
 */
public class TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String LENGTHABV = Setup.LENGTHABV;
	protected static final String BOX = " [ ] ";
	
	protected void pickupEngine(PrintWriter file, Engine engine){
		StringBuffer buf = new StringBuffer(BOX + rb.getString("Pickup")+ " ");
		String[] format = Setup.getPickupEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getEngineAttribute(engine, format[i], true));
		}
		addLine(file, buf.toString());
	}
	
	protected void dropEngine(PrintWriter file, Engine engine){
		StringBuffer buf = new StringBuffer(BOX + rb.getString("Drop")+ " ");
		String[] format = Setup.getDropEngineMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getEngineAttribute(engine, format[i], false));
		}
		addLine(file, buf.toString());
	}
	
	protected void pickupCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(BOX + rb.getString("Pickup")+ " ");
		String[] format = Setup.getPickupCarMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getCarAttribute(car, format[i], true));
		}
		addLine(file, buf.toString());
	}
	
	protected void dropCar(PrintWriter file, Car car){
		StringBuffer buf = new StringBuffer(BOX + rb.getString("Drop")+ " ");
		String[] format = Setup.getDropCarMessageFormat();
		for (int i=0; i<format.length; i++){
			buf.append(getCarAttribute(car, format[i], false));
		}
		addLine(file, buf.toString());
	}
	
	// writes string with level to console and file
	protected void addLine (PrintWriter file, String level, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
			file.println(level +"- " + string);
	}
	
	// writes string to console and file
	protected void addLine (PrintWriter file, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
			file.println(string);
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
	protected String splitString(String name){
		String[] fullname = name.split("-");
		String parsedName = fullname[0];
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
			buf.append(getCarAttribute(car, format[i], false));
		}
		addLine(file, buf.toString());
	}
	

	// @param pickup true when rolling stock is being picked up 	
	protected String getEngineAttribute(Engine engine, String attribute, boolean pickup){
		if (attribute.equals(Setup.MODEL))
			return "("+ engine.getModel() +") ";
		return getRollingStockAttribute(engine, attribute, pickup);
	}
	
	protected String getCarAttribute(Car car, String attribute, boolean pickup){
		if (attribute.equals(Setup.LOAD))
			return (car.isCaboose() || car.isPassenger())? "" : car.getLoad()+" ";
		else if (attribute.equals(Setup.HAZARDOUS))
			return (car.isHazardous()? "("+rb.getString("Hazardous")+") " : "");
		else if (attribute.equals(Setup.DROP_COMMENT))
			return CarLoads.instance().getDropComment(car.getType(), car.getLoad())+" ";
		else if (attribute.equals(Setup.PICKUP_COMMENT))
			return CarLoads.instance().getPickupComment(car.getType(), car.getLoad())+" ";
		return getRollingStockAttribute(car, attribute, pickup);
	}

	protected String getRollingStockAttribute(RollingStock rs, String attribute, boolean pickup){
		if (attribute.equals(Setup.NUMBER))
			return splitString(rs.getNumber()) +" ";
		else if (attribute.equals(Setup.ROAD))
			return rs.getRoad() +" ";
		else if (attribute.equals(Setup.TYPE)){
			String[] type = rs.getType().split("-");	// second half of string can be anything
			return type[0] +" ";
		}
		else if (attribute.equals(Setup.LENGTH))
			return rs.getLength()+ LENGTHABV +" ";
		else if (attribute.equals(Setup.COLOR))
			return rs.getColor() +" ";
		else if (attribute.equals(Setup.LOCATION) && pickup)
			return rb.getString("from")+ " "+splitString(rs.getTrackName() + " ");
		else if (attribute.equals(Setup.LOCATION) && !pickup)
			return rb.getString("from")+ " "+splitString(rs.getLocationName() + " ");
		else if (attribute.equals(Setup.DESTINATION) && pickup)
			return rb.getString("destination")+ " "+splitString(rs.getDestinationName() + " ");
		else if (attribute.equals(Setup.DESTINATION) && !pickup)
			return rb.getString("to")+ " "+splitString(rs.getDestinationTrackName() + " ");
		else if (attribute.equals(Setup.COMMENT))
			return rs.getComment() +" ";
		else if (attribute.equals(Setup.NONE))
			return "";
		return "error ";		
	}
	
	protected String getDate(){
		Calendar calendar = Calendar.getInstance();
		
		String year = Setup.getYearModeled();
		if (year.equals(""))
			year = Integer.toString(calendar.get(Calendar.YEAR));
		year = year.trim();
		
		// Use 24 hour clock
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		String h  = Integer.toString(hour);
		if (hour <10)
			h = "0"+ Integer.toString(hour);
		
		int minute = calendar.get(Calendar.MINUTE);
		String m = Integer.toString(minute);
		if (minute <10)
			m = "0"+ Integer.toString(minute);
					
		//remove AM_PM field
		//String AM_PM = (calendar.get(Calendar.AM_PM)== Calendar.AM)? "AM":"PM";
		String AM_PM = "";
		
		// Java 1.6 methods calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()
		// Java 1.6 methods calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault())
		String date = calendar.get(Calendar.MONTH)+1
				+ "/"
				+ calendar.get(Calendar.DAY_OF_MONTH) + ", " + year + " "
				+ h + ":" + m + " " 
				+ AM_PM;
		return date;
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCommon.class.getName());
}
