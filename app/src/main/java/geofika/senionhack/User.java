package geofika.senionhack;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Alexander on 2017-09-09.
 */

@SuppressWarnings("serial")
public class User implements Serializable{

    private String mName = "";
    private String mZone = "";
    private String mTeam = "";

    private enum Team {
        RED, GREEN, BLUE
    };

    private Team randomLetter() {
        int pick = new Random().nextInt(Team.values().length);
        return Team.values()[pick];
    }

    User(String name){
        this.mName = name;

        String team = randomLetter().name();
        this.mTeam = team;
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

    public String getTeam() { return  mTeam; }
}
