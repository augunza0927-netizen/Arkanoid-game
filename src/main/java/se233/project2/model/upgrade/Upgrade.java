package se233.project2.model.upgrade;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.state.GameState;


public abstract class Upgrade {

    private final UpgradeType type;
    private final String title;
    private final String description;
    private final int cardSpriteIndex;

    //Inherited
    protected Upgrade(UpgradeType type, String title, String description, int cardSpriteIndex) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.cardSpriteIndex = cardSpriteIndex;
    }

    protected Upgrade(UpgradeType type, String title, String description) {
        this(type, title, description, 0);
    }

    protected void validateState(GameState state) {
        if (state == null) {
            throw new InvalidGameStateException("Cannot apply Upgrade [" + title + "] to a null GameState");
        }
    }

    public abstract void apply(GameState state);

    public UpgradeType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCardSpriteIndex() {
        return cardSpriteIndex;
    }
}
