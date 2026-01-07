import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("a)******************************");
        Team myTeam = dataRetriever.findTeamById(1);
        System.out.println(myTeam);
        System.out.println(myTeam.getPlayersGoals());
        System.out.println("b)******************************");
        System.out.println(dataRetriever.findTeamById(5));
        System.out.println("c)******************************");
        System.out.println(dataRetriever.findPlayers(1, 2));
        System.out.println("d)******************************");
        System.out.println(dataRetriever.findPlayers(3, 5));
        System.out.println("e)******************************");
        System.out.println(dataRetriever.findTeamsByPlayerName("an"));
        System.out.println("f)******************************");
        System.out.println(dataRetriever.findPlayersByCriteria("ud", Player.PlayerPositionEnum.MIDF, "Madrid", Team.ContinentEnum.EUROPA, 1, 10));
//        System.out.println("g)******************************");
//        List<Player> players = new ArrayList<>();
//        Player jude = new Player(6, "Jude Bellingham", 23, Player.PlayerPositionEnum.STR, null);
        Player pedri = new Player(7, "Pedri", 24, Player.PlayerPositionEnum.MIDF, 3, null);
//        players.add(jude);
//        players.add(pedri);
//        System.out.println(dataRetriever.createPlayers(players));
//        System.out.println("h)******************************");
        List<Player> viniAndPedri = new ArrayList<>();
        Player vini = new Player(6, "Vini", 25, Player.PlayerPositionEnum.STR, 4, null);
        viniAndPedri.add(vini);
        viniAndPedri.add(pedri);
//        System.out.println(dataRetriever.createPlayers(viniAndPedri));
        System.out.println("i)******************************");
        List<Player> playerss = new ArrayList<>();
        Team team = new Team(1, "Tsy aiko", Team.ContinentEnum.EUROPA, viniAndPedri);
        playerss.add(vini);
        System.out.println(dataRetriever.saveTeam(team));
        System.out.println("j)******************************");
        List<Player> emptyPlayerList = new ArrayList<>();
        System.out.println(dataRetriever.saveTeam(new Team(2, "FC Barcelone", Team.ContinentEnum.EUROPA, emptyPlayerList)));
    }
}
