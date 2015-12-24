package smm;

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
}
