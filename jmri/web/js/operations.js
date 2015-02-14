/*
 * OperationsServlet specific JavaScript
 */

var jmri = null;

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

function getConductor(id, location) {
    var data = (location !== false) ? {format: "html", location: location} : {format: "html"};
    $.ajax({
        url: "/operations/trains/" + id + "/conductor",
        data: JSON.stringify(data),
        type: "PUT",
        contentType: "application/json; charset=utf-8",
        success: function(data) {
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
                $("#check-all").click(function() {
                    $(".rs-check").prop("checked", true);
                    $("#move-train").prop("disabled", false);
                });
                $("#clear-all").click(function() {
                    $(".rs-check").prop("checked", false);
                    $("#move-train").prop("disabled", true);
                });
                // disable/enable controls if no work
                if ($(".rs-check").length === 0) {
                    $("#move-train").prop("disabled", false);
                    $("#check-all").prop("disabled", true);
                    $("#clear-all").prop("disabled", true);
                }
                if (!$("#move-train").data("location")) {
                    $("#move-train").prop("disabled", true);
                }
                // enable move button only if all checkboxs are checked
                $(".rs-check").click(function() {
                    var disabled = true;
                    if (this.checked) {
                        disabled = false;
                        $(".rs-check").each(function() {
                            if (!this.checked) {
                                disabled = true;
                                return false;
                            }
                        });
                    }
                    $("#move-train").prop("disabled", disabled);
                });
                // add function to move button
                $("#move-train").click(function() {
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
        success: function(json) {
            $("#navbar-operations-train").append(json.data.iconName + " (" + json.data.description + ")");
        },
        dataType: "json"
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    if (window.location.pathname.indexOf("/manifest") >= 0) {
        $("#manifest").removeClass("hidden").addClass("show");
        getManifest($("html").data("train"));
    } else if (window.location.pathname.indexOf("/conductor") >= 0) {
        $("#conductor").removeClass("hidden").addClass("show");
        jmri = $.JMRI({
            open: function() {
                jmri.getTrain($("html").data("train"));
            },
            train: function(id, data) {
                getConductor(id, false);
            }
        });
        jmri.connect();
    } else {
        getTrains(getParameterByName('show') === "all");
        $("#show-all-trains > input").prop("checked", getParameterByName('show') === "all");
        $("#show-all-trains > span").tooltip({delay: {show: 500, hide: 0}});
        $("#show-all-trains > input").change(function() {
            getTrains($(this).is(":checked"));
        });
    }
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
});
