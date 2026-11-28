/**
* Represents an apple variety and stores its information.
*/
public final class Apple {
    private final String name;
    private final String pageUrl;
    private final String description;
    private final String imageUrl;
    
    public Apple(String name, String pageUrl, String description) {
        this(name, pageUrl, description, "");
    }
    
    public Apple(String name, String pageUrl, String description, String imageUrl) {
        this.name = name;
        this.pageUrl = pageUrl;
        this.description = description;
        this.imageUrl = imageUrl;
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", name, pageUrl, description);
    }
}
