package jmri.jmrix.loconet.alm;

import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.BoxLayout;

import jmri.jmrix.loconet.swing.LnPanel;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.ds64.SimpleTurnoutStateEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all the route of a device.
 *
 * @author B. Milhaupt (C) 2024
 */
public class LnSimple7thGenDeviceRoutes extends LnPanel {
    private JComponent[] routePanel;
    private LnSimple7thGenRoute[] route; // has route info plus swing stuff!
    private final int deviceNumber;
    private final int serNum;
    private int baseAddr;
    private int howManyRoutes;
    private final String[] entryName;

    private int opsw1to7;

    private JTabbedPane routesTabbedPane;

    public LnSimple7thGenDeviceRoutes(int deviceType, int serNum) {
        this.deviceNumber = deviceType;
        this.serNum = serNum;
        howManyRoutes = 8;
        if (this.deviceNumber == LnConstants.RE_IPL_DIGITRAX_HOST_DS78V) {
            this.howManyRoutes = 16;
        }
        route = new LnSimple7thGenRoute[howManyRoutes];
        for (int i = 0;
                i < howManyRoutes; ++ i) {
            route[i] = new LnSimple7thGenRoute();
        }
        this.entryName = new String[]{"Top", "2", "3", "4", "5", "6", "7", "8"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        routesTabbedPane = new JTabbedPane();
        routePanel = new JComponent[howManyRoutes];
        route = new LnSimple7thGenRoute[howManyRoutes];
        for (int i = 0; i < howManyRoutes; ++i) {
            route[i] = new LnSimple7thGenRoute();
            routePanel[i] = makeTextPanel(i, 8);
            routesTabbedPane.addTab("Route "+Integer.toString(i+1), null,
                    routePanel[i] , "");
            routesTabbedPane.setMnemonicAt(i, KeyEvent.VK_1 +i);
        }
        routePanel[howManyRoutes-1].setPreferredSize(new Dimension(460, 50));

        add(routesTabbedPane);

        //The following line enables to use scrolling tabs.
        routesTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        routesTabbedPane.addChangeListener((ChangeEvent e) -> {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                int routeNum = pane.getSelectedIndex() + 1;
                // get the route guiEntry
                memo.getLnTrafficController().sendLocoNetMessage(new LocoNetMessage(new int[] {
                    LnConstants.OPC_IMM_PACKET_2, 0x10, 0x02, 0x02,
                    (routeNum - 1) * 2, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0}));
            }
        });
        routesTabbedPane.repaint();

        // Select device X s/n N for route accesss.
        memo.getLnTrafficController().sendLocoNetMessage(new LocoNetMessage(new int[] {
            LnConstants.OPC_IMM_PACKET_2, 0x10, 0x02, 0x0e,
            0, 0, 0, 0,
            0, deviceNumber, this.opsw1to7, serNum & 0x7f,
            (serNum >> 7) & 0x7F, (this.baseAddr-1) & 0x7f,
            ((this.baseAddr-1) >> 7) & 0x7f,
            0}));
    }

    protected JComponent makeTextPanel(int i, int numEntries) {
        JPanel panel = new JPanel(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JPanel jp;
        for (int j = 0; j < 8; ++j) {
            jp = new JPanel();
            jp.setLayout(new FlowLayout());
            SimpleTurnoutStateEntry stse = route[i].getRouteEntry(j).guiEntry;
            jp.add(stse.createEntryPanel(entryName[j]));

            jp.add(stse.getAddressField());
            jp.add(stse.closedRadioButton);
            jp.add(stse.thrownRadioButton);
            jp.add(stse.unusedRadioButton);
            ButtonGroup bg = new ButtonGroup();
            bg.add(stse.closedRadioButton);
            bg.add(stse.thrownRadioButton);
            bg.add(stse.unusedRadioButton);

            stse.unusedRadioButton.setSelected(true);
            panel.add(jp);
        }

        return panel;
    }

    /**
     * Get the device turnoutNumber (IPL device turnoutNumber) from the device name.
     * @param name Device name
     * @return device turnoutNumber
     */
    public static int getDeviceType(String name) {
        String s = name.toUpperCase();
        switch (s) {
            case "DS74":
                return LnConstants.RE_IPL_DIGITRAX_HOST_DS74;
            case "DS78V":
                return LnConstants.RE_IPL_DIGITRAX_HOST_DS78V;
            case "PM74":
                return LnConstants.RE_IPL_DIGITRAX_HOST_PM74;
            case "SE74":
                return LnConstants.RE_IPL_DIGITRAX_HOST_SE74;
            default:
                return -1;
        }
    }

    /**
     * Get the device name from the device (IPL) nuber.
     * @param typeNum device type number
     * @return String containing the device name
     */
    public static String getDeviceName(int typeNum) {
        switch (typeNum) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
                return "DS74";
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                return "DS78V";
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                return "PM74";
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return "SE74";
            default:
                return null;
        }
    }
    /**
     * Getter.
     * @return device type turnoutNumber
     */
    public int getDeviceType() {
        return deviceNumber;
    }

    /**
     * Getter.
     * @return  device serial turnoutNumber
     */
    public int getSerNum() {
        return serNum;
    }

/**
 * Get the routes.
 * @return cloned LnSimple7thGenRoute[], if any.
 */
    public LnSimple7thGenRoute[] getRoutes() {
        return route.clone();
    }

