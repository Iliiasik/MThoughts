package mt.client.model;

public enum SlideCategory {
    FACT("facts"),
    LORE("lore"),
    SURREAL("surreal"),
    SPECIAL("special"),
    NIGHTMARE("nightmares");

    private final String fileName;

    SlideCategory(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}
