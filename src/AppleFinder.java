import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
* Loads apple varieties from the Apples from New York catalog
* and recommands based on user preferences. 
*/
public class AppleFinder {
    private static final int OUTPUT_WIDTH = 80;
    private static final String LINK_BLUE = "\033[38;2;84;138;247m";
    private static final int QUIT = 0;
    private static final URI APPLES_FROM_NEW_YORK = URI.create(
            "https://www.applesfromny.com/varieties/");

    public static void run(HttpClient client, Scanner in, PrintStream out)
            throws IOException, InterruptedException {
        run(client, APPLES_FROM_NEW_YORK, in, out);
    }

    public static void run(HttpClient client, URI newYorkAppleUri, Scanner in, PrintStream out)
            throws IOException, InterruptedException {
        out.println("Welcome to Apple Finder!");
        out.println("Loading...");
        Document index = download(client, newYorkAppleUri);
        List<TasteProfile> profiles = loadProfiles(client, index);

        runSession(profiles, in, out, new AppleImages(client, outputWidth()));
    }

    public static List<Apple> fetchApples(HttpClient client)
            throws IOException, InterruptedException {
        return fetchApples(client, download(client, APPLES_FROM_NEW_YORK));
    }

    public static List<Apple> fetchApples(HttpClient client, Document index)
            throws IOException, InterruptedException {
        List<Apple> apples = new ArrayList<>();
        for (Element e : index.select(".apple-variety")) {
            String name = e.selectFirst(".copy h2").text();
            apples.add(readNewYorkApple(client, e, name));
        }
        return List.copyOf(apples);
    }

    private static Apple readNewYorkApple(HttpClient client, Element e, String name)
            throws IOException, InterruptedException {
        Element link = e.selectFirst(".copy a[href]:containsOwn(Learn More)");
        String pageUrl = link.absUrl("href");
        Document detail = download(client, URI.create(pageUrl));
        String description = parseDescription(detail, e.select(".copy > p").text());
        Element image = e.selectFirst(".image > img[src]");
        String imageUrl = image == null || image.attr("src").isBlank() ? "" : image.absUrl("src");
        return new Apple(name, detail.location(), description, imageUrl);
    }

    public static String parseDescription(Document detail, String teaser) {
        Element title = detail.selectFirst(".entry-content h1");
        List<String> paragraphs = new ArrayList<>();
        for (Element e : title.nextElementSiblings()) {
            if (e.is("h1, h2, h3, h4, h5, h6")) {
                break;
            }
            if (e.normalName().equals("p") && e.hasText()) {
                paragraphs.add(e.text());
            }
        }
        if (paragraphs.size() > 1 && paragraphs.getFirst().equals(teaser)) {
            paragraphs.removeFirst();
        }
        return String.join(" ", paragraphs);
    }

    private static List<TasteProfile> loadProfiles(HttpClient client, Document index)
            throws IOException, InterruptedException {
        List<TasteProfile> profiles = new ArrayList<>();
        for (Element e : index.select(".apple-variety")) {
            String name = e.selectFirst(".copy h2").text();
            Taste taste = readTaste(e, name);
            if (taste == null) {
                continue;
            }
            Apple apple = readNewYorkApple(client, e, name);
            profiles.add(new TasteProfile(apple, taste));
        }
        if (profiles.isEmpty()) {
            throw new IOException("No valid New York taste markers found on " + index.baseUri());
        }
        return List.copyOf(profiles);
    }

    public static Taste readTaste(Element e, String name) throws IOException {
        Taste taste = null;
        for (Element icon : e.select(".sweet-tart-scale .scale-icon")) {
            for (int position = 1; position <= 10; position++) {
                if (icon.hasClass("scale-" + position)) {
                    Taste group = Taste.marker(position);
                    if (taste != null && taste != group) {
                        throw new IOException("Conflicting taste markers for " + name);
                    }
                    taste = group;
                }
            }
        }
        return taste;
    }

    public static List<TasteProfile> recommend(List<TasteProfile> apples, Preferences preferences) {
        if (apples.isEmpty()) {
            throw new IllegalArgumentException("No varieties with taste profiles are available.");
        }
        List<TasteProfile> matches = new ArrayList<>();
        int best = Integer.MAX_VALUE;
        for (TasteProfile profile : apples) {
            int distance = tasteDistance(profile, preferences);
            if (distance < best) {
                best = distance;
                matches.clear();
            }
            if (distance == best) {
                insertByName(matches, profile);
            }
        }
        return List.copyOf(matches);
    }

    private static void insertByName(List<TasteProfile> matches, TasteProfile profile) {
        String name = profile.getApple().getName();
        for (int i = 0; i < matches.size(); i++) {
            String otherName = matches.get(i).getApple().getName();
            if (name.compareToIgnoreCase(otherName) < 0) {
                matches.add(i, profile);
                return;
            }
        }
        matches.add(profile);
    }

    private static int tasteDistance(TasteProfile apple, Preferences preferences) {
        Taste taste = apple.getTaste();
        int sweet = Math.abs(taste.getSweetness() - preferences.sweetness());
        int tart = Math.abs(taste.getTartness() - preferences.tartness());
        return sweet + tart;
    }

