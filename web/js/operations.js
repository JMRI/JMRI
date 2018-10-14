/*
 * OperationsServlet specific JavaScript
 */

var jmri = null;
var view = "";
var CODE_TERMINATED = 0x80;

/*
 * request and show a list of available trains from JMRI server
 */
function getTrains(showAll) {
    $.ajax({
        url: "/operations/trains?format=html" + ((showAll) ? "&show=all" : ""),
        data: {},
        success: function (data) {
//        	jmri.log("redrawing Trains table");
            if (data.length === 0) {
                $("#warning-no-trains").removeClass("hidden").addClass("show");
                $("#trains").removeClass("show").addClass("hidden");
            } else {
                $("#warning-no-trains").removeClass("show").addClass("hidden");
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
        success: function (data) {
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

function getConductor(id, location) {
    var data = (location !== false) ? {format: "html", location: location} : {format: "html"};
    $.ajax({
        url: "/operations/trains/" + id + "/conductor",
        data: JSON.stringify(data),
        type: "PUT",
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            if (data.length === 0) {
                $("#conductor").removeClass("show").addClass("hidden");
            } else {
                // insert data
                $("#conductor").removeClass("hidden").addClass("show");
                $("#conductor").empty();
                $("#conductor").append(data);
                var pickup = $.trim($("#ops-pickup > ul").html()) ? true : false;
                var setout = $.trim($("#ops-setout > ul").html()) ? true : false;
                var local = $.trim($("#ops-local > ul").html()) ? true : false;
                // hide empty panels
                if (pickup) {
                    if (!setout && !local) {
                        $("#ops-pickup").addClass("col-xs-12").removeClass("col-md-4");
                    } else if (!setout || !local) {
                        $("#ops-pickup").addClass("col-sm-6").removeClass("col-md-4");
                    }
                } else {
                    $("#ops-pickup").addClass("hidden").removeClass("col-md-4");
                }
                if (setout) {
                    if (!pickup && !local) {
                        $("#ops-setout").addClass("col-xs-12").removeClass("col-md-4");
                    } else if (!pickup || !local) {
                        $("#ops-setout").addClass("col-sm-6").removeClass("col-md-4");
                    }
                } else {
                    $("#ops-setout").addClass("hidden").removeClass("col-md-4");
                }
                if (local) {
                    if (!pickup && !setout) {
                        $("#ops-local").addClass("col-xs-12").removeClass("col-md-4");
                    } else if (!pickup || !setout) {
                        $("#ops-local").addClass("col-sm-6").removeClass("col-md-4");
                    }
                } else {
                    $("#ops-local").addClass("hidden").removeClass("col-md-4");
                }
                if (!$.trim($("#ops-engine-pickup > ul").html())) {
                    $("#ops-engine-pickup").addClass("hidden").removeClass("col-sm-6");
                    $("#ops-engine-setout").addClass("col-xs-12").removeClass("col-sm-6");
                } else if (!$.trim($("#ops-engine-setout > ul").html())) {
                    $("#ops-engine-setout").addClass("hidden").removeClass("col-sm-6");
                    $("#ops-engine-pickup").addClass("col-xs-12").removeClass("col-sm-6");
                }
                // make check/unckeck buttons usable
                $("#check-all").click(function () {
                    $(".rs-check").prop("checked", true);
                    $("#move-train").prop("disabled", false);
                });
                $("#clear-all").click(function () {
                    $(".rs-check").prop("checked", false);
                    $("#move-train").prop("disabled", true);
                });
                //disable move button if no location
                if (!$("#move-train").data("location")) {
                    $("#move-train").prop("disabled", true);
                }
                // disable/enable controls if no work
                if ($(".rs-check").length === 0) {
                    $("#move-train").prop("disabled", false);
                    $("#check-all").prop("disabled", true);
                    $("#clear-all").prop("disabled", true);
                }
                // enable move button if all checkboxs are checked
                $(".rs-check").click(function () {
                    var disabled = true;
                    if (this.checked) {
                        disabled = false;
                        $(".rs-check").each(function () {
                            if (!this.checked) {
                                disabled = true;
                                return false;
                            }
                        });
                    }
                    $("#move-train").prop("disabled", disabled);
                });
                //disable move button if train is terminated
                if ($("#move-train").data("statuscode") == CODE_TERMINATED) {
                    $("#move-train").prop("disabled", true);
                }
                
                // add function to move button
                $("#move-train").click(function () {
                    getConductor(id, $("#move-train").data("location"));
                });
            }
            $("#activity-alert").removeClass("show").addClass("hidden");
        },
        dataType: "html"
    });
}

function getTrainName(id) {
    $.ajax({
        url: "/json/train/" + id,
        data: {},
        success: function (json) {
            $("#navbar-operations-train").append(json.data.iconName + " (" + json.data.description + ")");
        },
        dataType: "json"
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function () {
    if (window.location.pathname.indexOf("/manifest") >= 0) {
        view = "manifest";
        $("#manifest").removeClass("hidden").addClass("show");
        getManifest($("html").data("train"));
    } else if (window.location.pathname.indexOf("/conductor") >= 0) {
        view = "conductor";
        $("#conductor").removeClass("hidden").addClass("show");
    } else {
        view = "trains";
        $("#show-all-trains > input").prop("checked", window.localStorage.getItem("jmri.operations.trains.showAll") === "true");
        $("#show-all-trains > span").tooltip({delay: {show: 500, hide: 0}});
        $("#show-all-trains > input").change(function () {
            getTrains($(this).is(":checked"));
            window.localStorage.setItem("jmri.operations.trains.showAll", $(this).is(":checked"));
        });
    }
    jmri = $.JMRI({
        open: function () {
            if (view === "conductor") {
                jmri.getTrain($("html").data("train"));
            } else if (view === "trains") {
                getTrains($("#show-all-trains > input").is(":checked"));
                jmri.getList("trains"); //request updates when trains are added or deleted
            }
        },
        trains: function (data) { //trains list received, refresh the trains table
            getTrains($("#show-all-trains > input").is(":checked")); //refresh the trains table
        },
        train: function (id, data) {
//        	jmri.log("in train: for " + data.iconName);
            if (view === "manifest") {
                if (id == $("html").data("train")) {
                    $("title").text(data.iconName + " (" + data.description + ") Manifest | " + $("html").data("railroad"));
                }
            } else if (view === "conductor") {
                if (id == $("html").data("train")) {
                    $("title").text(data.iconName + " (" + data.description + ") Conductor | " + $("html").data("railroad"));
                    getConductor(id, false);
                }
            } else if (view === "trains") { //if this train already shown, replace columns in that row
                var row = $("tr[data-train=" + id + "]");
                if ($(row).length) {
                    $(row).find(".train-name").text(data.iconName); // train icon name
                    $(row).children(".train-description").text(data.description); // description
                    $(row).children(".train-leadEngine").text(data.leadEngine); // leadEngine
                    $(row).children(".train-trainDepartsName").text(data.trainDepartsName); // origin ("departs")
                    $(row).children(".train-departureTime").text(data.departureTime); // origin departure time
                    $(row).children(".train-status").text(data.status); // status
                    $(row).children(".train-location").text(data.location); // location
                    $(row).children(".train-trainTerminatesName").text(data.trainTerminatesName); // destination
                    $(row).children(".train-route").text(data.route); // route
                } else { //add unknown trains only if showAll is checked, or train has cars (active)
                	if ($("#show-all-trains > input").is(":checked") 
                		|| data.cars.length > 0 ) {
//                		jmri.log("new train found, reloading trains table");
                		getTrains($("#show-all-trains > input").is(":checked"));
                	}
                }
            }
        }
    });
    // setup the functional menu items
    if ($("html").data("train") !== "") {
        getTrainName($("html").data("train"));
        $("#navbar-operations-manifest > a").attr("href", "/operations/trains/" + $("html").data("train") + "/manifest");
        $("#navbar-operations-conductor > a").attr("href", "/operations/trains/" + $("html").data("train") + "/conductor");
    } else {
        $("#navbar-operations-train-divider").addClass("hidden").removeClass("show");
        $("#navbar-operations-train").addClass("hidden").removeClass("show");
        $("#navbar-operations-manifest").addClass("hidden").removeClass("show");
        $("#navbar-operations-conductor").addClass("hidden").removeClass("show");
    }
    jmri.connect();
});