    /**
     * get a specific route.
     *
     * @param routeNumber route number
     * @return LnSimple7thGenRoute
     */
    public LnSimple7thGenRoute getRoutes(int routeNumber) {
        return route[routeNumber];
    }

    /**
     * Set the routes.
     *
     * @param newRoutes an array of routes
     */
    public void setRoutes(LnSimple7thGenRoute[] newRoutes) {
        route = newRoutes.clone();
    }

    /**
     * Set one route entry.
     *
     * @param routeNum route number
     * @param entryNum entry number
     * @param turn Turnout number
     * @param posn Position
     */
    public void setOneEntry(int routeNum, int entryNum, int turn,
            RouteSwitchPositionEnum posn) {
        LnSimpleRouteEntry entry = new LnSimpleRouteEntry();
        entry.setNumber(turn);
        entry.setPosition(posn);
        route[routeNum].setRouteEntry(entryNum, entry);
    }

    /**
     * Set four entries for a route
     * @param routeNum Route number
     * @param entrySet Entry set
     * @param entrya Entry a of the set
     * @param entryb Entry b of the set
     * @param entryc Entry c of the set
     * @param entryd Entry d of the set
     * @return a route
     */
    public LnSimple7thGenRoute setFourEntries(int routeNum, int entrySet,
            int entrya, int entryb, int entryc, int entryd) {
        int entry = (entrySet == 1) ? 4 : 0;

        int entryaTurn = (entrya == 0x3fff) ? 0 : (entrya & 0x7ff);
        RouteSwitchPositionEnum entrya_posn = RouteSwitchPositionEnum.UNUSED ;
        if (entrya != 0x3fff) {
            entrya_posn = (((entrya & 0x3800) == 0x1800) ?
                RouteSwitchPositionEnum.CLOSED : RouteSwitchPositionEnum.THROWN);
        }
        setOneEntry(routeNum, entry, entryaTurn, entrya_posn);

        int entrybTurn = (entryb == 0x3fff) ? 0 : (entryb & 0x7ff);
        RouteSwitchPositionEnum entryb_posn =RouteSwitchPositionEnum.UNUSED ;
        if (entryb != 0x3fff) {
            entryb_posn = (((entryb & 0x3800) == 0x1800) ?
                RouteSwitchPositionEnum.CLOSED : RouteSwitchPositionEnum.THROWN);
        }
        setOneEntry(routeNum, entry+1, entrybTurn, entryb_posn);

        int entrycTurn = (entryc == 0x3fff) ? 0 : (entryc & 0x7ff);
        RouteSwitchPositionEnum entryc_posn =RouteSwitchPositionEnum.UNUSED ;
        if (entryc != 0x3fff) {
            entryc_posn = (((entryc & 0x3800) == 0x1800) ?
                RouteSwitchPositionEnum.CLOSED : RouteSwitchPositionEnum.THROWN);
        }
        setOneEntry(routeNum, entry+2, entrycTurn, entryc_posn);

        int entrydTurn = (entryd == 0x3fff) ? 0 : (entryd & 0x7ff);
        RouteSwitchPositionEnum entryd_posn =RouteSwitchPositionEnum.UNUSED ;
        if (entryd != 0x3fff) {
            entryd_posn = (((entryd & 0x3800) == 0x1800) ?
                RouteSwitchPositionEnum.CLOSED : RouteSwitchPositionEnum.THROWN);
        }
        setOneEntry(routeNum, entry+3, entrydTurn, entryd_posn);

        log.debug("setFourEntries: done: route {} entry {} thru {}, {}, {}, {}, {}",
                routeNum, entry, entry + 3,
                getOneEntryString(routeNum, entry), getOneEntryString(routeNum, entry+1),
                getOneEntryString(routeNum, entry+2), getOneEntryString(routeNum, entry+3));

        return getRoute(routeNum);
    }

    /**
     * get a route entry as a string.
     * @param routeNum Route number
     * @param entryNum Entry number
     * @return String like "Unused" or "1c" or "2044t"
     */
    public String getOneEntryString(int routeNum, int entryNum) {
        RouteSwitchPositionEnum r = route[routeNum].getRouteEntry(entryNum).getPosition();
        if (r.equals(RouteSwitchPositionEnum.UNUSED)) {
            return "Unused";
        }
        return Integer.toString(route[routeNum].getRouteEntry(entryNum).getNumber()) + r.toString();
    }

    /**
     * Get a route.
     * @param routeNum Route number
     * @return LnSimple7thGenRoute, or null if route not defined
     */
    public LnSimple7thGenRoute getRoute(int routeNum) {
        if ((routeNum >=0) && (routeNum < route.length)) {
            return route[routeNum];
        } else {
            log.warn("getRoutes({}): not configured.", routeNum);
            return null;
        }
    }

    /**
     * Getter.
     * @return Base Addr
     */
    public int getBaseAddr() {
        return baseAddr;
    }

    /**
     * Setter.
     * @param baseAddr the base address
     */
    public void setBaseAddr(int baseAddr) {
        this.baseAddr = baseAddr;
    }

    private final static Logger log = LoggerFactory.getLogger(LnSimple7thGenDeviceRoutes.class);
}
