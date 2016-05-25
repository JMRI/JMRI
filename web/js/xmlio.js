//
// XmlIO javascript class for simple XmlIO in JMRI
//

var $XmlIO = new function() {

    var railroad = null;
    var framesTbody = null;
    var panelsTbody = null;

    this.get = function(command, parameters, successHandler) {
        $.get("/xmlio/" + command, parameters, successHandler, "xml");
    }

    this.post = function(data, successHandler) {
        $.post("/xmlio/", data, successHandler, "xml");
    }

    this.getRailroad = function(element) {
        if (!railroad) {
            this.get("list", {
                type: "railroad"
            }, function rrSuccess(data, status, response) {
                railroad = $(data).find("railroad").attr("name");
                $(element).html(railroad);
            });
        } else {
            $(element).html(railroad);
        }
    }

    this.getFramesTbody = function(element) {
        if (!framesTbody) {
            this.get("list", {type: "frame"}, function frameSuccess(data, status, response) {
                $(data).find("frame").each(function(){
                    framesTbody += "<tr>";
                    framesTbody += "<td><a href=\"/frame/" + $(this).attr("name") + ".html\">" + $(this).attr("userName") + "</a></td>";
                    framesTbody += "<td><a href=\"/frame/" + $(this).attr("name") + ".html\"><img src=\"/frame/" + $(this).attr("name") + ".png\"></a></td>";
                    framesTbody += "</tr>";
                });
                $(element).html(framesTbody);
            });
        } else {
            $(element).html(framesTbody);
        }
    }

    this.getPanelsTbody = function(element) {
        if (!panelsTbody) {
            this.get("list", {type: "panel"}, function panelSuccess(data, status, response) {
                $(data).find("panel").each(function(){
                	var $t = $(this).attr("name").split("/");
                	panelsTbody += "<tr><td><a href='/panel?name=" + $(this).attr("name") + "'>" + $(this).attr("userName") + "</a></td>";
                	panelsTbody += "<td>" + $t[0] + "</td>";
                });
                $(element).html(panelsTbody);
            });
        } else {
            $(element).html(panelsTbody);
        }
    }

};
