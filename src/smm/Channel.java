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
        this.submissionQueue.offer(s);
    }

    public Submission getNextLevel() {
        return this.submissionQueue.poll();
    }

    @Override
    public boolean equals(Object o) {
        return this.channel.equals(((Channel) o).channel);
    }

    public void updatePriorities(HashSet<String> viewers, long priorityInterval) {
//        this.submissionQueue.stream()
//                .filter(s -> viewers.contains(s.user))
//                .forEach(s -> s.time -= priorityInterval);
        for (Submission s : submissionQueue) {
            if (viewers.contains(s.user)) s.time -= priorityInterval * 10; //TODO: think about a good multiplier here
        }
    }
}
