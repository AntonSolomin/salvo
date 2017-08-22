package project.salvo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by Anton on 13.07.2017.
 */

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(String firstName,
                                               String inputLastname,
                                               String inputUserName,
                                               String password) {

        if (inputUserName.isEmpty()) {
            return new ResponseEntity<>("No name given", HttpStatus.FORBIDDEN);
        }
        final Player existingPlayer = playerRepository.findByUserName(inputUserName);
        if (existingPlayer != null) {
            return new ResponseEntity<>("Name already used", HttpStatus.CONFLICT);
        }
        playerRepository.save(new Player(firstName, inputLastname, inputUserName, password));
        return new ResponseEntity<>("Player created", HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> createGame(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else {
            final Player player = currentAuthedUser(authentication);
            Game newGame = new Game();
            gameRepository.save(newGame);
            GamePlayer firstGamePlayer = new GamePlayer(player, newGame);
            gamePlayerRepository.save(firstGamePlayer);
            response.put("gpid", firstGamePlayer.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public Map<String, Object> getGames(Authentication authentication) {

        List<Object> gamesDto = gameRepository
                .findAll()
                .stream()
                .map(g -> makeGameDTO(g))
                .collect(toList());

        Map<String, Object> finalDto = new LinkedHashMap<>();
        if (isGuest(authentication)) {
            finalDto.put("user", "unidentified user");
            finalDto.put("games", gamesDto);
            finalDto.put("leaderboard", makeScoresDto());
            return finalDto;
        }

        Player user = currentAuthedUser(authentication);
        if (user != null) {
            finalDto.put("user", makePlayerDto(user));
        }

        finalDto.put("games", gamesDto);
        finalDto.put("leaderboard", makeScoresDto());

        return finalDto;
    }


    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Object> gameView(@PathVariable long gamePlayerId,
                                           Authentication authentication) {

        Map<String, Object> toReturn = new LinkedHashMap<>();
        List<Object> gamePlayers = new ArrayList<>();
        List<Object> locations = new ArrayList<>();
        List<Object> history = new ArrayList<>();


        // game player has id in the repository
        GamePlayer currentGamePlayer = gamePlayerRepository.findOne(gamePlayerId);

        // the user is not logged in (a guest)
        if (isGuest(authentication)) {
            toReturn.put("error", "please log in");
            return new ResponseEntity<Object>(toReturn, HttpStatus.UNAUTHORIZED);
        }

        // gp id is not in the repo
        if (currentGamePlayer == null) {
            toReturn.put("error", "no such game player");
            return new ResponseEntity<Object>(toReturn, HttpStatus.UNAUTHORIZED);
        }

        // the current authed user
        Player player = currentAuthedUser(authentication);



        // check if he has id of the url in his set
        if (player != null && checkPlayerHasGamePlayerWithId(player, gamePlayerId)) {
            toReturn.put("id", currentGamePlayer.getGame().getGameId());
            toReturn.put("creation_date", currentGamePlayer.getGame().getGameCreationDate());
            Map<Long, Object> playerSalvos = new LinkedHashMap<>();

            for (GamePlayer gp : currentGamePlayer.getGame().getGamePlayers()) {
                gamePlayers.add(makeGamePlayerDto(gp));
                //creating history objs
                history.add(makeHistoryDto(gp));
                playerSalvos.put(gp.getId(), makeSalvoDto(gp));
            }
            for (Ship sp : currentGamePlayer.getShips()) {
                locations.add(makeShipDto(sp));
            }
            toReturn.put("salvos", playerSalvos);
            toReturn.put("game_players", gamePlayers);
            toReturn.put("ships", locations);

            // adding history to the gameview
            toReturn.put("history", history);

            return new ResponseEntity<Object>(toReturn, HttpStatus.OK);
        } else {
            toReturn.put("error", "no cheating");
            return new ResponseEntity<Object>(toReturn, HttpStatus.UNAUTHORIZED);
        }
    }


    @RequestMapping(path = "games/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(@PathVariable long gameId,
                                           Authentication authentication) {
        // getting the current player
        Player user = currentAuthedUser(authentication);
        // response to be returned
        Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game with the necessary ID. if there isn't return forbidden
        Game gameToJoin = gameRepository.findOne(gameId);
        if (gameToJoin == null) {
            response.put("error", "No such game");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }

        //check if the game has only one player. if 2 return forbidden
        if (gameToJoin.getGamePlayers().size() == 2) {
            response.put("error", "Game is full");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }


        GamePlayer newGameplayer = new GamePlayer(user, gameToJoin);
        gamePlayerRepository.save(newGameplayer);

        //including new game player's ID
        Long gpid = newGameplayer.getId();
        response.put("gpid", gpid);
        return new ResponseEntity<Object>(response, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> createShips(@PathVariable long gamePlayerId,
                                              Authentication authentication,
                                              @RequestBody List<Ship> ships) {
        // getting the current player
        Player user = currentAuthedUser(authentication);
        // response to be returned
        Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game player with the necessary ID. if there isn't return Unauthorized
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        if (gamePlayer == null) {
            response.put("error", "No such game player");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        //if the user owns a game player to which he is trying to add ships. if yes return Unauthorized
        if (gamePlayer.getPlayer() != user) {
            response.put("error", "the user is attempting to add ships for other players");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // checking if the user has already added ships
        if (gamePlayer.getShips().size() != 0) {
            response.put("error", "the user has already placed the ships");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }

        // otherwise add and save ships in the ship repository and send CREATED response
        for (Ship s : ships) {
            s.setGamePlayer(gamePlayer);
            shipRepository.save(s);
        }
        response.put("success", "the ships have been successfuly placed");
        return new ResponseEntity<Object>(response, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<Object> postSalvos (@PathVariable long gamePlayerId,
                                              Authentication authentication,
                                              @RequestBody List<String> salvo) {
        // getting the current player
        Player user = currentAuthedUser(authentication);
        // response to be returned
        Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game player with the necessary ID. if there isn't return Unauthorized
        GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        if (gamePlayer == null) {
            response.put("error", "No such game player");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        //if the user owns a game player to which he is trying to add salvos. if yes return Unauthorized
        if (gamePlayer.getPlayer() != user) {
            response.put("error", "the user is attempting to add salvos for other players");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // checking if the user has already added salvos
        if (gamePlayer.getSalvo().size() != 0) {
            response.put("error", "the user has already submitted salvos");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }

        // create an int with the turn number to be increased. Just the number
        Integer currentTurnNumber = 1;
        // all my salvos
        Set<Salvo> salvoSet = gamePlayer.getSalvo();
        // We need to find the salvo with the highest turn number value
        // if there is 0 in the salvoSet it means it is the 1st
        for (Salvo salvo1: salvoSet) {
            if (salvo1.getTurnNumber() > currentTurnNumber) {
                currentTurnNumber = salvo1.getTurnNumber();
            }
        }

        // Here currentTurnNumber has the value of the las turn
        // if salvoSet is empty it means it is the first turn and it
        // is 1 and shouldn't be incremented
        if (!salvoSet.isEmpty()) {
            currentTurnNumber++;
        }

        Salvo mySalvo = new Salvo(gamePlayer, salvo, currentTurnNumber);
        salvoRepository.save(mySalvo);
        response.put("success", "the salvos have been successfuly placed");
        return new ResponseEntity<Object>(response, HttpStatus.CREATED);
    }


    private Map<Object, Object> makeHistoryDto (GamePlayer gamePlayer) {

        //this will be returned
        Map<Object,Object> dto = new HashMap<>();
        // getting my shot locations and enemy ship locations
        Set<Salvo> mySet = gamePlayer.getSalvo();
        // i need the ships of the other game player
        Set<Ship> enemyShips = getOtherShips(gamePlayer);
        // this will be an array of turns
        List<Object> turnsArr = new ArrayList<>();
        // this is each turn
        Map<Object,Object> turn = new HashMap<>();

        // itirate through both salvos and ships and if they match add the obj to hits
        for (Salvo sv : mySet) {

            //putting the turn number
            turn.put("turn", sv.getTurnNumber());
            //arr figure out how many have sunk
            turn.put("sunk", getSunk(enemyShips, sv));
            // Adding locations that are hit to hit arr and adding them to turn
            turn.put("hit", getHits(enemyShips, sv));
            // number of ships left
            turn.put("left", gamePlayer.getShips().size());

            // adding this turn to turns arr
            turnsArr.add(turn);
        }

        dto.put("gpid", gamePlayer.getId());
        dto.put("action", turnsArr);
        return dto;
    }

    // recieve my salvos and enemy ships
    private List<String> getSunk(Set<Ship> enemyShips, Salvo sv) {
        // this will have the "sunk" ships
        final List<String> sunk = new ArrayList<>();
        //each ship
        for (Ship enemyShip : enemyShips) {
            // it's size number
            Integer shipSize = enemyShip.getLocations().size();
            // my shots
            for (String shotLocation : sv.getShotLocations() ) {
                // ship locations
                for (String shipLocation : enemyShip.getLocations()) {
                    // if the ship has been completely destroyed add it to the SUNK
                    if ( shotLocation.equals(shipLocation) ) {
                        // check if it is destroyed
                        if (shipSize == 0) {
                            sunk.add(enemyShip.getShipClass().toString());
                        }
                        // if not decrement the number
                        shipSize--;
                    }
                }
            }
        }
        return sunk;
    }

    private List<String> getHits(Set<Ship> enemyShips, Salvo sv) {
        final List<String> hits = new ArrayList<>();
        for (Ship enemyShip : enemyShips) {
            for (String shotLocation : sv.getShotLocations() ) {
                for (String shipLocation : enemyShip.getLocations()) {
                    if ( shotLocation.equals(shipLocation )) {
                        hits.add(shotLocation);
                    }
                }
            }
        }
        return hits;
    }

    private Set<Ship> getOtherShips(GamePlayer gamePlayer) {
        Set<GamePlayer> gp = gamePlayer.getGame().getGamePlayers();
        for (GamePlayer gamep : gp) {
            // if one of them is not me
            if (gamePlayer.getId() != gamep.getId()) {
                // then make myShips be the other dude's ships
                return gamep.getShips();
            }
        }
        return new HashSet<>();
    }


    private Map<Integer, Object> makeSalvoDto(GamePlayer gamePlayer) {
        Map<Integer, Object> dto = new HashMap<>();
        Set<Salvo> mySet = gamePlayer.getSalvo();
        for (Salvo sv : mySet) {
            dto.put(sv.getTurnNumber(), sv.getShotLocations());
        }
        return dto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        final Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getGameId());
        dto.put("date", game.getDate());
        List<Object> gamePlayerDtos = game.getGamePlayers().stream().map(gp -> makeGamePlayerDto(gp)).collect(toList());
        dto.put("gamePlayers", gamePlayerDtos);
        return dto;
    }

    private Map<String, Object> makeGamePlayerDto(GamePlayer gamePlayer) {
        Map<String, Object> gamePlayerDto = new LinkedHashMap<>();
        gamePlayerDto.put("id", gamePlayer.getId());
        gamePlayerDto.put("player", makePlayerDto(gamePlayer.getPlayer()));
        return gamePlayerDto;
    }

    private Map<String, Object> makeScoresDto() {
        Map<String, Object> playerScoresDto = new LinkedHashMap<>();
        playerRepository.findAll()
                .stream()
                .forEach(player -> playerScoresDto.put(player.getUserName(), makeScoreDto(player)));
        return playerScoresDto;
    }

    private Map<String, Object> makeScoreDto(Player player) {
        Map<String, Object> userScoreCount = new HashMap<>();
        double total = 0;
        double won = 0;
        double lost = 0;
        double tied = 0;
        Set<Score> playerScoreSet = player.getScores();
        for (Score ps : playerScoreSet) {
            if (ps.getPoints() == 1.0) {
                won++;
                total += 1;
            }
            if (ps.getPoints() == 0.5) {
                tied++;
                total += 0.5;
            }
            if (ps.getPoints() == 0) {
                lost++;
            }
        }
        userScoreCount.put("total", total);
        userScoreCount.put("won", won);
        userScoreCount.put("lost", lost);
        userScoreCount.put("tied", tied);
        return userScoreCount;
    }

    private Map<String, Object> makePlayerDto(Player player) {
        Map<String, Object> playerDto = new LinkedHashMap<>();
        playerDto.put("id", player.getUserId());
        playerDto.put("email", player.getUserName());
        return playerDto;
    }

    private Map<String, Object> makeShipDto(Ship ship) {
        Map<String, Object> shipDto = new LinkedHashMap<>();
        shipDto.put("type", ship.getShipClass());
        shipDto.put("locations", ship.getLocations());
        return shipDto;
    }

    private Player currentAuthedUser(Authentication auth) {
        return playerRepository.findByUserName(auth.getName());
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    // given a player and pg id we look for the gp ids in the player if found one return true
    private boolean checkPlayerHasGamePlayerWithId(Player player, long gamePlayerId) {

        Optional<GamePlayer> any = player.getGamePlayers().stream()
                .filter(gamePlayer -> gamePlayerId == gamePlayer.getId())
                .findAny();

        return any.isPresent();
        /*Set<GamePlayer> playerGps = player.getGamePlayers();
        for(GamePlayer gp : playerGps) {
            if (gamePlayerId == gp.getId()) {
                return true;
            }
        }
        return false;*/
    }

    public static Long convertToLong(Object o){
        String stringToConvert = String.valueOf(o);
        Long convertedLong = Long.parseLong(stringToConvert);
        return convertedLong;

    }
}