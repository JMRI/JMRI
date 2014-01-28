/*
 * Common JavaScript functionality for BootStrap-based servlets.
 */

var $getPanels = function() {
    $.ajax({
        url: "/json/panels",
        data: {},
        success: function(data) {
            $(".navbar-panel-item").remove();
            if (data.length !== 0) {
                $("#empty-panel-list").addClass("hidden").removeClass("show");
                $.each(data.reverse(), function(index, value) {
                    $("#navbar-panels").prepend("<li class=\"navbar-panel-item\"><a href=\"/panel?name=" + value.name + "\">" + value.userName + "</a></li>");
                });
            } else {
                $("#empty-panel-list").addClass("show").removeClass("hidden");
            }
        }
    });
};

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    $getPanels();

    // temporary - should be handled by Servlet
    $(".context-panel-only").addClass("hidden");
});

