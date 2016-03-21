package jmri.jmrit.operations.trains;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import jmri.jmris.json.JSON;
import jmri.jmris.json.JsonUtil;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A minimal manifest in JSON.
 *
 * This manifest is intended to be read by machines for building manifests in
 * other, human-readable outputs. This manifest is retained at build time so
 * that manifests can be endlessly recreated in other formats, even if the
 * operations database state has changed. It is expected that the parsers for
 * this manifest will be capable of querying operations for more specific
 * information while transforming this manifest into other formats.
 *
 * @author rhwood
 * @author Daniel Boudreau 1/26/2015 Load all cars including utility cars into
 * the JSON file, and tidied up the code a bit.
 *
 */
public class JsonManifest extends TrainCommon {

    protected final Locale locale = Locale.getDefault();
    protected final Train train;
    private final ObjectMapper mapper = new ObjectMapper();

    private final static Logger log = LoggerFactory.getLogger(JsonManifest.class);

    public JsonManifest(Train train) {
        this.train = train;
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public File getFile() {
        return TrainManagerXml.instance().getManifestFile(this.train.getName(), JSON.JSON);
    }

    public void build() throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        if (!this.train.getRailroadName().equals(Train.NONE)) {
            root.put(JSON.RAILROAD, StringEscapeUtils.escapeHtml4(this.train.getRailroadName()));
        } else {
            root.put(JSON.RAILROAD, StringEscapeUtils.escapeHtml4(Setup.getRailroadName()));
        }
        root.put(JSON.NAME, StringEscapeUtils.escapeHtml4(this.train.getName()));
        root.put(JSON.DESCRIPTION, StringEscapeUtils.escapeHtml4(this.train.getDescription()));
        root.put(JSON.LOCATIONS, this.getLocations());
        if (!this.train.getManifestLogoURL().equals(Train.NONE)) {
            // The operationsServlet will need to change this to a usable URL
            root.put(JSON.IMAGE, this.train.getManifestLogoURL());
        }
        root.put(JSON.DATE, TrainCommon.getISO8601Date(true)); // Validity
        this.mapper.writeValue(TrainManagerXml.instance().createManifestFile(this.train.getName(), JSON.JSON), root);
    }

