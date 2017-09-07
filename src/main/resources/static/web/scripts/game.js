//to be sent to the back end
var queryObj;
var myData;
var shipLength = 0;
var shipClass = "";
var shipClassElement;
var locationsArr = [];
var shipsArr = [];
var arr = [];
var salvo = [];
var link;
var refreshing;
var waiting;

$(function () {
	$("#submitlogout").click(logOut);
	$("#redirectToGames").click(toGames);
	$("#submitShots").click(sendSalvo);
	$("#submitShips").click(sendShips);
	$("#boardClear").click(clearTheBoard);

	//getting correct link
	queryObj = parseQueryObject();
	if (queryObj.hasOwnProperty("gp")) {
		var id = queryObj.gp;
		link = "/api/game_view/" + id;
		$.getJSON(link, onDataReady);
	}
});

function onDataReady(data) {
	//fun thing you can do with css vars
	//$(":root").css("--borderBottom","100px");
	
	myData = data;
	console.log(data);

	//hiding the enemy map until we submit the ships 
	$("#salvosMap").hide();
	$("#history").hide();
	$("#shootingControls").hide();

	renderTables(1);
	renderTables(2);
	var myMap = "#yourShipsMap";
	var otherMap = "#salvosMap";
	addBackgoundToMaps(myMap);
	addBackgoundToMaps(otherMap);
	
	renderShips(data, "#yourShipsMap");
	renderSalvos(data, "#salvosMap");
	renderSalvos(data, "#yourShipsMap");
	renderPlayerInfo(data);
	printHistoryTable(data);

	// page appearence
	// after reload if ships have been placed we enter here
	if (data.ships.length !== 0) {
		$("#salvosMap").show();
		$("#placingShipsControls").hide();
		$(".ship").hide();
		//$("#shooting").show();
		$("#shootingControls").show();
		$("#history").show();
	}

	$("td[data-length]").click(choseShip);
	$("td[data-location1]").mouseover(hoverHighlight);
	$("td[data-location1]").mouseleave(hoverUnhighlight);
	$("td[data-location1]").click(placeShipsOnTheMap);
	

	// turns logic
	turns(data);
	messages();

	if (waiting != true) {
		$("td[data-location2]").mouseover(shootHighlight);
		$("td[data-location2]").mouseleave(unhighlight);
		$("td[data-location2]").click(shootAdd);
	}
}

function addBackgoundToMaps(map) {
	//var $locationTop = $("#yourShipsMap").find("th");
	var $locationCenter = $(map).find("tr");
	var $topTds = $($locationCenter[1]).find("td");
	
	// drawing the first row after the alphabet
	$($topTds[1]).addClass("topLeft");
	for (var i = 2; i < $topTds.length -1; ++i) {
		$($topTds[i]).addClass("topCenter");
	}
	$($topTds[$topTds.length-1]).addClass("topRight");
	
	
	//drawing central rows
	for (var c = 2; c < $locationCenter.length -1; ++c) {
		var $tds = $($locationCenter[c]).find("td");
		$($tds[1]).addClass("middleLeft");
		for (var e = 2; e < $tds.length; ++e ) {
			$($tds[e]).addClass("middleCenter")
		}
		$($tds[$tds.length - 1]).addClass("middleRight");
	}
	
	
	//drawing bottom row
	var $lastRowTds = $($locationCenter[$locationCenter.length-1]).find("td");
	$($lastRowTds[1]).addClass("bottomLeft");
	for (var w = 2; w < $lastRowTds.length + 1; ++w) {
		$($lastRowTds[w]).addClass("bottomCenter");
	}
	$($lastRowTds[$lastRowTds.length-1]).addClass("bottomRight");
	
}

function messages() {
	if (myData.ships.length == 0) {
		$("#message").html("Place the ships on the map.");
		return true;
	}
	if (myData.enemyShipsPlaced == false) {
		$("#message").html("Waiting for the enemy to place ships.");
	}
}

