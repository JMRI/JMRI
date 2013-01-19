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
	
	protected final static String DEL = ","; 	 // NOI18N delimiter
	protected final static String ESC = "\"";	 // NOI18N escape
	
	protected final static String HEADER = Bundle.getMessage("csvOperator") + DEL
			+ Bundle.getMessage("csvDescription") + DEL + Bundle.getMessage("csvParameters");
	
	protected final static String AH = "AH"+DEL+Bundle.getMessage("csvAddHelpers"); // NOI18N
	protected final static String AT = "AT"+DEL+Bundle.getMessage("csvArrivalTime")+DEL; // NOI18N
	protected final static String CC = "CC"+DEL+Bundle.getMessage("csvChangeLocosAndCaboose"); // NOI18N
	protected final static String CL = "CL"+DEL+Bundle.getMessage("csvChangeLocos"); // NOI18N
	protected final static String DT = "DT"+DEL+Bundle.getMessage("csvDepartureTime")+DEL; // NOI18N
	protected final static String DTR = "DTR"+DEL+Bundle.getMessage("csvDepartureTimeRoute")+DEL; // NOI18N
	protected final static String EDT = "EDT"+DEL+Bundle.getMessage("csvEstimatedDepartureTime")+DEL; // NOI18N
	protected final static String LC = "LC"+DEL+Bundle.getMessage("csvLocationComment")+DEL; // NOI18N
	protected final static String LN = "LN"+DEL+Bundle.getMessage("csvLocationName")+DEL; // NOI18N
	protected final static String LOGO = "LOGO"+DEL+Bundle.getMessage("csvLogoFilePath")+DEL; // NOI18N
	protected final static String NW = "NW"+DEL+Bundle.getMessage("csvNoWork"); // NOI18N
	protected final static String PC = "PC"+DEL+Bundle.getMessage("csvPickUpCar"); // NOI18N
	protected final static String PL = "PL"+DEL+Bundle.getMessage("csvPickUpLoco"); // NOI18N
	protected final static String PRNTR = "PRNTR"+DEL+Bundle.getMessage("csvPrinterName")+DEL; // NOI18N
	protected final static String RC = "RC"+DEL+Bundle.getMessage("csvRouteComment")+DEL; // NOI18N
	protected final static String RLC = "RLC"+DEL+Bundle.getMessage("csvRouteLocationComment")+DEL; // NOI18N
	protected final static String RH = "RH"+DEL+Bundle.getMessage("csvRemoveHelpers"); // NOI18N
	protected final static String RN = "RN"+DEL+Bundle.getMessage("csvRailroadName")+DEL; // NOI18N
	protected final static String SC = "SC"+DEL+Bundle.getMessage("csvSetOutCar"); // NOI18N
	protected final static String SL = "SL"+DEL+Bundle.getMessage("csvSetOutLoco"); // NOI18N
	protected final static String TC = "TC"+DEL+Bundle.getMessage("csvTrainComment")+DEL; // NOI18N
	protected final static String TD = "TD"+DEL+Bundle.getMessage("csvTrainDeparts")+DEL; // NOI18N
	protected final static String TL = "TL"+DEL+Bundle.getMessage("csvTrainLengthEmptiesCars")+DEL; // NOI18N
	protected final static String TM = "TM"+DEL+Bundle.getMessage("csvTrainManifestDescription")+DEL; // NOI18N
	protected final static String TN = "TN"+DEL+Bundle.getMessage("csvTrainName")+DEL; // NOI18N
	protected final static String TRUN = "TRUN"+DEL+Bundle.getMessage("csvTruncate"); // NOI18N
	protected final static String TW = "TW"+DEL+Bundle.getMessage("csvTrainWeight")+DEL; // NOI18N
	protected final static String TT = "TT"+DEL+Bundle.getMessage("csvTrainTerminates")+DEL; // NOI18N
	protected final static String VT = "VT"+DEL+Bundle.getMessage("csvValid")+DEL; // NOI18N
	
	// switch list specific operators
	protected final static String DL = "DL"+DEL+Bundle.getMessage("csvDepartureLocationName")+DEL; // NOI18N
	protected final static String ETA = "ETA"+DEL+Bundle.getMessage("csvExpectedTimeArrival")+DEL; // NOI18N
	protected final static String ETE = "ETE"+DEL+Bundle.getMessage("csvEstimatedTimeEnroute")+DEL; // NOI18N
	protected final static String NCPU = "NCPU"+DEL+Bundle.getMessage("csvNoCarPickUp"); // NOI18N
	protected final static String NCSO = "NCSO"+DEL+Bundle.getMessage("csvNoCarSetOut"); // NOI18N
	protected final static String TA = "TA"+DEL+Bundle.getMessage("csvTrainArrives")+DEL; // NOI18N
	protected final static String TDC = "TDC"+DEL+Bundle.getMessage("csvTrainChangesDirection")+DEL; // NOI18N
	protected final static String TIR = "TIR"+DEL+Bundle.getMessage("csvTrainInRoute"); // NOI18N
	protected final static String TDONE = "TDONE"+DEL+Bundle.getMessage("csvTrainHasAlreadyServiced"); // NOI18N	
	protected final static String VN = "VN"+DEL+Bundle.getMessage("csvVisitNumber")+DEL; // NOI18N
	
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

