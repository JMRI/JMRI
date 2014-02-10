/*
 * OperationsServlet specific JavaScript
 */

/*
 * request and show a list of available trains from JMRI server
 */
function getTrains(showAll) {
    $.ajax({
        url: "/operations/trains?format=html" + ((showAll) ? "&show=all" : ""),
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
            $("#trains-options").removeClass("hidden").addClass("show");
        },
        dataType: "html"
    });
}

function getManifest(id) {
    $.ajax({
        url: "/operations/manifest/" + id + "?format=html",
        data: {},
        success: function(data) {
            if (data.length === 0) {
                $("#manifest").removeClass("show").addClass("hidden");
            } else {
                $("#manifest").removeClass("hidden").addClass("show");
                $("#manifest").empty();
                $("#manifest").append(data);
            }
            $("#activity-alert").removeClass("show").addClass("hidden");
        },
        dataType: "html"
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    var pathElements = window.location.pathname.replace(/\/$/, '').split('/');
    console.log("pathElements:" + pathElements);
    if (pathElements[2] === "manifest") {
        $("#manifest").removeClass("hidden").addClass("show");
        getManifest(pathElements[3]);
    } else if (window.location.pathname.indexOf("/conductor") >= 0) {
        $("#conductor").removeClass("hidden").addClass("show");
    } else {
        getTrains(getParameterByName('show') === "all");
        $("#show-all-trains > input").prop("checked", getParameterByName('show') === "all");
        $("#show-all-trains > span").tooltip({delay: {show: 500, hide: 0}});
        $("#show-all-trains > input").change(function() {
            getTrains($(this).is(":checked"));
        });
    }
});
