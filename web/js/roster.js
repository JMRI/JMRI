/*
 * RosterServlet specific JavaScript
 */

var jmri = null;

/*
 * request and show roster
 */
function getRosterTable(group) {
	$.ajax({
        url: "/roster?format=html" + ((group) ? "&group=" + encodeURIComponent(group) : ""),
        data: {},
        success: function(data) {
            if (data.length === 0) {
                if ($("html").data("roster-group")) {
                    $("#warning-group-no-entries").removeClass("hidden").addClass("show");
                } else {
                    $("#warning-roster-no-entries").removeClass("hidden").addClass("show");
                }
                $("#roster").removeClass("show").addClass("hidden");
            } else {
                $("#warning-roster-no-entries").removeClass("show").addClass("hidden");
                $("#warning-group-no-entries").removeClass("show").addClass("hidden");
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
                        window.open("/web/webThrottle.html?loconame=" + $(this).parent().data("rosterEntry"), $(this).parent().data("address")).focus();
                    }
                });
                $(".roster-entry td").css("cursor", "pointer");
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

function initUploads() {
	$('#fileupload').fileupload({

		singleFileUpload: false,
		
		dataType: 'json',

		done: function (e, data) {
			$.each(data.result, function (index, msg) {
				$("#msgList").prepend(
						msg + '<br />'
				)//end $("#msgList").append()
			}); 		
		},

		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			$('#progress .bar').css('width', progress + '%');
			if (!$('#progress .bar').is(":visible")) {
				$('#progress .bar').show();
			}
			if (progress==100) {
				$('#progress .bar').fadeOut("slow");
			}
		},
		dropZone: $('#dropzone')
	}).bind('fileuploadsubmit', function (e, data) {
		data.formData = {
				fileReplace: $('#fileReplace').is(':checked'), //include the Replace flag 
				rosterGroup: $("html").data("roster-group")    //  and rosterGroup as variables in upload
				};         
	});
	
	//add hover effects when dragging files over dropzone
	$(document).bind('dragover', function (e) {
	    var dropZone = $('#dropzone'),
	        timeout = window.dropZoneTimeout;
	    if (!timeout) {
	        dropZone.addClass('in');
	    } else {
	        clearTimeout(timeout);
	    }
	    var found = false,
	        node = e.target;
	    do {
	        if (node === dropZone[0]) {
	            found = true;
	            break;
	        }
	        node = node.parentNode;
	    } while (node != null);
	    if (found) {
	        dropZone.addClass('hover');
	    } else {
	        dropZone.removeClass('hover');
	    }
	    window.dropZoneTimeout = setTimeout(function () {
	        window.dropZoneTimeout = null;
	        dropZone.removeClass('in hover');
	    }, 100);
	});

}

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	jmri = $.JMRI({});
    getRosterTable($("html").data("roster-group"));
    initUploads();
    
    //listen for roster changes and refresh the roster table when this occurs
    //  by overriding processing of websocket messages of interest
    jmri = $.JMRI({
    	//wait for the hello message
    	hello: function(data) {
    		jmri.getList("roster"); // request updates to the roster via websocket 
    	},
    	//roster "add" and "remove" messages
    	roster: function(data) {
    		//jmri.log("in roster: data="+JSON.stringify(data).substr(0,180) + "...");
    	    getRosterTable($("html").data("roster-group"));
    	},
    	//received an updated rosterEntry, rebuild the entire roster table
    	rosterEntry: function(name, data) {
    		//jmri.log("in rosterEntry. name="+name+", data="+JSON.stringify(data).substr(0,180) + "...");
    	    getRosterTable($("html").data("roster-group"));
    	},
    });
});

