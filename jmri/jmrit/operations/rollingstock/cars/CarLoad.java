package jmri.jmrit.operations.rollingstock.cars;

/**
 * Represents a car load, includes pickup and drop comments.
 * @author Daniel Boudreau (C) 2010
 *
 */
public class CarLoad {
	String _name;
	String _pickupComment = "";
	String _dropComment = "";
	
	public CarLoad(String name){
		_name = name;
	}
	
	public CarLoad(String name, String pickupComment, String dropComment){
		_name = name;
		_pickupComment = pickupComment;
		_dropComment = dropComment;		
	}
	
	public String getName(){
		return _name;
	}
	
	public void setName(String name){
		_name = name;
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
