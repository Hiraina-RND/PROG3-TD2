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
        Connection connection = dbConnection.getDBConnection();

        String SQL = """
            SELECT id, name, continent
            FROM "Team"
            WHERE id = ?
            """;

        try (
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
        } finally {
            dbConnection.close(connection);
        }
        return null;
    }

    private List<Player> findAllPlayerByIdTeam(Integer idTeam) {
        DBConnection dbConnection = new DBConnection();
        List<Player> players = new ArrayList<>();
        Connection connection = dbConnection.getDBConnection();

        String SQL = """
                SELECT id, name, age, "position", id_team
                FROM "Player"
                WHERE id_team = ?
                """;

        try(
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
        } finally {
            dbConnection.close(connection);
        }
        return players;
    }

    public Team findTeamById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        String SQL = """
                SELECT id, name, continent
                FROM "Team"
                WHERE id = ?
                """;
        Team team = null;

        try (
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
        } finally {
            dbConnection.close(connection);
        }
        return team;
    }

    List<Player> findPlayers(int page, int size) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        String SQL = """
                SELECT id, name, age, position, id_team
                FROM "Player"
                LIMIT ? OFFSET ?
                """;
        List<Player> players = new ArrayList<>();

        try (
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
        } finally {
            dbConnection.close(connection);
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
                        if (plr.getId() == existing.getId() || (plr.getName().equals(existing.getName())
                                && plr.getAge() == existing.getAge()
                                && plr.getPosition() == existing.getPosition())) {
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
        } finally {
            dbConnection.close(connection);
        }

        return playersToAdd;
    }

    public List<Team> findAllExistingTeamsWithoutPlayers() {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Team> allTeam = new ArrayList<>();
        String SQL = """
                SELECT id, name, continent
                FROM "Team"
                """;

        try (
                PreparedStatement ps = connection.prepareStatement(SQL);
                ResultSet resultSet = ps.executeQuery()
        ) {
            while (resultSet.next()) {
                allTeam.add(new Team(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        Team.ContinentEnum.valueOf(resultSet.getString("continent"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        } finally {
            dbConnection.close(connection);
        }
        return allTeam;
    }

    public Player findPlayerById(int idPlayer) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        Player foundPlayer = null;

        String SQL = """
            SELECT id, name, age, "position", id_team
            FROM "Player"
            WHERE id = ?
            """;

        try (
             PreparedStatement ps = connection.prepareStatement(SQL)
        ) {

            ps.setInt(1, idPlayer);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundPlayer = new Player(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            Player.PlayerPositionEnum.valueOf(rs.getString("position")),
                            findTeamById(rs.getInt("id_team"))
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching player by id", e);
        } finally {
            dbConnection.close(connection);
        }

        return foundPlayer;
    }

    public List<Player> findAllPlayers() {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Player> players = new ArrayList<>();
        String SQL = """
                SELECT id, name, age, "position", id_team
                FROM "Player"
                """;

        try (
             PreparedStatement ps = connection.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                players.add(mapToPlayer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        } finally {
            dbConnection.close(connection);
        }

        return players;
    }


    public Team saveTeam(Team teamToSave) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        Team addedTeam = null;

        String SQLToInsertTeam = """
            INSERT INTO "Team" (name, continent)
            VALUES (?, ?::continents_enum)
            RETURNING id
            """;

        String SQLToInsertPlayer = """
            INSERT INTO "Player" (name, age, "position", id_team)
            VALUES (?, ?, ?::positions_enum, ?)
            """;

        String SQLToUpdateIdTeamOnPlayer = """
            UPDATE "Player"
            SET id_team = ?
            WHERE id = ?
            """;

        String SQLToUpdateTeam = """
            UPDATE "Team"
            SET name = ?, continent = ?::continents_enum
            WHERE id = ?
            """;

        List<Player> allPlayers = findAllPlayers();
        List<Team> existingTeams = findAllExistingTeamsWithoutPlayers();
        Team existingTeam = null;

        for (Team team : existingTeams) {
            if ((teamToSave.getId() != 0 && team.getId() == teamToSave.getId())
                    || (team.getName().equals(teamToSave.getName())
                    && team.getContinent().equals(teamToSave.getContinent()))) {
                existingTeam = team;
                break;
            }
        }
        connection.setAutoCommit(false);

        try {
            if (existingTeam != null) {
                teamToSave.setId(existingTeam.getId());

                try (PreparedStatement psUpdate = connection.prepareStatement(SQLToUpdateTeam)) {
                        psUpdate.setString(1, teamToSave.getName());
                        psUpdate.setString(2, teamToSave.getContinent().name());
                        psUpdate.setInt(3, teamToSave.getId());
                        psUpdate.executeUpdate();
                    }

                if (teamToSave.getPlayers() != null) {
                    if (teamToSave.getPlayers().isEmpty()) {
                        try (PreparedStatement psToUpdateIdTeamOnPlayer = connection.prepareStatement(SQLToUpdateIdTeamOnPlayer)) {
                            for (Player player : findAllPlayerByIdTeam(teamToSave.getId())){
                                psToUpdateIdTeamOnPlayer.setNull(1, Types.INTEGER);
                                psToUpdateIdTeamOnPlayer.setInt(2, player.getId());

                                psToUpdateIdTeamOnPlayer.executeUpdate();
                            }
                        }
                    } else {
                        for (Player player : teamToSave.getPlayers()) {
                            if (player.getId() == 0 || findPlayerById(player.getId()) == null) {
                                    boolean playerExists = allPlayers.stream().anyMatch(plr ->
                                            plr.getName().equals(player.getName())
                                                    && plr.getAge() == player.getAge()
                                                    && plr.getPosition() == player.getPosition()
                                    );

                                    if (!playerExists) {
                                        try (PreparedStatement psInsert = connection.prepareStatement(SQLToInsertPlayer)) {
                                            psInsert.setString(1, player.getName());
                                            psInsert.setInt(2, player.getAge());
                                            psInsert.setString(3, player.getPosition().name());
                                            psInsert.setInt(4, teamToSave.getId());

                                            psInsert.executeUpdate();
                                        }
                                    }
                            }
                        }
                    }
                }
            } else {
                    try (PreparedStatement psInsertTeam = connection.prepareStatement(SQLToInsertTeam)) {
                        psInsertTeam.setString(1, teamToSave.getName());
                        psInsertTeam.setString(2, teamToSave.getContinent().name());

                        try (ResultSet rs = psInsertTeam.executeQuery()) {
                            rs.next();
                            teamToSave.setId(rs.getInt("id"));
                        }
                    }

                    if (teamToSave.getPlayers() != null) {
                        for (Player player : teamToSave.getPlayers()) {
                            try (PreparedStatement psInsert = connection.prepareStatement(SQLToInsertPlayer)) {
                                psInsert.setString(1, player.getName());
                                psInsert.setInt(2, player.getAge());
                                psInsert.setString(3, player.getPosition().name());
                                psInsert.setInt(4, teamToSave.getId());

                                psInsert.executeUpdate();
                            }
                        }
                    }
                }

                connection.commit();
                addedTeam = findTeamById(teamToSave.getId());

            } catch (SQLException e) {
                connection.rollback();
                throw e;
        } finally {
            dbConnection.close(connection);
        }
        return addedTeam;
    }
    public List<Team> findTeamsByPlayerName(String playerName){
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        List<Integer> allTeamId = new ArrayList<>();
        List<Team> foundedTeams = new ArrayList<>();

        String SQLToFindAllIdTeamByPlayerName = """
                SELECT id_team
                FROM "Player"
                WHERE name ILIKE ?
                """;

        try (
                PreparedStatement psToFindAllIdTeamByPlayerName = connection.prepareStatement(SQLToFindAllIdTeamByPlayerName)
        ){
            psToFindAllIdTeamByPlayerName.setString(1, "%" + playerName + "%");

            try (ResultSet resultSet = psToFindAllIdTeamByPlayerName.executeQuery()){
                while(resultSet.next()){
                    allTeamId.add(resultSet.getInt("id_team"));
                }
            }
            allTeamId = allTeamId
                    .stream()
                    .distinct()
                    .toList();

            for (Integer i : allTeamId){
                foundedTeams.add(findTeamById(i));
            }
            return foundedTeams;
        } catch (SQLException e){
            throw new RuntimeException("Database error", e);
        } finally {
            dbConnection.close(connection);
        }
    }

    static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever = new DataRetriever();
        List<Player> players = new ArrayList<>();
        Player rakoto = new Player("rakoto", 25, Player.PlayerPositionEnum.valueOf("GK"), null);
        Player rabe = new Player("rabe", 28, Player.PlayerPositionEnum.valueOf("GK"), null);
        players.add(rakoto);
        players.add(rabe);

        Team team = new Team( 1,"FC Tsenakely", Team.ContinentEnum.valueOf("AFRICA"), players);
        System.out.println(dataRetriever.saveTeam(team));

    }
}
