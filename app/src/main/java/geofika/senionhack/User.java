package geofika.senionhack;

import java.io.Serializable;

/**
 * Created by Alexander on 2017-09-09.
 */

@SuppressWarnings("serial")
public class User implements Serializable{

    private String mName = "";
    private String mZone = "";
    private int score;

    User(String name){
        this.mName =  name;
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
