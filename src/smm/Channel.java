package smm;

import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;

public class Channel {
    public String channel;
    public PriorityBlockingQueue<Submission> submissionQueue;

    public Channel(String channel) {
        this.channel = channel;
        this.submissionQueue = new PriorityBlockingQueue<>();
    }

    public void submitLevel(Submission s) {
        if (!this.submissionQueue.contains(s))
            this.submissionQueue.offer(s);
    }

    public Submission getNextLevel(HashSet<String> viewers) {
        // assuming this iterates through in priority order
        //TODO: confirm the above statement
        for (Submission s : this.submissionQueue) {
            if (!s.present || viewers.contains(s.submitter)) {
                submissionQueue.remove(s);
                return s;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return this.channel.equals(((Channel) o).channel);
    }

    public void updatePriorities(HashSet<String> viewers, long priorityInterval) {
//        this.submissionQueue.stream()
//                .filter(s -> viewers.contains(s.submitter))
//                .forEach(s -> s.time -= priorityInterval);
        for (Submission s : submissionQueue) {
            if (viewers.contains(s.submitter))
                s.time -= priorityInterval * 10; //TODO: think about a good multiplier here
        }
    }
}
