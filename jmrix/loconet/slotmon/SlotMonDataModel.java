/** 
 * SlotMonDataModel.java
 *
 * Description:		Table data model for display of slot manager contents
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.slotmon;

import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotManager;
import jmri.jmrix.loconet.LnConstants;

import ErrLoggerJ.ErrLog;

import java.util.Vector;

public class SlotMonDataModel extends javax.swing.table.AbstractTableModel implements SlotListener  {

	String headers[] = new String[]  {
		"Slot", "Address", "Speed", "Decoder Type", "status", "consisted", "direction",
		"f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "throttle ID" };
		
	Vector[] table = new Vector[16];   // each vector is a column, will hold the rows
		
	SlotMonDataModel(int row, int column) {  
		// connect to SlotManager for updates
		SlotManager.instance().addSlotListener(this);
		}
	
	// basic methods for AbstractTableModel implementation
	public int getRowCount() { 
		if (_allSlots) { 
			// will show the entire set
			return 128; 
		} else {
			return nActiveSlots();	
		}
	}
	
	public int getColumnCount( ){ return headers.length;}

	public String getColumnName(int col) { return headers[col];}
		
	public Object getValueAt(int row, int col) { 
		int slotNum;
		if (_allSlots) { 
			// will show the entire set
			slotNum = row; 
		} else {
			slotNum = ithActiveSlot(row);
		}
		
		LocoNetSlot s = SlotManager.instance().slot(slotNum);
		
		if (s == null) ErrLog.msg(ErrLog.error, "SlotMonDataModel", "getValueAt",
				"slot pointer was null for slot row: "+row+" col: "+col);
		
		switch (col) {
			case 0:  // slot number
				return new Integer(slotNum);
			case 1:  // 
				return new Integer(s.locoAddr());
			case 2:  // 
				return new Integer(s.speed());
			case 3:  // 
				switch (s.decoderType()) {
					case LnConstants.DEC_MODE_128A:		return "128 step advanced";
					case LnConstants.DEC_MODE_28A:		return "28 step advanced";
					case LnConstants.DEC_MODE_128:		return "128 step";
					case LnConstants.DEC_MODE_14:		return "14 step";
					case LnConstants.DEC_MODE_28TRI:	return "28 step trinary";
					case LnConstants.DEC_MODE_28:		return "28 step";
					default:							return "<unknown>";
				}
			case 4:  // 
				switch (s.slotStatus()) {
					case LnConstants.LOCO_IN_USE: 	return "In Use";
					case LnConstants.LOCO_IDLE:		return "Idle";
					case LnConstants.LOCO_COMMON: 	return "Common";
					case LnConstants.LOCO_FREE: 	return "Free";
					default: 	return "<error>";
				}
			case 5:  // 
				switch (s.consistStatus()) {
					case LnConstants.CONSIST_MID:	return "mid";
					case LnConstants.CONSIST_TOP:	return "top";
					case LnConstants.CONSIST_SUB:	return "sub";
					case LnConstants.CONSIST_NO:	return "none";
					default: 	return "<error>";
				}
			case 6:  // 
				return (s.isForward() ? "F" : "R");
			case 7:  // 
				return (s.isF0() ? "On" : "Off");
			case 8:  // 
				return (s.isF1() ? "On" : "Off");
			case 9:  // 
				return (s.isF2() ? "On" : "Off");
			case 10:  // 
				return (s.isF3() ? "On" : "Off");
			case 11:  // 
				return (s.isF4() ? "On" : "Off");
			case 12:  // 
				return (s.isF5() ? "On" : "Off");
			case 13:  // 
				return (s.isF6() ? "On" : "Off");
			case 14:  // 
				return (s.isF7() ? "On" : "Off");
			case 15:  // 
				return (s.isF8() ? "On" : "Off");
			case 16:
				return new Integer(s.id());
		
			default:
				ErrLog.msg(ErrLog.error, "SlotMonDataModel", "getValueAt", 
					"internal state inconsistent with table requst for "+row+" "+col);
				return null;
			}			
	};
	
	// methods to communicate with SlotManager
	public synchronized void notifyChangedSlot(LocoNetSlot s) {
		// update model from this slot
		
		int slotNum = -1;
		if (_allSlots) slotNum=s.getSlot();		// this will be row until we show only active slots
		
		// notify the JTable object that a row has changed; do that in the Swing thread!
		Runnable r = new Notify(slotNum, this);   // -1 in first arg means all
		javax.swing.SwingUtilities.invokeLater(r);
		}

	class Notify implements Runnable {
		private int _row;
		javax.swing.table.AbstractTableModel _model;
		public Notify(int row, javax.swing.table.AbstractTableModel model) 
			{ _row = row; _model = model;}
		public void run() { 
			if (-1 == _row) {  // notify about entire table
				_model.fireTableDataChanged();  // just that row
			}
			else {
				// notify that _row has changed
				_model.fireTableRowsUpdated(_row, _row);  // just that row
				}
			}
		}
	
	// methods for control of "all slots" vs "only active slots"
	private boolean _allSlots = true;
	
	public void showAllSlots(boolean val) { _allSlots = val; }
	
	// the following two service functions should probably use a local cache instead
	// of counting/searching each time
	public int nActiveSlots() {
		int n = 0;
		for (int i=0; i<128; i++) {
			LocoNetSlot s = SlotManager.instance().slot(i);
			if (s.slotStatus() != LnConstants.LOCO_FREE) n++;
		}
		return n;
	}

	public int ithActiveSlot(int i) {   // turns slot number for ith active slot, counting from 0
										// intended to be called with a row number	
		int slotNum;
		int n = -1;   // need to find a used slot to have the 0th one!
		for (slotNum=0; slotNum<128; slotNum++) {
			LocoNetSlot s = SlotManager.instance().slot(slotNum);
			if (s.slotStatus() != LnConstants.LOCO_FREE) n++;
			if (n == i) break;
		}
		return slotNum;
	}
	
}
