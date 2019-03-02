function handleInfoResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle transaction response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
    	console.log("Add Star Success");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
    }
    $("#add_message").text(resultDataJson["message"]);
}

function handleAddStar(cartEvent) {
    console.log("submit Transactional Info");
    cartEvent.preventDefault();

    $.get(
        "api/addStar",
        // Serialize the cart form to the data sent by POST request
        $("#addStar").serialize(),
        (resultDataString) => handleInfoResult(resultDataString)
    );
}

function handleMovieResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle transaction response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
    	console.log("Add Star Success");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
    }
    $("#add_message2").text(resultDataJson["message"]);
}

function handleAddMovie(cartEvent) {
    console.log("submit Transactional Info");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.get(
        "api/addMovie",
        // Serialize the cart form to the data sent by POST request
        $("#addMovie").serialize(),
        (resultDataString) => handleMovieResult(resultDataString)
    );
}

$("#addStar").submit((event) => handleAddStar(event));
$("#addMovie").submit((event) => handleAddMovie(event));