/*
 * Common JavaScript functionality for BootStrap-based servlets.
 */

var minFontSize = parseInt(10);
var maxFontSize = parseInt(48);
var nbJmri = null;

/*
 * Populate the panels menu with a list of open panels
 */
function getPanels() {
    $.ajax({
        url: "/panel?format=json",
        data: {},
        success: function (data) {
            if (!Array.isArray(data)) {
                data = [data];
            }
            $(".navbar-panel-item").remove();
            if (data.length !== 0) {
                $("#empty-panel-list").addClass("hidden").removeClass("show");
                $.each(data, function (index, value) {
                    $("#navbar-panels").append("<li class=\"navbar-panel-item\"><a href=\"/panel/" + value.data.name + "\">" + value.data.userName + "</a></li>");
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
        success: function (data) {
            if (!Array.isArray(data)) {
                data = [data];
            }
            $(".navbar-roster-group-item").remove();
            if (data.length !== 0) {
                $.each(data, function (index, value) {
                    $("#navbar-roster-groups").append("<li class=\"navbar-roster-group-item\"><a href=\"/roster/group/" + encodeURIComponent(value.data.name) + "\"><span class=\"badge pull-right\">" + value.data.length + "</span>" + value.data.name + "</a></li>");
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
        success: function (data) {
            if (!Array.isArray(data)) {
                data = [data];
            }
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
                $.each(data, function (index, value) {
                    var service = value.data.type.split(".")[0];
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
    selector.each(function () {
        thisHeight = $(this).height();
        if (thisHeight > tallest) {
            tallest = thisHeight;
        }
    });
    selector.each(function () {
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
        $("#font-size-smaller").click(function () {
            setFontSize(-1);
            return false;
        });
        $("#font-size-larger").click(function () {
            setFontSize(1);
            return false;
        });
    } else if (change === 1 && size < maxFontSize) {
        size++;
        $("#font-size-smaller").parent().removeClass("disabled");
    } else if (change === -1 && size > minFontSize) {
        size--;
        $("#font-size-larger").parent().removeClass("disabled");
    } else if (change === 14) {
        size = 14;
        $("#font-size-smaller").parent().removeClass("disabled");
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
 * Allow users to fix the navbar to the browser window. First use after page
 * loads should pass a null value as its parameter to read either localStorage
 * or page parameters.
 *
 * The parameter jmri.css.navbar.fixed=0 can be appended to the URL to set the
 * default value to false if it has not already been set in localStorage.
 *
 * @param {type} fixed true if navbar should be fixed
 * @returns {undefined}
 */
function setNavbarFixed(fixed) {
    if (fixed === null) {
        if (window.localStorage.getItem("jmri.css.navbar.fixed") !== null) {
            fixed = (parseInt(window.localStorage.getItem("jmri.css.navbar.fixed")) === 1);
        }
        $("#navbar-fixed-position").change(function () {
            setNavbarFixed($(this).prop("checked"));
        });
    }
    if (fixed === null) {
        if (getParameterByName("jmri.css.navbar.fixed") !== null) {
            fixed = (parseInt(getParameterByName("jmri.css.navbar.fixed")) === 1);
        } else {
            fixed = true;
        }
    }
    if (fixed === true) {
        $(".navbar").removeClass("navbar-static-top").addClass("navbar-fixed-top");
        $("body").css("padding-top", "50px");
    } else {
        $(".navbar").removeClass("navbar-fixed-top").addClass("navbar-static-top");
        $("body").css("padding-top", "0px");
    }
    $("#navbar-fixed-position").prop("checked", (fixed === true) ? "checked" : "");
    window.localStorage.setItem("jmri.css.navbar.fixed", ((fixed === true) ? 1 : 0));
    if (window.console) {
        console.log("Navbar is " + ((fixed === true) ? "fixed" : "floating"));
    }
}

/*
 * Define localStorage for the retention of data if not already defined.
 * From https://developer.mozilla.org/en-US/docs/Web/Guide/API/DOM/Storage#localStorage
 */
if (!window.localStorage) {
    Object.defineProperty(window, "localStorage", new (function () {
        var aKeys = [], oStorage = {};
        Object.defineProperty(oStorage, "getItem", {
            value: function (sKey) {
                return sKey ? this[sKey] : null;
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "key", {
            value: function (nKeyId) {
                return aKeys[nKeyId];
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "setItem", {
            value: function (sKey, sValue) {
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
            get: function () {
                return aKeys.length;
            },
            configurable: false,
            enumerable: false
        });
        Object.defineProperty(oStorage, "removeItem", {
            value: function (sKey) {
                if (!sKey) {
                    return;
                }
                document.cookie = escape(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
            },
            writable: false,
            configurable: false,
            enumerable: false
        });
        this.get = function () {
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
$(document).ready(function () {
    getNetworkServices(); // hide or show and network service specific elements
    getPanels(); // complete the panels menu
    getRosterGroups(); // list roster groups in menu
    setFontSize(0); // get the user's prefered font size
    setNavbarFixed(null); // make the navbar floating or fixed
    $("#reset-local-preferences").click(function () {
        window.localStorage.removeItem("jmri.css.navbar.fixed"); // remove user preference
        setNavbarFixed(null); // reset to default or passed in parameter
        setFontSize(14); // reset to default
        return false;
    });
    nbJmri = $.JMRI({
        open: function () {
            nbJmri.getPower();
        },
    	hello: function(data) {
    		nbJmri.getList("rosterGroups"); // request updates to the rosterGroups via websocket 
        },
        rosterGroups: function (name, data) {
            getRosterGroups(); // refresh the roster groups in menu
        },
        power: function (state) {
            $('#navbar-power').data('power', state);
            disabled = ($('#navbar-power button').hasClass('disabled')) ? " - Setting power disabled" : "";
            switch (state) {
                case nbJmri.UNKNOWN:
                    $('#navbar-power button').addClass('btn-warning').removeClass('btn-success').removeClass('btn-danger');
                    $('#navbar-power button').prop('title', "Layout power unknown" + disabled);
                    $('#navbar-power a').text("Layout Power Unknown");
                    break;
                case nbJmri.POWER_ON:
                    $('#navbar-power button').addClass('btn-success').removeClass('btn-danger').removeClass('btn-warning');
                    $('#navbar-power button').prop('title', "Layout power on" + disabled);
                    $('#navbar-power a').text("Layout Power On");
                    break;
                case nbJmri.POWER_OFF:
                    $('#navbar-power button').addClass('btn-danger').removeClass('btn-success').removeClass('btn-warning');
                    $('#navbar-power button').prop('title', "Layout power off" + disabled);
                    $('#navbar-power a').text("Layout Power Off");
                    break;
            }
            $('#navbar-power-modal-label').text($('#navbar-power a').text());
        }
    });
    nbJmri.connect();
    if ($('#navbar-power').data('power') === 'readwrite') {
        $('#navbar-power').removeClass('disabled');
        $('#navbar-power button').removeClass('disabled');
        $('#navbar-power button').click(function (event) {
            $('#navbar-power-modal').modal('show');
        });
        $('#navbar-power-modal-on').click(function (event) {
            nbJmri.setPower(nbJmri.POWER_ON);
            $('#navbar-power-modal').modal('hide');
        });
        $('#navbar-power-modal-off').click(function (event) {
            nbJmri.setPower(nbJmri.POWER_OFF);
            $('#navbar-power-modal').modal('hide');
        });
        $('#navbar-power a').click(function (event) {
            $('#navbar-power-modal').modal('show');
        });
        $('#navbar-power-modal').on('show.bs.modal', function () {
            $('#navbar-power-modal-label').text($('#navbar-power a').text());
            $('.navbar-collapse').collapse('hide');
            $('#navbar-power-modal').css("z-index", "1500");
        });
    }
});
