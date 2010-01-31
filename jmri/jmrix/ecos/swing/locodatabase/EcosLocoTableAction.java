package jmri.jmrix.ecos.swing.locodatabase;



import java.awt.event.ActionEvent;

import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;


public class EcosLocoTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
   public EcosLocoTableAction(String s) {
        super(s);
        
    }
    public EcosLocoTableAction() { this("Ecos Loco Table");}
    
    void createModel() {
    
        m = new EcosLocoTableDataModel() {

    		public int getColumnCount(){ return NUMCOLUMN;}

            public void clickOn(jmri.jmrix.ecos.EcosLocoAddressManager m) {}
            
/*<<<<<<< EcosLocoTableAction.java
            //public EcosLocoAddressManager getManager() {return (EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);}
            public EcosLocoAddressManager getManager() {return jmri.jmrix.ecos.EcosLocoAddressManager.instance();}
            public EcosLocoAddress getByEcosObject(String object) {return getManager().getByEcosObject(object);}
            //public EcosLocoAddress getByDccAddress(int address) {return ((EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class)).getByDccAddress(address);}
            public EcosLocoAddress getByDccAddress(int address) {return getManager().getByDccAddress(address);}
=======*/
            public EcosLocoAddressManager getManager() {return jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);}

            public EcosLocoAddress getByEcosObject(String object) {return (jmri.InstanceManager.getDefault(EcosLocoAddressManager.class)).getByEcosObject(object);}
            public EcosLocoAddress getByDccAddress(int address) {return (jmri.InstanceManager.getDefault(EcosLocoAddressManager.class)).getByDccAddress(address);}
            
//>>>>>>> 1.4
            public String getValue(String s) {
                return "Set";
            }
            /*public JButton configureButton() {
                return new JButton(" Set ");
            }*/
        };
    }

    
    void setTitle() {
        f.setTitle("Ecos Loco Table");
    }
    
    String helpTarget() {
        return "package.jmri.jmrix.ecos.ecosLocoTable";
    }
    
    void savePressed(ActionEvent e){
        //System.out.println("anything");
        //jmri.jmrit.roster.Roster.writeRosterFile();
    //This function isn't available.
    }
}