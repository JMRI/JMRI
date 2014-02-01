/*
 * Common JavaScript functionality for BootStrap-based servlets.
 */

/*
 * Populate the panels menu with a list of open panels
 */
function getPanels() {
    $.ajax({
        url: "/panel?format=json",
        data: {},
        success: function(data) {
            $(".navbar-panel-item").remove();
            if (data.length !== 0) {
                $("#empty-panel-list").addClass("hidden").removeClass("show");
                $.each(data, function(index, value) {
                    $("#navbar-panels").append("<li class=\"navbar-panel-item\"><a href=\"/panel/" + value.name + "\">" + value.userName + "</a></li>");
                });
            } else {
                $("#empty-panel-list").addClass("show").removeClass("hidden");
            }
        }
    });
}

function getRosterGroups() {
    $.ajax({
        url: "/json/rosterGroups",
        data: {},
        success: function(data) {
            $(".navbar-roster-group-item").remove();
            if (data.length !== 0) {
                $.each(data, function(index, value) {
                    $("#navbar-roster-groups").append("<li class=\"navbar-roster-group-item\"><a href=\"/roster?group=" + value.name + "\"><span class=\"badge pull-right\">" + value.length + "</span>" + value.name + "</a></li>");
                });
            }
        }
    });
}

/*
 * Get list of in-use network services and hide or show elements as appropriate
 */
function getNetworkServices() {
    $.ajax({
        url: "/json/networkServices",
        data: {},
        success: function(data) {
            // show all hidden when service is available elements 
            $(".hidden-jmri_jmri-json").addClass("show").removeClass("hidden");
            $(".hidden-jmri_jmri-locormi").addClass("show").removeClass("hidden");
            $(".hidden-jmri_jmri-simple").addClass("show").removeClass("hidden");
            $(".hidden-jmri_srcp").addClass("show").removeClass("hidden");
            $(".hidden-jmri_withrottle").addClass("show").removeClass("hidden");
            // hide all visible when service is available elements 
            $(".visible-jmri_jmri-json").addClass("hidden").removeClass("show");
            $(".visible-jmri_jmri-locormi").addClass("hidden").removeClass("show");
            $(".visible-jmri_jmri-simple").addClass("hidden").removeClass("show");
            $(".visible-jmri_srcp").addClass("hidden").removeClass("show");
            $(".visible-jmri_withrottle").addClass("hidden").removeClass("show");
            if (data.length !== 0) {
                $.each(data, function(index, value) {
                    var service = value.type.split(".")[0];
                    $(".visible-jmri" + service).addClass("show").removeClass("hidden");
                    $(".hidden-jmri" + service).addClass("hidden").removeClass("show");
                });
            }
        }
    });
}

/*
 * Find a parameter in the URL
 */
function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && match[1];
}

/*
 * Set the title to the parameter + railroad name
 * This also returns the new title should it be needed elsewhere
 */
function setTitle(value) {
    var title = $("html").data("railroad");
    if (value) {
        title = value + " | " + title;
    }
    $(document).attr("title", title);
    return title;
}

/*
 * Set every element matching the selector to the height of the tallest element
 */
function equalHeight(selector) {
    tallest = 0;
    selector.each(function() {
        thisHeight = $(this).height();
        if (thisHeight > tallest) {
            tallest = thisHeight;
        }
    });
    selector.each(function() {
        $(this).height(tallest);
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
// perform tasks that all BootStrap-based servlets need
$(document).ready(function() {
    getNetworkServices(); // hide or show and network service specific elements
    getPanels(); // complete the panels menu
    getRosterGroups(); // list roster groups in menu
});
