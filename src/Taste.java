/**
* Each new Taste object has a shared label, sweetness score, 
* and tartness score per group. They are final to prevent 
* reassignment. Mutiple apples can refer to the same Taste object. 
* 
* Groups are decided based on four custom ranges from 1 to 10,
* since the website only provides a specturm with a bar showing
* how sweet/tart the apple variety is. 
*
*/
public final class Taste {
    public static final Taste VERY_TART = new Taste("Very Tart", 1, 4);
    public static final Taste BALANCED = new Taste("Balanced (Sweet-Tart)", 2, 3);
    public static final Taste MILDLY_SWEET = new Taste("Mildly Sweet", 3, 2);
    public static final Taste VERY_SWEET = new Taste("Very Sweet", 4, 1);

    private final String label;
    private final int sweetness;
    private final int tartness;

    private Taste(String label, int sweetness, int tartness) {
        this.label = label;
        this.sweetness = sweetness;
        this.tartness = tartness;
    }

    public String getLabel() {
        return label;
    }

    public int getSweetness() {
        return sweetness;
    }

    public int getTartness() {
        return tartness;
    }

    public static Taste[] values() {
        return new Taste[] { VERY_TART, BALANCED, MILDLY_SWEET, VERY_SWEET };
    }

    static Taste marker(int position) {
        if (position < 1 || position > 10) {
            throw new IllegalArgumentException("Marker position must be from 1 to 10.");
        }
        if (position <= 2) {
            return VERY_SWEET;
        }
        if (position <= 4) {
            return MILDLY_SWEET;
        }
        if (position <= 6) {
            return BALANCED;
        }
        return VERY_TART;
    }

    @Override
    public String toString() {
        return label;
    }
}
