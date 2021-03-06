$(function () {
	$("#submit").click(logIn);
	$("#submitlogout").click(logOut);
	$("#register").hide();
	$("#newUser").click(displayRegisterForm);
	$("#back").click(displayBackToLogIn);
	$("#register").click(registerUser);
	$("#newGame").click(newGame);
	$("#login").css("display", "none");
	$.getJSON("/api/games", onDataReady).fail(function () {
		$("#login").show();
	});
});

function onDataReady(data) {
	$("#submitlogout").hide();
	$("#newGame").hide();

	console.log(data);
	displayGreetings(data);
	renderGames(data);
	
	$(".joinGame").click(joinGame);
	$(".backGame").click(backToGame);
	renderLeaderboard(data);
	$(".joinGame").hide();
	$("#games").attr('class', 'col-sm-5');
	$("#leaderBoard").attr('class', 'col-sm-4');

	if (data.user != "unidentified user") {
		$("#submitlogout").show();
		$("#newGame").show();
		$(".joinGame").show();
		$("#games").attr('class', 'col-sm-6');
		$("#leaderBoard").attr('class', 'col-sm-6');
	};
	// keep refreshing to see new games
	setTimeout(function () {$.getJSON("/api/games", onDataReady);}, 5000);
}

function newGame() {
	$.post("/api/games").done(function (data) {
		location.assign("/web/game.html?gp=" + data.gpid);
	});
}

function joinGame() {
	$.post("/api/games/" + $(this).attr("data-game-id") + "/players").done(function (data) {
		location.assign("/web/game.html?gp=" + data.gpid);
	}).fail(function (data) {
		//parsing response text and alerting if something is wrong
		var obj = JSON.parse(data.responseText);
		alert(obj.error);
	});
}

function backToGame() {
	window.location = '/web/game.html?gp=' + $(this).attr("data-gamePlayer-id");
}

function renderGames(data) {
	var output = "";

	for (var i = 0; i < data.games.length; i++) {
		var myDate = new Date(data.games[i].date);
		// showing only the games that have not ended
		if (data.games[i].isFinished == false) {
			output += "<tr>";
			output += "<td>";
			output += "<a" + addIdToLink(data.user, data.games[i]) + ">";
			output += myDate.toDateString();
			output += "</a>";
			output += "</td>";
			output += "<td>";
			for (var j = 0; j < data.games[i].gamePlayers.length; ++j) {
				output += data.games[i].gamePlayers[j].player.email + "<br>";
			}
			output += "</td>";

			// checking who is in the game and if one of the users is make back button
			if (data.games[i].gamePlayers.length == 1) {
				if (data.games[i].gamePlayers[0].player.id != data.user.id) {
					output += "<td><button type='button' class='btn btn-default joinGame' data-game-id=" + data.games[i].id + ">" + "Join" + "</button></td>";
				} else {
					output += "<td><button type='button' class='btn btn-default backGame' data-gamePlayer-id=" + data.games[i].gamePlayers[0].id + ">" + "Return" + "</button></td>";
				}
			} else {
				for (var n = 0; n < data.games[i].gamePlayers.length; ++n) {
					if (data.games[i].gamePlayers[n].player.id == data.user.id) {
						output += "<td><button type='button' class='btn btn-default backGame' data-gamePlayer-id=" + data.games[i].gamePlayers[n].id + ">" + "Return" + "</button></td>";
					}
				}
			}
			output += "</tr>";
		}
	}
	$("#games2").html(output);
}

function addIdToLink(user, game) {
	var link = "";
	if (user != "guest user") {
		for (var j = 0; j < game.gamePlayers.length; j++) {
			if (game.gamePlayers[j].player.id == user.id) {
				link = " href='/web/game.html?gp=" + game.gamePlayers[j].id + "' ";
			}
		}
	}
	return link;
}

function renderLeaderboard(data) {
	var arr = [];

	//creating an arr of objects
	for (var key in data.leaderboard) {
		var obj = data.leaderboard[key];
		obj.email = key;
		arr.push(obj);
	}

	//sorting arr of objects
	arr.sort(function (a, b) {
		return b.total - a.total;
	});


	//adding medals
	arr[0].medal = "<img  src='https://vignette3.wikia.nocookie.net/overwatch/images/4/44/Competitive_Gold_Icon.png/revision/latest?cb=20161122023755'>";
	arr[1].medal = "<img  src='https://d1u1mce87gyfbn.cloudfront.net/game/rank-icons/season-2/rank-2.png'>";
	arr[2].medal = "<img  src='https://d1u1mce87gyfbn.cloudfront.net/game/rank-icons/season-2/rank-1.png'>";

	var output = "";
	
	// instead of ptinting arr.length we'll only print 5
	for (var i = 0; i < 5; ++i) {
		output += "<tr>";
		// adding empty td if theres no medal
		// if there are no points there are no medals
		if (arr[i].medal != undefined && arr[i].total != 0) {
			output += "<td>" + arr[i].medal + "</td>";
		} else {
			output += "<td>" + "</td>";
		}
		output += "<td>" + arr[i].email + "</td>";
		output += "<td>" + arr[i].total + "</td>";
		output += "<td>" + arr[i].won + "</td>";
		output += "<td>" + arr[i].lost + "</td>";
		output += "<td>" + arr[i].tied + "</td>";
		output += "</tr>";
	}
	$("#leaderBoardResult").html(output);
}

function logIn() {
	var obj = {
		userName: $("#email").val(),
		password: $("#pwd").val()
	};
	$.post("/api/login", obj).done(function () {
		$.getJSON("/api/games", onDataReady);
		$("#submitlogout").show();
	});
}

function logOut() {
	$.post("/api/logout").done(function () {
		$.getJSON("/api/games", onDataReady);

		 
	});
}

function displayForms(button, display) {
	$("#login").css("display", "none");
	$("#logout").css("display", "none");
	$(button).css("display", display);
}

function displayGreetings(data) {
	if (data.user.email == null) {
		console.log("User is not logged in");
		displayForms("#login", "inline");
	} else if (data.user.email !== null) {
		$("#greetings").html("Hello " + data.user.email);
		displayForms("#logout", "inline");
	}
}

function displayRegisterForm() {
	var inputName = $("<div id='name' class='form-group'><label for='name'>First name:</label><input type='name' class='form-control'></div>");
	var inputLastName = $("<div id='lastname' class='form-group'><label for='lastname'>Last name:</label><input type='lastname' class='form-control'></div>");
	$("#login").prepend(inputName);
	$("#login").prepend(inputLastName);
	$("#register").show();
	$("#back").show();
	$("#newUser").hide();
	$("#checkbox").hide();
	$("#submit").hide();
}

function displayBackToLogIn() {
	$("#name").remove();
	$("#lastname").remove();
	$("#register").hide();
	$("#back").hide();
	$("#newUser").show();
	$("#checkbox").show();
	$("#submit").show();
}

function registerUser() {
	var name = $("#name").val();
	var lastName = $("#lastname").val();
	var email = $("#email").val();
	var psw = $("#pwd").val();

	$.post("/api/players", {
		firstName: name,
		inputLastname: lastName,
		inputUserName: email,
		password: psw
	}).done(function () {
		$.post("/api/login", {
			userName: email,
			password: psw
		}).done(function () {
			$.getJSON("/api/games", onDataReady);
		}).fail(function () {});
	}).fail(function (arr) {
		console.log(arr);
		console.log("User already exists");
	});
}
