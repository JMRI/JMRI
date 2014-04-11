package jmri.jmrit.operations.trains;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.jmris.json.JSON;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.web.servlet.operations.Manifest;
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
 */
public class JsonManifest extends TrainCommon {

    protected final Locale locale = Locale.getDefault();
    protected final Train train;
    protected String resourcePrefix;
    private final ObjectMapper mapper = new ObjectMapper();

    private final static Logger log = LoggerFactory.getLogger(Manifest.class);

    public JsonManifest(Train train) {
        this.train = train;
        this.cars = 0;
        this.emptyCars = 0;
        this.resourcePrefix = "Manifest";
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void build() throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(JSON.LOCATIONS, this.getLocations());
        root.put("validity", getDate(true));
        this.mapper.writeValue(TrainManagerXml.instance().createJsonManifestFile(this.train.getName()), root);
    }

    public ArrayNode getLocations() {
        // build manifest
        ArrayNode locations = this.mapper.createArrayNode();

        //StringBuilder builder = new StringBuilder(); // uncomment to commit without breaking everything
        List<RollingStock> engineList = EngineManager.instance().getByTrainList(train);

        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        log.debug("Train has {} cars assigned to it", carList.size());

        ObjectNode node = this.mapper.createObjectNode();
        boolean work = false;
        this.newWork = false; // true if current and next locations are the same
        boolean oldWork; // true if last location was worked
        String previousRouteLocationName = null;
        List<RouteLocation> sequence = train.getRoute().getLocationsBySequenceList();

        for (RouteLocation location : sequence) {
            oldWork = work;
            work = isThereWorkAtLocation(train, location.getLocation());

            // print info only if new location
            String routeLocationName = splitString(location.getName());
            if (work && !routeLocationName.equals(previousRouteLocationName) || (!oldWork && !this.newWork)) {
                // add line break between locations without work and ones with work
                // TODO sometimes an extra line break appears when the user has two or more locations with the
                // "same" name and the second location doesn't have work
                if (oldWork) {
                    locations.add(node);
                    node = this.mapper.createObjectNode();
                }
                this.newWork = true;
                node.put("routeLocationId", location.getId());
                node.put("routeLocationName", routeLocationName);
                node.put("expectedArrivalTime", train.getExpectedArrivalTime(location));
                node.put("expectedDepartureTime", train.getExpectedDepartureTime(location));
                if (sequence.indexOf(location) == 0) {
                    node.put("departureTime", train.getFormatedDepartureTime());
                } else if (!location.getDepartureTime().equals("")) {
                    node.put("departureTime", location.getFormatedDepartureTime());
                }
                node.put("routeLocationComment", location.getComment());

                node.put("trackComments", this.getTrackComments(location, carList));

                // add location comment
                node.put("locationComment", location.getLocation().getComment());
            }

            // engine change or helper service? should be rebuilt by parsers
            node.put("pickupEngines", pickupEngines(engineList, location));
            node.put("blockCarsByTrack", blockCarsByTrack(carList, sequence, location, sequence.indexOf(location), true));
            node.put("dropEngines", dropEngines(engineList, location));

            if (sequence.indexOf(location) != sequence.size() - 1) {
                // Is the next location the same as the previous?
                RouteLocation rlNext = sequence.get(sequence.indexOf(location) + 1);
                if (!routeLocationName.equals(splitString(rlNext.getName()))) {
                    if (newWork) {
                        node.put(JSON.DIRECTION, location.getTrainDirection());
                        node.put(JSON.LENGTH, train.getTrainLength(location));
                        node.put("lengthUnit", Setup.getLengthUnit());
                        node.put(JSON.WEIGHT, train.getTrainWeight(location));
                        node.put(JSON.CARS, cars);
                        node.put("loads", cars - emptyCars);
                        node.put("empties", emptyCars);
                        newWork = false;
                    } else {
                        if (sequence.indexOf(location) == 0) {
                            node.put("NoScheduledWorkAtWithDepartureTime", train.getFormatedDepartureTime());
                        } else if (!location.getDepartureTime().isEmpty()) {
                            node.put("NoScheduledWorkAtWithDepartureTime", location.getFormatedDepartureTime());
                        } else if (Setup.isUseDepartureTimeEnabled()) {
                            node.put("NoScheduledWorkAtWithDepartureTime", train.getExpectedDepartureTime(location));
                        }
                        node.put("CommentAt", location.getComment());
                    }
                    // add location comment
                    node.put("LocationComment", location.getLocation().getComment());
                }
            } else {
                node.put("TrainTerminatesIn", routeLocationName);
            }
            previousRouteLocationName = routeLocationName;
        }

        // Are there any cars that need to be found?
        locations.add(addCarsLocationUnknown());
        // return builder.toString();
        return locations;
    }

