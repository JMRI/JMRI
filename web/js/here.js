/*
Highlight the li element with href link to the current page in the left sidebar
Since Nov 2019
*/

document.documentElement.className="hasJS";

$(function(){
var $page = window.location.href;
  $('a').each(function() {
    if ($(this).prop('href') == $page) {
      $(this.parentNode).addClass('here');
    }
  });
});
