package jmri.jmrit.display.layoutEditor;

import jmri.jmrit.display.Editor;
import java.awt.event.MouseEvent;
/**
 * An icon to display a status of a Memory.<P>
 */

public class MemoryIcon extends jmri.jmrit.display.MemoryIcon {

    String defaultText = "  ";

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
        super.displayState();
        if (getMemory() != null && getMemory().getBean()!=null && getMemory().getBean().getValue()==null) {
            setIcon(null);
            setText(defaultText);
            _text = true;
            _icon = false;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}

