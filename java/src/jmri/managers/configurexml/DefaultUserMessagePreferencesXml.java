package jmri.managers.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.SortOrder;
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

        for (int i = 0; i < settingList.size(); i++) {
            String name = settingList.get(i).getText();
            p.setSimplePreferenceState(name, true);
        }

        List<Element> comboList = shared.getChildren("comboBoxLastValue");

        for (int i = 0; i < comboList.size(); i++) {
            List<Element> comboItem = comboList.get(i).getChildren("comboBox");
            for (int x = 0; x < comboItem.size(); x++) {
                String combo = comboItem.get(x).getAttribute("name").getValue();
                String setting = comboItem.get(x).getAttribute("lastSelected").getValue();
                p.addComboBoxLastSelection(combo, setting);
            }
        }

        List<Element> classList = shared.getChildren("classPreferences");
        for (int k = 0; k < classList.size(); k++) {
            List<Element> multipleList = classList.get(k).getChildren("multipleChoice");
            String strClass = classList.get(k).getAttribute("class").getValue();
            for (int i = 0; i < multipleList.size(); i++) {
                List<Element> multiItem = multipleList.get(i).getChildren("option");
                for (int x = 0; x < multiItem.size(); x++) {
                    String item = multiItem.get(x).getAttribute("item").getValue();
                    int value = 0x00;
                    try {
                        value = multiItem.get(x).getAttribute("value").getIntValue();
                    } catch (org.jdom2.DataConversionException e) {
                        log.error("failed to convert positional attribute");
                    }
                    p.setMultipleChoiceOption(strClass, item, value);
                }
            }

            List<Element> preferenceList = classList.get(k).getChildren("reminderPrompts");
            for (int i = 0; i < preferenceList.size(); i++) {
                List<Element> reminderBoxes = preferenceList.get(i).getChildren("reminder");
                for (int j = 0; j < reminderBoxes.size(); j++) {
                    String name = reminderBoxes.get(j).getText();
                    p.setPreferenceState(strClass, name, true);
                }
            }
        }

        List<Element> windowList = shared.getChildren("windowDetails");
        for (int k = 0; k < windowList.size(); k++) {
            String strClass = windowList.get(k).getAttribute("class").getValue();
            List<Element> locListX = windowList.get(k).getChildren("locX");
            double x = 0.0;
            for (int i = 0; i < locListX.size(); i++) {
                try {
                    x = Double.parseDouble(locListX.get(i).getText());
                } catch (NumberFormatException e) {
                    log.error("failed to convert positional attribute");
                }
            }
            List<Element> locListY = windowList.get(k).getChildren("locY");
            double y = 0.0;
            for (int i = 0; i < locListY.size(); i++) {
                try {
                    y = Double.parseDouble(locListY.get(i).getText());
                } catch (NumberFormatException e) {
                    log.error("failed to convert positional attribute");
                }
            }
            p.setWindowLocation(strClass, new java.awt.Point((int) x, (int) y));

            List<Element> sizeWidth = windowList.get(k).getChildren("width");
            double width = 0.0;
            for (int i = 0; i < sizeWidth.size(); i++) {
                try {
                    width = Double.parseDouble(sizeWidth.get(i).getText());
                } catch (NumberFormatException e) {
                    log.error("failed to convert positional attribute");
                }
            }
            List<Element> heightList = windowList.get(k).getChildren("height");
            double height = 0.0;
            for (int i = 0; i < heightList.size(); i++) {
                try {
                    height = Double.parseDouble(heightList.get(i).getText());
                } catch (NumberFormatException e) {
                    log.error("failed to convert positional attribute");
                }
            }
            p.setWindowSize(strClass, new java.awt.Dimension((int) width, (int) height));

            Element prop = windowList.get(k).getChild("properties");
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
        for (Element tables : tablesList) {
            List<Element> tableList = tables.getChildren("table");
            for (Element table : tableList) {
                List<Element> columnList = table.getChildren("column");
                String strTableName = table.getAttribute("name").getValue();
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

                    p.setTableColumnPreferences(strTableName, strColumnName, order, width, sort, hidden);
                }
            }
        }
        p.finishLoading();
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultUserMessagePreferencesXml.class);
}
