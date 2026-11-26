/**
* Pairs an apple variety with its taste group to create a profile.  
*/
public final class TasteProfile {
    private final Apple apple;
    private final Taste taste;

    public TasteProfile(Apple apple, Taste taste) {
        this.apple = apple;
        this.taste = taste;
    }

    public Apple getApple() {
        return apple;
    }

    public Taste getTaste() {
        return taste;
    }

    @Override
    public String toString() {
        return "[" + apple + ", " + taste + "]";
    }
}
