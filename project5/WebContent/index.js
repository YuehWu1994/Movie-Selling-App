

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

function visitPage(type){
	//window.location='index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&sort='+type;
	window.location='index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+'&sort='+type;
}

function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    let currentPage = parseInt(getParameterByName('p'));
    let recordNum = parseInt(getParameterByName('numRecord'));
    let moviePage = Math.ceil(parseInt(resultData[0]['movieSize'])/recordNum);
    let genre=getParameterByName('genre');
    //console.log(moviePage);
    
    // Populate the star table
    let starTableBodyElement = jQuery("#movie_table_body");
    for (let i = 1; i <= Math.min(recordNum, resultData.length-1); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        let num=i;
        rowHTML += "<th>" + num + "</th>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&id=' + resultData[i]['movie_id'] +'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+ '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        //rowHTML += "<th>" + resultData[i]["genreList"] + "</th>";
        
        //For genres list
        let genreList=[];
        if(resultData[i]["genreList"] != null){
        	genreList=resultData[i]["genreList"].split(",");
        }
        rowHTML += "<th>";
        for(let j=0; j<genreList.length; j++){
        	rowHTML+=('<a href="index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+ genreList[j] + '">'
                    + genreList[j] +     // display movie_name for the link text
                    '</a>'
        	);
        	rowHTML+= ",";
        }
        //rowHTML = rowHTML.substring(0, rowHTML.length-1);
        //rowHTML +="null";
        rowHTML += "</th>";
        
        //For stars list
        let stars_name = resultData[i]["stars_name"].split(",");
        let stars_id = resultData[i]["stars_id"].split(",");
        rowHTML += "<th>";
        for (let j=0; j<stars_name.length-1; j++){
        	rowHTML+=('<a href="single-star.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&id=' + stars_id[j] + '">'
                    + stars_name[j] +     // display movie_name for the link text
                    '</a>'
        	);
        	rowHTML+= ",";
        }
        //rowHTML = rowHTML.substring(0, rowHTML.length-1);
        rowHTML += "</th>";
        
        //For rating
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";    
        
        //For add cart
        rowHTML += "<th><a class='btn btn-outline-primary' href='shoppingCart.html?movie=" + resultData[i]["movie_title"] + "'" + "role='button'>Add Cart</a></th>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
    
    
    let paginationElement = jQuery("#pagination_list");
    let rowHTML = "<ul class='pagination justify-content-center'>";
    if(currentPage-1 >= 0) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage-1) + '&numRecord=' + recordNum +'&genre='+genre+ "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + 'Previous' + "</a></li>";
    for (let i = currentPage-3; i <= currentPage+3; i++) {
    	if(i < 0 || i >= moviePage) continue;
    	if(i == currentPage) rowHTML += "<li class='page-item active'>";
    	else rowHTML += "<li class='page-item'>";
    	rowHTML += "<a class='page-link' href='?p="+ i + '&numRecord=' + recordNum +'&genre='+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + (i+1) + "</a></li>";
    }
    if(currentPage+1 < moviePage) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage+1) + '&numRecord=' + recordNum +'&genre='+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + 'Next' + "</a></li>"+ "</ul>";
    rowHTML += "</ul>";
    paginationElement.append(rowHTML);
    

    
    
    let movieperPageBtn = jQuery("#movieperPageBtn");
    movieperPageBtn.append("<button class='btn btn-secondary dropdown-toggle btn-sm' type='button' id='dropdownMenuButton' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>"
    		+ "Movie per Page" + "</button>"
    		+ "<div class='dropdown-menu' aria-labelledby='dropdownMenuButton'>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/10) + '&numRecord=' + 10 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "10" + "</a>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/20) + '&numRecord=' + 20 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "20" + "</a>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/40) + '&numRecord=' + 40 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "40" + "</a>"+ "</div>");
    
    
    let sort_button = jQuery("#sort_button");
    //sort_button.append("<button onclick='location.href='http://www.example.com'' type='button'> test &#9650 </button>")
    
    movieperPageBtn.append(
    		 "<button onclick="+"visitPage('title_up')"+" type='button'> Title &#9650 </button>"
    		+"<button onclick="+"visitPage('title_down')"+" type='button'> Title &#9660 </button>"
            +"<button onclick="+"visitPage('rating_up')"+" type='button'> Rating &#9650 </button>"
            +"<button onclick="+"visitPage('rating_down')"+" type='button'> Rating &#9660 </button>"		
    );
    
}


function handleAddcart(addCardEvent){
	addCardEvent.preventDefault();
	console.log("hello");
//    $.get(
//        "api/shoppingCart",
//        // Serialize the cart form to the data sent by POST request
//        {removeMovie: $("#cart tbody tr td:nth-child(1) input").val()},
//        (resultDataString) => handleCartArray(resultDataString)
//    );
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let currentPage = parseInt(getParameterByName('p'));
let recordNum = getParameterByName('numRecord');
let genre = getParameterByName('genre');
let Title = getParameterByName('Title');
let Year = getParameterByName('Year');
let Director = getParameterByName('Director');
let Star_name = getParameterByName('Star_name');
let Sort_type = getParameterByName('sort');
let autocom = getParameterByName('autocom');

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?p=" + currentPage + "&numRecord=" +recordNum+"&genre="+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"&autocom="+autocom, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

//Automatic complete
let store_qry={};

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated, query is: ", query)
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

