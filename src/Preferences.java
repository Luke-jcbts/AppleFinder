/**
* Stores the user's preferred sweetness and tartness levels, each from 1 to 4. 
*/ 
public final class Preferences {
    private final int sweetness;
    private final int tartness;

    public Preferences(int sweetness, int tartness) {
        if (sweetness < 1 || sweetness > 4 || tartness < 1 || tartness > 4) {
            throw new IllegalArgumentException("Please enter a whole number from 1 to 4");
        }

        this.sweetness = sweetness;
        this.tartness = tartness;
    }

    public int sweetness() {
        return sweetness;
    }

    public int tartness() {
        return tartness;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", sweetness, tartness);
    }
}
