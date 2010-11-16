package jmri.jmrix.ecos.swing.locodatabase;


import jmri.InstanceManager;
import jmri.Manager;
import java.awt.event.ActionEvent;
import jmri.NamedBean;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import java.util.List;


public class EcosLocoTableAction extends jmri.jmrit.beantable.AbstractTableAction {

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
    
    
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame(m, helpTarget()){
        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    
    protected void createModel() {
    
        m = new EcosLocoTableDataModel() {

    		public int getColumnCount(){ return NUMCOLUMN;}

            public void clickOn(jmri.jmrix.ecos.EcosLocoAddressManager m) {}
            
            public EcosLocoAddressManager getManager() {return jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);}

            public EcosLocoAddress getByEcosObject(String object) {return (jmri.InstanceManager.getDefault(EcosLocoAddressManager.class)).getByEcosObject(object);}
            public EcosLocoAddress getByDccAddress(int address) {return (jmri.InstanceManager.getDefault(EcosLocoAddressManager.class)).getByDccAddress(address);}
            
            public String getValue(String s) {
                return "Set";
            }
            
            public int getDisplayDeleteMsg() { return -1; }
            public void setDisplayDeleteMsg(int boo) {  }
            public void clickOn(jmri.NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
            
            /*public void setValueAt(Object value, int row, int col) {
                if (col==ROSTERIDCOL) {
                    List<RosterEntry> l;
                    if (value==null)
                        return;
                    if (value.equals(" ")){
                        l = Roster.instance().getEntriesWithAttributeKeyValue("EcosObject", ecosObjectIdList.get(row));
                        if(l.size()!=0){
                            l.get(0).deleteAttribute("EcosObject");
                            getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                            l.get(0).updateFile();
                        }
                    } else{
                        l = Roster.instance().matchingList(null, null, null, null, null, null, (String) value);
                        for (int i = 0; i < l.size(); i++) {
                            if ((l.get(i).getAttribute("EcosObject")==null)||(l.get(i).getAttribute("EcosObject").equals(""))){
                                l.get(i).putAttribute("EcosObject", ecosObjectIdList.get(row));
                                getByEcosObject(ecosObjectIdList.get(row)).setRosterId((String) value);
                            } else{
                                value=null;
                                //return;
                            }
                            l.get(i).updateFile();
                        }
                    }
                    fireTableRowsUpdated(row, row);
                    Roster.writeRosterFile();

                }  else if (col==ADDTOROSTERCOL) {
                    // button fired, delete Bean
                    addToRoster(row, col);
                }
            }*/
            
            public NamedBean getBySystemName(String name) { return null;}
            public NamedBean getByUserName(String name) { return null;}
        };
    }

    
    protected void setTitle() {
        f.setTitle("Ecos Loco Table");
    }
    
    String helpTarget() {
        return "package.jmri.jmrix.ecos.ecosLocoTable";
    }
    
    protected void addPressed(ActionEvent e){ }
}