import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

/**
 * Starts an AppleFinder session using the terminal’s input.
 */
public class Main {
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8);

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
             Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            AppleFinder.run(client, input, out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            err.println("Thread interrupted while loading source data. Please try again.");
        } catch (IOException | UncheckedIOException e) {
            err.println("Could not load source data or read input: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            err.println("Could not interpret source data: " + e.getMessage());
        }
    }
}
