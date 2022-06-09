package com.zpedroo.voltzevents.mysql;

import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.objects.event.SpecialItem;
import com.zpedroo.voltzevents.utils.serialization.ItemStatusSerialization;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class DBManager {

    public void savePlayerData(PlayerData data) {
        String query = "REPLACE INTO `" + DBConnection.TABLE + "` (`uuid`, `wins`, `participations`, `items_status`) VALUES " +
                "('" + data.getUniqueId() + "', " +
                "'" + data.getWinsAmount() + "', " +
                "'" + data.getParticipationsAmount() + "', " +
                "'" + ItemStatusSerialization.serialize(data.getSpecialItemsStatus()) + "');";
        executeUpdate(query);
    }

    public PlayerData getPlayerDataFromDatabase(Player player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + player.getUniqueId() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                int winsAmount = result.getInt(2);
                int participationsAmount = result.getInt(3);
                Map<SpecialItem, Boolean> specialItemsStatus = ItemStatusSerialization.deserialize(result.getString(4));

                return new PlayerData(player.getUniqueId(), winsAmount, participationsAmount, specialItemsStatus);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return new PlayerData(player.getUniqueId(), 0, 0, new HashMap<>(2));
    }

    public List<PlayerData> getTopWinsFromDatabase() {
        List<PlayerData> top = new ArrayList<>(10);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` ORDER BY `wins` DESC LIMIT 10;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                int winsAmount = result.getInt(2);
                int participationsAmount = result.getInt(3);
                Map<SpecialItem, Boolean> specialItemsStatus = ItemStatusSerialization.deserialize(result.getString(4));

                top.add(new PlayerData(uuid, winsAmount, participationsAmount, specialItemsStatus));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return top;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255), `wins` INTEGER, `participations` INTEGER, `items_status` VARCHAR(100), PRIMARY KEY(`uuid`));");
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}