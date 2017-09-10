package geofika.senionhack;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Alexander on 2017-09-09.
 */

@SuppressWarnings("serial")
public class User implements Serializable{

    private String mName = "";
    private String mZone = "None";
    private String mTeam = "";
    private String id = "";
    private String teamID = "";

    public int getScore() {
        return score;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public String getTeamID() {
        return teamID;
    }

    private enum Team {
        RED, GREEN, BLUE
    };

    private Team randomLetter() {
        int pick = new Random().nextInt(Team.values().length);
        return Team.values()[pick];
    }
    private int score;

    User(String name){
        this.mName = name;

        //String team = randomLetter().name();
        //this.mTeam = team;
    }

    public void setZone(String mZone) {
        this.mZone = mZone;
    }

    public String getZone() {
        return mZone;
    }

    public String getName() {
        return mName;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
