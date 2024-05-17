/*
 * Cars specific JavaScript
 *
 */

var jmri = null;
var gSelectString = "";

//convert page parms into array to use for filtering rows
const urlSearchParams = new URLSearchParams(window.location.search);
const params = Object.fromEntries(urlSearchParams.entries());
            
//handle an error message returned via the websocket from the server
//  parms: html error code, message is the message text
function showError(code, message) {
    $("#activity-alert").removeClass("show").addClass("hidden");
    $("table#jmri-data").removeClass("show").addClass("hidden");
    $("#warning-no-data").removeClass("show").addClass("hidden");
    $("#error-message").html("Error " + code + ":" + message);
    $("#error-message").removeClass("hidden").addClass("show");
}

//append a new row to the table, or replace an existing row, based on name
function setRow(name, data){
  var tbody = $("table#jmri-data tbody").html(); //get current table body
  var carType = data.carType + ", " + data.color;
  var tds = "<td class='name'>" + data.name + "</td><td class='carType'>" + carType + "</td><td class='location'>"; //build cells
  //format location
  if (data.locationUnknown == true) {  
      tds += "&lt;?&gt;";
  } else if (data.location != null) {
      tds += data.location.userName;
      if (data.location.track != null) {
          tds += " (" + data.location.track.userName + ")"; 
      }
  } else {
      tds += "&nbsp;"; 
  } 
  //format train name
  tds += "</td><td>" + (data.trainName != null ? data.trainName : "&nbsp;") + "</td>";
    
//  var tr = "<tr data-name='" + data.name + "' data-cartype='" + carType + "'>" + tds + "</tr>"; //build row with key
  var tr = "<tr data-name='" + data.name + "'>" + tds + "</tr>"; //build row with key
  var row = $("table#jmri-data tr[data-name='" + name + "']"); //look for row by key
  if ($(row).length) {
      row.html(tds); //if found, replace cells
  } else {
      $("table#jmri-data tbody").html(tbody + tr); //if not found, append row to table body
  }
};

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function () {

    // add table type to heading and title
    $("#table-type").text($("html").data("table-type"));
    document.title = $("h1.title").text();

    //get train if passed in
    var gTrainName = getParameterByName("train");
    $("#searchBox").text(gTrainName);
        
    jmri = $.JMRI({
        // when we get the hello message, send a websocket list request which
        // returns the list and sets up change listeners
        // note: the functions and parameter names must match exactly those in jquery.jmri.js
        hello: function (data) {
//            jmri.log("in hello: data=" + JSON.stringify(data).substr(0, 180) + "...");
            jmri.getList("cars");
            jmri.getList("locations");
        },
        // when the JMRI object receives an array of cars, call this
        cars: function (data) {
            jmri.log("in cars: data=" + JSON.stringify(data).substr(0, 180) + "...");
            data.forEach(el => {
//                jmri.log("Requesting update listener for " + el.type +" '" + el.data.name +"'");
                jmri.getObject(el.type, el.data.name);
            });
            //reset the table   
            $("#activity-alert").removeClass("hidden").addClass("show");
            $("table#jmri-data").removeClass("show").addClass("hidden");
            $("#warning-no-data").removeClass("show").addClass("hidden");
            $("#error-message").removeClass("show").addClass("hidden");
                                 
            $("table#cars tbody").html(""); 
            $("table#jmri-data").removeClass("hidden").addClass("show");
            $("#activity-alert").removeClass("show").addClass("hidden");
        },
        // when the JMRI object receives a car update, call this
        car: function (name, data) {
//            jmri.log("in car: name='" + name + "', data=" 
//                + JSON.stringify(data).substr(0, 180) + "...");
            setRow(name, data); // add or update the row
        },       
        // when the JMRI object receives an array of locations, call this
        locations: function (data) {
            jmri.log("in locations: data=" + JSON.stringify(data).substr(0, 180) + "...");

            Locations = data;//JSON.parse(data);
            // Sort the location by userName
            Locations.sort(function(a,b) {
                var at = a.data.userName;
                var bt = b.data.userName;         
                return (at > bt)?1:((at < bt)?-1:0); 
            });
        
            var s = '';
            var t = '';
            for (var i = 0; i < Locations.length; i++) {
                // Add the locations to the the dropdown
                s += '<option value="' + Locations[i].data.name + '">' + Locations[i].data.userName + '</option>';
                
                // Sort the tracks within each location by the track userName
                Locations[i].data.track.sort(function(a,b) {
                    var at = a.userName;
                    var bt = b.userName;         
                    return (at > bt)?1:((at < bt)?-1:0); 
                });
                
                // Add each track to the dropdown with a data- property to enable searching in ddlbLocation_OnChange()
                for (var j = 0; j < Locations[i].data.track.length; j++){
                    t += '<option value="' + Locations[i].data.track[j].name + '" data-locationid="' + Locations[i].data.name + '">' + Locations[i].data.track[j].userName + '</option>';
                }
            }
            $('#modal-car-edit-select-location').html(s);
            $('#modal-car-edit-select-track').html(t);

        },
        error: function(error) {
            $("#error-alert div").text(error.message);
            $("#error-alert").addClass("show").removeClass("hidden");
        },
        
        // all messages call console(), so use it for debugging
        console: function (originalData) {
//            var data = JSON.parse(originalData);
//            jmri.log("in console: data=" + JSON.stringify(data).substr(0, 180) + "...");
        }                    
    });

    // trigger the initial connection to the JMRI server
//    jmri.connect();
    
    //setup the Search.. box functionality
    $("#searchBox").on("keyup", function() {
        var value = $(this).val().toLowerCase();
        $("table#jmri-data tbody tr").filter(function() {
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });

    //listen for clicks on rows
    $('table#jmri-data')
      .on('click', 'tbody tr', function (e) {
        //copy data from selected column to edit modal
        carName = $(this).data('name');
        $('#modal-car-edit-carName').text(carName);
        carType = $(this).find('td.carType').text();
        $('#modal-car-edit-carType').text(carType);
        locationName = $(this).find('td.location').text();
        $('#modal-car-edit-location').text(locationName);
        $('#modal-car-edit').modal("show");
     });

    //setup change listener for location select
    function Select_Location_OnChange() {
        // refresh ddlbTrack
        var locationID = $('#modal-car-edit-select-location').find(":selected").val();
        var tracks = $('#modal-car-edit-select-track').children();
        var firstOption = '';
        $(tracks).each(function() {
            if($(this).data("locationid") == locationID) {
                $(this).show();
                if (firstOption == '') {
                    firstOption = $(this).val();
                }
            } 
            else {
                $(this).hide();
            }
        });
        $('#modal-car-edit-select-track').val(firstOption);
        Select_Track_OnChange();
//        $('#txtCarNumber').val('');
    }
    $("[id=modal-car-edit-select-location]").on('change', Select_Location_OnChange);

    function Select_Track_OnChange() {
        // Get track details
        var locationID = $('#modal-car-edit-select-location').find(":selected").val();
        var trackID = $('#modal-car-edit-select-track').find(":selected").val();
        var trackLength = 'unknown';
        var carTypes = 'unknown';
        Locations.forEach(function(thisLocation) {
            if (thisLocation.data.name == locationID) {
                thisLocation.data.track.forEach(function(thisTrack) {
//                    var breakppoint = 1;
                    if (thisTrack.name == trackID) {
                        trackLength = thisTrack.length;
                        carTypes = thisTrack.carType.join('; ');
                    }
                });
            }
        });
        $('#modal-car-edit-trackLength').text(trackLength + 'ft');
        $('#modal-car-edit-carTypes').text(carTypes);
        
//        getCarsOnTrack();
    }
    $("[id=modal-car-edit-select-track]").on('change', Select_Track_OnChange);


    function Save_OnClick() {
        cmd = {
            name: $('#modal-car-edit-carName').text(), 
            location: {
                name: $('#modal-car-edit-select-location').find(":selected").val(), 
                track: {
                    name: $('#modal-car-edit-select-track').find(":selected").val(), 
                }
            }
        };
        $('#modal-car-edit').modal("hide");
        jmri.socket.send('car', cmd, 'post');    
    }
    $("[id=modal-car-edit-save]").on('click', Save_OnClick);


});
