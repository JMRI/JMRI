package jmri.managers.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.SortOrder;
import jmri.InstanceManager;
import jmri.swing.JmriJTablePersistenceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read XML persistence data for the now removed
 * jmri.managers.DefaultUserMessagePreferences class so that the current
 * {@link jmri.UserPreferencesManager} can use it.
 */
public class DefaultUserMessagePreferencesXml extends jmri.configurexml.AbstractXmlAdapter {

    public DefaultUserMessagePreferencesXml() {
        super();
    }

    /**
     * Default implementation for storing the contents of a User Messages
     * Preferences
     *
     * @param o Object to store, but not really used, because info to be stored
     *          comes from the DefaultUserMessagePreferences
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        // nothing to do, since this class exists only to load older preferences if they exist
        return null;
    }

    public void setStoreElementClass(Element messages) {
        messages.setAttribute("class", "jmri.managers.configurexml.DefaultUserMessagePreferencesXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // ensure the master object exists
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        p.setLoading();

        List<Element> settingList = shared.getChildren("setting");

        for (Element set : settingList) {
            String name = set.getText();
            p.setSimplePreferenceState(name, true);
        }

        List<Element> comboList = shared.getChildren("comboBoxLastValue");

        for (Element cmb : comboList) {
            List<Element> comboItem = cmb.getChildren("comboBox");
            for (int x = 0; x < comboItem.size(); x++) {
                String combo = comboItem.get(x).getAttribute("name").getValue();
                String setting = comboItem.get(x).getAttribute("lastSelected").getValue();
                p.setComboBoxLastSelection(combo, setting);
            }
        }

        List<Element> classList = shared.getChildren("classPreferences");
        for (Element cls : classList) {
            List<Element> multipleList = cls.getChildren("multipleChoice");
            String strClass = cls.getAttribute("class").getValue();
            for (Element mul : multipleList) {
                List<Element> multiItem = mul.getChildren("option");
                for (Element muli : multiItem) {
                    String item = muli.getAttribute("item").getValue();
                    int value = 0x00;
                    try {
                        value = muli.getAttribute("value").getIntValue();
                    } catch (org.jdom2.DataConversionException e) {
                        log.error("failed to convert positional attribute");
                    }
                    p.setMultipleChoiceOption(strClass, item, value);
                }
            }

            List<Element> preferenceList = cls.getChildren("reminderPrompts");
            for (Element pref : preferenceList) {
                List<Element> reminderBoxes = pref.getChildren("reminder");
                for (Element rem : reminderBoxes) {
                    String name = rem.getText();
                    p.setPreferenceState(strClass, name, true);
                }
            }
        }

        List<Element> windowList = shared.getChildren("windowDetails");
        for (Element win : windowList) {
            String strClass = win.getAttribute("class").getValue();
            p.setWindowLocation(strClass,
                    new java.awt.Point(extractCoord(win, "locX"), extractCoord(win, "locY")));
            p.setWindowSize(strClass,
                    new java.awt.Dimension(extractCoord(win, "width"), extractCoord(win, "height")));

            Element prop = win.getChild("properties");
            if (prop != null) {
                for (Object next : prop.getChildren("property")) {
                    Element e = (Element) next;

                    try {
                        Class<?> cl;
                        Constructor<?> ctor;
                        // create key object
                        String key = e.getChild("key").getText();

                        // create value object
                        Object value = null;
                        if (e.getChild("value") != null) {
                            cl = Class.forName(e.getChild("value").getAttributeValue("class"));
                            ctor = cl.getConstructor(new Class<?>[]{String.class});
                            value = ctor.newInstance(new Object[]{e.getChild("value").getText()});
                        }

                        // store
                        p.setProperty(strClass, key, value);
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        log.error("Error loading properties", ex);
                    }
                }
            }
        }

        List<Element> tablesList = shared.getChildren("tableDetails");
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((jtpm) -> {
            for (Element tables : tablesList) {
                List<Element> tableList = tables.getChildren("table");
                for (Element table : tableList) {
                    String strTableName = table.getAttribute("name").getValue();
                    // if this table is already persisted, do not try to persist it again
                    // this can happen if profile preferences have only been partly migrated
                    if (!jtpm.isPersistenceDataRetained(strTableName)) {
                        List<Element> columnList = table.getChildren("column");
                        for (Element column : columnList) {
                            String strColumnName = column.getAttribute("name").getValue();
                            int order = -1;
                            int width = -1;
                            SortOrder sort = SortOrder.UNSORTED;
                            boolean hidden = false;
                            if (column.getChild("order") != null) {
                                order = Integer.parseInt(column.getChild("order").getText());
                            }
                            if (column.getChild("width") != null) {
                                width = Integer.parseInt(column.getChild("width").getText());
                            }
                            if (column.getChild("sortOrder") != null) {
                                sort = SortOrder.valueOf(column.getChild("sortOrder").getText());
                                // before 4.3.5 we used "sort" save column sort state
                            } else if (column.getChild("sort") != null) {
                                switch (Integer.parseInt(column.getChild("sort").getText())) {
                                    case 1: // old sort scheme used 1 for ascending
                                        sort = SortOrder.ASCENDING;
                                        break;
                                    case -1: // old sort scheme used -1 for descending
                                        sort = SortOrder.DESCENDING;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (column.getChild("hidden") != null && column.getChild("hidden").getText().equals("yes")) {
                                hidden = true;
                            }

                            setTableColumnPreferences(jtpm, strTableName, strColumnName, order, width, sort, hidden);
                        }
                    }
                }
            }
        });
        p.finishLoading();
        return true;
    }

    private int extractCoord(Element win, String name) {
        List<Element> locList = win.getChildren(name);
        double coord = 0.0;
        for (Element loc : locList) {
            try {
                coord = Double.parseDouble(loc.getText());
            } catch (NumberFormatException e) {
                log.error("failed to convert positional attribute");
            }
        }
        return (int) coord;
    }

    private void setTableColumnPreferences(JmriJTablePersistenceManager jtpm, String table, String column, int order, int width, SortOrder sort, boolean hidden) {
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultUserMessagePreferencesXml.class);

}
