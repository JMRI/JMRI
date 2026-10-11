"""
JMRI Jython script: WiThrottle Monitor Table

Monitors connected WiThrottle devices and locomotives (address, speed).
Run from JMRI: Scripting → Jython → Run Script (or include as a startup item).

Requires the WiThrottle server to be running (open WiThrottle from the menu first,
or this script will try to start it).
Author: steambigboy with help of AI, copyright 2026
Part of JMRI distribution
"""

import jmri
from jmri.util import JmriJFrame
from javax.swing import JFrame, JTable, JScrollPane, JPanel, JLabel, JButton, JTabbedPane
from javax.swing import SwingUtilities
from javax.swing.table import AbstractTableModel
from java.awt import BorderLayout, Dimension, FlowLayout, Color
from java.awt.event import ActionListener, ActionEvent
from javax.swing.table import DefaultTableCellRenderer
from java.lang import Object
from java.lang.reflect import Field


def _get_field(obj, field_name):
    """Get a (possibly private) field from a Java object via reflection."""
    try:
        f = obj.getClass().getDeclaredField(field_name)
        f.setAccessible(True)
        return f.get(obj)
    except:
        return None


def _collect_loco_rows(device_list):
    """Collect (device_name, slot, address, roster_id, speed, direction) for each active loco."""
    rows = []
    if device_list is None:
        return rows
    for i in range(device_list.size()):
        device = device_list.get(i)
        dev_name = device.getName() or "?"
        if dev_name == "?" and hasattr(device, "getUDID"):
            udid = device.getUDID()
            if udid:
                dev_name = udid[:12] + "..." if len(udid) > 12 else udid

        # Multi-throttle (modern apps)
        multi_throttles = _get_field(device, "multiThrottles")
        if multi_throttles is not None and not multi_throttles.isEmpty():
            for entry in multi_throttles.entrySet():
                slot_char = str(entry.getKey())
                mt = entry.getValue()
                throttles_map = _get_field(mt, "throttles")
                if throttles_map is not None:
                    for e2 in throttles_map.entrySet():
                        mtc = e2.getValue()
                        addr, roster, speed, fwd = _loco_info_from_controller(mtc)
                        if addr or roster:
                            rows.append((dev_name, slot_char, addr, roster, speed, fwd))

        # Legacy single/double throttle (only when device has no multi-throttles)
        if multi_throttles is None or multi_throttles.isEmpty():
            for tc_field in ("throttleController", "secondThrottleController"):
                tc = _get_field(device, tc_field)
                if tc is not None:
                    addr, roster, speed, fwd = _loco_info_from_controller(tc)
                    if addr or roster:
                        rows.append((dev_name, tc_field[0].upper(), addr, roster, speed, fwd))
    return rows


def _loco_info_from_controller(tc):
    """Get (address, roster_id, speed_step_str, direction_str) from ThrottleController."""
    addr, roster, speed_str, fwd_str = "", "", "", ""
    try:
        addr = tc.getCurrentAddressString() or ""
        roster = tc.getCurrentRosterIdString() or ""
        t = tc.getThrottle()
        if t is not None:
            sp = t.getSpeedSetting()
            steps = 0
            try:
                steps = t.getSpeedSteps()
            except:
                steps = 0
            if steps is None or steps <= 0:
                steps = 126  # sensible default when unknown
            if sp is not None:
                speed_step = int(round(sp * steps))
                speed_str = "%d" % speed_step
            else:
                speed_str = ""
            fwd_str = "Fwd" if t.getIsForward() else "Rev"
    except:
        pass
    return (addr, roster, speed_str, fwd_str)


def get_wi_throttle_frame():
    """Get the existing WiThrottle UserInterface frame, or create it."""
    try:
        from jmri.jmrit.withrottle import UserInterface, WiThrottleCreationAction
        from java.awt.event import ActionEvent as AEv

        fl = JmriJFrame.getFrameList()
        for i in range(fl.size()):
            f = fl.get(i)
            if isinstance(f, UserInterface):
                return f

        # No window yet: create WiThrottle server/window
        action = WiThrottleCreationAction()
        action.actionPerformed(AEv(action, 1001, "open"))  # 1001 = ACTION_PERFORMED
        # Try again after a short delay so the frame is registered
        fl = JmriJFrame.getFrameList()
        for i in range(fl.size()):
            f = fl.get(i)
            if isinstance(f, UserInterface):
                return f
    except Exception as e:
        print "WiThrottleMonitorTable: Error getting WiThrottle frame: ", e
    return None


