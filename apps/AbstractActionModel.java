// AbstractActionModel.java

package apps;

/**
 * Provide services for invoking actions during configuration
 * and startup.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 * @see PerformActionPanel
 */
public abstract class AbstractActionModel {

    public AbstractActionModel() {
        className="";
    }

    String className;

    public String getClassName() {
        return className;
    }

    public String getName() {
        for (int i =0; i< nameList().length; i++)
            if (classList()[i].getName().equals(className))
                return nameList()[i];
        return null;
    }

    public void setName(String n) {
        for (int i =0; i< nameList().length; i++)
            if (nameList()[i].equals(n))
                className = classList()[i].getName();
    }

    public void setClassName(String n) {
        className = n;
    }

    static public String[] nameList() {
        return new String[] {
            "Open memory monitor",
            "Start LocoNet Server",
            "Open turnout table",
            "Open sensor table",
            "Open signal table",
            "Open signal logic panel",
            "Open power control",
            "Open turnout control",
            "Open single CV programmer",
            "Open speedometer",
            "New panel",
            "Load panel",
            "Open C/MRI monitor",
            "Open EasyDCC monitor",
            "Open XPressNet monitor",
            "Open LocoNet monitor",
            "Open LocoNet slot monitor",
            "Open NCE monitor",
            "Open Zimo monitor"
        };
    }

    static public Class[] classList() {
        return new Class[] {
            jmri.jmrit.MemoryFrameAction.class,
            jmri.jmrix.loconet.locormi.LnMessageServerAction.class,
            jmri.jmrit.beantable.TurnoutTableAction.class,
            jmri.jmrit.beantable.SensorTableAction.class,
            jmri.jmrit.beantable.SignalHeadTableAction.class,
            jmri.jmrit.blockboss.BlockBossAction.class,
            jmri.jmrit.powerpanel.PowerPanelAction.class,
            jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction.class,
            jmri.jmrit.simpleprog.SimpleProgAction.class,
            jmri.jmrit.speedometer.SpeedometerAction.class,
            jmri.jmrit.display.PanelEditorAction.class,
            jmri.configurexml.LoadXmlConfigAction.class,
            jmri.jmrix.cmri.serial.serialmon.SerialMonAction.class,
            jmri.jmrix.easydcc.easydccmon.EasyDccMonAction.class,
            jmri.jmrix.lenz.mon.XNetMonAction.class,
            jmri.jmrix.loconet.locomon.LocoMonAction.class,
            jmri.jmrix.loconet.slotmon.SlotMonAction.class,
            jmri.jmrix.nce.ncemon.NceMonAction.class,
            jmri.jmrix.zimo.zimomon.Mx1MonAction.class
        };
    }
}


