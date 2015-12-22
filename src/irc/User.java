package irc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class User {
    public String name, Oauth;

    public User(String filename) {
        try {
            Scanner scan = new Scanner(new File(filename));
            this.name = scan.nextLine();
            this.Oauth = scan.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
