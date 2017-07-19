$(function () {
	$.getJSON("/api/games", onDataReady);

	function onDataReady(serverData) {
		console.log(serverData);
		var output = "";
		for (var i = 0; i < serverData.length; i++) {
			var myDate = new Date(serverData[i].date);
			output += "<li>";
			output += "Game id: " + serverData[i].id + ". Creation date: " + myDate;
			for (var j = 0; j < serverData[i].gamePlayers.length; ++j) {
				output += "<br/>" +  " User email: " + serverData[i].gamePlayers[j].player.email + ". ";
				output += " User ID: " + serverData[i].gamePlayers[j].player.id + ". ";
			}
			output += "</li>";
		}
		$("#output").html(output);
	}

	function msToTime(s) {
		// Pad to 2 or 3 digits, default is 2
		var pad = (n, z = 2) => ('00' + n).slice(-z);
		return pad(s / 3.6e6 | 0) + ':' + pad((s % 3.6e6) / 6e4 | 0) + ':' + pad((s % 6e4) / 1000 | 0) + '.' + pad(s % 1000, 3);
	}


});
