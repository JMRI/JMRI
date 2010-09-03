// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.PrintWriter;
import java.util.ResourceBundle;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.setup.Setup;

/**
 * Common routines for trains
 * @author Daniel Boudreau (C) Copyright 2008, 2009
 *
 */
public class TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String LENGTHABV = Setup.LENGTHABV;
	protected static final String BOX = " [ ] ";
	
	protected static final String ONE = Setup.BUILD_REPORT_MINIMAL;
	protected static final String THREE = Setup.BUILD_REPORT_NORMAL;
	protected static final String FIVE = Setup.BUILD_REPORT_DETAILED;
	protected static final String SEVEN = Setup.BUILD_REPORT_VERY_DETAILED;

	protected void pickupEngine(PrintWriter file, Engine engine){
		String comment = (Setup.isAppendCarCommentEnabled() ? " "
				+ engine.getComment(): "");
		addLine(file, BOX + rb.getString("Pickup") +" "
				+ rb.getString("Engine") + " "
				+ engine.getRoad() + " "
				+ engine.getNumber() + " ("
				+ engine.getModel() + ") "
				+ rb.getString("from") + " "
				+ engine.getTrackName() + comment);
	}
	
	protected void dropEngine(PrintWriter file, Engine engine){
		String comment = (Setup.isAppendCarCommentEnabled() ? " "
				+ engine.getComment(): "");
		addLine(file, BOX + rb.getString("Drop") +" "
				+ rb.getString("Engine") + " "
				+ engine.getRoad() + " "
				+ engine.getNumber() + " ("
				+ engine.getModel() + ") "
				+ rb.getString("to") + " "
				+ engine.getDestinationTrackName() + comment);
	}
	
	protected void  pickupCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carLength = (Setup.isShowCarLengthEnabled() ? " "+car.getLength()+ LENGTHABV : "");
		String carLoad = (Setup.isShowCarLoadEnabled()& !car.isCaboose()  ? " "+car.getLoad() : "");
		String carColor = (Setup.isShowCarColorEnabled() ? " "+car.getColor() : "");
		String carDestination = (Setup.isShowCarDestinationEnabled() ? ", destination "+splitString(car.getDestinationName()) : "");
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		String carPickupComment = " " +CarLoads.instance().getPickupComment(car.getType(), car.getLoad());
		addLine(file, BOX + rb.getString("Pickup")+" " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0]
				+ carLength + carLoad + carColor 
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+")" : "")
				+ (car.hasFred() ? " ("+rb.getString("FRED")+")" : "") + " " + rb.getString("from")+ " "
				+ splitString(car.getTrackName()) + carDestination + carComment + carPickupComment);
	}
	
	protected void dropCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carLength = (Setup.isShowCarLengthEnabled() ? " "+car.getLength()+ LENGTHABV : "");
		String carLoad = (Setup.isShowCarLoadEnabled()& !car.isCaboose() ? " "+car.getLoad() : "");
		String carColor = (Setup.isShowCarColorEnabled() ? " "+car.getColor() : "");	
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		String carDropComment = " " +CarLoads.instance().getDropComment(car.getType(), car.getLoad());
		addLine(file, BOX + rb.getString("Drop")+ " " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0]
				+ carLength + carLoad + carColor
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ rb.getString("to") + " " + splitString(car.getDestinationTrackName())
				+ carComment + carDropComment);
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
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainCommon.class.getName());
}
