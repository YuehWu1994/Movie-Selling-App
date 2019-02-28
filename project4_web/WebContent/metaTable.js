

/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
$(document).ready(function() {
    $('body').hide().fadeIn(1000);
	$("a").click(function(e) {
	    e.preventDefault();
	    $link = $(this).attr("href");
	    $("body").fadeOut(1000,function(){
		    window.location =  $link; 
		});
    });
});


function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handlemetaDataResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    let currentPage = parseInt(getParameterByName('p'));
    let recordNum = parseInt(getParameterByName('numRecord'));
    let moviePage = Math.ceil(parseInt(resultData[0]['movieSize'])/recordNum);
    let genre=getParameterByName('genre');
    //console.log(moviePage);
    
    // Populate the star table
    let starTableBodyElement = jQuery("#tableName");
    for (let i = 1; i <= resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        let num=i;
        rowHTML += "<th>" + num + "</th>";
        rowHTML += "<th>" + resultData[i-1]["field"] + "</th>";
        rowHTML += "<th>" + resultData[i-1]["type"] + "</th>";
        rowHTML += "<th>" + resultData[i-1]["checkNull"] + "</th>";
        rowHTML += "<th>" + resultData[i-1]["key"] + "</th>";
        rowHTML += "<th>" + resultData[i-1]["checkDefault"] + "</th>";
        rowHTML += "<th>" + resultData[i-1]["extra"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let tableName = getParameterByName('t');

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/metaTable?t=" + tableName,
    success: (resultData) => handlemetaDataResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
