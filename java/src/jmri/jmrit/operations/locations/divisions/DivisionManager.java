package jmri.jmrit.operations.locations.divisions;

import java.util.*;

import javax.swing.JComboBox;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.trains.TrainManifestHeaderText;

/**
 * Manages divisions.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class DivisionManager extends PropertyChangeSupport implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "divisionsListLength"; // NOI18N

    public DivisionManager() {
    }

    private int _id = 0;

    public void dispose() {
        _divisionHashTable.clear();
        _id = 0;
    }

    protected Hashtable<String, Division> _divisionHashTable = new Hashtable<String, Division>();

    /**
     * @return Number of divisions
     */
    public int getNumberOfdivisions() {
        return _divisionHashTable.size();
    }

    /**
     * @param name The string name of the Division to get.
     * @return requested Division object or null if none exists
     */
    public Division getDivisionByName(String name) {
        Division Division;
        Enumeration<Division> en = _divisionHashTable.elements();
        while (en.hasMoreElements()) {
            Division = en.nextElement();
            if (Division.getName().equals(name)) {
                return Division;
            }
        }
        return null;
    }

    public Division getDivisionById(String id) {
        return _divisionHashTable.get(id);
    }

    /**
     * Finds an existing Division or creates a new Division if needed requires
     * Division's name creates a unique id for this Division
     *
     * @param name The string name for a new Division.
     *
     *
     * @return new Division or existing Division
     */
    public Division newDivision(String name) {
        Division division = getDivisionByName(name);
        if (division == null) {
            _id++;
            division = new Division(Integer.toString(_id), name);
            Integer oldSize = Integer.valueOf(_divisionHashTable.size());
            _divisionHashTable.put(division.getId(), division);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_divisionHashTable.size()));
        }
        return division;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param division The Division to add.
     */
    public void register(Division division) {
        Integer oldSize = Integer.valueOf(_divisionHashTable.size());
        _divisionHashTable.put(division.getId(), division);
        // find last id created
        int id = Integer.parseInt(division.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_divisionHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param division The Division to delete.
     */
    public void deregister(Division division) {
        if (division == null) {
            return;
        }
        Integer oldSize = Integer.valueOf(_divisionHashTable.size());
        _divisionHashTable.remove(division.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_divisionHashTable.size()));
    }

    /**
     * Sort by Division name
     *
     * @return list of divisions ordered by name
     */
    public List<Division> getDivisionsByNameList() {
        // first get id list
        List<Division> sortList = getList();
        // now re-sort
        List<Division> out = new ArrayList<Division>();
        for (Division division : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (division.getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
                    out.add(j, division);
                    break;
                }
            }
            if (!out.contains(division)) {
                out.add(division);
            }
        }
        return out;

    }

    /**
     * Sort by Division id
     *
     * @return list of divisions ordered by id numbers
     */
    public List<Division> getDivisionsByIdList() {
        List<Division> sortList = getList();
        // now re-sort
        List<Division> out = new ArrayList<Division>();
        for (Division Division : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(Division.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, Division);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(Division)) {
                out.add(Division);
            }
        }
        return out;
    }

    /**
     * Gets an unsorted list of all divisions.
     *
     * @return All divisions.
     */
    public List<Division> getList() {
        List<Division> out = new ArrayList<Division>();
        Enumeration<Division> en = _divisionHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     *
     * @return ComboBox with divisions for this railroad
     */
    public JComboBox<Division> getComboBox() {
        JComboBox<Division> box = new JComboBox<>();
        updateComboBox(box);
        OperationsPanel.padComboBox(box);
        return box;
    }

    public void updateComboBox(JComboBox<Division> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Division division : getDivisionsByNameList()) {
            box.addItem(division);
        }
    }
    
    protected int _maxDivisionNameLength = 0;
    
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
            justification="I18N of Info Message")
    public int getMaxDivisionNameLength() {
        String maxName = TrainManifestHeaderText.getStringHeader_Division();
        for (Division div : getList()) {
            if (div.getName().length() > maxName.length()) {
                maxName = div.getName();
            }
        }
        if (maxName.length() != _maxDivisionNameLength) {
            log.info(Bundle.getMessage("InfoMaxDivisionName", maxName, maxName.length()));
            _maxDivisionNameLength = maxName.length();
        }
        return _maxDivisionNameLength;
    }

    public void load(Element root) {
        if (root.getChild(Xml.DIVISIONS) != null) {
            List<Element> divisions = root.getChild(Xml.DIVISIONS).getChildren(Xml.DIVISION);
            log.debug("readFile sees {} divisions", divisions.size());
            for (Element division : divisions) {
                register(new Division(division));
            }
        }
    }

    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.DIVISIONS));
        // add entries
        List<Division> DivisionList = getDivisionsByNameList();
        for (Division division : DivisionList) {
            values.addContent(division.store());
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(DivisionManager.class);

    @Override
    public void initialize() {
        // do nothing
    }
}
