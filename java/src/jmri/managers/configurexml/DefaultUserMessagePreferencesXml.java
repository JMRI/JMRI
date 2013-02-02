package jmri.managers.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import java.util.List;
import java.lang.reflect.*;

public class DefaultUserMessagePreferencesXml extends jmri.configurexml.AbstractXmlAdapter{

    public DefaultUserMessagePreferencesXml() {
        super();
    }

     /**
     * Default implementation for storing the contents of a
     * User Messages Preferences
     * @param o Object to store, but not really used, because 
     *              info to be stored comes from the DefaultUserMessagePreferences
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        jmri.UserPreferencesManager p = (jmri.UserPreferencesManager) o;

        Element messages = new Element("UserMessagePreferences");
        setStoreElementClass(messages);     
        
        java.util.ArrayList<String> preferenceList = ((jmri.managers.DefaultUserMessagePreferences)p).getSimplePreferenceStateList();
        for (int i = 0; i < preferenceList.size(); i++) {
            Element pref = new Element("setting");
            pref.addContent(preferenceList.get(i));
            messages.addContent(pref);
        }
        
        int comboBoxSize = p.getComboBoxSelectionSize();
        if (comboBoxSize >0){
            Element comboList = new Element("comboBoxLastValue");
                for(int i = 0; i<comboBoxSize; i++){
                    //No point in storing the last entered/selected value if it is blank
                    if ((p.getComboBoxLastSelection(i)!=null)&&(!p.getComboBoxLastSelection(i).equals(""))){
                        Element combo = new Element("comboBox");
                        combo.setAttribute("name", p.getComboBoxName(i));
                        combo.setAttribute("lastSelected", p.getComboBoxLastSelection(i));
                        comboList.addContent(combo);
                    }
                }
            messages.addContent(comboList);
        }
        java.util.ArrayList<String> preferenceClassList = p.getPreferencesClasses();
        for (int k = 0; k<preferenceClassList.size(); k++){
            String strClass = preferenceClassList.get(k);
            java.util.ArrayList<String> multipleList = p.getMultipleChoiceList(strClass);
            Element classElement = new Element("classPreferences");
            classElement.setAttribute("class", strClass);
            //This bit deals with the multiple choice
            boolean store = false;
            Element multiOption = new Element("multipleChoice");
            for (int i=0; i<multipleList.size(); i++){
                String itemName = p.getChoiceName(strClass, i);
                if (p.getMultipleChoiceDefaultOption(strClass, itemName)!=p.getMultipleChoiceOption(strClass, itemName)){
                    //Only save if we are not at the default value.
                    Element multiOptionItem = new Element("option");
                    store = true;
                    multiOptionItem.setAttribute("item", itemName);
                    multiOptionItem.setAttribute("value", Integer.toString(p.getMultipleChoiceOption(strClass, itemName)));
                    multiOption.addContent(multiOptionItem);
                }
            }
            if (store){
                classElement.addContent(multiOption);
                    
            }
            
            boolean listStore=false;
            java.util.ArrayList<String> singleList = p.getPreferenceList(strClass);
            if (singleList.size()!=0){
                Element singleOption = new Element("reminderPrompts");
                for (int i = 0; i<singleList.size(); i++){
                    String itemName = p.getPreferenceItemName(strClass, i);
                    if(p.getPreferenceState(strClass, itemName)){
                        Element pref = new Element("reminder");
                        pref.addContent(singleList.get(i));
                        singleOption.addContent(pref);
                        listStore = true;
                    }
                }
                if (listStore)
                    classElement.addContent(singleOption);
            }
            
            //This bit deals with simple hiding of messages
            if ((store) || (listStore))
                messages.addContent(classElement);
        }
        
        java.util.ArrayList<String> windowList = p.getWindowList();
        if (windowList != null && windowList.size() != 0){
            for (String strClass : windowList) {
                Element windowElement = new Element("windowDetails");
                windowElement.setAttribute("class", strClass);
                boolean set = false;
                if(p.getSaveWindowLocation(strClass)){
                    try {
                        double x = p.getWindowLocation(strClass).getX();
                        double y = p.getWindowLocation(strClass).getY();
                        Element loc = new Element("locX");
                        loc.addContent(Double.toString(x));
                        windowElement.addContent(loc);
                        loc = new Element("locY");
                        windowElement.addContent(loc);
                        loc.addContent(Double.toString(y));
                        set=true;
                    } catch (NullPointerException ex){
                        //Considered normal if the window hasn't been closed or all of the information hasn't been set
                    }
                }
                if(p.getSaveWindowSize(strClass)){
                    try {
                        double width=p.getWindowSize(strClass).getWidth();
                        double height=p.getWindowSize(strClass).getHeight();
                        //We do not want to save the width or height if it is set to zero!
                        if (!(width==0.0 && height==0.0)){
                            Element size = new Element("width");
                            size.addContent(Double.toString(width));
                            windowElement.addContent(size);
                            size = new Element("height");
                            size.addContent(Double.toString(height));
                            windowElement.addContent(size);
                            set=true;
                        }
                    } catch (NullPointerException ex){
                        //Considered normal if the window hasn't been closed
                    }
                }
                java.util.Set<Object> s = p.getPropertyKeys(strClass);
                if (s != null && s.size() != 0){
                    Element ret = new Element("properties");
                    windowElement.addContent(ret);
                    for (Object key : s) {
                        Object value = p.getProperty(strClass, key);
                        Element prop = new Element("property");
                        ret.addContent(prop);
                        prop.addContent(new Element("key")
                                        .setAttribute("class", key.getClass().getName())
                                        .setText(key.toString())
                                    );
                        if (value != null) {
                            prop.addContent(new Element("value")
                                        .setAttribute("class", value.getClass().getName())
                                        .setText(value.toString())
                                        );
                        }
                    }
                    set=true;
                }

                
                if (set)
                    messages.addContent(windowElement);
            }
        }
        
        if(p.getTablesList().size()!=0){
            Element tablesElement = new Element("tableDetails");
            for(String table: p.getTablesList()){
                Element tableElement = new Element("table");
                tableElement.setAttribute("name", table);
                for(String column: p.getTablesColumnList(table)){
                    Element columnElement = new Element("column");
                    columnElement.setAttribute("name", column);
                    if(p.getTableColumnOrder(table, column)!=-1){
                        columnElement.addContent(new Element("order").addContent(Integer.toString(p.getTableColumnOrder(table, column))));
                    }
                    if(p.getTableColumnWidth(table, column)!=-1){
                        columnElement.addContent(new Element("width").addContent(Integer.toString(p.getTableColumnWidth(table, column))));
                    }
                    if(p.getTableColumnSort(table, column)!=0){
                        columnElement.addContent(new Element("sort").addContent(Integer.toString(p.getTableColumnSort(table, column))));
                    }
                    if(p.getTableColumnHidden(table, column)){
                        columnElement.addContent(new Element("hidden").addContent("yes"));
                    }
                    tableElement.addContent(columnElement);
                }
                tablesElement.addContent(tableElement);
            }
            messages.addContent(tablesElement);
        }
        return messages;
    }
     
    public void setStoreElementClass(Element messages) {
        messages.setAttribute("class","jmri.managers.configurexml.DefaultUserMessagePreferencesXml");
    }
    
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param messages Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element messages) {
        // ensure the master object exists
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        p.setLoading();
                
        List<Element> settingList = messages.getChildren("setting");
        
        for (int i = 0; i < settingList.size(); i++) {
            String name = settingList.get(i).getText();
            p.setSimplePreferenceState(name, true);
        }
        
        List<Element> comboList = messages.getChildren("comboBoxLastValue");
        
        for (int i = 0; i < comboList.size(); i++) {    
            List<Element> comboItem = comboList.get(i).getChildren("comboBox");
            for (int x = 0; x<comboItem.size(); x++){
                String combo = comboItem.get(x).getAttribute("name").getValue();
                String setting = comboItem.get(x).getAttribute("lastSelected").getValue();
                p.addComboBoxLastSelection(combo, setting);
            }
        }
        
        List<Element> classList = messages.getChildren("classPreferences");
        for (int k = 0; k < classList.size(); k++) {
            List<Element> multipleList = classList.get(k).getChildren("multipleChoice");
            String strClass = classList.get(k).getAttribute("class").getValue();
            for (int i = 0; i < multipleList.size(); i++) {
                List<Element> multiItem = multipleList.get(i).getChildren("option");
                for (int x = 0; x<multiItem.size(); x++){
                    String item = multiItem.get(x).getAttribute("item").getValue();
                    int value = 0x00;
                     try {
                        value = multiItem.get(x).getAttribute("value").getIntValue();
                    } catch ( org.jdom.DataConversionException e) {
                        log.error("failed to convert positional attribute");
                    }
                    p.setMultipleChoiceOption(strClass, item, value);
                }
            }

            List<Element> preferenceList = classList.get(k).getChildren("reminderPrompts");
            for (int i = 0; i<preferenceList.size(); i++){
                List<Element> reminderBoxes = preferenceList.get(i).getChildren("reminder");
                for (int j = 0; j < reminderBoxes.size(); j++) {
                    String name = reminderBoxes.get(j).getText();
                    p.setPreferenceState(strClass, name, true);
                }
            }
        }
        

        List<Element> windowList = messages.getChildren("windowDetails");
        for (int k = 0; k < windowList.size(); k++) {
            String strClass = windowList.get(k).getAttribute("class").getValue();
            List<Element> locListX = windowList.get(k).getChildren("locX");
            double x=0.0;
            for (int i = 0; i < locListX.size(); i++) {
                try {
                    x = Double.parseDouble(locListX.get(i).getText());
                } catch ( NumberFormatException e) {
                    log.error("failed to convert positional attribute");
                }
            }
            List<Element> locListY = windowList.get(k).getChildren("locY");
            double y=0.0; 
            for (int i = 0; i < locListY.size(); i++) {
                try {
                    y = Double.parseDouble(locListY.get(i).getText());
                } catch ( NumberFormatException e) {
                        log.error("failed to convert positional attribute");
                }
            }
            p.setWindowLocation(strClass, new java.awt.Point((int)x, (int)y));


            List<Element> sizeWidth = windowList.get(k).getChildren("width");
            double width=0.0;
            for (int i = 0; i < sizeWidth.size(); i++) {
                try {
                    width = Double.parseDouble(sizeWidth.get(i).getText());
                } catch ( NumberFormatException e) {
                        log.error("failed to convert positional attribute");
                }
            }
            List<Element> heightList = windowList.get(k).getChildren("height");
            double height=0.0; 
            for (int i = 0; i < heightList.size(); i++) {
                try {
                    height = Double.parseDouble(heightList.get(i).getText());
                } catch ( NumberFormatException e) {
                        log.error("failed to convert positional attribute");
                }
            }
            p.setWindowSize(strClass, new java.awt.Dimension((int)width, (int)height));
            
            Element prop = windowList.get(k).getChild("properties");
            if (prop != null){
                for (Object next : prop.getChildren("property")) {
                    Element e = (Element) next;
                    
                    try {
                        Class<?> cl;
                        Constructor<?> ctor;
                        // create key object
                        cl = Class.forName(e.getChild("key").getAttributeValue("class"));
                        ctor = cl.getConstructor(new Class<?>[] {String.class});
                        Object key = ctor.newInstance(new Object[] {e.getChild("key").getText()});

                        // create value object
                        Object value = null;
                        if (e.getChild("value") != null) {
                            cl = Class.forName(e.getChild("value").getAttributeValue("class"));
                            ctor = cl.getConstructor(new Class<?>[] {String.class});
                            value = ctor.newInstance(new Object[] {e.getChild("value").getText()});
                        }
                        
                        // store
                        p.setProperty(strClass, key, value);
                    } catch (Exception ex) {
                        log.error("Error loading properties", ex);
                    }
                }
            }
        
        }
        
        List<Element> tablesList = messages.getChildren("tableDetails");
        for (Element tables : tablesList) {
            List<Element> tableList = tables.getChildren("table");
            for (Element table : tableList) {
                List<Element> columnList = table.getChildren("column");
                String strTableName = table.getAttribute("name").getValue();
                for(Element column: columnList){
                    String strColumnName = column.getAttribute("name").getValue();
                    int order = -1;
                    int width = -1;
                    int sort = 0;
                    boolean hidden = false;
                    if(column.getChild("order")!=null){
                        order = Integer.parseInt(column.getChild("order").getText());
                    }
                    if(column.getChild("width")!=null){
                        width = Integer.parseInt(column.getChild("width").getText());
                    }
                    if(column.getChild("sort")!=null){
                        sort = Integer.parseInt(column.getChild("sort").getText());
                    }
                    if(column.getChild("hidden")!=null && column.getChild("hidden").getText().equals("yes")){
                        hidden=true;
                    }
                    
                    p.setTableColumnPreferences(strTableName, strColumnName, order, width, sort, hidden);
                }
            }
        
        }
        p.finishLoading();
        return true;
    }
    
    
    static Logger log = Logger.getLogger(DefaultUserMessagePreferencesXml.class.getName());
}
    
