/*
 * Functions for the HomeServlet
 */

/*
 * Populate the panels menu with a list of open frames.
 * 
 * By removing "&panels=true" from the url, shows panel editors and displays
 * panels as screen scrapes.
 */
function listFrames(path) {
    var framePath = "/frame/list?format=json&panels=true";
    var panelPath = "/panel?format=json";
    if (typeof path === "undefined") {
        path = framePath;
    }
    $.ajax({
        url: path,
        data: {},
        success: function (data, textStatus, jqXHR) {
            if (data.length !== 0) {
                $("#no-open-frames").addClass("hidden").removeClass("show");
                $("#frame-list").empty();
                $("#frame-list").addClass("show").removeClass("hidden");
                $.each(data, function (index, value) {
                    if (value.type === "panel") {
                        $("#frame-list").append("<li class=\"list-group-item\"><a href=\"/panel/" + value.data.name + "\"><img src=\"/panel/" + value.data.name + "?format=png\" style=\"max-width: 100%;\"><div class=\"caption\">" + value.data.userName + "</div></a></li>");
                    } else {
                        $("#frame-list").append("<li class=\"list-group-item\"><a href=\"" + value.URL + "\"><img src=\"" + value.png + "\" style=\"max-width: 100%;\"><div class=\"caption\">" + value.name + "</div></a></li>");
                    }
                });
            } else {
                $("#no-open-frames").addClass("show").removeClass("hidden");
                $("#frame-list").addClass("hidden").removeClass("show");
            }
        },
        statusCode: {
            403: function() {
                if (path !== panelPath) {
                    listFrames(panelPath);
                }
            }
        }
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
// perform tasks that the Home servlet needs
$(document).ready(function () {
    listFrames();
});