function turns(data) {
	$("#submitShots").hide();
	// message here to let user kow he is waiting for the enemy to place ships
	// refreching for the first turn to get the enemy ships before firing
	if (data.enemyShipsPlaced == false && data.first == queryObj.gp && data.ships.length != 0) {
		setTimeout(function () {
			$.getJSON(link, onDataReady);
		}, 5000);
	}

	if (data.enemyShipsPlaced == true) {
		$("#submitShots").show();
		// here delete the wait message
	}

	// turn numbers for both players
	var myTurnNumber = 1;
	var enemyTurnNumber = 1;
	for (var gpid in data.salvos) {
		for (var key in data.salvos[gpid]) {
			if (queryObj.gp == gpid) {
				//+key turns a string with a number into an integer 
				myTurnNumber = +key + 1;
			} else {
				enemyTurnNumber = +key + 1;
			}
		}
	}

	// number of ships each player has left 
	var myShipsLeft;
	var enemyShipsLeft;
	for (var i = 0; i < data.history.length; ++i) {
		if (queryObj.gp == data.history[i].gpid) {
			for (var j = 0; j < data.history[i].action.length; ++j) {
				if (data.history[i].action[j].left <= enemyShipsLeft || enemyShipsLeft == undefined) {
					// how many ships i still have to sink of the enemy
					enemyShipsLeft = data.history[i].action[j].left;
				}
			}
		} else {
			for (var l = 0; l < data.history[i].action.length; ++l) {
				if (data.history[i].action[l].left <= myShipsLeft || myShipsLeft == undefined) {
					myShipsLeft = data.history[i].action[l].left;
				}
			}
		}
	}

	// changing/disabling buttons, depending on whose turn is it 
	if (myTurnNumber == enemyTurnNumber && data.first == queryObj.gp) {
		$("#submitShots").css("background-color", "green");
		$("#submitShots").html("End Turn");
		$("#submitShots").removeAttr("disabled");
		waiting = false;
		$("#message").html("It is your turn! Select 5 squares and click 'End Turn'. ");
	} else if (myTurnNumber < enemyTurnNumber) {
		$("#submitShots").css("background-color", "green");
		$("#submitShots").html("End Turn");
		$("#submitShots").removeAttr("disabled");
		waiting = false;
		$("#message").html("It is your turn! Select 5 squares and click 'End Turn'. ");
	} else {
		$("#submitShots").css("background-color", "yellow");
		$("#submitShots").html("Enemy Turn");
		$('#submitShots').attr("disabled", "disabled")
		$("#message").html("Wait for the enemy to finish his turn.");
		waiting = true;
		if (data.ships.length != 0) {
			setTimeout(function () {
				$.getJSON(link, onDataReady);
			}, 5000);
		}
	}

	// ending game
	if (enemyShipsLeft == 0 && enemyShipsLeft != undefined) {
		$("#message").html("You won!");
		$('#submitShots').hide();
	} else if (myShipsLeft == 0) {
		$("#message").html("You lost! :( ");
		$('#submitShots').hide();
	}

	var first;
	if (queryObj.gp == data.first) {
		first = true;
	} else {
		first = false;
	}

	console.log("My turn number: " + myTurnNumber);
	console.log("Enemy turn number: " + enemyTurnNumber);
	console.log("My ships left: " + myShipsLeft);
	console.log("Enemy ships left: " + enemyShipsLeft);
	console.log("Enemy ships placed: " + data.enemyShipsPlaced);
	console.log("First? " + first);
	console.log("Waiting? " + waiting);
}

function printHistoryTable(data) {
	var myOutput = "";
	var enemyOutput = "";
	$.each(data.history, function (index) {
		// each user in an arr history
		var obj = data.history[index];
		// this is me 
		if (queryObj.gp == obj.gpid) {
			for (var n = 0; n < obj.action.length; n++) {
				myOutput += Mustache.render($("#myTemplate").html(), obj.action[n]);
			}
			// this is the enemy user
		} else if (queryObj.gp != obj.gpid) {
			for (var i = 0; i < obj.action.length; i++) {
				enemyOutput += Mustache.render($("#myTemplate").html(), obj.action[i]);
			}
		}
	});
	// output both separately
	$("#output").html(myOutput);
	$("#output2").html(enemyOutput);
}

