/*
Hilights the current page (hyperlink) in the left sidebar
Egbert Broerse 2019
*/

document.documentElement.className="hasJS";

$(function(){
    var $page = jQuery.url.attr("file");
    $('ul.side li a').each(function(){
        var $href = $(this).attr('href');
        if ( ($href == $page) || ($href == '') ) {
            $(this).addClass('here');
        } else {
            $(this).removeClass('here');
        }
    });
});

