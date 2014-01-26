/*
 * OperationsServlet specific JavaScript
 */

//request and show a list of available trains from JMRI server
var $getTrains = function(showAll) {
    $.ajax({
        url: "/operations/trains?format=tr" + ((showAll) ? "&show=all" : ""), //request proper url for train list
        data: {},
        success: function(data) {
            if (data.length === 0) {
                $("#warning-no-trains").removeClass("hidden").addClass("show");
                $("#trains").removeClass("show").addClass("hidden");
            } else {
                $("#trains").removeClass("hidden").addClass("show");
                $("#trains > tbody").empty();
                $("#trains > tbody").append(data);
            }
            $("#activity-alert").removeClass("show").addClass("hidden");
        },
        dataType: "html"
    });
};

//parse the page's input parameter and return value for name passed in
function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && match[1];
}
;

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    // need to read path to get method
    // /operations/manifest/id > getManifest for Id
    // /operations/conductor/id > getConductor for Id
    // all other > getTrains
    $getTrains((getParameterByName('show') === "all") ? true : false);
});
