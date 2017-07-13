package salvoDeSenorAntonio.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by Anton on 13.07.2017.
 */

@RestController
@RequestMapping("/api")
public class SalvoController {
    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public List<Object> getGames () {
        List<Object> returnDto = new ArrayList<>();
        List<Game> myList = gameRepository.findAll();
        for (int i = 0; i<myList.size(); i++) {
            Game game = myList.get(i);
            returnDto.add(makeGameDTO(game));
        }
        return  returnDto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getGameId());
        dto.put("date", game.getDate());

        List<Object> gamePlayerDtos = new ArrayList<>();
        List<GamePlayer> gamePlayers = new ArrayList<>(game.getGamePlayers());
        for (int i = 0; i< gamePlayers.size(); i++) {
            GamePlayer currentGamePlayer = gamePlayers.get(i);
            Map<String, Object> currentGamePlayerDto = makeGamePlayerDto(currentGamePlayer);
            gamePlayerDtos.add(currentGamePlayerDto);
        }
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
        return  playerDto;
    }
}
