package src.db;

import java.sql.*;

public class UsersDB {

    static Connection connection;
    static Statement statement;

    public static void open(String dbPath, boolean autoTableCreate) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            statement = connection.createStatement();
            if (autoTableCreate) createTable();
        } catch (SQLException | ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public static void createTable() {
        String sql =
                "CREATE TABLE IF NOT EXISTS 'users' (" +
                "   'id' INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE," +
                "   'salt' VARCHAR(18) NOT NULL," +
                "   'hash' VARCHAR(100) NOT NULL," +
                "   'login' VARCHAR(10) NOT NULL UNIQUE," +
                "   'nickname' VARCHAR(10) NOT NULL UNIQUE);";

        try { statement.execute(sql); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static boolean isFieldUnique(String fieldName, String data) {
        String sql =
                "SELECT '" + fieldName + "' FROM 'users' " +
                "WHERE " + fieldName + " = '" + data + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                resultSet.close();
                return false;
            }
            return true;

        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static boolean isUserLoginUnique(String login) {
        return isFieldUnique("login", login);
    }

    public static boolean isUserNicknameUnique(String nickname) {
        return isFieldUnique("nickname", nickname);
    }

    public static String getNicknameByLogin(String login) {
        String sql =
                "SELECT * FROM 'users' " +
                        "WHERE login = '" + login + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.getString("nickname");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static String[] getSaltHashByLogin(String login) {
        String sql =
                "SELECT * FROM 'users' " +
                "WHERE login = '" + login + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                String salt = resultSet.getString("salt");
                String hash = resultSet.getString("hash");
                resultSet.close();
                return new String[]{salt, hash};
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return null;
    }

    public static void addUser(String salt, String hash, String login, String nickname) {
        String sql =
                "INSERT INTO 'users' (" +
                "   'salt'," +
                "   'hash'," +
                "   'login'," +
                "   'nickname')" +
                "VALUES (" +
                "   '" + salt + "', " +
                "   '" + hash + "', " +
                "   '" + login + "', " +
                "   '" + nickname + "');";

        try { statement.execute(sql); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static void removeUserById(int id) {
        String sql =
                "REMOVE FROM 'users'" +
                "WHERE 'id' = " + id + ";";

        try { statement.execute(sql); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

}
