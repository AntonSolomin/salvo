$(function () {
	//getting correct link
	var queryObj = parseQueryObject();

	if (queryObj.hasOwnProperty("gp")) {
		var id = queryObj.gp;
		var link = "/api/game_view/" + id;
		//

		$.getJSON(link, onDataReady);
	}
});


function onDataReady(data) {
	renderTable();
	renderShips(data);
	renderPlayerInfo(data);
}

function renderTable() {

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
		output += "<tr>" + "<td data-location=side>" + counter1 + "</td>";
		for (var c = 0; c < alphabet.length && c < 10; c++) {
			output += '<td class="fields" data-location="' + alphabet[c + 1] + counter1 + '">' + " " + "</td>";
		}
		output += "</tr>";
		counter1++;
	}
	$("#yourShipMap").html(output);
}

function renderShips(data) {
	var $map = $(".fields");
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
	console.log(data);
	var youPlayer = "";
	var otherPlayer = "";
	var queryObj = parseQueryObject();

	for (var i = 0; i < data.game_players.length; ++i) {
		if (youPlayer === "") {
			youPlayer += data.game_players[i].player.email;
			if (data.game_players[i].id == queryObj.gp) {
				youPlayer += " (You)";
			}
		}
		else {
			otherPlayer += data.game_players[i].player.email;
			if (data.game_players[i].id == queryObj.gp) {
				otherPlayer += " (You) ";
			}
		}
	}
	
	$("#gameInfo").html(youPlayer + " <--VS--> " + otherPlayer);
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
