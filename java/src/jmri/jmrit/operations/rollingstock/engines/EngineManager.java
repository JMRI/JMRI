package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;
import java.util.*;

import javax.swing.JComboBox;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManifestHeaderText;

/**
 * Manages the engines.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class EngineManager extends RollingStockManager<Engine>
        implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public EngineManager() {
    }

    /**
     * Finds an existing engine or creates a new engine if needed requires engine's
     * road and number
     *
     * @param engineRoad   The engine's road initials
     * @param engineNumber The engine's road number
     *
     * @return new engine or existing engine
     */
    @Override
    public Engine newRS(String engineRoad, String engineNumber) {
        Engine engine = getByRoadAndNumber(engineRoad, engineNumber);
        if (engine == null) {
            engine = new Engine(engineRoad, engineNumber);
            register(engine);
        }
        return engine;
    }

    @Override
    public void deregister(Engine engine) {
        super.deregister(engine);
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
    }

    /**
     * Sort by engine model
     *
     * @return list of engines ordered by engine model
     */
    public List<Engine> getByModelList() {
        return getByList(getByRoadNameList(), BY_MODEL);
    }

    /**
     * Sort by engine consist
     *
     * @return list of engines ordered by engine consist
     */
    public List<Engine> getByConsistList() {
        return getByList(getByRoadNameList(), BY_CONSIST);
    }

    public List<Engine> getByHpList() {
        return getByList(getByModelList(), BY_HP);
    }

    // The special sort options for engines
    private static final int BY_MODEL = 30;
    private static final int BY_CONSIST = 31;
    private static final int BY_HP = 32;

    // add engine options to sort comparator
    @Override
    protected java.util.Comparator<Engine> getComparator(int attribute) {
        switch (attribute) {
            case BY_MODEL:
                return (e1, e2) -> (e1.getModel().compareToIgnoreCase(e2.getModel()));
            case BY_CONSIST:
                return (e1, e2) -> (e1.getConsistName().compareToIgnoreCase(e2.getConsistName()));
            case BY_HP:
                return (e1, e2) -> (e1.getHpInteger() - e2.getHpInteger());
            default:
                return super.getComparator(attribute);
        }
    }

    /**
     * return a list available engines (no assigned train) engines are ordered least
     * recently moved to most recently moved.
     *
     * @param train The Train requesting this list.
     *
     * @return Ordered list of engines not assigned to a train
     */
    public List<Engine> getAvailableTrainList(Train train) {
        // now build list of available engines for this route
        List<Engine> out = new ArrayList<>();
        // get engines by moves list
        for (RollingStock rs : getByMovesList()) {
            Engine engine = (Engine) rs;
            if (engine.getTrack() != null && (engine.getTrain() == null || engine.getTrain() == train)) {
                out.add(engine);
            }
        }
        return out;
    }

    /**
     * Returns a list of locos sorted by blocking number for a train. This returns a
     * list of consisted locos in the order that they were entered in.
     *
     * @param train The Train requesting this list.
     * @return A list of sorted locos.
     */
    public List<Engine> getByTrainBlockingList(Train train) {
        return getByList(super.getByTrainList(train), BY_BLOCKING);
    }

    /**
     * Get a list of engine road names.
     *
     * @param model The string model name, can be NONE.
     *
     * @return List of engine road names.
     */
    public List<String> getEngineRoadNames(String model) {
        List<String> names = new ArrayList<>();
        Enumeration<String> en = _hashTable.keys();
        while (en.hasMoreElements()) {
            Engine engine = getById(en.nextElement());
            if ((engine.getModel().equals(model) || model.equals(NONE)) && !names.contains(engine.getRoadName())) {
                names.add(engine.getRoadName());
            }
        }
        java.util.Collections.sort(names);
        return names;
    }

    public void updateEngineRoadComboBox(String engineModel, JComboBox<String> roadEngineBox) {
        roadEngineBox.removeAllItems();
        roadEngineBox.addItem(NONE);
        List<String> roads = getEngineRoadNames(engineModel);
        for (String roadName : roads) {
            roadEngineBox.addItem(roadName);
        }
        OperationsPanel.padComboBox(roadEngineBox);
    }

    int _commentLength = 0;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
            justification="I18N of Info Message")
    public int getMaxCommentLength() {
        if (_commentLength == 0) {
            _commentLength = TrainManifestHeaderText.getStringHeader_Comment().length();
            String comment = "";
            Engine engineMax = null;
            for (Engine engine : getList()) {
                if (engine.getComment().length() > _commentLength) {
                    _commentLength = engine.getComment().length();
                    comment = engine.getComment();
                    engineMax = engine;
                }
            }
            if (engineMax != null) {
                log.info(Bundle.getMessage("InfoMaxComment", engineMax.toString(), comment, _commentLength));
            }
        }
        return _commentLength;
    }

    public void load(Element root) {
        if (root.getChild(Xml.ENGINES) != null) {
            List<Element> engines = root.getChild(Xml.ENGINES).getChildren(Xml.ENGINE);
            log.debug("readFile sees {} engines", engines.size());
            for (Element e : engines) {
                register(new Engine(e));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     *
     * @param root The common Element for operations-engines.dtd.
     *
     */
    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.ENGINES));
        // add entries
        for (RollingStock rs : getByRoadNameList()) {
            Engine eng = (Engine) rs;
            values.addContent(eng.store());
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Engine.COMMENT_CHANGED_PROPERTY)) {
            _commentLength = 0;
        }
        super.propertyChange(evt);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        // create manager to load engines and their attributes
        InstanceManager.getDefault(EngineManagerXml.class);
    }
}
