package jmri.jmrit.display.layoutEditor;
/**
 * An icon to display a status of a Memory.<P>
 */

public class MemoryIcon extends jmri.jmrit.display.MemoryIcon {

    public MemoryIcon(String s, LayoutEditor panel) {
        super(s, panel);
    }

    public void setText(String text) {
        if (text==null || text.length()==0) {
            super.setText(defaultText); 
        } else {
            super.setText(text); 
        }
    }

    public void displayState() {
        setDefaultIcon(null);
        if (getMemory() == null || getMemory().getBean()==null || getMemory().getBean().getValue()==null
            || getMemory().getBean().getValue().toString().trim().length()==0) {
            if (log.isDebugEnabled()) log.debug("displayState: no value");
            setIcon(null);
            setText(defaultText);
            _text = true;
            _icon = false;
            updateSize();
        } else {
            if (log.isDebugEnabled()) log.debug("displayState: value= \""+getMemory().getBean().getValue().toString()+"\"");
            super.displayState();
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}

