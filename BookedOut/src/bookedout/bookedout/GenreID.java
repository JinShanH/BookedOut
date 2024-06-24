package bookedout;

/**
 * Enumerator for Genres
 * @author T10A-BookedOut
 */
public enum GenreID {
    MYSTERY(0, "MYSTERY"),
    FICTION(1, "FICTION"),
    NONFICTION(2, "NONFICTION"),
    THRILLER(3, "THRILLER"),
    ROMANCE(4, "ROMANCE"),
    HORROR(5, "HORROR"),
    HISTORY(6, "HISTORY"),
    CLASSIC(7, "CLASSIC"),
    CRIME(8, "CRIME"),
    FANTASY(9, "FANTASY"),
    COMEDY(10, "COMEDY");

    private int id;
    private String type;

    private GenreID(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getID() {
        return id;
    }

    public String getType() {
        return type;
    }

    public static String getType(int id) {
        for (GenreID genre : GenreID.values()) {
            if (genre.getID() == id) {
                return genre.getType();
            }
        }
        return null;
    }

    public static int getID(String type) {
        for (GenreID genre : GenreID.values()) {
            if (genre.getType().equals(type)) {
                return genre.getID();
            }
        }
        return -1;
    }
}