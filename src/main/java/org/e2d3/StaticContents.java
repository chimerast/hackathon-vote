package org.e2d3;

public class StaticContents {

    public static String getUrl(Team team) {
        return "/static/" + team.id + "/";
    }

    public static String getUrl(String teamId) {
        return "/static/" + teamId + "/";
    }

}
