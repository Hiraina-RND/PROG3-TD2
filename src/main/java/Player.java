import java.util.Objects;

public class Player {

    public enum PlayerPositionEnum {
        GK,
        DEF,
        MIDF,
        STR
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && age == player.age && Objects.equals(name, player.name) && position == player.position && Objects.equals(team, player.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, position, team);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", position=" + position +
                ", team=" + team +
                '}';
    }

    private final int id;
    private final String name;
    private final int age;
    private final PlayerPositionEnum position;
    private final Team team;

    private String getTeamName() {
        return this.team.getName();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public PlayerPositionEnum getPosition() {
        return position;
    }

    public Team getTeam() {
        return team;
    }


    public Player(int id, String name, int age, PlayerPositionEnum position, Team team) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
        this.team = team;
    }
}
