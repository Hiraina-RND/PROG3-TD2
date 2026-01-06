import java.util.List;
import java.util.Objects;

public class Team {

    public enum ContinentEnum {
        AFRICA,
        EUROPA,
        ASIA,
        AMERICA
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id && Objects.equals(name, team.name) && continent == team.continent && Objects.equals(players, team.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, continent, players);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", continent=" + continent +
                ", players=" + players.stream()
                .map(p -> "Player{" +
                        "id=" + p.getId() +
                        ", name='" + p.getName() + '\'' +
                        ", age=" + p.getAge() +
                        ", position='" + p.getPosition() + '\'' +
                        '}')
                .toList() +
                '}';
    }

    private int id;
    private String name;
    private ContinentEnum continent;
    private List<Player> players;

    private Integer getPlayersCount() {
        return players.size();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ContinentEnum getContinent() {
        return continent;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContinent(ContinentEnum continent) {
        this.continent = continent;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Team(int id, String name, ContinentEnum continent, List<Player> players) {
        this.id = id;
        this.name = name;
        this.continent = continent;
        this.players = players;
    }

    public Team(String name, ContinentEnum continent, List<Player> players) {
        this.id = id;
        this.name = name;
        this.continent = continent;
        this.players = players;
    }

    public Team(int id, String name, ContinentEnum continent) {
        this.id = id;
        this.name = name;
        this.continent = continent;
    }

    public Team(String name, ContinentEnum continent) {
        this.id = id;
        this.name = name;
        this.continent = continent;
    }
}
