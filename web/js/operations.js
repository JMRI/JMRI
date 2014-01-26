/*
 * OperationsServlet specific JavaScript
 */

//request and show a list of available trains from JMRI server
var $showTrainList = function(showAll) {
//    $("#trains > tbody").empty();
    console.log($("#trains > tbody"));
    $("#trains > tbody").load({
        url: "/operations/trains?format=tr" + ((showAll) ? "&show=all" : ""),
        complete: function(response, status, xhr) {
            if (status === "error") {
                console.log("Error: " + status + " (" + response + ")");
            } else {
                $("#trains").removeClass("hidden").addClass("show");
            }
            $("#activity-alert").removeClass("show").addClass("hidden");
        }
    });
    $.ajax({
        url: "/operations/trains?format=tr" + ((showAll) ? "&show=all" : ""), //request proper url for train list
        data: {},
        success: function(data) {
            if (data.length === 0) {
                $("#warning-no-trains").removeClass("hidden").addClass("show");
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
    console.log("Getting trains");
    $showTrainList((getParameterByName('show') === "all") ? true : false);
});
