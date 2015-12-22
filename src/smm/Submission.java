package smm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Submission {
    public String user, level;
    public long time;

    public Submission(String user, String level) {
        this.user = user;
        this.level = level;
        this.time = System.currentTimeMillis();
    }

    public String toString() {
        return this.level + " by " + this.user;
    }
}
