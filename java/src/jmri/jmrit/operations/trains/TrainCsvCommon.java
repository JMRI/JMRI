// TrainCsvCommon.java

package jmri.jmrit.operations.trains;

import java.io.PrintWriter;

import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;

/**
 * Contains the csv operators for manifests and switch lists
 * @author Daniel Boudreau Copyright (C) 2011
 * @version             $Revision: 1 $
 *
 */
public class TrainCsvCommon extends TrainCommon {
	
	protected final static String DEL = ","; 	// delimiter
	protected final static String ESC = "\"";	// escape
	
	protected final static String HEADER = "Operator"+DEL+"Description"+DEL+"Parameters";
	
	protected final static String AH = "AH"+DEL+"Add Helpers";
	protected final static String AT = "AT"+DEL+"Arrival Time"+DEL;
	protected final static String CC = "CC"+DEL+"Change Locos and Caboose";
	protected final static String CL = "CL"+DEL+"Change Locos";
	protected final static String DT = "DT"+DEL+"Departure Time"+DEL;
	protected final static String DTR = "DTR"+DEL+"Departure Time Route"+DEL;
	protected final static String EDT = "EDT"+DEL+"Estimated Departure Time"+DEL;
	protected final static String LC = "LC"+DEL+"Location Comment"+DEL;
	protected final static String LN = "LN"+DEL+"Location Name"+DEL;
	protected final static String LOGO = "LOGO"+DEL+"Logo file path"+DEL;
	protected final static String NW = "NW"+DEL+"No Work";
	protected final static String PC = "PC"+DEL+"Pick up car";
	protected final static String PL = "PL"+DEL+"Pick up loco";
	protected final static String RC = "RC"+DEL+"Route Comment"+DEL;
	protected final static String RLC = "RLC"+DEL+"Route Location Comment"+DEL;
	protected final static String RH = "RH"+DEL+"Remove Helpers";
	protected final static String RN = "RN"+DEL+"Railroad Name"+DEL;
	protected final static String SC = "SC"+DEL+"Set out car";
	protected final static String SL = "SL"+DEL+"Set out loco";
	protected final static String TC = "TC"+DEL+"Train Comment"+DEL;
	protected final static String TD = "TD"+DEL+"Train Departs"+DEL;
	protected final static String TL = "TL"+DEL+"Train Length Empties Cars"+DEL;
	protected final static String TM = "TM"+DEL+"Train Manifest Description"+DEL;
	protected final static String TN = "TN"+DEL+"Train Name"+DEL;
	protected final static String TRUN = "TRUN"+DEL+"Truncate";
	protected final static String TW = "TW"+DEL+"Train Weight"+DEL;
	protected final static String TT = "TT"+DEL+"Train Terminates"+DEL;
	protected final static String VT = "VT"+DEL+"Valid"+DEL;
	
	// switch list specific operators
	protected final static String DL = "DL"+DEL+"Departure Location Name"+DEL;
	protected final static String ETA = "ETA"+DEL+"Expected Time Arrival"+DEL;
	protected final static String ETE = "ETE"+DEL+"Estimated Time Enroute"+DEL;
	protected final static String NCPU = "NCPU"+DEL+"No Car Pick Up";
	protected final static String NCSO = "NCSO"+DEL+"No Car Set Out";
	protected final static String TA = "TA"+DEL+"Train Arrives"+DEL;
	protected final static String TDC = "TDC"+DEL+"Train changes direction, departs"+DEL;
	protected final static String TIR = "TIR"+DEL+"Train In Route";
	protected final static String TDONE = "TDONE"+DEL+"Train has already serviced this location";	
	protected final static String VN = "VN"+DEL+"Visit Number"+DEL;
	
