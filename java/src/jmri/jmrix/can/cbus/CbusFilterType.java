package jmri.jmrix.can.cbus;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;

/**
 * ENUM to represent various CBUS OPC Filters.
 * The filters are used within the CBUS Console filtering ecosystem.
 * Many OPCs are allocated to multiple filters by CbusOpcData.xml.
 * Some filters only make sense when allocated by CBUS Console ( eg. CFIN / CFOUT )
 * @author Steve Young (C) 2020
 */
public enum CbusFilterType {
    /**
     * Incoming CAN Frame (category head).
     */
    CFIN(Bundle.getMessage("Incoming"),null) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if (m instanceof jmri.jmrix.can.CanReply) {
                return super.action(m,cf);
            }
            return -1;
        } },
    /**
     * Outgoing CAN Frame (category head).
     */
    CFOUT(Bundle.getMessage("Outgoing"),null) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if (m instanceof jmri.jmrix.can.CanMessage) {
                return super.action(m, cf);
            }
            return -1;
        } },
    /**
     * CBUS Event (category head).
     */
    CFEVENT(Bundle.getMessage("CbusEvents"),null),
    /**
     * A minimum CBUS Event number.
     */
    CFEVENTMIN(Bundle.getMessage("MinEvent"),CFEVENT) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getEvent(m) < cf.getEvMin()) {
                return super.action(m,cf);
            }
            return -1;
        }
    },
    /**
     * A maximum CBUS Event number.
     */
    CFEVENTMAX(Bundle.getMessage("MaxEvent"),CFEVENT) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getEvent(m) > cf.getEvMax()){
                return super.action(m,cf);
            }
            return -1;
        } },
    /**
     * CBUS Event On.
     */
    CFON(Bundle.getMessage("CbusOnEvents"),CFEVENT),
    /**
     * CBUS Event Off.
     */
    CFOF(Bundle.getMessage("CbusOffEvents"),CFEVENT),
    /**
     * CBUS Short Event.
     */
    CFSHORT(Bundle.getMessage("ShortEvents"),CFEVENT),
    /**
     * CBUS Long Event.
     */
    CFLONG(Bundle.getMessage("LongEvents"),CFEVENT),
    /**
     * CBUS Standard Event ( i.e. not a request or response event but may have added data, e.g. ASOF2 ).
     */
    CFSTD(Bundle.getMessage("StandardEvents"),CFEVENT),
    /**
     * CBUS Request Event.
     */
    CFREQUEST(Bundle.getMessage("RequestEvents"),CFEVENT),
    /**
     * CBUS Response Event.
     */
    CFRESPONSE(Bundle.getMessage("ResponseEvents"),CFEVENT),
    /**
     * CBUS Event with 0 bytes added data ( eg. standard ACON / ASOF ).
     */
    CFED0(Bundle.getMessage("EVD0"),CFEVENT),
    /**
     * CBUS Event with 1 byte added data ( eg. ACON1 / ASOF1 ).
     */
    CFED1(Bundle.getMessage("EVD1"),CFEVENT),
    /**
     * CBUS Event with 2 bytes added data ( eg. ACON2 / ASOF2 ).
     */
    CFED2(Bundle.getMessage("EVD2"),CFEVENT),
    /**
     * CBUS Event with 3 bytes added data ( eg. ACON3 / ASOF3 ).
     */
    CFED3(Bundle.getMessage("EVD3"),CFEVENT),

    /**
     * CBUS OPC with Data (category head).
     */
    CFDATA(Bundle.getMessage("OPC_DA"),null),
    /**
     * CBUS OPC with Data.
     */
    CFACDAT("ACDAT",CFDATA),

    /**
     * CBUS DDES and DDWS data categories.
     * Device Data Event Short or Device Data Write Short.
     */
    CFDDES("DDES + DDWS",CFDATA),

    /**
     * Data.
     */
    CFRQDAT("RQDAT",CFDATA),

    /**
     * Accessory (event) request for data.
     */
    CFARDAT("ARDAT",CFDATA),
    /**
     * Device Data Response Short Event.
     */
    CFDDRS("DDRS",CFDATA),
    /**
     * Request device data – Short Event.
     */
    CFRQDDS("RQDDS",CFDATA),
    /**
     * Cab data.
     */
    CFCABDAT("CABDAT",CFDATA),

    /**
     * Command Station OPCs. (category head).
     */
    CFCS(Bundle.getMessage("CommandStation"),null),
    /**
     *
     */
    CFCSAQRL(Bundle.getMessage("LocoCommands"),CFCS),
    /**
     * Throttle Keep-Alive frames.
     */
    CFCSKA(Bundle.getMessage("KeepAlive"),CFCS),
    /**
     * Throttle Speed / Direction frames.
     */
    CFCSDSPD(Bundle.getMessage("SpeedDirection"),CFCS),
    /**
     * Throttle Functions.
     */
    CFCSFUNC(Bundle.getMessage("Functions"),CFCS),
    /**
     * Thorttle / DCC Programming.
     */
    CFCSPROG(Bundle.getMessage("Programming"),CFCS),
    /**
     * Layout Commands.
     */
    CFCSLC(Bundle.getMessage("LayoutCommands"),CFCS),
    /**
     * Command Station Controls.
     */
    CFCSC(Bundle.getMessage("CommandStationControl"),CFCS),

    /**
     * Node Configuration (category head).
     */
    CFNDCONFIG(Bundle.getMessage("NodeConfiguration"),null),
    /**
     * General node Setup commands.
     */
    CFNDSETUP(Bundle.getMessage("GeneralNodeSetup"),CFNDCONFIG),
    /**
     * Node Variable setup commands.
     */
    CFNDVAR(Bundle.getMessage("NodeVariables"),CFNDCONFIG),
    /**
     * Node Event setup commands.
     */
    CFNDEV(Bundle.getMessage("NodeEvents"),CFNDCONFIG),
    /**
     * Node Number setup commands.
     */
    CFNDNUM(Bundle.getMessage("NodeNumbers"),CFNDCONFIG),

    /**
     * VLCB OPCs.
     */
    CFVLCB(Bundle.getMessage("VLCB"),null),
    CFSVC(Bundle.getMessage("Services"),CFVLCB),
    CFDIAG(Bundle.getMessage("Diagnostics"),CFVLCB),
    CFLONGMSG(Bundle.getMessage("LongMessages"),CFVLCB),
    CFHEARTB(Bundle.getMessage("HeartBeat"),CFVLCB),
    CFENACK(Bundle.getMessage("EventAck"),CFEVENT),

    /**
     * Miscellaneous (category head)
     */
    CFMISC(Bundle.getMessage("Misc"),null),
    /**
     * Extended or RTR CanFrames.
     */
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
    /**
     * Network Commands.
     */
    CFNETWK(Bundle.getMessage("NetworkCommands"),CFMISC),
    /**
     * Fast Clock Commands.
     */
    CFCLOCK(Bundle.getMessage("CBUS_FCLK"),CFMISC),
    /**
     * Other OPCs which are uncategorised.
     */
    CFOTHER(Bundle.getMessage("Others"),CFMISC),
    /**
     * Unknown OPCs.
     */
    CFUNKNOWN(Bundle.getMessage("Unknown"),CFMISC),

    /**
     * Nodes (category head)
     */
    CFNODE(Bundle.getMessage("CbusNodes"),null) {
        @Override
        public String getToolTip(){
            return null;
        }

    },
    /**
     * Minimum Node number.
     */
    CFNODEMIN(Bundle.getMessage("MinNode"),CFNODE) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getNodeNumber(m) < cf.getNdMin()){
                return super.action(m, cf);
            }
            return -1;
        }
    },
    /**
     * Maximum Node number.
     */
    CFNODEMAX(Bundle.getMessage("MaxNode"),CFNODE) {
        @Override
        public int action(AbstractMessage m, CbusFilter cf) {
            if ( CbusMessage.getNodeNumber(m) > cf.getNdMax()){
                return super.action(m, cf);
            }
            return -1;
        }

    };

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
    public static final Set<CbusFilterType> getCatHeads() {
        EnumSet<CbusFilterType> catSet = EnumSet.noneOf(CbusFilterType.class);
        catSet.add(CFEVENT);
        catSet.add(CFNODE);
        catSet.add(CFDATA);
        catSet.add(CFCS);
        catSet.add(CFNDCONFIG);
        catSet.add(CFVLCB);
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
        Set<CbusFilterType> alwaysDisplay = getCatHeads();
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
    public static final Set<CbusFilterType> allFilters(int opc) {
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
