package project.salvo.game;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Predicate;

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

    @Autowired
    private ScoreRepository scoreRepository;

    //making our db cache available
    //This would be the similar. only the controller would have access to it
    //private DBCache dbCache = new DBCache();
    @Autowired
    private DBCache dbCache;

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
        final Map<String, Object> response = new HashMap<>();
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else {
            final Player player = currentAuthenticatedUser(authentication);
            final Game newGame = new Game();
            gameRepository.save(newGame);
            final GamePlayer firstGamePlayer = new GamePlayer(player, newGame);
            gamePlayerRepository.save(firstGamePlayer);
            response.put("gpid", firstGamePlayer.getId());
            dbCache.apiGamesResponseChanged = true;
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public Map<String, Object> getGames(Authentication authentication) {

        final Map<String, Object> finalDto = new LinkedHashMap<>();

        final String user = currentAuthenticatedUserName(authentication);

        if (user != null) {
            if (!dbCache.apiPlayer.containsKey(user)) {
                Player player = playerRepository.findByUserName(user);
                dbCache.apiPlayer.put(user, makePlayerDto(player));
            }
            finalDto.put("user", dbCache.apiPlayer.get(user));
        } else  {
            finalDto.put("user", "unidentified user");
        }

        if (dbCache.apiGamesResponseChanged) {
            dbCache.apiGamesDto = gameRepository
                    .findAll()
                    .stream()
                    .map(g -> makeGameDTO(g))
                    .collect(toList());
            dbCache.apiLeaderBoardDto = makeScoresDto();
            dbCache.apiGamesResponseChanged = false;
        }

        finalDto.put("games", dbCache.apiGamesDto);
        finalDto.put("leaderboard", dbCache.apiLeaderBoardDto);
        return finalDto;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Object> gameView(@PathVariable long gamePlayerId,
                                           Authentication authentication) {
        final Map<String, Object> toReturn = new LinkedHashMap<>();

        final List<Object> gamePlayers = new ArrayList<>();
        final List<Object> locations = new ArrayList<>();
        final List<Object> history = new ArrayList<>();

        // game player has id in the repository
        final GamePlayer currentGamePlayer = gamePlayerRepository.findOne(gamePlayerId);

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
        final Player player = currentAuthenticatedUser(authentication);

        boolean playderHasGamePlayer = false;
        for (GamePlayer gamePlayer : player.getGamePlayers()) {
            if (gamePlayerId == gamePlayer.getId()) {
                playderHasGamePlayer = true;
            }
        }

        // check if he has id of the url in his set
        if (player != null && playderHasGamePlayer) {
            toReturn.put("id", currentGamePlayer.getGame().getGameId());
            toReturn.put("creation_date", currentGamePlayer.getGame().getGameCreationDate());
            final Map<Long, Object> playerSalvos = new LinkedHashMap<>();

            //this is the guy who will go first
            for (GamePlayer gp : currentGamePlayer.getGame().getGamePlayers()) {
                if (gp.isFirstGamePlayer()) {
                    toReturn.put("first", gp.getId());
                }
            }

            for (GamePlayer gp : currentGamePlayer.getGame().getGamePlayers()) {
                gamePlayers.add(makeGamePlayerDto(gp));
                history.add(makeHistoryDto(gp));
                playerSalvos.put(gp.getId(), makeSalvoDto(gp));
            }

            for (Ship sp : currentGamePlayer.getShips()) {
                locations.add(makeShipDto(sp));
            }
            
            toReturn.put("salvos", playerSalvos);
            toReturn.put("game_players", gamePlayers);
            toReturn.put("enemyShipsPlaced", isPlacedShips(currentGamePlayer));
            toReturn.put("ships", locations);
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
        final Player user = currentAuthenticatedUser(authentication);
        // response to be returned
        final Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game with the necessary ID. if there isn't return forbidden
        final Game gameToJoin = gameRepository.findOne(gameId);
        if (gameToJoin == null) {
            response.put("error", "No such game");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }

        //check if the game has only one player. if 2 return forbidden
        if (gameToJoin.getGamePlayers().size() == 2) {
            response.put("error", "Game is full");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }


        final GamePlayer newGamePlayer = new GamePlayer(user, gameToJoin);
        gamePlayerRepository.save(newGamePlayer);

        //including new game player's ID
        final Long gpid = newGamePlayer.getId();
        response.put("gpid", gpid);
        dbCache.apiGamesResponseChanged = true;
        return new ResponseEntity<Object>(response, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> createShips(@PathVariable long gamePlayerId,
                                              Authentication authentication,
                                              @RequestBody List<Ship> ships) {
        // getting the current player
        final Player user = currentAuthenticatedUser(authentication);
        // response to be returned
        final Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game player with the necessary ID. if there isn't return Unauthorized
        final GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
        if (gamePlayer == null) {
            response.put("error", "No such game player");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        //if the user owns a game player to which he is trying to add ships. if yes return Unauthorized
        if (gamePlayer.getPlayer().getUserId() != user.getUserId()) {
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
        final Player user = currentAuthenticatedUser(authentication);
        // response to be returned
        final Map<String, Object> response = new HashMap<>();

        // checking if there is a user logged in if not return Unauthorized
        if (isGuest(authentication)) {
            response.put("error", "please log in");
            return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
        }

        // getting the game player with the necessary ID. if there isn't return Unauthorized
        final GamePlayer gamePlayer = gamePlayerRepository.findOne(gamePlayerId);
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
        // change
        // compare turn numbers
        /*if (gamePlayer.getSalvo().size() != 0) {
            response.put("error", "the user has already submitted salvos");
            return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
        }*/

        // create an int with the turn number to be increased.
        Integer currentTurnNumber = gamePlayer.getSalvo().size() + 1;

        final Salvo mySalvo = new Salvo(gamePlayer, salvo, currentTurnNumber);
        salvoRepository.save(mySalvo);
        response.put("success", "the salvos have been successfuly placed");


        final Set<Ship> enemyShips = getOtherShips(gamePlayer);
        final Map<Ship, Integer> remainingShipLocations = new HashMap<>();

        final List<Salvo> salvos = gamePlayer.getSalvo().stream()
                .sorted(Comparator.comparingInt(Salvo::getTurnNumber))
                .collect(toList());

        for (Ship ship : enemyShips) {
            remainingShipLocations.put(ship, ship.getLocations().size());
        }

        for (Salvo salvo1 : salvos) {
            getSunk(enemyShips, salvo1, remainingShipLocations);
            if (getLeft(remainingShipLocations) == 0) {
                final Score scoreWon = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), 1.0);
                final Score scoreLost = new Score(gamePlayer.getGame(), gamePlayer.getEnemyGamePlayer().getPlayer(), 0);
                gamePlayer.getGame().setFinished(true);
                scoreRepository.save(scoreWon);
                scoreRepository.save(scoreLost);
                dbCache.apiGamesResponseChanged = true;
            }
        }
        return new ResponseEntity<Object>(response, HttpStatus.CREATED);
    }

    private boolean isPlacedShips(GamePlayer currentGamePlayer) {
        for (GamePlayer gp : currentGamePlayer.getGame().getGamePlayers()) {
            if (gp.getId() != currentGamePlayer.getId()){
                if (gp.getShips().size() != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<Object, Object> makeHistoryDto (GamePlayer gamePlayer) {
        final Map<Object,Object> dto = new HashMap<>();

        // my salvos compared and ordered by turn number
        final List<Salvo> salvos = gamePlayer.getSalvo().stream()
                .sorted(Comparator.comparingInt(Salvo::getTurnNumber))
                .collect(toList());

        // enemy ships
        final Set<Ship> enemyShips = getOtherShips(gamePlayer);
        // array of turns
        final List<Object> turns = new ArrayList<>();

        // this is the map with the ships with ships and ints for rem locations
        // every salvo submit we recount everything
        final Map<Ship,Integer> remainingShipLocations = new HashMap<>();
        for (Ship ship : enemyShips) {
            remainingShipLocations.put(ship, ship.getLocations().size());
        }

        for (Salvo salvo : salvos) {
            // this is each turn
            final Map<String,Object> turn = new LinkedHashMap<>();

            turn.put("turn", salvo.getTurnNumber());
            turn.put("hit", getHits(enemyShips, salvo));
            turn.put("sunk", getSunk(enemyShips, salvo, remainingShipLocations));
            turn.put("left", getLeft(remainingShipLocations));

            // adding turn to turns arr
            turns.add(turn);
        }
        dto.put("gpid", gamePlayer.getId());
        dto.put("action", turns);
        return dto;
    }

    private Integer getLeft (Map<Ship, Integer> remainingShipLocations) {
        int left = 0;
        for (Integer remainingShipLocation : remainingShipLocations.values()) {
            if (remainingShipLocation != 0) {
                ++left;
            }
        }
        return left;
    }

    private List<String> getSunk(Set<Ship> enemyShips, Salvo sv, Map<Ship, Integer> remainingShipLocations) {
        // this will have the "sunk" ships
        final List<String> sunk = new ArrayList<>();
        //each ship
        for (Ship enemyShip : enemyShips) {
            // it's size number
            Integer shipSize = remainingShipLocations.get(enemyShip);
            // my shots
            for (String shotLocation : sv.getShotLocations() ) {
                // ship locations
                for (String shipLocation : enemyShip.getLocations()) {
                    // if the ship has been completely destroyed add it to the SUNK
                    if ( shotLocation.equals(shipLocation) ) {
                        // if not decrement the number
                        shipSize--;
                        remainingShipLocations.put(enemyShip, shipSize);
                        // check if it is destroyed
                        if (shipSize == 0) {
                            // it is sunk, do stuff
                            String ship = enemyShip.getShipClass().toString();
                            sunk.add(ship);
                        }
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
        final Set<GamePlayer> gamePlayers = gamePlayer.getGame().getGamePlayers();
        for (GamePlayer gamePlayer1 : gamePlayers) {
            // if one of them is not me
            if (gamePlayer.getId() != gamePlayer1.getId()) {
                // then make myShips be the other dude's ships
                return gamePlayer1.getShips();
            }
        }
        return new HashSet<>();
    }

    private Map<Integer, Object> makeSalvoDto(GamePlayer gamePlayer) {
        final Map<Integer, Object> dto = new HashMap<>();
        final Set<Salvo> mySet = gamePlayer.getSalvo();
        for (Salvo sv : mySet) {
            dto.put(sv.getTurnNumber(), sv.getShotLocations());
        }
        return dto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        final Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getGameId());
        dto.put("date", game.getDate());
        final List<Object> gamePlayerDtos = game.getGamePlayers().stream().map(gp -> makeGamePlayerDto(gp)).collect(toList());
        dto.put("gamePlayers", gamePlayerDtos);
        dto.put("isFinished", game.isFinished());
        return dto;
    }

    private Map<String, Object> makeGamePlayerDto(GamePlayer gamePlayer) {
        final Map<String, Object> gamePlayerDto = new LinkedHashMap<>();
        gamePlayerDto.put("id", gamePlayer.getId());
        gamePlayerDto.put("player", makePlayerDto(gamePlayer.getPlayer()));
        return gamePlayerDto;
    }

    private Map<String, Object> makeScoresDto() {
        final Map<String, Object> playerScoresDto = new LinkedHashMap<>();
        playerRepository.findAll()
                .stream()
                .forEach(player -> playerScoresDto.put(player.getUserName(), makeScoreDto(player)));
        return playerScoresDto;
    }

    private Map<String, Object> makeScoreDto(Player player) {
        final Map<String, Object> userScoreCount = new HashMap<>();
        double total = 0;
        double won = 0;
        double lost = 0;
        double tied = 0;
        final Set<Score> playerScoreSet = player.getScores();
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
        final Map<String, Object> playerDto = new LinkedHashMap<>();
        playerDto.put("id", player.getUserId());
        playerDto.put("email", player.getUserName());
        return playerDto;
    }

    private Map<String, Object> makeShipDto(Ship ship) {
        final Map<String, Object> shipDto = new LinkedHashMap<>();
        shipDto.put("type", ship.getShipClass());
        shipDto.put("locations", ship.getLocations());
        return shipDto;
    }

    private Player currentAuthenticatedUser(Authentication authentication) {
        if (isGuest(authentication)) {
            return null;
        }
        return playerRepository.findByUserName(authentication.getName());
    }

    private String currentAuthenticatedUserName (Authentication authentication) {
        if (isGuest(authentication)) {
            return null;
        }
        return authentication.getName();
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
    }
}