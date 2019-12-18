package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.Train;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the engines.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class EngineManager extends RollingStockManager<Engine> implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    protected Hashtable<String, Consist> _consistHashTable = new Hashtable<>(); // stores Consists by number

    public static final String CONSISTLISTLENGTH_CHANGED_PROPERTY = "ConsistListLength"; // NOI18N

    public EngineManager() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized EngineManager instance() {
        return InstanceManager.getDefault(EngineManager.class);
    }

    /**
     * @return requested Engine object or null if none exists
     */
    @Override
    public Engine getById(String id) {
        return super.getById(id);
    }

    @Override
    public Engine getByRoadAndNumber(String engineRoad, String engineNumber) {
        String engineId = Engine.createId(engineRoad, engineNumber);
        return getById(engineId);
    }

    /**
     * Finds an existing engine or creates a new engine if needed requires
     * engine's road and number
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

    /**
     * Creates a new consist if needed
     *
     * @param name of the consist
     * @return consist
     */
    public Consist newConsist(String name) {
        Consist consist = getConsistByName(name);
        if (consist == null) {
            consist = new Consist(name);
            Integer oldSize = Integer.valueOf(_consistHashTable.size());
            _consistHashTable.put(name, consist);
            setDirtyAndFirePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_consistHashTable.size()));
        }
        return consist;
    }

    public void deleteConsist(String name) {
        Consist consist = getConsistByName(name);
        if (consist != null) {
            consist.dispose();
            Integer oldSize = Integer.valueOf(_consistHashTable.size());
            _consistHashTable.remove(name);
            setDirtyAndFirePropertyChange(CONSISTLISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_consistHashTable.size()));
        }
    }

    public Consist getConsistByName(String name) {
        return _consistHashTable.get(name);
    }

    public void replaceConsistName(String oldName, String newName) {
        Consist oldConsist = getConsistByName(oldName);
        if (oldConsist != null) {
            Consist newConsist = newConsist(newName);
            // keep the lead engine
            Engine leadEngine = oldConsist.getLead();
            leadEngine.setConsist(newConsist);
            for (Engine engine : oldConsist.getEngines()) {
                engine.setConsist(newConsist);
            }
        }
    }

    /**
     * Creates a combo box containing all of the consist names
     *
     * @return a combo box with all of the consist names
     */
    public JComboBox<String> getConsistComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(NONE);
        for (String name : getConsistNameList()) {
            box.addItem(name);
        }
        return box;
    }

    public void updateConsistComboBox(JComboBox<String> box) {
        box.removeAllItems();
        box.addItem(NONE);
        for (String name : getConsistNameList()) {
            box.addItem(name);
        }
    }

    public List<String> getConsistNameList() {
        String[] names = new String[_consistHashTable.size()];
        List<String> out = new ArrayList<>();
        Enumeration<String> en = _consistHashTable.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            names[i++] = en.nextElement();
        }
        java.util.Arrays.sort(names);
        for (String name : names) {
            out.add(name);
        }
        return out;
    }

    public int getConsistMaxNameLength() {
        int maxLength = 0;
        for (String name : getConsistNameList()) {
            if (name.length() > maxLength) {
                maxLength = name.length();
            }
        }
        return maxLength;
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
    private static final int BY_MODEL = 4;
    private static final int BY_CONSIST = 5;
    private static final int BY_HP = 13;

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
     * return a list available engines (no assigned train) engines are ordered
     * least recently moved to most recently moved.
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
     * Returns a list of locos sorted by blocking number for a train. This
     * returns a list of consisted locos in the order that they were entered in.
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

    @Override
    public void dispose() {
        for (String consistName : getConsistNameList()) {
            deleteConsist(consistName);
        }
        super.dispose();
    }

    public void load(Element root) {
        // new format using elements starting version 3.3.1
        if (root.getChild(Xml.NEW_CONSISTS) != null) {
            List<Element> consists = root.getChild(Xml.NEW_CONSISTS).getChildren(Xml.CONSIST);
            log.debug("Engine manager sees {} consists", consists.size());
            Attribute a;
            for (Element consist : consists) {
                if ((a = consist.getAttribute(Xml.NAME)) != null) {
                    newConsist(a.getValue());
                }
            }
        } // old format
        else if (root.getChild(Xml.CONSISTS) != null) {
            String names = root.getChildText(Xml.CONSISTS);
            if (!names.equals(NONE)) {
                String[] consistNames = names.split("%%"); // NOI18N
                log.debug("consists: {}", names);
                for (String name : consistNames) {
                    newConsist(name);
                }
            }
        }

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
        //     root.addContent(new Element(Xml.OPTIONS)); // nothing to store under options

        Element values;
        List<String> names = getConsistNameList();
        if (Control.backwardCompatible) {
            root.addContent(values = new Element(Xml.CONSISTS));
            for (String name : names) {
                String consistNames = name + "%%"; // NOI18N
                values.addContent(consistNames);
            }
        }
        // new format using elements
        Element consists = new Element(Xml.NEW_CONSISTS);
        for (String name : names) {
            Element consist = new Element(Xml.CONSIST);
            consist.setAttribute(new Attribute(Xml.NAME, name));
            consists.addContent(consist);
        }
        root.addContent(consists);

        root.addContent(values = new Element(Xml.ENGINES));
        // add entries
        for (RollingStock rs : getByRoadNameList()) {
            Engine eng = (Engine) rs;
            values.addContent(eng.store());

        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class
        ).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        // create manager to load engines and their attributes
        InstanceManager.getDefault(EngineManagerXml.class);
    }
}
