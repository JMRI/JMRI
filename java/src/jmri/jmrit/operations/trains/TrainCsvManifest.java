// TrainCsvManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train's manifest using Comma Separated Values (csv).
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 1 $
 */
public class TrainCsvManifest extends TrainCsvCommon {

	EngineManager engineManager = EngineManager.instance();
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();

	public TrainCsvManifest(Train train) {
		// create comma separated value manifest file
		File file = TrainManagerXml.instance().createTrainCsvManifestFile(train.getName());

		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")),
					true); // NOI18N
		} catch (IOException e) {
			log.error("can not open train csv manifest file");
			return;
		}
		// build header
		addLine(fileOut, HEADER);
		addLine(fileOut, RN + ESC + Setup.getRailroadName() + ESC);
		addLine(fileOut, TN + train.getName());
		addLine(fileOut, TM + ESC + train.getDescription() + ESC);
		addLine(fileOut, PRNTR + ESC
				+ locationManager.getLocationByName(train.getTrainDepartsName()).getDefaultPrinterName() + ESC);
		// add logo
		String logoURL = Setup.getManifestLogoURL();
		if (!train.getManifestLogoURL().equals(""))
			logoURL = train.getManifestLogoURL();
		if (!logoURL.equals(""))
			addLine(fileOut, LOGO + logoURL);
		addLine(fileOut, VT + getDate(true));
		// train comment can have multiple lines
		if (!train.getComment().equals("")) {
			String[] comments = train.getComment().split("\n"); // NOI18N
			for (int i = 0; i < comments.length; i++)
				addLine(fileOut, TC + ESC + comments[i] + ESC);
		}
		if (Setup.isPrintRouteCommentsEnabled())
			addLine(fileOut, RC + ESC + train.getRoute().getComment() + ESC);

		// get engine and car lists
		List<RollingStock> engineList = engineManager.getByTrainList(train);
		List<Car> carList = carManager.getByTrainDestinationList(train);

		int cars = 0;
		int emptyCars = 0;
		boolean newWork = false;
		String previousRouteLocationName = null;
		List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = routeList.get(r);
			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			String locationName = routeLocationName;
			if (locationName.contains(DEL)) {
				log.debug("location name has delimiter: " + locationName);
				locationName = ESC + routeLocationName + ESC;
			}
			if (!routeLocationName.equals(previousRouteLocationName)) {
				addLine(fileOut, LN + locationName);
				if (r != 0)
					addLine(fileOut, AT + train.getExpectedArrivalTime(rl));
				if (r == 0)
					addLine(fileOut, DT + train.getDepartureTime());
				else if (!rl.getDepartureTime().equals(""))
					addLine(fileOut, DTR + rl.getDepartureTime());
				else
					addLine(fileOut, EDT + train.getExpectedDepartureTime(rl));

				Location loc = locationManager.getLocationByName(rl.getName());
				// add location comment
				if (Setup.isPrintLocationCommentsEnabled() && !loc.getComment().equals("")) {
					// location comment can have multiple lines
					String[] comments = loc.getComment().split("\n"); // NOI18N
					for (int i = 0; i < comments.length; i++)
						addLine(fileOut, LC + ESC + comments[i] + ESC);
				}
				if (Setup.isTruncateManifestEnabled() && loc.isSwitchListEnabled())
					addLine(fileOut, TRUN);
			}
			// add route comment
			if (!rl.getComment().equals("")) {
				addLine(fileOut, RLC + ESC + rl.getComment() + ESC);
			}
			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NONE) {
				if (rl == train.getSecondLegStartLocation()) {
					engineCsvChange(fileOut, rl, train.getSecondLegOptions());
				}
				if (rl == train.getSecondLegEndLocation())
					addLine(fileOut, RH);
			}
			if (train.getThirdLegOptions() != Train.NONE) {
				if (rl == train.getThirdLegStartLocation()) {
					engineCsvChange(fileOut, rl, train.getThirdLegOptions());
				}
				if (rl == train.getThirdLegEndLocation())
					addLine(fileOut, RH);
			}

			for (int i = 0; i < engineList.size(); i++) {
				Engine engine = (Engine) engineList.get(i);
				if (engine.getRouteLocation() == rl)
					fileOutCsvEngine(fileOut, engine, PL);
			}
			for (int i = 0; i < engineList.size(); i++) {
				Engine engine = (Engine) engineList.get(i);
				if (engine.getRouteDestination() == rl)
					fileOutCsvEngine(fileOut, engine, SL);
			}

			// block cars by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = routeList.get(j);
				for (int k = 0; k < carList.size(); k++) {
					Car car = carList.get(k);
					if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
						fileOutCsvCar(fileOut, car, PC);
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
								CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
					}
				}
			}
			// car set outs
			for (int j = 0; j < carList.size(); j++) {
				Car car = carList.get(j);
				if (car.getRouteDestination() == rl) {
					fileOutCsvCar(fileOut, car, SC);
					cars--;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
							CarLoad.LOAD_TYPE_EMPTY))
						emptyCars--;
				}
			}
			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = routeList.get(r + 1);
				String nextRouteLocationName = splitString(rlNext.getName());
				if (!routeLocationName.equals(nextRouteLocationName)) {
					if (newWork) {
						addLine(fileOut, TD + locationName + DEL + rl.getTrainDirectionString());
						addLine(fileOut, TL + train.getTrainLength(rl) + DEL + emptyCars + DEL + cars);
						addLine(fileOut, TW + train.getTrainWeight(rl));
						newWork = false;
					} else {
						addLine(fileOut, NW);
					}
				}
			} else {
				addLine(fileOut, TT + locationName);
			}
			previousRouteLocationName = routeLocationName;
		}
		// TODO Are there any cars that need to be found?
		// getCarsLocationUnknown(fileOut);

		fileOut.flush();
		fileOut.close();
	}
}
