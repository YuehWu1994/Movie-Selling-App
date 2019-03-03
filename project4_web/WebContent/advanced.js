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
//let genre = getParameterByName('genre');

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
