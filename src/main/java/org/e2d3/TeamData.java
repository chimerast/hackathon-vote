package org.e2d3;

public class TeamData {

    public static void main(String[] args) {
        for (int i = 0; i < 20; ++i) {
            String teamId = Character.toString((char) ('A' + i));

            Team team = new Team();
            team.id = teamId;
            team.title = "名称未設定";

            DAO.save(team);
        }
    }

}