	protected void fileOutCsvCar(PrintWriter fileOut, Car car, String operation){
		// check for delimiter in names
      	String carType = car.getType();
    	if (carType.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in type field: "+carType);
    		carType = ESC+carType+ESC;
    	}
       	String carLocationName = car.getLocationName();
    	if (carLocationName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in location field: "+carLocationName);
    		carLocationName = ESC+carLocationName+ESC;
    	}
    	String carTrackName = car.getTrackName();
    	if (carTrackName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in track field: "+carTrackName);
    		carTrackName = ESC+carTrackName+ESC;
    	}
       	String carDestName = car.getDestinationName();
    	if (carDestName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination field: "+carDestName);
    		carDestName = ESC+carDestName+ESC;
    	}
    	String carDestTrackName = car.getDestinationTrackName();
    	if (carDestTrackName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination track field: "+carDestTrackName);
    		carDestTrackName = ESC+carDestTrackName+ESC;
    	}
    	String carRWEDestName = car.getReturnWhenEmptyDestinationName();
      	if (carRWEDestName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in RWE destination field: "+carRWEDestName);
    		carRWEDestName = ESC+carRWEDestName+ESC;
    	}
       	String carRWETrackName = car.getReturnWhenEmptyDestTrackName();
      	if (carRWETrackName.contains(DEL)){
    		log.debug("Car ("+car.toString()+") has delimiter in RWE destination track field: "+carRWETrackName);
    		carRWETrackName = ESC+carRWETrackName+ESC;
    	}

		addLine(fileOut, operation 
				+DEL+car.getRoad()
				+DEL+car.getNumber()
				+DEL+carType
				+DEL+car.getLength()
				+DEL+car.getLoad()
				+DEL+car.getColor()								
				+DEL+carLocationName
				+DEL+carTrackName
				+DEL+carDestName
				+DEL+carDestTrackName
				+DEL+car.getOwner()
				+DEL+car.getKernelName()
				+DEL+car.getComment()
				+DEL+car.getPickupComment()
				+DEL+car.getDropComment()
				+DEL+(car.isCaboose()?"C":"")
				+DEL+(car.hasFred()?"F":"")
				+DEL+(car.isHazardous()?"H":"")
				+DEL+car.getRfid()
				+DEL+carRWEDestName
				+DEL+carRWETrackName);
	}
	
	protected void fileOutCsvEngine(PrintWriter fileOut, Engine engine, String operation){	
		// check for delimiter in names
		String engineLocationName = engine.getLocationName();
		if (engineLocationName.contains(DEL)){
			log.debug("Engine ("+engine.toString()+") has delimiter in location field: "+engineLocationName);
			engineLocationName = ESC+engine.getLocationName()+ESC;
		}
		String engineTrackName = engine.getTrackName();
		if (engineTrackName.contains(DEL)){
			log.debug("Engine ("+engine.toString()+") has delimiter in track field: "+engineTrackName);
			engineTrackName = ESC+engine.getTrackName()+ESC;
		}
		String engineDestName = engine.getDestinationName();
		if (engineDestName.contains(DEL)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination field: "+engineDestName);
			engineDestName = ESC+engine.getDestinationName()+ESC;
		}
		String engineDestTrackName = engine.getDestinationTrackName();
		if (engineDestTrackName.contains(DEL)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination track field: "+engineDestTrackName);
			engineDestTrackName = ESC+engine.getDestinationTrackName()+ESC;
		}
		addLine(fileOut, operation
				+DEL+engine.getRoad()
				+DEL+engine.getNumber()
				+DEL+engine.getModel()						
				+DEL+engine.getLength()
				+DEL+engine.getType()
				+DEL+engine.getHp()								
				+DEL+engineLocationName
				+DEL+engineTrackName
				+DEL+engineDestName
				+DEL+engineDestTrackName
				+DEL+engine.getOwner()
				+DEL+engine.getConsistName()
				+DEL+engine.getComment()
				+DEL+engine.getRfid());
	}
	
	protected void engineCsvChange(PrintWriter fileOut, RouteLocation rl, int legOptions){
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			addLine(fileOut, AH);
		else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE || (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			addLine(fileOut, CC);
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			addLine(fileOut, CL);
	}
	
}