    private ObjectNode blockCarsByTrack(List<Car> carList, List<RouteLocation> routeList, RouteLocation location, int r, boolean isManifest) {
        ObjectNode carsNode = this.mapper.createObjectNode();
        ArrayNode pickups = carsNode.putArray("pickups");
        ArrayNode setouts = carsNode.putArray("setouts");
        List<Track> tracks = location.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<String>();
        this.clearUtilityCarTypes();
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name once
            // block cars by destination
            for (int j = r; j < routeList.size(); j++) {
                RouteLocation rld = routeList.get(j);
                for (Car car : carList) {
                    // note that a car in train doesn't have a track assignment
                    if (car.getRouteLocation() == location && car.getTrack() != null && car.getRouteDestination() == rld) {
                        if (car.isUtility()) {
                            pickups.add(jsonPickupUtilityCars(carList, car, location, rld, isManifest));
                        } else {
                            pickups.add(pickUpCar(car));
                        }
                        pickupCars = true;
                        cars++;
                        newWork = true;
                        if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(CarLoad.LOAD_TYPE_EMPTY)) {
                            emptyCars++;
                        }
                    }
                }
            }
            for (Car car : carList) {
                if (Setup.isSortByTrackEnabled()
                        && !splitString(track.getName()).equals(splitString(car.getDestinationTrackName()))) {
                    continue;
                }
                if (car.getRouteDestination() == location && car.getDestinationTrack() != null) {
                    if (car.isUtility()) {
                        setouts.add(setoutUtilityCars(carList, car, location, isManifest));
                    } else {
                        setouts.add(jsonDropCar(car, isLocalMove(car)));
                    }
                    dropCars = true;
                    cars--;
                    newWork = true;
                    if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(CarLoad.LOAD_TYPE_EMPTY)) {
                        emptyCars--;
                    }
                }
            }
            if (!Setup.isSortByTrackEnabled()) {
                break; // done
            }
        }
        return carsNode;
    }

    public ObjectNode jsonPickupUtilityCars(List<Car> carList, Car car, RouteLocation rl, RouteLocation rld, boolean isManifest) {
        if (this.countUtilityCars(Setup.carAttributes, carList, car, rl, rld, PICKUP) == 0) {
            return null; // already printed out this car type
        }
        return pickUpCar(car);
    }

    protected ObjectNode setoutUtilityCars(List<Car> carList, Car car, RouteLocation rl, boolean isManifest) {
        if (countUtilityCars(Setup.carAttributes, carList, car, rl, null, !PICKUP) == 0) {
            return null; // already printed out this car type
        }
        return jsonDropCar(car, isLocalMove(car));
    }

    protected ObjectNode pickUpCar(Car car) {
        if (isLocalMove(car)) {
            return null; // print nothing local move, see dropCar
        }
        ObjectNode node = this.mapper.createObjectNode();
        for (String attribute : Setup.carAttributes) {
            if (this.isComplexCarAttribute(attribute)) {
                node.put(attribute, this.getComplexCarAttribute(car, attribute));
                continue;
            }
            node.put(attribute, getCarAttribute(car, attribute, PICKUP, !LOCAL));
        }
        log.debug("Picking up car {}", node);
        return node;
    }

    public ObjectNode jsonDropCar(Car car, boolean isLocal) {
        ObjectNode node = this.mapper.createObjectNode();
        for (String attribute : Setup.carAttributes) {
            if (this.isComplexCarAttribute(attribute)) {
                node.put(attribute, this.getComplexCarAttribute(car, attribute));
                continue;
            }
            node.put(attribute, getCarAttribute(car, attribute, !PICKUP, isLocal));
        }
        node.put("isLocal", isLocal);
        log.debug("Dropping {}car {}", (isLocal) ? "local " : "", node);
        return node;
    }

    private ObjectNode addCarsLocationUnknown() {
        ObjectNode node = this.mapper.createObjectNode();
        node.putNull("routeLocationId");
        ArrayNode miaCars = this.mapper.createArrayNode();
        for (Car car : CarManager.instance().getCarsLocationUnknown()) {
            miaCars.add(addSearchForCar(car));
        }
        node.put(JSON.CARS, miaCars);
        return node;
    }

    private ObjectNode addSearchForCar(Car car) {
        ObjectNode node = this.mapper.createObjectNode();
        for (String attribute : Setup.carAttributes) {
            if (this.isComplexCarAttribute(attribute)) {
                node.put(attribute, this.getComplexCarAttribute(car, attribute));
                continue;
            }
            node.put(attribute, getCarAttribute(car, attribute, !PICKUP, !LOCAL));
        }
        return node;
    }

    protected ArrayNode dropEngines(List<RollingStock> engines, RouteLocation location) {
        ArrayNode node = this.mapper.createArrayNode();
        for (RollingStock engine : engines) {
            if (engine.getRouteDestination().equals(location)) {
                ObjectNode object = this.mapper.createObjectNode();
                for (String attribute : Setup.engineAttributes) {
                    object.put(attribute, getEngineAttribute((Engine) engine, attribute, true));
                }
                node.add(object);
            }
        }
        return node;
    }

    protected ArrayNode pickupEngines(List<RollingStock> engines, RouteLocation location) {
        ArrayNode node = this.mapper.createArrayNode();
        for (RollingStock engine : engines) {
            if (engine.getRouteLocation().equals(location) && !engine.getTrackName().equals("")) {
                ObjectNode object = this.mapper.createObjectNode();
                for (String attribute : Setup.engineAttributes) {
                    object.put(attribute, getEngineAttribute((Engine) engine, attribute, true));
                }
                node.add(object);
            }
        }
        return node;
    }

    private boolean isComplexCarAttribute(String attribute) {
        return (attribute.equals(Setup.RWE) || attribute.equals(Setup.FINAL_DEST_TRACK));
    }

    private JsonNode getComplexCarAttribute(Car car, String attribute) {
        ObjectNode node = this.mapper.createObjectNode();
        if (attribute.equals(Setup.RWE)) {
            if (!car.getReturnWhenEmptyDestName().equals("")) {
                node.put("returnWhenEmptyDestinationName", car.getReturnWhenEmptyDestinationName());
                node.put("returnWhenEmptyDestTrackName", car.getReturnWhenEmptyDestTrackName());
            }
        } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
            if (!car.getFinalDestinationName().equals("")) {
                node.put("finalDestinationName", car.getFinalDestinationName());
                node.put("finalDestinationTrackName", car.getFinalDestinationTrackName());
            }
        }
        return node;
    }

    private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.LOAD)) {
            return (car.isCaboose() || car.isPassenger()) ? "" : StringEscapeUtils.escapeHtml4(car.getLoadName()); // NOI18N
        } else if (attribute.equals(Setup.HAZARDOUS)) {
            return (car.isHazardous() ? Setup.getHazardousMsg() : ""); // NOI18N
        } else if (attribute.equals(Setup.DROP_COMMENT)) {
            return car.getDropComment();
        } else if (attribute.equals(Setup.PICKUP_COMMENT)) {
            return car.getPickupComment();
        } else if (attribute.equals(Setup.KERNEL)) {
            return car.getKernelName();
        } else if (attribute.equals(Setup.FINAL_DEST)) {
            return car.getFinalDestinationName();
        }
        return getRollingStockAttribute(car, attribute, isPickup, isLocal);
    }

    private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
        if (attribute.equals(Setup.MODEL)) {
            return engine.getModel();
        }
        if (attribute.equals(Setup.CONSIST)) {
            return engine.getConsistName();
        }
        return getRollingStockAttribute(engine, attribute, isPickup, false);
    }

    private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.NUMBER)) {
            return splitString(rs.getNumber());
        } else if (attribute.equals(Setup.ROAD)) {
            return StringEscapeUtils.escapeHtml4(rs.getRoadName());
        } else if (attribute.equals(Setup.TYPE)) {
            String[] type = rs.getTypeName().split("-"); // second half of string
            // can be anything
            return type[0];
        } else if (attribute.equals(Setup.LENGTH)) {
            return rs.getLength();
        } else if (attribute.equals(Setup.COLOR)) {
            return rs.getColor();
        } else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal)) {
            if (rs.getTrack() != null) {
                return rs.getTrackName();
            }
            return "";
        } else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal) {
            return rs.getLocationName();
        } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
            return rs.getDestinationName();
        } else if (attribute.equals(Setup.DESTINATION) && !isPickup) {
            return rs.getDestinationTrackName();
        } else if (attribute.equals(Setup.DEST_TRACK)) {
            return rs.getDestinationTrackName();
        } else if (attribute.equals(Setup.OWNER)) {
            return StringEscapeUtils.escapeHtml4(rs.getOwner());
        } else if (attribute.equals(Setup.COMMENT)) {
            return StringEscapeUtils.escapeHtml4(rs.getComment());
        } else if (attribute.equals(Setup.NONE) || attribute.equals(Setup.NO_NUMBER)
                || attribute.equals(Setup.NO_ROAD) || attribute.equals(Setup.NO_COLOR)
                || attribute.equals(Setup.NO_DESTINATION) || attribute.equals(Setup.NO_DEST_TRACK)
                || attribute.equals(Setup.NO_LOCATION) || attribute.equals(Setup.TAB)) { // attrbiutes that don't print
            return "";
        }
        return Bundle.getMessage(locale, "ErrorPrintOptions"); // the operations code insanely stores what should be NOI18N information in localized manners, so this can easily be triggered
    }

    protected ArrayNode getTrackComments(RouteLocation location, List<Car> cars) {
        ArrayNode comments = this.mapper.createArrayNode();
        if (location.getLocation() != null) {
            List<Track> tracks = location.getLocation().getTrackByNameList(null);
            for (Track track : tracks) {
                // any pick ups or set outs to this track?
                boolean pickup = false;
                boolean setout = false;
                for (Car car : cars) {
                    if (car.getRouteLocation() == location && car.getTrack() != null && car.getTrack() == track) {
                        pickup = true;
                    }
                    if (car.getRouteDestination() == location && car.getDestinationTrack() != null
                            && car.getDestinationTrack() == track) {
                        setout = true;
                    }
                }
                // print the appropriate comment if there's one
                if (pickup && setout && !track.getCommentBoth().equals("")) {
                    comments.add(this.mapper.createObjectNode().put(track.getId(), track.getCommentBoth()));
                } else if (pickup && !setout && !track.getCommentPickup().equals("")) {
                    comments.add(this.mapper.createObjectNode().put(track.getId(), track.getCommentPickup()));
                } else if (!pickup && setout && !track.getCommentSetout().equals("")) {
                    comments.add(this.mapper.createObjectNode().put(track.getId(), track.getCommentSetout()));
                }
            }
        }
        return comments;
    }
}
