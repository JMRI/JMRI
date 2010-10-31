package jmri;

/**
 * Represent a Named Bean (e.g.&nbsp;Turnout) 
 * and specific setting for it.
 * These can be used e.g. to represent part of a particular
 * path through a layout, or a condition that has to be 
 * true as part of something.
 *<p>
 * Objects of this class are immutable, in that once created
 * the selected bean and required setting cannot be changed.
 * However, the value of the <code><a href="#check()">check</a></code> method does
 * change, because it's a function of the current bean setting(s).
 *
 * @author	Bob Jacobsen  Copyright (C) 2006, 2008
 * @version	$Revision: 1.6 $
 */
@net.jcip.annotations.Immutable
public class BeanSetting  {

    public BeanSetting(jmri.NamedBean t, int setting) {
        _bean = t;
        _setting = setting;
    }
    
    /**
     * Convenience method; check if the Bean currently has the desired setting
     */
    public boolean check(){
        return _bean.getState() == _setting;
    }
    
    public NamedBean getBean() { return _bean; }
    public int getSetting() { return _setting; }
    public void setSetting(int setting) { _setting = setting; }
    
    private NamedBean _bean;
    private int _setting;
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BeanSetting.class.getName());
}
