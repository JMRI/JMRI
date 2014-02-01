/*
 * Functions for the HomeServlet
 */

/*
 * Populate the panels menu with a list of open panels
 */
function listFrames() {
    $.ajax({
        url: "/frame/list?format=json",
        data: {},
        success: function(data, textStatus, jqXHR) {
            if (data.length !== 0) {
                $("#frame-list").empty();
                $("#frame-list").addClass("show").removeClass("hidden");
                $.each(data, function(index, value) {
                    $("#frame-list").append("<li class=\"list-group-item\"><a href=\"" + value.URL + "\"><img src=\"" + value.png + "\" style=\"max-width: 100%;\"><div class=\"caption\">" + value.name + "</div></a></li>");
                });
            }
        }
    });
}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
// perform tasks that the Home servlet needs
$(document).ready(function() {
    listFrames();
});
