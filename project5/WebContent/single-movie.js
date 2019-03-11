

/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
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

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");
    
    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<li class='list-group-item'>Movie Id: " + resultData[0]["m_id"] + "</li>" +
    	"<li class='list-group-item'>Movie Name: " + resultData[0]["m_title"] + "</li>" +
    	"<li class='list-group-item'>Movie Year: " + resultData[0]["m_year"] + "</li>" +
    	"<li class='list-group-item'>Director: " + resultData[0]["m_director"] + "</li>" +
    	"<li class='list-group-item'>Genres: " + resultData[0]["genreList"] + "</li>" +
    	"<li class='list-group-item'>Rating: " + resultData[0]["m_ratings"] + "</li>" +
    	"<li class='list-group-item'><a class='btn btn-outline-primary' href='shoppingCart.html?movie=" + resultData[0]["m_title"] + "'" + "role='button'>Add Cart</a></li>");
    
    

    let infoElement = jQuery("#info_m");
    infoElement.append("<br></br>" +
    		"<div class='row'><div class='col-lg-1'></div>" +
    		"<div class='col-lg-8'><h2>" + resultData[0]["m_title"] + "</h2></div>" +
    		"<div class='col-lg-2'><a class='btn btn-outline-primary' href='shoppingCart.html?movie=" + resultData[0]["m_title"] + "'" + " role='button'>Add Cart</a></div>" +
    		"</div>" +
    	    "<div class='row'><div class='col-lg-1'></div>" +
			"<div class='col-lg-3'><h4>Year</h4></div>" +
			"<div class='col-lg-6'><h4>" + resultData[0]["m_year"] + "</h4></div>" +
			"<div class='col-lg-2'></div>" +
			"</div>" +
    	    "<div class='row'><div class='col-lg-1'></div>" +
			"<div class='col-lg-3'><h4>Director</h4></div>" +
			"<div class='col-lg-6'><h4>" + resultData[0]["m_director"] + "</h4></div>" +
			"<div class='col-lg-2'></div>" +
			"</div>" +
    	    "<div class='row'><div class='col-lg-1'></div>" +
			"<div class='col-lg-3'><h4>Ratings</h4></div>" +
			"<div class='col-lg-6'><h4>" + resultData[0]["m_ratings"] + "</h4></div>" +
			"<div class='col-lg-2'></div>" +
			"</div>" +
			"<div class='row'><div class='col-lg-1'></div>" +
			"<div class='col-lg-3'><h4>Genres</h4></div>" +
			"<div class='col-lg-6'><h4>" + resultData[0]["genreList"] + "</h4></div>" +
			"<div class='col-lg-2'></div>" +
			"</div>");
 
    let rowHTML = "<div class='row'><div class='col-lg-1'></div><div class='col-lg-3'><h4>Stars</h4></div><div class='col-lg-6'><h5>";
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        rowHTML +=
            '<a href="single-star.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&id=' + resultData[i]['starId'] +'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+ '">'
            + resultData[i]["starName"] + ', ' + "</a>";

    }  
    rowHTML += "</h5></div><div class='col-lg-2'></div></div><br></br>";
    infoElement.append(rowHTML);
    

    
    //Go back to movies list
    let go_back = jQuery("#go_back");
    go_back.append('<a href="index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+'">'+'Movie List'+'</a>');
    //go_back.append('href="index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'"');
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');
let currentPage = parseInt(getParameterByName('p'));
let recordNum = getParameterByName('numRecord');
let genre = getParameterByName('genre');
let Title = getParameterByName('Title');
let Year = getParameterByName('Year');
let Director = getParameterByName('Director');
let Star_name = getParameterByName('Star_name');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    //url: "api/single-movie?id=" + movieId +"&p=" + currentPage + "&numRecord=" +recordNum,
    //url: "api/movies?p=" + currentPage + "&numRecord=" +recordNum+"&genre="+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"&autocom="+autocom,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

let store_qry={};

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")
	//console.log("sending AJAX request to backend Java Servlet")
	
	// TODO: if you want to check past query results first, you can do it here
	if(query in store_qry){
		console.log("Return query result in cache");
		handleLookupAjaxSuccess(store_qry[query], query, doneCallback);
		return;
	}
	
	// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
	// with the query data
	console.log("Return query result in Ajax");
	jQuery.ajax({
		"method": "GET",
		// generate the request url from the query.
		// escape the query string to avoid errors caused by special characters 
		"url": "main_page?query=" + escape(query),
		"success": function(data) {
			// pass the data, query, and doneCallback function into the success handler
			handleLookupAjaxSuccess(data, query, doneCallback) 
		},
		"error": function(errorData) {
			console.log("lookup ajax error")
			console.log(errorData)
		}
	})
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
	//console.log("lookup ajax successful");
	
	let data_sliced;
	for (let i=0; i<data.length; i++){
		if("value" in data[i]){
			data_sliced=data.slice(i);
			break;
		}
	}
	
	if(data_sliced == null) return;
	console.log("suggestion list");
	console.log(data_sliced);
	var jsonData = JSON.parse(JSON.stringify(data_sliced.slice(0,10)));

	// TODO: if you want to cache the result into a global variable you can do it here
	store_qry[query]=data;
	
	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}

/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
	// TODO: jump to the specific result page based on the selected suggestion
	console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movie_id"])
	//window.location='index.html?p=0&numRecord=20&genre=&Title='+suggestion["value"]+'&autocom=true';
	window.location='single-movie.html?Title='+suggestion["value"]+'&id='+suggestion["data"]["movie_id"];
}

//$('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,
});

/*
 * do normal full text search if no suggestion is selected 
 */
function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
	window.location='index.html?p=0&numRecord=20&genre=&Title='+query;
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})

//TODO: if you have a "search" button, you may want to bind the onClick event as well of that button