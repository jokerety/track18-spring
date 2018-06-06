package ru.track.prefork;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database implements ConversationService {
    Connection[] connections = new Connection[3];

    Database()
    {
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());

            String url = "jdbc:mysql://tdb-%d.trail5.net:3306/track17?user=track_student&password=7EsH.H6x";

            connections[0] = DriverManager.getConnection(String.format(url, 1));
            connections[1] = DriverManager.getConnection(String.format(url, 2));
            connections[2] = DriverManager.getConnection(String.format(url, 3));
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    Connection fromName(String name)
    {
        char firstSymbol = Character.toUpperCase(name.charAt(0));
        if ((firstSymbol <= 'J') && (firstSymbol >= 'A'))
        {
            return connections[0];
        }
        if ((firstSymbol <= 'T') && (firstSymbol >= 'K'))
        {
            return connections[1];
        }
        if ((firstSymbol <= 'Z') && (firstSymbol >= 'U'))
        {
            return connections[2];
        }
        return null;
    }

    @Override
    public synchronized long store(Message msg) {
        try {
            Connection currentConnection = fromName(msg.getSenderName());

            String query = "INSERT INTO messages (user_name, text, ts) VALUES (?, ?, ?)";
            PreparedStatement stmt = currentConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, msg.getSenderName());
            stmt.setString(2, msg.getData());
            stmt.setTimestamp(3, new Timestamp(msg.getTime()));
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return  generatedKeys.getLong(1);
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public synchronized List<Message> getHistory(long from, long to, long limit) {
        try {
            List<Message> messages = new ArrayList<>();
            String query = "SELECT * FROM messages WHERE ts>=? AND ts<? ORDER BY ts LIMIT ?";

            for (Connection connection : connections) {
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setTimestamp(1,new Timestamp(from));
                stmt.setTimestamp(2,new Timestamp(to));
                stmt.setLong(3,limit);

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    messages.add(new Message(resultSet.getString(3), resultSet.getLong(4), resultSet.getString(2)));
                }
            }
            messages.sort((a,b) -> (int)(a.getTime()- b.getTime()));
            return messages;

        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized List<Message> getByUser(String username, long limit) {
        try {
            String query = "SELECT * FROM messages WHERE user_name=? ORDER BY ts LIMIT ?";
            PreparedStatement stmt =  fromName(username).prepareStatement(query);
            stmt.setString(1, username);
            stmt.setLong(2, limit);

            ResultSet resultSet = stmt.executeQuery();
            List<Message> messages = new ArrayList<>();

            while (resultSet.next()) {
                messages.add(new Message(resultSet.getString(3), resultSet.getLong(4), resultSet.getString(2)));
            }

            return messages;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
