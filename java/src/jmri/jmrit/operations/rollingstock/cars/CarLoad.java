package jmri.jmrit.operations.rollingstock.cars;

import java.util.ResourceBundle;

/**
 * Represents a car load, includes pickup and drop comments.
 * @author Daniel Boudreau (C) 2010
 *
 */
public class CarLoad {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	public static final String PRIORITY_LOW = rb.getString("PriorityLow");
	public static final String PRIORITY_HIGH = rb.getString("PriorityHigh");

	String _name;
	String _priority = PRIORITY_LOW;
	String _pickupComment = "";
	String _dropComment = "";
	
	public CarLoad(String name){
		_name = name;
	}
	
	public CarLoad(String name, String priority, String pickupComment, String dropComment){
		_name = name;
		_priority = priority;
		_pickupComment = pickupComment;
		_dropComment = dropComment;		
	}
	
	public String getName(){
		return _name;
	}
	
	public void setName(String name){
		_name = name;
	}
	
	public String getPriority(){
		return _priority;
	}
	
	public void setPriority(String priority){
		_priority = priority;
	}
	
	public String getPickupComment(){
		return _pickupComment;
	}
	
	public void setPickupComment(String comment){
		_pickupComment = comment;
	}
	
	public String getDropComment(){
		return _dropComment;
	}
	
	public void setDropComment(String comment){
		_dropComment = comment;
	}

}
