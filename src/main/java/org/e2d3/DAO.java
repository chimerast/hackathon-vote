package org.e2d3;

import java.net.UnknownHostException;
import java.util.List;

import org.mongojack.JacksonDBCollection;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class DAO {

    private static MongoClient CLIENT;

    static {
        try {
            CLIENT = new MongoClient();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DBCollection getCollection(String collectionName) {
        return CLIENT.getDB("hackathon").getCollection(collectionName);
    }

    private static JacksonDBCollection<User, String> getUsers() {
        return JacksonDBCollection.wrap(getCollection("users"), User.class, String.class);
    }

    private static JacksonDBCollection<Team, String> getTeams() {
        return JacksonDBCollection.wrap(getCollection("teams"), Team.class, String.class);
    }

    public static User getUser(String userId) {
        return getUsers().findOneById(userId);
    }

    public static Team getTeam(String teamId) {
        return getTeams().findOneById(teamId);
    }

    public static List<User> getUserList() {
        return getUsers().find().toArray();
    }

    public static List<Team> getTeamList() {
        return getTeams().find().toArray();
    }

    public static void save(User user) {
        getUsers().save(user);
    }

    public static void save(Team team) {
        getTeams().save(team);
    }

}
