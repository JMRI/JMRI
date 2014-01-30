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
;

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
    getPanels(); // complete the panels menu
});

