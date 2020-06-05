package plugin.framework;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.function.Consumer;

public class Framework {
    public static void main(String[] args) {
        String filePath = "..pride-and-prejudice.txt";
        if (args.length >= 1) {
            filePath = args[0];
        } else {
            System.out.println("Warming: Reading default file pride-and-prejudice.txt...");
        }
        Scanner in = new Scanner(System.in);
        String name = in.nextLine();
        in.close();

        try {
            URL classUrl = new URL("file:///home/runner/262P/Week7/app/" + name + ".jar");
            URL[] classUrls = {classUrl};
            URLClassLoader cloader = new URLClassLoader(classUrls);
            Class cls = cloader.loadClass(name);
            Consumer app = (Consumer) cls.newInstance();
            app.accept(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
