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


$(function () {
	$("#submitlogout").click(logOutRedirect);
	$("#redirectToGames").click(toGames);
	//getting correct link
	queryObj = parseQueryObject();
	if (queryObj.hasOwnProperty("gp")) {
		var id = queryObj.gp;
		var link = "/api/game_view/" + id;
		$.getJSON(link, onDataReady);
	}
});


var test = {
	"history": [
		{
			"gpid": 5,
			"action": [
				{
					"turn": 1,
					"sunk": ["BATTLESHIP", "ELSE"],
					"hit": ["A2", "A3", "A4"],
					"left": 3
			},
				{
					"turn": 2,
					"sunk": ["GKOGE", "GENOGE"],
					"hit": ["C2", "C3", "C4"],
					"left": 4
			}
			],
		},
		{
			"gpid": 99,
			"action": [
				{
					"turn": 1,
					"sunk": ["FIRST", "GEGE"],
					"hit": ["N2", "N3", "N4"],
					"left": 5
			},
				{
					"turn": 2,
					"sunk": ["KKKKKK", "LLLLLLL"],
					"hit": ["F2", "F3", "F4"],
					"left": 4
			}
			]

		}
	]
};

function onDataReady(data) {
	//hiding the enemy map until we submit the ships 
	$("#salvosMap").hide();
	myData = data;

	console.log(data);
	renderTables(1);
	renderTables(2);
	renderShips(data, "#yourShipsMap");
	renderSalvos(data, "#salvosMap");
	renderSalvos(data, "#yourShipsMap");
	renderPlayerInfo(data);
	printHistoryTable(data);

	//after reload if ships have been placed we enter here
	if (data.ships.length !== 0) {
		$("#salvosMap").show();
		$("#submitShips").hide();
		$("#boardClear").hide();
		$(".ship").hide();
		$("#message").html("Prepare for battle");
		$("#shooting").show();
		$("td[data-location2]").mouseover(shootHighlight);
		$("td[data-location2]").click(shootAdd);
		$("#submitShots").click(sendSalvo);
	}

	$("td[data-length]").click(choseShip);
	$("td[data-location1]").click(placeShipsOnTheMap);
	$("td[data-location1]").mouseover(hoverHighlight);
	$("#submitShips").click(sendShips);
	$("#boardClear").click(clearTheBoard);
}



// to print history table
//test if this prints sample data correctly
function printHistoryTable(data) {
	//var queryObj = parseQueryObject();
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
		arr[i].removeClass("highlight");
		arr[i].removeClass("overlap");
	}
	//clearing the arr
	arr = [];
	// otherwise highlighting the current 
	var current = $(this).attr("data-location2");
	$("td[data-location2='" + current + "']").addClass("highlight");
	arr.push($("td[data-location2='" + current + "']"));
}

function shootAdd() {
	// arr to salvo arr
	var current = $(this).attr("data-location2");

	// this is so that it isnt possible to shoot the numbers on the left 
	if (!$(this).hasClass("fields")) {
		return false;
	}

	//removing from the salvo arr and highlight
	if ($(this).hasClass("shot")) {
		$("td[data-location2='" + current + "']").removeClass("shot");

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

	$("td[data-location2='" + current + "']").addClass("shot");
	salvo.push(current);
}

function sendSalvo() {
	//var queryObj = parseQueryObject();
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
				console.log("Paint!");
				for (var g = 0; g < test2.length; g++) {

					$("td[data-location1='" + test2[g] + "']").addClass("overlap");
				}
			}
		}
	}
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

function choseShip() {
	shipLength = $(this).attr("data-length");
	shipClass = $(this).parent().attr("id");
	shipClassElement = $(this).parent();
}

function placeShipsOnTheMap() {

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
	if (!isOverlap(location)) {
		$("td[data-location1='" + location + "']").addClass("placed");
	}
	if (!isOverEdgeVertical(location)) {
		$("td[data-location1='" + location + "']").addClass("placed");
	}
}

function sendShips() {
	console.log("What you send is : " + shipsArr);
	//var queryObj = parseQueryObject();
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
	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
	var output = "";
	//top row ABC..
	output += "<tr>";
	for (var i = 0; i < alphabet.length && i < 11; i++) {
		output += "<th>" + alphabet[i] + "</th>";
	}
	output += "</tr>";
	var counter1 = 1;
	for (var j = 0; j < 10; ++j) {
		output += "<tr>" + "<td data-location" + str + "=side>" + counter1 + "</td>";
		for (var c = 0; c < alphabet.length && c < 10; c++) {
			output += '<td class="fields" data-location' + str + '="' + alphabet[c + 1] + counter1 + '">' + " " + "</td>";
		}
		output += "</tr>";
		counter1++;
	}
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
					$field.css("background-color", "green");
				}
			}
		}
	}
}

function renderPlayerInfo(data) {

	var youPlayer = "";
	var otherPlayer = "";
	//var queryObj = parseQueryObject();

	for (var i = 0; i < data.game_players.length; ++i) {
		if (youPlayer === "") {
			youPlayer += data.game_players[i].player.email;
			if (data.game_players[i].id == queryObj.gp) {
				youPlayer += " (You)";
			}
		} else {
			otherPlayer += data.game_players[i].player.email;
			if (data.game_players[i].id == queryObj.gp) {
				otherPlayer += " (You) ";
			}
		}
	}
	$("#gameInfo").html(youPlayer + " <--VS--> " + otherPlayer);
}

function renderSalvos(data, tableSelector) {
	//var queryObj = parseQueryObject();
	for (var key in data.salvos) {
		//my shots on the enemy map
		if (key == queryObj.gp) {
			var $myMap = $("#salvosMap").find(".fields");
			var mySalvos = data.salvos[key];
			for (var turnKey in mySalvos) {
				// Arrays with shots
				var mySalvoTurn = mySalvos[turnKey];
				for (var i = 0; i < mySalvoTurn.length; ++i) {
					for (var j = 0; j < $myMap.length; ++j) {
						var $field = $($myMap[j]);
						if (mySalvoTurn[i] == $field.attr("data-location2")) {
							$field.html("<p class='hit'>" + turnKey + "</p>");
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
				for (var i = 0; i < enemySalvoTurn.length; ++i) {
					for (var j = 0; j < $enemyMap.length; ++j) {
						var $enemyField = $($enemyMap[j]);
						if (enemySalvoTurn[i] == $enemyField.attr("data-location1")) {
							$enemyField.html("<p class='hit'>" + enemyTurnKey + "</p>");
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

function logOutRedirect() {
	$.post("/api/logout").done(function () {
		location.replace("/web/games.html");
	});
}