function shootHighlight() {
	if (salvo.length >= 5) {
		return false;
	}
	// this is so that it isnt possible to highlight the numbers on the left 
	if (!$(this).hasClass("fields")) {
		return false;
	}
	// remove coloring classes and clear arr on a new hover
	for (var i = 0; i < arr.length; i++) {
		arr[i].removeClass("toBomb");
		arr[i].removeClass("overlap");
	}
	//clearing the arr
	arr = [];
	// otherwise highlighting the current 
	var current = $(this).attr("data-location2");
	
	

	if (isPreviouslyShot(current)) {
		console.log("Has been shot before");
		return false;
	}

	$("td[data-location2='" + current + "']").addClass("toBomb");
	
	if ($("td[data-location2='" + current + "']").hasClass("bombed")) {
			$("td[data-location2='" + current + "']").removeClass("toBomb");
			$("td[data-location2='" + current + "']").addClass("bombed");
	}
	
	arr.push($("td[data-location2='" + current + "']"));
}

function unhighlight() {
	$(this).removeClass("toBomb");
}

function shootAdd() {
	// add here the logic to prevent selecting previous shots

	// arr to salvo arr
	var current = $(this).attr("data-location2");
	// this is so that it isnt possible to shoot the numbers on the left 
	if (!$(this).hasClass("fields")) {
		return false;
	}

	//removing from the salvo arr and highlight
	if ($(this).hasClass("bombed")) {
		$("td[data-location2='" + current + "']").removeClass("bombed");
		for (var i = 0; i < salvo.length; ++i) {
			if (salvo[i] == current) {
				salvo.splice(i, 1);
				return false
			}
		}
	}

	//dont add if they have 5 shots in a salvo
	if (salvo.length >= 5) {
		return false;
	}
	for (var key in myData.salvos) {
		if (myData.id == key) {
			// my shots
			var x = myData.salvos[key];
			for (var key2 in x) {
				// shots per turn 
				var oom = x[key2];
				for (var y = 0; y < oom.length; y++) {
					// every shot in a turn
					if (oom[y] == current) {
						console.log("You may not shoot previously shot locations");
						return false
					}
				}
			}
		}
	}
	if (isPreviouslyShot(current)) {
		console.log("Again, it has been shot");
		return false;
	}
	
	$("td[data-location2='" + current + "']").removeClass("toBomb");
	$("td[data-location2='" + current + "']").addClass("bombed");
	salvo.push(current);
}

function isPreviouslyShot(current) {
	for (var key in myData.salvos) {
		if (queryObj.gp == key) {
			for (var turn in myData.salvos[key]) {
				var t = myData.salvos[key];
				for (var i = 0; i < t[turn].length; ++i) {
					if (current == t[turn][i]) {
						return true;
					}
				}
			}
		}
	}
	return false;
}

function sendSalvo() {
	// making sure they submit 5 shots in a salvo
	if (salvo.length != 5) {
		console.log("First you need to select exactly 5 salvos");
		return false;
	}
	$.post({
		url: "/api/games/players/" + queryObj.gp + "/salvos",
		data: JSON.stringify(salvo),
		dataType: "json",
		contentType: "application/json"
	}).done(function () {
		location.reload();
		console.log("You have sent your salvo!");
		salvo = [];
	}).fail(function () {
		console.log("You failed sending salvo!");
	});
}

function toGames() {
	location.assign("/web/games.html");
}

function clearTheBoard() {
	shipLength = 0;
	shipClass = "";
	locationsArr = [];
	shipsArr = [];
	arr = [];
	$("td[data-location1]").removeClass("placed");
	$(".ship").removeClass("hide");
}

