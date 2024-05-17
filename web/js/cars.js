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
        // when the JMRI object receives an array of cars, call this
        locations: function (data) {
            jmri.log("in locations: data=" + JSON.stringify(data).substr(0, 180) + "...");
            gSelectString  = '<div class="btn-group">';
            gSelectString += '<button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">';
            gSelectString += 'Select New Location</button>';
            gSelectString += '<ul class="dropdown-menu" role="menu">';
            data.forEach(l => {
//                jmri.log("Received " + l.type +" '" + l.data.name +"'");
                l.data.track.forEach(t => {
//                    jmri.log("Found track '" + t.name +"'");
                    gSelectString += '<li><a href="#">' + t.userName + '</a></li>';                                     
                }); //forEach (t)rack
//                jmri.getObject(el.type, el.data.name);
            }); //forEach (l)ocation
            gSelectString += "</ul></div>";
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
//        currLocation = $(this).text();
//        $(this).html(gSelectString); 
//        jmri.log("clicked '" + carName + "' at '" + currLocation + "'");
        $('#modal-car-edit').modal("show");

     });

});
