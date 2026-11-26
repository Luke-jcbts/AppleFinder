/**
* Represents a fetched apple variety.
* 
* The class stores information about the variety,
* including its name, url, and website description.
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
