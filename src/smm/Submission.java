package smm;

public class Submission {
    public String user, level;
    public long time;

    public Submission(String user, String level) {
        this.user = user;
        this.level = level;
        this.time = System.currentTimeMillis();
    }

    public Submission(String user, String level, long time) {
        this.user = user;
        this.level = level;
        this.time = time;
    }

    public String toString() {
        return this.level + " by " + this.user;
    }
}
