import irc.SMMQueueBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SmmQueueManager {
    public static void main(String[] args) {
        String[] botUser = getUser("userinfo");

        SMMQueueBot bot = new SMMQueueBot(botUser[0], botUser[1]);
        System.out.println("I am ready.");

        try {
            while (true) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            bot.checkpoint();
            e.printStackTrace();
        }
    }

    private static String[] getUser(String filename) {
        String[] user = new String[2];
        try {
            Scanner scan = new Scanner(new File(filename));
            user[0] = scan.nextLine();
            user[1] = scan.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return user;
    }
}
