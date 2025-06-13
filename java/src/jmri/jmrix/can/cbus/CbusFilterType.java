package jmri.jmrix.can.cbus;

import java.util.EnumSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;

/**
 * ENUM to represent various CBUS OPC Filters.
 * 
 * @author Steve Young (C) 2020
 */
public enum CbusFilterType {
    CFIN(Bundle.getMessage("Incoming"),null) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if (m instanceof jmri.jmrix.can.CanReply) {
                return super.action(m,cf);
            }
            return -1;
        } },
    CFOUT(Bundle.getMessage("Outgoing"),null) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if (m instanceof jmri.jmrix.can.CanMessage) {
                return super.action(m, cf);
            }
            return -1;
        } },
    CFEVENT(Bundle.getMessage("CbusEvents"),null),
    CFEVENTMIN(Bundle.getMessage("MinEvent"),CFEVENT) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getEvent(m) < cf.getEvMin()) {
                return super.action(m,cf);
            }
            return -1;
        }
    },
    CFEVENTMAX(Bundle.getMessage("MaxEvent"),CFEVENT) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getEvent(m) > cf.getEvMax()){
                return super.action(m,cf);
            }
            return -1;
        } },
    CFON(Bundle.getMessage("CbusOnEvents"),CFEVENT),
    CFOF(Bundle.getMessage("CbusOffEvents"),CFEVENT),
    CFSHORT(Bundle.getMessage("ShortEvents"),CFEVENT),
    CFLONG(Bundle.getMessage("LongEvents"),CFEVENT),
    CFSTD(Bundle.getMessage("StandardEvents"),CFEVENT),
    CFREQUEST(Bundle.getMessage("RequestEvents"),CFEVENT),
    CFRESPONSE(Bundle.getMessage("ResponseEvents"),CFEVENT),
    CFED0(Bundle.getMessage("EVD0"),CFEVENT),
    CFED1(Bundle.getMessage("EVD1"),CFEVENT),
    CFED2(Bundle.getMessage("EVD2"),CFEVENT),
    CFED3(Bundle.getMessage("EVD3"),CFEVENT),

    CFDATA(Bundle.getMessage("OPC_DA"),null),
    CFACDAT("ACDAT",CFDATA),
    CFDDES("DDES + DDWS",CFDATA),
    CFRQDAT("RQDAT",CFDATA),
    CFARDAT("ARDAT",CFDATA),
    CFDDRS("DDRS",CFDATA),
    CFRQDDS("RQDDS",CFDATA),
    CFCABDAT("CABDAT",CFDATA),

    CFCS(Bundle.getMessage("CommandStation"),null),
    CFCSAQRL(Bundle.getMessage("LocoCommands"),CFCS),
    CFCSKA(Bundle.getMessage("KeepAlive"),CFCS),
    CFCSDSPD(Bundle.getMessage("SpeedDirection"),CFCS),
    CFCSFUNC(Bundle.getMessage("Functions"),CFCS),
    CFCSPROG(Bundle.getMessage("Programming"),CFCS),
    CFCSLC(Bundle.getMessage("LayoutCommands"),CFCS),
    CFCSC(Bundle.getMessage("CommandStationControl"),CFCS),

    CFNDCONFIG(Bundle.getMessage("NodeConfiguration"),null),
    CFNDSETUP(Bundle.getMessage("GeneralNodeSetup"),CFNDCONFIG),
    CFNDVAR(Bundle.getMessage("NodeVariables"),CFNDCONFIG),
    CFNDEV(Bundle.getMessage("NodeEvents"),CFNDCONFIG),
    CFNDNUM(Bundle.getMessage("NodeNumbers"),CFNDCONFIG),

    CFMISC(Bundle.getMessage("Misc"),null),
    CFEXTRTR("Extended / RTR",CFMISC){
        @Override 
        public int action(AbstractMessage m, CbusFilter cf) {
        if (m instanceof CanFrame && ((CanFrame) m).extendedOrRtr() ) {
            if ( cf.isFilterActive(ordinal()) ){
                return ordinal();
            } else {
                return -2; // special return as unable to contiinue filtering if extended or rtr
            }
        }
        return -1;
        } },
    CFNETWK(Bundle.getMessage("NetworkCommands"),CFMISC),
    CFCLOCK(Bundle.getMessage("CBUS_FCLK"),CFMISC),
    CFOTHER(Bundle.getMessage("Others"),CFMISC),
    CFUNKNOWN(Bundle.getMessage("Unknown"),CFMISC),

    CFNODE(Bundle.getMessage("CbusNodes"),null) {
        @Override
        public String getToolTip(){
            return null;
        }

    },
    CFNODEMIN(Bundle.getMessage("MinNode"),CFNODE) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getNodeNumber(m) < cf.getNdMin()){
                return super.action(m, cf);
            }
            return -1;
        }
    },
    CFNODEMAX(Bundle.getMessage("MaxNode"),CFNODE) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getNodeNumber(m) > cf.getNdMax()){
                return super.action(m, cf);
            }
            return -1;
        } };

    /**
     * Perform Filter check for a particular message.
     * Can be overridden by specific filters.
     * 
     * @param m CanMessage or CanReply
     * @param cf main CbusFilter instance
     * @return Filter category which blocked, else -1 or -2 if passed 
     */
    public int action(AbstractMessage m, @Nonnull CbusFilter cf){
        if ( cf.isFilterActive(ordinal()) ){
            return ordinal();
        } else {
            return -1;
        }
    }

    private final String _bundleString;
    private final CbusFilterType _category;

    /**
     * Create new CbusFilterType.
     */
    CbusFilterType(String bundle, CbusFilterType category) {
        this._bundleString=bundle;
        this._category = category;
    }

    /**
     * Get Filter Name
     * @return Filter Name
     */
    public final String getName(){
        return _bundleString;
    }

    /**
     * Get Filter Category
     * @return Filter Category, else null if Category Head
     */
    @CheckForNull
    public final CbusFilterType getCategory() {
        return _category;
    }

    /**
     * Get an EnumSet of Category Heads
     * @return set
     */
    public static final java.util.Set<CbusFilterType> getCatHeads() {
        EnumSet<CbusFilterType> catSet = EnumSet.noneOf(CbusFilterType.class);
        catSet.add(CFEVENT);
        catSet.add(CFNODE);
        catSet.add(CFDATA);
        catSet.add(CFCS);
        catSet.add(CFNDCONFIG);
        catSet.add(CFMISC);
        return catSet;
    }

    /**
     * Is the Filter a parent of a category?
     * @return true if category parent
     */
    public final boolean isCategoryHead() {
        return getCatHeads().contains(this);
    }

    /**
     * Should the Filter always be displayed?
     * @return true if category head or in / out filter.
     */
    public final boolean alwaysDisplay() {
        java.util.Set<CbusFilterType> alwaysDisplay = getCatHeads();
        alwaysDisplay.add(CFIN);
        alwaysDisplay.add(CFOUT);
        return alwaysDisplay.contains(this);
    }

    /**
     * Get if the Filter needs to display a number spinner
     * @return true to display a spinner
     */
    public final boolean showSpinners() {
        EnumSet<CbusFilterType> spinnerSet = EnumSet.noneOf(CbusFilterType.class);
        spinnerSet.addAll(EnumSet.of(CFEVENTMIN,CFEVENTMAX,CFNODEMIN,CFNODEMAX));
        return spinnerSet.contains(this);
    }

    /**
     * Get All Filters for a particular OPC
     * @param opc OPC to get Filter List for
     * @return set of Filters to use for the OPC.
     */
    @Nonnull
    public static final EnumSet<CbusFilterType> allFilters(int opc) {
        EnumSet<CbusFilterType> mergedSet = EnumSet.noneOf(CbusFilterType.class);
        mergedSet.addAll(EnumSet.of(CFIN,CFOUT,CFEXTRTR));
        mergedSet.addAll(CbusOpCodes.getOpcFilters(opc));
        if (mergedSet.contains(CbusFilterType.CFEVENT)){
            mergedSet.addAll(EnumSet.of(CbusFilterType.CFEVENTMIN,CbusFilterType.CFEVENTMAX));
        }
        if (mergedSet.contains(CbusFilterType.CFNODE)){
           mergedSet.addAll(EnumSet.of(CbusFilterType.CFNODEMIN,CbusFilterType.CFNODEMAX));
        }
        return mergedSet;
    }

    /**
     * Get ToolTip Text for the Filter
     * @return HMTL list of OPCs with description, may be null if no ToolTip
     */
    @CheckForNull
    public String getToolTip(){
        StringBuilder t = new StringBuilder();
        for ( int i=0 ; (i < 257) ; i++) {
            if (CbusOpCodes.getOpcFilters(i).contains(this) 
            && !CbusOpCodes.getOpcName(i).isEmpty()){
                t.append(CbusOpCodes.getOpcName(i))
                .append(" : ")
                .append(Bundle.getMessage("CBUS_" + CbusOpCodes.getOpcName(i)))
                .append(" : ")
                .append(Bundle.getMessage("CTIP_" + CbusOpCodes.getOpcName(i)))
                .append("<br>");
            }
        }
        if (!t.toString().isEmpty()){
            t.insert(0,"<html>");
            t.append("</html>");
            return t.toString();
        }
        return null;
    }

    /**
     * Get Filter Type by name.
     * @param name the #getName string to search for.
     * @return Filter Type, or null if not found.
     */
    @CheckForNull
    public static CbusFilterType getFilterByName(String name) {
        for ( CbusFilterType type : CbusFilterType.values() ) {
            if ( type.getName().equals(name) ) {
                return type;
            }
        }
        return null;
    }

}
