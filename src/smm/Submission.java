package smm;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class Submission implements Comparable {
    public String submitter, id, creator, title;
    public long time;
    public double clearRate;
    public boolean present;

    public Submission(String submitter, String level, long time, boolean present) {
        this.submitter = submitter;
        this.id = level;
        this.time = time;
        this.present = present;

        try {
            String raw = new Scanner(new URL("http://smm.butthole.tv/course/" + level).openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONObject json = new JSONObject(raw);
            this.creator = json.getJSONObject("creator").getString("display_name");
            this.clearRate = json.getDouble("clear_rate");
            this.title = json.getString("title");
        } catch (IOException e) {
//            e.printStackTrace();
            // bad level, set id to null so bot can ignore it
            this.id = null;
        }
    }

    public Submission(String submitter, String level, boolean present) {
        this(submitter, level, System.currentTimeMillis(), present);
    }

    public String toString() {
        return String.format("%s by %s. ID: %s. Clear Rate: %.3f%%",
                this.title,
                this.creator,
                this.id,
                this.clearRate * 100);
    }

    @Override
    public boolean equals(Object o) {
        return this.id.equals(((Submission) o).id);
    }

    @Override
    public int compareTo(Object o) {
        Submission s = (Submission) o;
        return (int) (this.time - s.time);
    }
}