def create_monitor_frame():
    """Build and show the WiThrottle monitor table window."""
    wi_frame = get_wi_throttle_frame()
    if wi_frame is None:
        print "WiThrottleMonitorTable: Could not get WiThrottle window. Open WiThrottle from the menu first."
        return

    try:
        list_model = wi_frame.getThrottleList()
    except Exception as e:
        print "WiThrottleMonitorTable: Could not get throttle list: ", e
        return

    device_list = _get_field(list_model, "deviceList")

    class LocoTableModel(AbstractTableModel):
        COL_NAMES = ("Device", "Slot", "Address", "Roster ID", "Speed step", "Direction")

        def __init__(self, dev_list):
            AbstractTableModel.__init__(self)
            self.dev_list = dev_list
            self.rows = []

        def refresh(self):
            self.rows = _collect_loco_rows(self.dev_list)
            self.fireTableDataChanged()

        def getRowCount(self):
            return len(self.rows)

        def getColumnCount(self):
            return len(self.COL_NAMES)

        def getColumnName(self, col):
            return self.COL_NAMES[col]

        def getValueAt(self, row, col):
            if 0 <= row < len(self.rows) and 0 <= col < len(self.COL_NAMES):
                return str(self.rows[row][col])
            return ""

    class MonitorFrame(JmriJFrame):
        def __init__(self, model, dev_list):
            JmriJFrame.__init__(self, "WiThrottle Monitor")
            self.model = model
            self.dev_list = dev_list
            self.loco_model = LocoTableModel(dev_list)
            self.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
            self.setLayout(BorderLayout(5, 5))

            tabs = JTabbedPane()

            # Tab 1: Devices
            dev_panel = JPanel(BorderLayout())
            dev_panel.add(JLabel("Connected WiThrottle devices:"), BorderLayout.NORTH)
            self.dev_table = JTable(model)
            self.dev_table.setAutoCreateRowSorter(True)
            self.dev_table.setPreferredScrollableViewportSize(Dimension(600, 150))
            dev_panel.add(JScrollPane(self.dev_table), BorderLayout.CENTER)
            tabs.addTab("Devices", dev_panel)

            # Tab 2: Locomotives (address & speed)
            loco_panel = JPanel(BorderLayout())
            loco_panel.add(JLabel("Locomotives (address, speed):"), BorderLayout.NORTH)
            self.loco_table = JTable(self.loco_model)
            self.loco_table.setAutoCreateRowSorter(True)
            self.loco_table.setPreferredScrollableViewportSize(Dimension(600, 200))
            # Color entire row TEXT based on Speed % (model column index 4):
            # green if speed > 0, red if speed == 0/blank.
            class RowSpeedTextRenderer(DefaultTableCellRenderer):
                def getTableCellRendererComponent(self, table, value, isSelected, hasFocus, row, col):
                    c = DefaultTableCellRenderer.getTableCellRendererComponent(
                        self, table, value, isSelected, hasFocus, row, col
                    )
                    if not isSelected:
                        try:
                            model_row = table.convertRowIndexToModel(row)
                            sp_val = table.getModel().getValueAt(model_row, 4)
                            sp = int(str(sp_val).strip()) if sp_val else 0
                            c.setForeground(Color.RED if sp > 0 else Color.BLACK)
                        except:
                            c.setForeground(Color.BLACK)
                    return c
            self.loco_table.setDefaultRenderer(Object, RowSpeedTextRenderer())
            loco_panel.add(JScrollPane(self.loco_table), BorderLayout.CENTER)
            tabs.addTab("Locomotives", loco_panel)

            self.add(tabs, BorderLayout.CENTER)

            # Refresh button
            def refresh(_):
                self.dev_table.repaint()
                self.loco_model.refresh()
            btn = JButton("Refresh")
            btn.addActionListener(refresh)
            south = JPanel(FlowLayout(FlowLayout.RIGHT))
            south.add(btn)
            self.add(south, BorderLayout.SOUTH)

            # Timer to poll locomotive speed/address every 0.5 seconds
            def on_timer(e):
                try:
                    self.loco_model.refresh()
                except:
                    pass
            from javax.swing import Timer
            self.timer = Timer(500, on_timer)
            self.timer.start()
            self.loco_model.refresh()

            self.pack()
            self.setLocationRelativeTo(None)
            self.setVisible(True)

        def dispose(self):
            try:
                if hasattr(self, "timer") and self.timer is not None:
                    self.timer.stop()
            except:
                pass
            JmriJFrame.dispose(self)

    SwingUtilities.invokeLater(lambda: MonitorFrame(list_model, device_list))


# Run when script is executed
create_monitor_frame()
