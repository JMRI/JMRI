package jmri.web.miniserver;


import org.jdom.Attribute;
import org.jdom.Element;

/**
 *	@author Modifications by Steve Todd   Copyright (C) 2011
 *	@version $Revision$
 */
public class MiniServerPreferences extends AbstractMiniServerPreferences{
    
    //  Flag that prefs have not been saved:
    private boolean isDirty = false;

    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean rebuildIndex = false;
    private boolean showComm = false;
    private String port = "12080";
    
    public MiniServerPreferences(String fileName){
        super.openFile(fileName);
    }
    
    public MiniServerPreferences(){}

    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute("getClickDelay")) != null ) {
        	try{
        		setClickDelay(Integer.valueOf(a.getValue()));
        	}catch (NumberFormatException e){
        		log.debug(e);
        	}
        }
        if ((a = child.getAttribute("getRefreshDelay")) != null ) {
        	try{
        		setRefreshDelay(Integer.valueOf(a.getValue()));
        	}catch (NumberFormatException e){
        		log.debug(e);
        	}
        }
        if ((a = child.getAttribute("isRebuildIndex")) != null )  setRebuildIndex(a.getValue().equalsIgnoreCase("true"));
        if ((a = child.getAttribute("isShowComm")) != null )  setShowComm(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("getPort")) != null ) setPort(a.getValue());
    }

    public boolean compareValuesDifferent(MiniServerPreferences prefs){
        if (getClickDelay() != prefs.getClickDelay()) return true;
        if (getRefreshDelay() != prefs.getRefreshDelay()) return true;
        if (isRebuildIndex() != prefs.isRebuildIndex()) return true;
        if (isShowComm() != prefs.isShowComm()) return true;
        if (!(getPort().equals(prefs.getPort()))) return true;
        return false;
    }

    public void apply(MiniServerPreferences prefs){
        setClickDelay(prefs.getClickDelay());
        setRefreshDelay(prefs.getRefreshDelay());
        setRebuildIndex(prefs.isRebuildIndex());
        setShowComm(prefs.isShowComm());
        setPort(prefs.getPort());
    }

    public Element store() {
    	Element element = new Element("MiniServerPreferences");
        element.setAttribute("getClickDelay", "" + getClickDelay());
        element.setAttribute("getRefreshDelay", "" + getRefreshDelay());
        element.setAttribute("isRebuildIndex", "" + isRebuildIndex());
        element.setAttribute("isShowComm", "" + isShowComm());
        element.setAttribute("getPort", "" + getPort());
        setIsDirty(false);  //  Resets only when stored
        return element;
    }
    
    public boolean getIsDirty(){
        return isDirty;
    }
    public void setIsDirty(boolean value){
        isDirty = value;
    }

    public int getClickDelay(){
        return clickDelay;
    }
    public void setClickDelay(int value){
        clickDelay = value;
    }
    
    public int getRefreshDelay(){
        return refreshDelay;
    }
    public void setRefreshDelay(int value){
        refreshDelay = value;
    }
    
    public boolean isRebuildIndex(){
        return rebuildIndex;
    }
    public void setRebuildIndex(boolean value){
        rebuildIndex = value;
    }

    public boolean isShowComm(){
        return showComm;
    }
    public void setShowComm(boolean value){
        showComm = value;
    }

    public String getPort(){
        return port;
    }
    public void setPort(String value){
        port = value;
    }

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServerPreferences.class.getName());

}
