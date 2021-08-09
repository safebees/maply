import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileUtils {


    public static void write(String fileDestination, String content) {

        try (PrintWriter out = new PrintWriter(fileDestination)) {

            out.println(content);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