    public ArrayNode getLocations() {
        // get engine and car lists
        List<Engine> engineList = engineManager.getByTrainBlockingList(train);
        List<Car> carList = carManager.getByTrainDestinationList(train);
        ArrayNode locations = this.mapper.createArrayNode();
        List<RouteLocation> route = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation routeLocation : route) {
            String locationName = splitString(routeLocation.getName());
            ObjectNode jsonLocation = this.mapper.createObjectNode();
            ObjectNode jsonCars = this.mapper.createObjectNode();
            jsonLocation.put(JSON.NAME, StringEscapeUtils.escapeHtml4(locationName));
            jsonLocation.put(JSON.ID, routeLocation.getId());
            if (routeLocation != train.getRoute().getDepartsRouteLocation()) {
                jsonLocation.put(JSON.ARRIVAL_TIME, train.getExpectedArrivalTime(routeLocation));
            }
            if (routeLocation == train.getRoute().getDepartsRouteLocation()) {
                jsonLocation.put(JSON.DEPARTURE_TIME, train.getDepartureTime());
            } else if (!routeLocation.getDepartureTime().equals(RouteLocation.NONE)) {
                jsonLocation.put(JSON.DEPARTURE_TIME, routeLocation.getDepartureTime());
            } else {
                jsonLocation.put(JSON.EXPECTED_DEPARTURE, train.getExpectedDepartureTime(routeLocation));
            }
            // add location comment and id
            ObjectNode locationNode = this.mapper.createObjectNode();
            locationNode.put(JSON.COMMENT, StringEscapeUtils.escapeHtml4(routeLocation.getLocation().getComment()));
            locationNode.put(JSON.ID, routeLocation.getLocation().getId());
            jsonLocation.put(JSON.LOCATION, locationNode);
            jsonLocation.put(JSON.COMMENT, StringEscapeUtils.escapeHtml4(routeLocation.getComment()));
            // engine change or helper service?
            if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                ArrayNode options = this.mapper.createArrayNode();
                if (routeLocation == train.getSecondLegStartLocation()) {
                    if ((train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
                        options.add(JSON.ADD_HELPERS);
                    } else if ((train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
                            || (train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
                        options.add(JSON.CHANGE_CABOOSE);
                    } else if ((train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
                        options.add(JSON.CHANGE_ENGINES);
                    }
                }
                if (routeLocation == train.getSecondLegEndLocation()) {
                    options.add(JSON.REMOVE_HELPERS);
                }
                jsonLocation.put(JSON.OPTIONS, options);
            }
            if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                ArrayNode options = this.mapper.createArrayNode();
                if (routeLocation == train.getThirdLegStartLocation()) {
                    if ((train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
                        options.add(JSON.ADD_HELPERS);
                    } else if ((train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
                            || (train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
                        options.add(JSON.CHANGE_CABOOSE);
                    } else if ((train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
                        options.add(JSON.CHANGE_ENGINES);
                    }
                }
                if (routeLocation == train.getThirdLegEndLocation()) {
                    options.add(JSON.ADD_HELPERS);
                }
                jsonLocation.put(JSON.OPTIONS, options);
            }

            ObjectNode engines = this.mapper.createObjectNode();
            engines.put(JSON.ADD, pickupEngines(engineList, routeLocation));
            engines.put(JSON.REMOVE, dropEngines(engineList, routeLocation));
            jsonLocation.put(JSON.ENGINES, engines);

            // block cars by destination
            ArrayNode pickups = this.mapper.createArrayNode();
            for (RouteLocation destination : route) {
                for (Car car : carList) {
                    if (car.getRouteLocation() == routeLocation && car.getRouteDestination() == destination) {
                        pickups.add(JsonUtil.getCar(car));
                    }
                }
            }
            jsonCars.put(JSON.ADD, pickups);
            // car set outs
            ArrayNode setouts = this.mapper.createArrayNode();
            for (Car car : carList) {
                if (car.getRouteDestination() == routeLocation) {
                    setouts.add(JsonUtil.getCar(car));
                }
            }
            jsonCars.put(JSON.REMOVE, setouts);

            if (routeLocation != train.getRoute().getTerminatesRouteLocation()) {
                jsonLocation.put(JSON.TRACK, this.getTrackComments(routeLocation, carList));
                jsonLocation.put(JSON.DIRECTION, routeLocation.getTrainDirection());
                ObjectNode length = this.mapper.createObjectNode();
                length.put(JSON.LENGTH, train.getTrainLength(routeLocation));
                length.put(JSON.UNIT, Setup.getLengthUnit());
                jsonLocation.put(JSON.LENGTH, length);
                jsonLocation.put(JSON.WEIGHT, train.getTrainWeight(routeLocation));
                int cars = train.getNumberCarsInTrain(routeLocation);
                int emptyCars = train.getNumberEmptyCarsInTrain(routeLocation);
                jsonCars.put(JSON.TOTAL, cars);
                jsonCars.put(JSON.LOADS, cars - emptyCars);
                jsonCars.put(JSON.EMPTIES, emptyCars);
            } else {
                log.debug("Train terminates in {}", locationName);
                jsonLocation.put("TrainTerminatesIn", StringEscapeUtils.escapeHtml4(locationName));
            }
            jsonLocation.put(JSON.CARS, jsonCars);
            locations.add(jsonLocation);
        }
        return locations;
    }

    protected ArrayNode dropEngines(List<Engine> engines, RouteLocation routeLocation) {
        ArrayNode node = this.mapper.createArrayNode();
        for (Engine engine : engines) {
            if (engine.getRouteDestination().equals(routeLocation)) {
                node.add(JsonUtil.getEngine(engine));
            }
        }
        return node;
    }

    protected ArrayNode pickupEngines(List<Engine> engines, RouteLocation routeLocation) {
        ArrayNode node = this.mapper.createArrayNode();
        for (Engine engine : engines) {
            if (engine.getRouteLocation().equals(routeLocation)) {
                node.add(JsonUtil.getEngine(engine));
            }
        }
        return node;
    }

    // TODO: migrate comments into actual setout/pickup track location spaces
    private ObjectNode getTrackComments(RouteLocation routeLocation, List<Car> cars) {
        ObjectNode comments = this.mapper.createObjectNode();
        if (routeLocation.getLocation() != null) {
            List<Track> tracks = routeLocation.getLocation().getTrackByNameList(null);
            for (Track track : tracks) {
                ObjectNode jsonTrack = this.mapper.createObjectNode();
                // any pick ups or set outs to this track?
                boolean pickup = false;
                boolean setout = false;
                for (Car car : cars) {
                    if (car.getRouteLocation() == routeLocation && car.getTrack() != null && car.getTrack() == track) {
                        pickup = true;
                    }
                    if (car.getRouteDestination() == routeLocation && car.getDestinationTrack() != null
                            && car.getDestinationTrack() == track) {
                        setout = true;
                    }
                }
                if (pickup) {
                    jsonTrack.put(JSON.ADD, StringEscapeUtils.escapeHtml4(track.getCommentPickup()));
                }
                if (setout) {
                    jsonTrack.put(JSON.REMOVE, StringEscapeUtils.escapeHtml4(track.getCommentSetout()));
                }
                if (pickup && setout) {
                    jsonTrack.put(JSON.ADD_AND_REMOVE, StringEscapeUtils.escapeHtml4(track.getCommentBoth()));
                }
                if (pickup || setout) {
                    jsonTrack.put(JSON.COMMENT, StringEscapeUtils.escapeHtml4(track.getComment()));
                    comments.put(track.getId(), jsonTrack);
                }
            }
        }
        return comments;
    }
}
