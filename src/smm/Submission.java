package smm;

public class Submission {
    String user, level;
    long time;

    public Submission(String user, String level) {
        this.user = user;
        this.level = level;
        this.time = System.currentTimeMillis();
    }

    public String toString() {
        return this.user + ": " + this.level + " at " + this.time;
    }
}
