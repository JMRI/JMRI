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
	
	protected final static String del = ","; 	// delimiter
	
	protected final static String HEADER = "Operator"+del+"Description"+del+"Parameters";
	
	protected final static String AH = "AH"+del+"Add Helpers";
	protected final static String AT = "AT"+del+"Arrival Time"+del;
	protected final static String CC = "CC"+del+"Change Locos and Caboose";
	protected final static String CL = "CL"+del+"Change Locos";
	protected final static String DT = "DT"+del+"Departure Time"+del;
	protected final static String DTR = "DTR"+del+"Departure Time Route"+del;
	protected final static String EDT = "EDT"+del+"Estimated Departure Time"+del;
	protected final static String LC = "LC"+del+"Location Comment"+del;
	protected final static String LN = "LN"+del+"Location Name"+del;
	protected final static String LOGO = "LOGO"+del+"Logo file path"+del;
	protected final static String NW = "NW"+del+"No Work";
	protected final static String PC = "PC"+del+"Pick up car";
	protected final static String PL = "PL"+del+"Pick up loco";
	protected final static String RC = "RC"+del+"Route Comment"+del;
	protected final static String RH = "RH"+del+"Remove Helpers";
	protected final static String RN = "RN"+del+"Railroad Name"+del;
	protected final static String SC = "SC"+del+"Set out car";
	protected final static String SL = "SL"+del+"Set out loco";
	protected final static String TC = "TC"+del+"Train Comment"+del;
	protected final static String TD = "TD"+del+"Train Departs"+del;
	protected final static String TL = "TL"+del+"Train Length"+del;
	protected final static String TM = "TM"+del+"Train Manifest Description"+del;
	protected final static String TN = "TN"+del+"Train Name"+del;
	protected final static String TW = "TW"+del+"Train Weight"+del;
	protected final static String TT = "TT"+del+"Train Terminates"+del;
	protected final static String VT = "VT"+del+"Valid"+del;
	
	// switch list specific operators
	protected final static String DL = "DL"+del+"Departure Location Name"+del;
	protected final static String ETA = "ETA"+del+"Expected Time Arrival"+del;
	protected final static String ETE = "ETE"+del+"Estimated Time Enroute"+del;
	protected final static String NCPU = "NCPU"+del+"No Car Pick Up";
	protected final static String NCSO = "NCSO"+del+"No Car Set Out";
	protected final static String TA = "TA"+del+"Train Arrives"+del;
	protected final static String TDC = "TDC"+del+"Train changes direction, departs"+del;
	protected final static String TIR = "TIR"+del+"Train In Route";
	protected final static String TDONE = "TDONE"+del+"Train has already serviced this location";	
	protected final static String VN = "VN"+del+"Visit Number"+del;
	
	protected void fileOutCsvCar(PrintWriter fileOut, Car car, String operation){
		// check for delimiter in names
      	String carType = car.getType();
    	if (carType.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in type field: "+carType);
    		carType = "\""+carType+"\"";
    	}
       	String carLocationName = car.getLocationName();
    	if (carLocationName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in location field: "+carLocationName);
    		carLocationName = "\""+carLocationName+"\"";
    	}
    	String carTrackName = car.getTrackName();
    	if (carTrackName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in track field: "+carTrackName);
    		carTrackName = "\""+carTrackName+"\"";
    	}
       	String carDestName = car.getDestinationName();
    	if (carDestName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination field: "+carDestName);
    		carDestName = "\""+carDestName+"\"";
    	}
    	String carDestTrackName = car.getDestinationTrackName();
    	if (carDestTrackName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination track field: "+carDestTrackName);
    		carDestTrackName = "\""+carDestTrackName+"\"";
    	}
    	String carRWEDestName = car.getReturnWhenEmptyDestinationName();
      	if (carRWEDestName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in RWE destination field: "+carRWEDestName);
    		carRWEDestName = "\""+carRWEDestName+"\"";
    	}
       	String carRWETrackName = car.getReturnWhenEmptyDestTrackName();
      	if (carRWETrackName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in RWE destination track field: "+carRWETrackName);
    		carRWETrackName = "\""+carRWETrackName+"\"";
    	}

		addLine(fileOut, operation 
				+del+car.getRoad()
				+del+car.getNumber()
				+del+carType
				+del+car.getLength()
				+del+car.getLoad()
				+del+car.getColor()								
				+del+carLocationName
				+del+carTrackName
				+del+carDestName
				+del+carDestTrackName
				+del+car.getOwner()
				+del+car.getKernelName()
				+del+car.getComment()
				+del+car.getPickupComment()
				+del+car.getDropComment()
				+del+(car.isCaboose()?"C":"")
				+del+(car.hasFred()?"F":"")
				+del+(car.isHazardous()?"H":"")
				+del+car.getRfid()
				+del+carRWEDestName
				+del+carRWETrackName);
	}
	
	protected void fileOutCsvEngine(PrintWriter fileOut, Engine engine, String operation){	
		// check for delimiter in names
		String engineLocationName = engine.getLocationName();
		if (engineLocationName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in location field: "+engineLocationName);
			engineLocationName = "\""+engine.getLocationName()+"\"";
		}
		String engineTrackName = engine.getTrackName();
		if (engineTrackName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in track field: "+engineTrackName);
			engineTrackName = "\""+engine.getTrackName()+"\"";
		}
		String engineDestName = engine.getDestinationName();
		if (engineDestName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination field: "+engineDestName);
			engineDestName = "\""+engine.getDestinationName()+"\"";
		}
		String engineDestTrackName = engine.getDestinationTrackName();
		if (engineDestTrackName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination track field: "+engineDestTrackName);
			engineDestTrackName = "\""+engine.getDestinationTrackName()+"\"";
		}
		addLine(fileOut, operation
				+del+engine.getRoad()
				+del+engine.getNumber()
				+del+engine.getModel()						
				+del+engine.getLength()
				+del+engine.getType()
				+del+engine.getHp()								
				+del+engineLocationName
				+del+engineTrackName
				+del+engineDestName
				+del+engineDestTrackName
				+del+engine.getOwner()
				+del+engine.getConsistName()
				+del+engine.getComment()
				+del+engine.getRfid());
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

