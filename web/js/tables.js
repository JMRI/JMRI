/*
 * TablesServlet specific JavaScript
 * 
 * TODO: add children listeners to additional types (below the line)
 * TODO: update other language NavBar.html
 * TODO: update json help with correct program references
 * TODO: add structure change listeners for routes
 */

var jmri = null;

//parm is the array of items from which to build table
function rebuildTable(data) {
	$("#activity-alert").removeClass("hidden").addClass("show");
	$("table#jmri-data").removeClass("show").addClass("hidden");
	$("#warning-no-data").removeClass("show").addClass("hidden");
	if (data.length) {
		//build header row from first row of data
		var thead = '<tr>';
		$.each(data[0].data, function (index, value) {
//			jmri.log("head " + index+"="+value);
			thead += '<th>' + index + '</th>'; 
		});
		thead += '</tr>';
		$("table#jmri-data thead").html(thead);
		//build all data rows for table body, store item name in rows for later lookup
		var tbody = '';
		data.forEach(function(item) {
			tbody += '<tr data-name="'+item.data.name+'">';
			$.each(item.data, function (index, value) {
				tbody += '<td>' + displayCellValue($("html").data("table-type"), index, value) + '</td>'; //replace some values with descriptions
			});
			tbody += '</tr>';
		});
		$("table#jmri-data tbody").html(tbody);
		$("table#jmri-data").removeClass("hidden").addClass("show");
	} else {
		$("#warning-no-data").removeClass("hidden").addClass("show");
	}
	$("#activity-alert").removeClass("show").addClass("hidden");
	hideEmptyColumns("table#jmri-data tr th");
}

function replaceRow(name, data) {
	jmri.log("in replaceRow: name='" + name + "'");
    var row = $("table#jmri-data tr[data-name='" + name + "']");
    if ($(row).length) {
    	var r = "";
    	$.each(data, function (index, value) {
			r += '<td>' + displayCellValue($("html").data("table-type"), index, value) + '</td>'; //replace some values with descriptions
		});
    	row.html(r);
    	hideEmptyColumns("table#jmri-data tr th");
    } else {
    	jmri.log("row not found for name='" + name + "'");
    	//TODO: handle addition of row
    }
}

function displayCellValue(type, colName, value) {
	if (value==null) {
		return ""; //return empty string for any null value
	}
	if ($.isArray(value)) {
		return "[array]" ; //placeholder						
	}
	if (typeof value === "object") { //special treatment for objects
			if (value.name) {
				return value.name;  //if it has a name, use it
			} else {
				return "[obj]" ; //placeholder				
			}
	}
	//convert known states to human-readable strings, if not known show as is
	if ((colName == "state") || (colName == "occupiedSense")) {
		switch (type) {
		case "turnouts":
			switch (value) {
			case 0:
				return "unknown";
			case 2:
				return "closed";
			case 4:
				return "thrown";
			case 8:
				return "inconsistent";
			default:
				return value;
			}
			break;
		case "routes":
		case "sensors":
		case "layoutBlocks":
			switch (value) {
			case 0:
				return "unknown";
			case 2:
				return "active";
			case 4:
				return "inactive";
			case 8:
				return "inconsistent";
			default:
				return value;
			}
		default:
			return value; //not special, just return the passed in value
		break;
		}
	}
	return htmlEncode(value);
}

//replace some special chars with html equivalents
function htmlEncode( html ) {
    return document.createElement( 'a' ).appendChild( 
        document.createTextNode( html ) ).parentNode.innerHTML;
};

function hideEmptyColumns(selector) {
	$(selector).each(function(index) {
		//select all tds in this column
		var tds = $(this).parents('table').find('tr td:nth-child(' + (index + 1) + ')');
		//check if all the cells in this column are empty
		if (tds.length === tds.filter(':empty').length) {			
			$(this).addClass("hidden"); //hide header			
			tds.addClass("hidden"); //hide cells
		} else {			
			$(this).removeClass("hidden"); //show header			
			tds.removeClass("hidden"); //show cells			
		}
	});
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	jmri = $.JMRI({});

	//replace title with the table type
	document.title = "JMRI Tables: " + $("html").data("table-type");
	$("h1.title").text($("html").data("table-type"));

	jmri = $.JMRI({
		//when we get the hello message, send a websocket list request which 
		//  returns the list and sets up change listeners
		hello: function(data) {
			jmri.getList($("html").data("table-type")); // request list and updates for the table-type 
		},
		//everything calls console()
		console: function(originalData) {
			var data = JSON.parse(originalData);
			jmri.log("in console: data="+JSON.stringify(data).substr(0,180) + "...");
			if ($.isArray(data)) {  //if its an array, 
				rebuildTable(data); //  replace the table with the array list			
			} else if ((data.type) && (data.type !== "pong")) {
				replaceRow(data.data.name, data.data); //if single item, update the row
			}
		},	
	});
});

