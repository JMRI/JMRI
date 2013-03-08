package jmri.jmrit.beantable.beanedit;

import javax.swing.JPanel;
import javax.swing.AbstractAction;

/**
 * Hold the information for each bean panel in a structured mannor.
 */
public class EditBeanItem extends JPanel{

    public EditBeanItem(){
        super();
    }
    
    public void saveItem(){
        save.actionPerformed(null);
    }
    
    public void resetField(){
        reset.actionPerformed(null);
        
    }

    
    public void setSaveItem(AbstractAction save){
        this.save = save;
    }
    
    public void setResetItem(AbstractAction reset){
        this.reset = reset;
    }
    
    AbstractAction save;
    AbstractAction reset;
    
    String name;
    public void setName(String name){ this.name = name; }
    public String getName() { return name; }

}