function hoverHighlight() {
	// this is so that it isnt possible to highlight the numbers on the left 
	if (!$(this).hasClass("fields")) {
		return false;
	}

	// remove coloring classes and clear arr on a new hover
	for (var i = 0; i < arr.length; i++) {
		arr[i].removeClass("highlight");
		arr[i].removeClass("overlap");
	}
	//clearing the arr
	arr = [];

	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
	var current = $(this).attr("data-location1");

	var currentLetter = current.split("");
	var currentNumber = current.slice(1);


	if (!$("#vertical").is(":checked")) {
		//here goes the horizontal logic
		for (var i = 0; i < alphabet.length; ++i) {
			if (alphabet[i] == currentLetter[0]) {
				var nextLetter = alphabet.slice(i);
				var test = [];
				for (var j = 0; j < shipLength; ++j) {
					var letter = nextLetter[j].slice(0, 1);
					var location = letter + currentNumber;
					$("td[data-location1='" + location + "']").addClass("highlight");
					// add to an arr to remember what to clear
					arr.push($("td[data-location1='" + location + "']"));
					// adding locations to test arr so we can paind whole ships red later if they go overedge
					test.push(location);
					if (isOverlap(location)) {
						// add to an arr to remember what to clear
						arr.push($("td[data-location1='" + location + "']").addClass("overlap"));
					}
				}
				// printing whole ships red if they go over the edge
				for (var u = 0; u < test.length; u++) {
					if (isOverEdge(test[u])) {
						for (var l = 0; l < test.length; l++) {
							$("td[data-location1='" + test[l] + "']").addClass("overlap");
						}
					}
				}
			}
		}
	} else if ($("#vertical").is(":checked")) {
		// here goes vertical logic
		var currentN = currentNumber;
		var test2 = [];
		var overedgearr = [];
		for (var m = 0; m < shipLength; m++) {
			// loc is the locations of the ship
			var loc = currentLetter[0] + currentN;
			// highlighting locations
			$("td[data-location1='" + loc + "']").addClass("highlight");
			// saving location to clear after
			arr.push($("td[data-location1='" + loc + "']"));
			// adding locations to test arr so we can paind whole ships red later if they go overedge
			test2.push(loc);
			//incrementing to get the next location for the whole length of the ship
			currentN++
			if (isOverlap(loc)) {
				// add to an arr to remember what to clear
				arr.push($("td[data-location1='" + loc + "']").addClass("overlap"));
			}
		}
		// printing whole ships red if they go over the edge
		for (var v = 0; v < test2.length; v++) {
			if (isOverEdgeVertical(test2[v])) {
				for (var g = 0; g < test2.length; g++) {
					$("td[data-location1='" + test2[g] + "']").addClass("overlap");
				}
			}
		}
	}
}

function hoverUnhighlight() {
	// remove coloring classes and clear arr on a new hover
	for (var i = 0; i < arr.length; i++) {
		arr[i].removeClass("highlight");
		arr[i].removeClass("overlap");
	}
	//clearing the arr
	arr = [];
}

function isOverlap(toCheck) {
	for (var i = 0; i < shipsArr.length; i++) {
		for (var j = 0; j < shipsArr[i].locations.length; j++) {
			if (shipsArr[i].locations[j] == toCheck) {
				return true;
			}
		}
	}
	return false
}

function isOverEdge(toCheck) {
	var alphabet = "KLMNOPQRSTUVWXYZ".split("");
	var letter = toCheck.split("");
	for (var i = 0; i < alphabet.length; ++i) {
		if (alphabet[i] == letter[0]) {
			return true;
		}
	}
	return false;
}

function isOverEdgeVertical(toCheck) {
	var numbers = ["11", "12", "13", "14", "15", "16"];
	var num = toCheck.slice(1);
	for (var i = 0; i < numbers.length; ++i) {
		if (numbers[i] == num) {
			return true;
		}
	}
	return false;
}

function isHit(data, toCheck){
	for (var i = 0; i<data.history.length; ++i) {
		if (data.history[i].gpid == queryObj.gp) {
			for (var w = 0; w< data.history[i].action.length; ++w) {
				for (var turn in data.history[i].action[w]) {
					var hits = data.history[i].action[w].hit;
					for (var k = 0; k<hits.length; ++k) {
						if (toCheck == hits[k]) {
							return true;	
						}
					}
				}
			}
		} else {
			for (var q = 0; q< data.history[i].action.length; ++q) {
				for (var turn in data.history[i].action[q]) {
					var hits2 = data.history[i].action[q].hit;
					for (var r = 0; r<hits2.length; ++r) {
						if (toCheck == hits2[r]) {
							return true;	
						}
					}
				}
			}
		}
	}
	return false;
}

function choseShip() {
	shipLength = $(this).attr("data-length");
	shipClass = $(this).parent().attr("id");
	shipClassElement = $(this).parent();
}

