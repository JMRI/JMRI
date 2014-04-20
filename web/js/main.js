/*
 * Common JavaScript functionality for BootStrap-based servlets.
 */

var minFontSize = parseInt(12);
var maxFontSize = parseInt(20);

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

/*
 * Increment/Decrement the text size
 */
function setFontSize(change) {
    change = parseInt(change);
    size = parseInt($("body").css("fontSize").replace("px", "").trim());
    if (change === 0) {
        if (window.localStorage.getItem("jmri.css.font-size.body")) {
            size = parseInt(window.localStorage.getItem("jmri.css.font-size.body"));
        }
        $("#font-size-smaller").click(function() {
            setFontSize(-1);
            return false;
        });
        $("#font-size-larger").click(function() {
            setFontSize(1);
            return false;
        });
    } else if (change === 1 && size < maxFontSize) {
        size++;
        $("#font-size-smaller").parent().removeClass("disabled");
    } else if (change === -1 && size > minFontSize) {
        size--;
        $("#font-size-larger").parent().removeClass("disabled");
    }
    $("body").css("fontSize", size + "px");
    window.localStorage.setItem("jmri.css.font-size.body", size);
    $("#font-size-value").text(size + "px");
    if (size >= maxFontSize) {
        $("#font-size-larger").parent().addClass("disabled");
    }
    if (size <= minFontSize) {
        $("#font-size-smaller").parent().addClass("disabled");
    }
    if (window.console) {
        console.log("Body font size is " + $("body").css("fontSize"));
    }
}

/*
 * Define localStorage for the retention of data if not already defined.
 * From https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Storage#localStorage
 */
if (!window.localStorage) {
    Object.defineProperty(window, "localStorage", new (function() {
        var aKeys = [], oStorage = {};
        Object.defineProperty(oStorage, "getItem", {
            value: function(sKey) {
                return sKey ? this[sKey] : null;
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "key", {
            value: function(nKeyId) {
                return aKeys[nKeyId];
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "setItem", {
            value: function(sKey, sValue) {
                if (!sKey) {
                    return;
                }
                document.cookie = escape(sKey) + "=" + escape(sValue) + "; expires=Tue, 19 Jan 2038 03:14:07 GMT; path=/";
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "length", {
            get: function() {
                return aKeys.length;
            },
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "removeItem", {
            value: function(sKey) {
                if (!sKey) {
                    return;
                }
                document.cookie = escape(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        this.get = function() {
            var iThisIndx;
            for (var sKey in oStorage) {
                iThisIndx = aKeys.indexOf(sKey);
                if (iThisIndx === -1) {
                    oStorage.setItem(sKey, oStorage[sKey]);
                }
                else {
                    aKeys.splice(iThisIndx, 1);
                }
                delete oStorage[sKey];
            }
            for (aKeys; aKeys.length > 0; aKeys.splice(0, 1)) {
                oStorage.removeItem(aKeys[0]);
            }
            for (var aCouple, iKey, nIdx = 0, aCouples = document.cookie.split(/\s*;\s*/); nIdx < aCouples.length; nIdx++) {
                aCouple = aCouples[nIdx].split(/\s*=\s*/);
                if (aCouple.length > 1) {
                    oStorage[iKey = unescape(aCouple[0])] = unescape(aCouple[1]);
                    aKeys.push(iKey);
                }
            }
            return oStorage;
        };
        this.configurable = false;
        this.enumerable = true;
    })());
}
//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
// perform tasks that all BootStrap-based servlets need
$(document).ready(function() {
    getNetworkServices(); // hide or show and network service specific elements
    getPanels(); // complete the panels menu
    getRosterGroups(); // list roster groups in menu
    setFontSize(0);
});
