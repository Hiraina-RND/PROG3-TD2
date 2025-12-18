import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private Team mapToTeam(ResultSet resultSet) throws SQLException {
        return new Team(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                Team.ContinentEnum.valueOf(resultSet.getString("continent")),
                findAllPlayerByIdTeam(resultSet.getInt("id"))
        );
    }

    private Player mapToPlayer(ResultSet resultSet) throws SQLException {
        return new Player(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("age"),
                Player.PlayerPositionEnum.valueOf(resultSet.getString("position")),
                findTeamByIdWithoutPlayers(resultSet.getInt("id_team"))
        );
    }

    private Team findTeamByIdWithoutPlayers(Integer id) {
        DBConnection dbConnection = new DBConnection();

        String SQL = """
            SELECT id, name, continent
            FROM "Team"
            WHERE id = ?
            """;

        try (
                Connection connection = dbConnection.getDBConnection();
                PreparedStatement ps = connection.prepareStatement(SQL)
        ) {
            ps.setInt(1, id);

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return new Team(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            Team.ContinentEnum.valueOf(resultSet.getString("continent")),
                            null
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<Player> findAllPlayerByIdTeam(Integer idTeam) {
        DBConnection dbConnection = new DBConnection();
        List<Player> players = new ArrayList<>();

        String SQL = """
                SELECT id, name, age, "position", id_team
                FROM "Player"
                WHERE id_team = ?
                """;

        try(
                Connection connection = dbConnection.getDBConnection();
                PreparedStatement ps = connection.prepareStatement(SQL)
        ) {
            ps.setInt(1, idTeam);

            try(
                    ResultSet resultSet = ps.executeQuery()
            ) {
                while (resultSet.next()) {
                    players.add(mapToPlayer(resultSet));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException("Error executing query", e);
        }
        return players;
    }

    public Team findTeamById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        String SQL = """
                SELECT id, name, continent
                FROM "Team"
                WHERE id = ?
                """;
        Team team = null;

        try (
                Connection connection = dbConnection.getDBConnection();
                PreparedStatement ps = connection.prepareStatement(SQL)
        ){
            ps.setInt(1, id);

            try (
                    ResultSet resultSet = ps.executeQuery()
            ) {
                if (resultSet.next()) {
                    team = mapToTeam(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
        return team;
    }

    List<Player> findPlayers(int page, int size) {
        DBConnection dbConnection = new DBConnection();
        String SQL = """
                SELECT id, name, age, position, id_team
                FROM "Player"
                LIMIT ? OFFSET ?
                """;
        List<Player> players = new ArrayList<>();

        try (
                Connection connection = dbConnection.getDBConnection();
                PreparedStatement ps = connection.prepareStatement(SQL);
        ){
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);

            try (
                    ResultSet resultSet = ps.executeQuery();
            ){
                while (resultSet.next()){
                    players.add(mapToPlayer(resultSet));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error executing query");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing query", e);
        }
        return players;
    }

    List<Player> createPlayers(List<Player> newPlayers) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Player> existingPlayers = new ArrayList<>();
        List<Player> playersToAdd = new ArrayList<>();

        String SQLToSelectExistingPlayers = """
        SELECT id, name, age, "position", id_team
        FROM "Player"
        """;

        String SQLToInsertPlayer = """
        INSERT INTO "Player" (name, age, "position", id_team)
        VALUES (?, ?, ?::positions_enum, ?)
        RETURNING id
        """;

        try {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement psToSelectExistingPlayers = connection.prepareStatement(SQLToSelectExistingPlayers);
                    ResultSet resultSet = psToSelectExistingPlayers.executeQuery()
            ) {
                while (resultSet.next()) {
                    existingPlayers.add(mapToPlayer(resultSet));
                }

                for (Player plr : newPlayers) {
                    Player foundExisting = null;

                    for (Player existing : existingPlayers) {
                        if (plr.getName().equals(existing.getName())
                                && plr.getAge() == existing.getAge()
                                && plr.getPosition() == existing.getPosition()) {
                            foundExisting = existing;
                            break;
                        }
                    }

                    if (foundExisting != null) {
                        throw new RuntimeException("Player already existing: " + foundExisting.getId());
                    } else {
                        playersToAdd.add(plr);
                    }
                }
            }

            try (PreparedStatement psToInsertNewPlayers = connection.prepareStatement(SQLToInsertPlayer)) {

                for (Player player : playersToAdd) {
                    psToInsertNewPlayers.setString(1, player.getName());
                    psToInsertNewPlayers.setInt(2, player.getAge());
                    psToInsertNewPlayers.setString(3, player.getPosition().name());

                    if (player.getTeam() != null) {
                        psToInsertNewPlayers.setInt(4, player.getTeam().getId());
                    } else {
                        psToInsertNewPlayers.setNull(4, Types.INTEGER);
                    }

                    try (ResultSet rs = psToInsertNewPlayers.executeQuery()) {
                        if (rs.next()) {
                            int generatedId = rs.getInt("id");
                            player.setId(generatedId);
                        }
                    }
                }
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException("Error: ", e);
        }

        return playersToAdd;
    }
}