function placeShipsOnTheMap() {
	// making it not possible to place ships without selecting them first
	if (shipClass == "") {
		return false;
	}

	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
	//getting the start location
	var start = $(this).attr("data-location1");
	// getting the number and the letter of the selected start location
	var startLetter = start.split("");
	var startNumber = start.slice(1);

	if (!$("#vertical").is(":checked")) {
		// horisontal placement logic
		for (var i = 0; i < alphabet.length; ++i) {
			if (alphabet[i] == startLetter[0]) {
				var nextLetter = alphabet.slice(i);
				//clearing locations arr
				locationsArr = [];
				var cellsToBeColored = [];
				for (var j = 0; j < shipLength; ++j) {
					var letter = nextLetter[j].slice(0, 1);
					var location = (letter + startNumber);
					//if there's an overlap return false
					if (isOverlap(location)) {
						console.log("You may not place ships on top of one another");
						return false;
					} else if (isOverEdge(location)) {
						console.log("You may not place ships over the edge of the map");
						return false;
					} else {
						cellsToBeColored.push(location);
						//adding to locations arr(for ships obj)
						locationsArr.push(location);
					}
				}
				//painting ship locations
				for (var p = 0; p < cellsToBeColored.length; p++) {
					paintShipOnTheMap(cellsToBeColored[p]);
				}
				//hiding from selection
				shipClassElement.addClass("hide");
				// adding to ships obj
				addtoShipsObj();
				//clearing the highlight after a ship's been placed
				shipLength = 0;
				shipClass = "";
			}
		}
	} else if ($("#vertical").is(":checked")) {
		// vertical placement logic
		var currentN = startNumber;
		//clearing locations arr
		locationsArr = [];
		var toColor = [];
		for (var t = 0; t < shipLength; ++t) {
			var loc = startLetter[0] + currentN;
			if (isOverlap(loc)) {
				console.log("You may not place ships on top of one another");
				return false;
			} else if (isOverEdgeVertical(loc)) {
				console.log("You may not place ships over the edge of the map");
				return false;
			} else {
				toColor.push(loc);
				//adding to locations arr(for ships obj)
				locationsArr.push(loc);
			}
			currentN++;
		}
		//painting ship locations
		for (var n = 0; n < toColor.length; n++) {
			paintShipOnTheMap(toColor[n]);
		}
		//hiding from selection
		shipClassElement.addClass("hide");
		// adding to ships obj
		addtoShipsObj();
		//clearing the highlight after a ship's been placed
		shipLength = 0;
		shipClass = "";
	}
}

function addtoShipsObj() {
	//creating a ships obj and adding it to ships arr(that will be sent to back)
	var ship = {};
	ship.locations = locationsArr;
	ship.shipClass = shipClass;
	shipsArr.push({
		"locations": ship.locations,
		"shipClass": ship.shipClass
	});
	console.log("You have " + shipsArr.length + " ships at this moment.");
}

function paintShipOnTheMap(location) {
	$("td[data-location1='" + location + "']").addClass("placed");
}

function sendShips() {
	console.log("What you send is : " + shipsArr);
	console.log("You are sending this number of ships: " + shipsArr.length);
	if (shipsArr.length == 5) {
		$.post({
			url: "/api/games/players/" + queryObj.gp + "/ships",
			data: JSON.stringify(shipsArr),
			dataType: "json",
			contentType: "application/json"
		}).done(function () {
			location.reload();
			console.log("Success!");
		}).fail(function () {
			console.log("Fail!");
		});
	} else {
		alert("You must place all the ships on the board");
	}
}

function renderTables(str) {
	var size = 10;
	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
	var output = "";
	
	
	//top row ABC..
	output += "<tr>";
	output += "<th></th>"
	for (var i = 0; i < size +1 ; i++) {
		output += "<th>" + alphabet[i] + "</th>";
	}
	output += "<th></th>"
	output += "</tr>";
	
	output += "<tr>";
	output += "<td></td>"
	for (var t = 0; t < size +1 ; t++) {
		output += "<td></td>";
	}
	output += "<td></td>"
	output += "</tr>";
	
	
	var counter1 = 1;
	for (var j = 0; j < size; ++j) {
		output += "<tr>" + "<td data-location" + str + "=side>" + counter1 + "</td>";
		output +=	"<td></td>";
		for (var c = 0; c < alphabet.length && c < size; c++) {
			output += '<td class="fields" data-location' + str + '="' + alphabet[c + 1] + counter1 + '">' + " " + "</td>";
		}
		output += "<td></td>"
		output += "</tr>";
		counter1++;
	}
	
	output += "<tr>"
	for (var m = 0; m<size+3; ++m) {
		output += "<td></td>"
	}
	output += "</tr>"
	
	
	if (str == 1) {
		$("#yourShipsMap").html(output);
	} else {
		$("#salvosMap").html(output);
	}
}