    private static void runSession(List<TasteProfile> profiles, Scanner in, PrintStream out,
                                   AppleImages images) throws IOException, InterruptedException {
        while (true) {
            Preferences preferences = readPreferences(in, out);
            if (preferences == null) {
                break;
            }
            List<TasteProfile> matches = recommend(profiles, preferences);
            out.printf("%nYour preferences: sweetness %d/4, tartness %d/4.%n",
                    preferences.sweetness(), preferences.tartness());
            if (tasteDistance(matches.getFirst(), preferences) > 0) {
                out.println("Showing the closest profiles:");
            } else {
                out.println(matches.size() == 1 ? "Closest match:" : "Equally close matches:");
            }
            browseMatches(matches, in, out, images);
        }
        out.println("Thank you!");
    }

    private static void browseMatches(List<TasteProfile> profiles, Scanner in, PrintStream out,
                                      AppleImages images) throws IOException, InterruptedException {
        for (int i = 0; i < profiles.size(); i++) {
            TasteProfile match = profiles.get(i);
            out.printf("%d. %s%n", i + 1, match.getApple().getName());
        }
        while (true) {
            int number = readNumber(in, out, "Choose a variety to read about",
                    profiles.size(), "return to preferences");
            if (number == QUIT) {
                return;
            }
            printRecommendation(profiles.get(number - 1), out, images);
        }
    }

    private static void printRecommendation(TasteProfile profile, PrintStream out, AppleImages images)
            throws InterruptedException {
        Apple apple = profile.getApple();
        Taste taste = profile.getTaste();
        out.println();

        try {
            out.println(images.artFor(apple));
        } catch (IOException e) {
            out.println("(Image unavailable)");
        }
        out.println();
        out.println("Recommended variety: " + apple.getName());
        out.printf("Taste: %s (sweetness %d/4, tartness %d/4).%n", taste.getLabel(), taste.getSweetness(), taste.getTartness());
        String url = apple.getPageUrl();
        if (AppleImages.terminalUsesColor()) {
            url = LINK_BLUE + url + AppleImages.DEFAULT_FOREGROUND;
        }
        out.println("Description (" + url + "):");
        for (String line : wrapText(apple.getDescription(), outputWidth())) {
            out.println(line);
        }
        out.println();
    }

    private static int outputWidth() {
        String columns = System.getenv("COLUMNS");
        if (columns != null) {
            try {
                int width = Integer.parseInt(columns.strip());
                if (width > 0) {
                    return width;
                }
            } catch (NumberFormatException e) {

            }
        }
        return OUTPUT_WIDTH;
    }

    public static List<String> wrapText(String text, int width) {
        if (width < 1) {
            throw new IllegalArgumentException("Output width must be positive.");
        }
        String remaining = text.strip().replaceAll("\\s+", " ");
        List<String> lines = new ArrayList<>();
        while (remaining.codePointCount(0, remaining.length()) > width) {
            int limit = remaining.offsetByCodePoints(0, width);
            int end = remaining.lastIndexOf(' ', limit);
            if (end <= 0) {
                end = limit;
            }
            lines.add(remaining.substring(0, end));
            remaining = remaining.substring(end).stripLeading();
        }
        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }
        return List.copyOf(lines);
    }

    public static Preferences readPreferences(Scanner in, PrintStream out)
            throws IOException {
        int sweetness = readNumber(in, out, "Preferred sweetness", 4, "exit AppleFinder");
        if (sweetness == QUIT) {
            return null;
        }
        int tartness = readNumber(in, out, "Preferred tartness", 4, "exit AppleFinder");
        if (tartness == QUIT) {
            return null;
        }
        return new Preferences(sweetness, tartness);
    }

    private static int readNumber(Scanner in, PrintStream out, String prompt,
                                  int maximum, String quitAction) throws IOException {
        while (true) {
            out.printf("%s (1-%d, or q to %s): ", prompt, maximum, quitAction);
            out.flush();
            if (!in.hasNextLine()) {
                if (in.ioException() != null) {
                    throw new IOException("Could not read terminal input.", in.ioException());
                }
                return QUIT;
            }
            String answer = in.nextLine().strip();
            if (answer.equalsIgnoreCase("q") || answer.equalsIgnoreCase("quit")) {
                return QUIT;
            }
            try {
                int number = Integer.parseInt(answer);
                if (number >= 1 && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException e) {

            }
            out.printf("Please enter a whole number from 1 to %d, or q to %s.%n", maximum, quitAction);
        }
    }

    public static List<Apple> search(List<Apple> apples, String keyword) {
        List<Apple> matches = new ArrayList<>();
        for (Apple apple : apples) {
            String description = apple.getDescription();
            for (int start = 0; start <= description.length() - keyword.length(); start++) {
                if (description.regionMatches(true, start, keyword, 0, keyword.length())) {
                    matches.add(apple);
                    break;
                }
            }
        }
        return List.copyOf(matches);
    }

    private static Document download(HttpClient client, URI uri)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/html")
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " from " + uri);
        }
        return Jsoup.parse(response.body(), response.uri().toString());
    }
}
