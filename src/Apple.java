/**
* Represents an apple variety and stores its information.
*/
public final class Apple {
    private final String name;
    private final String pageUrl;
    private final String description;

    public Apple(String name, String pageUrl, String description) {
        this.name = name;
        this.pageUrl = pageUrl;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", name, pageUrl, description);
    }
}