function renderShips(data, tableSelector) {
	var $map = $(tableSelector).find(".fields");
	for (var i = 0; i < data.ships.length; ++i) {
		for (var k = 0; k < data.ships[i].locations.length; ++k) {
			for (var j = 0; j < $map.length; ++j) {
				var $field = $($map[j]);
				if (data.ships[i].locations[k] == $field.attr("data-location1")) {
					$($field).addClass("placed");
				}
			}
		}
	}
}

function renderPlayerInfo(data) {

	var youPlayer = "";
	var otherPlayer = "";

	for (var i = 0; i < data.game_players.length; ++i) {
		if (queryObj.gp == data.game_players[i].id) {
			youPlayer = data.game_players[i].player.email + " (You)";
		} else {
			otherPlayer += data.game_players[i].player.email + " (Enemy)";
		}
	}
	if (otherPlayer == ""){
		otherPlayer = "Your opponent hasn't joined the game yet";
	}
	$("#youPlayer").html(youPlayer);
	$("#otherPlayer").html(otherPlayer);
}

function renderSalvos(data, tableSelector) {
	for (var key in data.salvos) {
		//my shots on the enemy map
		if (key == queryObj.gp) {
			var $myMap = $("#salvosMap").find(".fields");
			var mySalvos = data.salvos[key];
			for (var turnKey in mySalvos) {
				// Arrays with shots
				var mySalvoTurn = mySalvos[turnKey];
				for (let i = 0; i < mySalvoTurn.length; ++i) {
					for (let j = 0; j < $myMap.length; ++j) {
						var $field = $($myMap[j]);
						if (mySalvoTurn[i] == $field.attr("data-location2")) {
							var toCheck = $field.attr("data-location2");
							if (isHit(data, toCheck)) {
								$field.addClass("bombedDuck");	
							} else {
									// to add a feature to turn turns in hits onn and off
									$field.html("<p class='notHit'>" + turnKey + "</p>");			 
							}
						}
					}
				}
			}
		}
		//enemy shots on my map
		if (key != queryObj.gp) {
			var $enemyMap = $("#yourShipsMap").find(".fields");
			var enemySalvos = data.salvos[key];
			for (var enemyTurnKey in enemySalvos) {
				// Arrays with shots
				var enemySalvoTurn = enemySalvos[enemyTurnKey];
				for (let i = 0; i < enemySalvoTurn.length; ++i) {
					for (let j = 0; j < $enemyMap.length; ++j) {
						var $enemyField = $($enemyMap[j]);
						if (enemySalvoTurn[i] == $enemyField.attr("data-location1")) {
							var toCheck2 = $enemyField.attr("data-location1");
								if (isHit(data, toCheck2)) {
								$($enemyField).addClass("bombedDuck");	
							} else {
								// to add a feature to turn turns in hits onn and off
								$enemyField.html("<p class='notHit'>" + enemyTurnKey + "</p>");			 
							}
						}
					}
				}
			}
		}
	}
}

function parseQueryObject() {
	// using substring to get a string from position 1
	var queryString = location.search.substring(1); /*"?gp=1&mp=23&sdfs=3rr"*/ ;
	var obj = {};
	// You can pass a regex into Javascript's split operator.
	var arr = queryString.split(/=|&/);
	if (queryString !== "") {
		arr.forEach(function (item, index) {
			if (index % 2 === 0) {
				obj[item] = arr[index + 1];
			}
		});
	}
	return obj;
}

function logOut() {
	$.post("/api/logout").done(function () {
		location.replace("/web/games.html");
	});
}
