$(function () {
	$.getJSON("/api/games", onDataReady);
});

function onDataReady(data) {
	renderGames(data);
	renderLeaderboard(data);
}

function renderGames(data) {
	console.log(data);
		var output = "";
		for (var i = 0; i < data.games.length; i++) {
			var myDate = new Date(data.games[i].date);
			output += "<li>";
			output += "Game id: " + data.games[i].id + ". Creation date: " + myDate;
			for (var j = 0; j < data.games[i].gamePlayers.length; ++j) {
				output += "<br/>" +  " User email: " + data.games[i].gamePlayers[j].player.email + ". ";
				output += " User ID: " + data.games[i].gamePlayers[j].player.id + ". ";
			}
			output += "</li>";
		}
		$("#output").html(output);
}

function renderLeaderboard(data) {
	var arrTh = ["Name", "Total", "Won", "Lost", "Tied"];
	var output = "";
	output += "<tr>";
	for (var i = 0; i<arrTh.length; ++i) {
		output += "<th>" + arrTh[i] + "</th>";
	}
	output += "</tr>";
	for (var key in data.leaderboard) {
		output += "<tr>";
		output += "<td>" + key + "</td>";
		output += "<td>" + data.leaderboard[key].total + "</td>";
		output += "<td>" + data.leaderboard[key].won + "</td>";
		output += "<td>" + data.leaderboard[key].lost + "</td>";
		output += "<td>" + data.leaderboard[key].tied + "</td>";
		output += "</tr>";
	}
	$("#leaderBoard").html(output);
}