package project.salvo.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBCache {
    //creting cache to be used instead of the questions to db

    //games.html
    Map<String,Object> apiLeaderBoardDto;
    Map<String,Map<String, Object>> apiPlayer = new HashMap<>();
    List<Object> apiGamesDto;
    boolean apiGamesResponseChanged = true;


    //gameview
    Map<Long, Object> apiGameView;
    Map<Long, Boolean> apiGameViewChanged;
}
