$(function () {
	$("#submitlogout").click(logOutRedirect);
	//getting correct link
	var queryObj = parseQueryObject();
	if (queryObj.hasOwnProperty("gp")) {
		var id = queryObj.gp;
		var link = "/api/game_view/" + id;
		$.getJSON(link, onDataReady);
	}
});

function onDataReady(data) {
	console.log(data);
	renderTables(1);
	renderTables(2);
	renderShips(data, "#yourShipsMap");
	renderSalvos(data, "#salvosMap");
	renderSalvos(data, "#yourShipsMap");
	renderPlayerInfo(data);
	$("#salvosMap").hide();
	$("td[data-length]").click(choseShip);

	$("td[data-location1]").click(placeShipsOnTheMap);
	$("td[data-location1]").mouseover(hoverHighlight);
	$("#submitShips").click(sendShips);
	//$("#boardClear").click(clearTheBoard);
}

var shipLength = 0;
var shipClass = "";
var locationsArr = [];
var shipsArr = [];

var arr = [];

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
	for (var i = 0; i < arr.length; i++) {
		arr[i].removeClass("highlight");
	}
	arr = [];
	//$(this).css("background-color", "black");
	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
	var current = $(this).attr("data-location1");

	var currentLetter = current.split("");
	var currentNumber = current.slice(1);

	for (var i = 0; i < alphabet.length; ++i) {
		if (alphabet[i] == currentLetter[0]) {
			var nextLetter = alphabet.slice(i);
			for (var j = 0; j < shipLength; ++j) {
				var letter = nextLetter[j].slice(0, 1);
				$("td[data-location1='" + letter + currentNumber + "']").addClass("highlight");
				arr.push($("td[data-location1='" + letter + currentNumber + "']"));
			}
		}
	}
}

function choseShip() {
	shipLength = $(this).attr("data-length");
	shipClass = $(this).parent().attr("id");
	shipClassElement = $(this).parent();
	console.log("Ship length is: " + shipLength);
	console.log(shipClassElement);
}

function placeShipsOnTheMap() {

	var alphabet = ' ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

	//getting the start location
	var start = $(this).attr("data-location1");
	console.log();

	// getting the number and the letter of the selected start location
	var startLetter = start.split("");
	var startNumber = start.slice(1);

	// horisontal placement logic
	for (var i = 0; i < alphabet.length; ++i) {
		if (alphabet[i] == startLetter[0]) {
			var nextLetter = alphabet.slice(i);
			locationsArr = [];
			addtoShipsObj();
			shipClassElement.addClass("hide");
			for (var j = 0; j < shipLength; ++j) {
				var letter = nextLetter[j].slice(0, 1);
				var location = (letter + startNumber);
				paintShipOnTheMap(location);
				locationsArr.push(location);
			}
		}
	}
}

function paintShipOnTheMap(location) {
	$("td[data-location1='" + location + "']").addClass("placed");
}

function addtoShipsObj() {
	var ship = {};
	ship.locations = locationsArr;
	ship.shipClass = shipClass;
	shipsArr.push({
		"locations": ship.locations,
		"shipClass": ship.shipClass
	});
	console.log(shipsArr);
}

function test() {
	console.log("Test");
	console.log(shipsArr);
}

function sendShips() {
	var queryObj = parseQueryObject();
	console.log("api/games/players/" + queryObj.gp + "/ships");
	console.log(shipsArr);

	$.post({
		url: "/api/games/players/" + queryObj.gp + "/ships",
		data: JSON.stringify(shipsArr),
		dataType: "json",
		contentType: "application/json"
	}).done(function () {
		$("#salvosMap").show();
		$("#submitShips").hide();
		$("#boardClear").hide();
		$("#message").html("Prepare for battle");
		
		console.log("Success!");
	}).fail(function () {
		console.log("Fail!");
	});
}

function getGamePlayerId() {
	var queryObj = parseQueryObject();
	if (queryObj.hasOwnProperty("gp")) {
		return queryObj.gp;
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
				if (data.ships[i].locations[k] == $field.attr("data-location")) {
					$field.css("background-color", "green");
				}
			}
		}
	}
}

function renderPlayerInfo(data) {

	var youPlayer = "";
	var otherPlayer = "";
	var queryObj = parseQueryObject();

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
	var queryObj = parseQueryObject();
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
						if (mySalvoTurn[i] == $field.attr("data-location")) {
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
						if (enemySalvoTurn[i] == $enemyField.attr("data-location")) {
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
