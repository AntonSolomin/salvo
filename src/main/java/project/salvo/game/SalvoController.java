package project.salvo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    private  SalvoRepository salvoRepository;

    @RequestMapping("/games")
    public List<Object> getGames () {
        List<Object> returnDto = gameRepository
                .findAll()
                .stream()
                .map(g -> makeGameDTO(g))
                .collect(toList());
        return  returnDto;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public  Map<String, Object> gameView (@PathVariable long gamePlayerId) {

        Map<String,Object> toReturn = new LinkedHashMap<>();
        List<Object> gamePlayers = new ArrayList<>();
        List<Object> locations = new ArrayList<>();

        GamePlayer currentGamePlayer = gamePlayerRepository.findOne(gamePlayerId);

        toReturn.put("id", currentGamePlayer.getGame().getGameId());
        toReturn.put("creation_date", currentGamePlayer.getGame().getGameCreationDate());

        Map<Long,Object> playerSalvos = new LinkedHashMap<>();


        for (GamePlayer gp : currentGamePlayer.getGame().getGamePlayers()) {
            gamePlayers.add(makeGamePlayerDto(gp));
            playerSalvos.put(gp.getId(), makeSalvoDto(gp));
        }

        for (Ship sp : currentGamePlayer.getShips()) {
            locations.add(makeShipDto(sp));
        }

        toReturn.put("salvos", playerSalvos);
        toReturn.put("game_players", gamePlayers);
        toReturn.put("ships", locations);

        return toReturn;
    }

    private Map<Long, Object> makeSalvoDto (GamePlayer gamePlayer) {
        Map<Long, Object> dto = new HashMap<>();
        Set<Salvo> mySet = gamePlayer.getSalvo();
        for (Salvo sv : mySet) {
            dto.put( sv.getTurnNumber(), sv.getShotLocations());
        }
        return dto;
    }



    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getGameId());
        dto.put("date", game.getDate());
        List<GamePlayer> gamePlayers = new ArrayList<>(game.getGamePlayers());
        List<Object> gamePlayerDtos = gamePlayers.stream().map(gp -> makeGamePlayerDto(gp)).collect(toList());
        dto.put("gamePlayers", gamePlayerDtos);
        return dto;
    }

    private Map<String, Object> makeGamePlayerDto (GamePlayer gamePlayer) {
        Map<String, Object> gamePlayerDto = new LinkedHashMap<>();
        gamePlayerDto.put("id", gamePlayer.getId());
        gamePlayerDto.put("player", makePlayerDto(gamePlayer.getPlayer()));
        return gamePlayerDto;
    }

    private Map<String, Object> makePlayerDto (Player player) {
        Map<String, Object> playerDto = new LinkedHashMap<>();
        playerDto.put("id", player.getUserId());
        playerDto.put("email", player.getUserName());
        return playerDto;
    }

    private Map<String,Object> makeShipDto (Ship ship) {
        Map<String,Object> shipDto = new LinkedHashMap<>();
        shipDto.put("type", ship.getShipClass());
        shipDto.put("locations", ship.getLocations());
        return shipDto;
    }
}
