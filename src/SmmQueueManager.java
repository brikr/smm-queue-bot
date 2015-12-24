import irc.SMMQueueBot;
import irc.User;

public class SmmQueueManager {
    public static void main(String[] args) {
        User botUser = new User("userinfo");

        SMMQueueBot bot = new SMMQueueBot(botUser);
        System.out.println("I am ready.");

        boolean on = true;

        try {
            while (on) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            bot.checkpoint();
            e.printStackTrace();
        }
    }
}
