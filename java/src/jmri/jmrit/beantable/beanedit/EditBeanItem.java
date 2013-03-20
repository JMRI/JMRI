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
        if(save!=null)
            save.actionPerformed(null);
    }
    
    public void resetField(){
        if(reset!=null)
            reset.actionPerformed(null);
        
    }

    /**
     *  Set the action to be performed when the save button is pressed
     */
    public void setSaveItem(AbstractAction save){
        this.save = save;
    }
    
    /**
     *  Set the action to be performed when the cancel button is pressed
     */
    public void setResetItem(AbstractAction reset){
        this.reset = reset;
    }
    
    AbstractAction save;
    AbstractAction reset;
    
    String name;
    public void setName(String name){ this.name = name; }
    public String getName() { return name; }

}