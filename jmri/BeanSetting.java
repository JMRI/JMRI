package jmri;

/**
 * Provide simple class to maintain a Named Bean (e.g. Turnout) and specific setting for it.
 *
 *<p>
 * Objects of this class are immutable.
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
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
    
    private NamedBean _bean;
    private int _setting;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BeanSetting.class.getName());
}
