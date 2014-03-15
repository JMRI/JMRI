/*
 * RosterServlet specific JavaScript
 */

var jmri = null;

/*
 * request and show roster
 */
function getTrains(group) {
    $.ajax({
        url: "/roster?format=html" + ((group) ? "&group=" + group : ""),
        data: {},
        success: function(data) {
            if (data.length === 0) {
                $("#warning-no-trains").removeClass("hidden").addClass("show");
                $("#roster").removeClass("show").addClass("hidden");
            } else {
                $("#roster").removeClass("hidden").addClass("show");
                $("#roster > tbody").empty();
                $("#roster > tbody").append(data);

                $(".entry-url-menu-item > a[href='']").each(function() {
                    $(this).parent().addClass("disabled");
                    $(this).attr("href", "#");
                });
                hideEmptyColumns("#roster tr th");
                $(".roster-entry td").click(function(event) {
                    if (event.target === this) {
                        window.open("/web/webThrottle.html?loconame=" + $(this).parent().data("id"), $(this).parent().data("address")).focus();
                    }
                });
            }
            $("#activity-alert").removeClass("show").addClass("hidden");
            $("#trains-options").removeClass("hidden").addClass("show");
        },
        dataType: "html"
    });
}

function hideImage(img) {
    // if image is in anchor, remove anchor instead of image
    if ($(img).parent().is("a")) {
        $(img).parent().remove();
    } else {
        $(img).remove();
    }
    hideEmptyColumns("#roster tr th"); // also ensure that if no roster icons or images are loaded, the icon or image column is hidden
    return true;
}

function hideEmptyColumns(selector) {
    $(selector).each(function(index) {
        //select all tds in this column
        var tds = $(this).parents('table').find('tr td:nth-child(' + (index + 1) + ')');
        //check if all the cells in this column are empty
        if (tds.length === tds.filter(':empty').length) {
            //hide header
            $(this).addClass("hidden");
            //hide cells
            tds.addClass("hidden");
        }
    });

}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    getTrains(getParameterByName("group"));
});
