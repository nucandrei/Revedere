package org.nuc.revedere.review;

import java.io.Serializable;

public class ReviewComment implements Serializable {
    private static final long serialVersionUID = -2586774114797140410L;

    private final int selectionStartingPosition;
    private final int selectionEndingPosition;
    private final String comment;

    public ReviewComment(int startingPosition, int endingPosition, String comment) {
        this.selectionStartingPosition = startingPosition;
        this.selectionEndingPosition = endingPosition;
        this.comment = comment;
    }

    public int getSelectionStartingPosition() {
        return selectionStartingPosition;
    }

    public int getSelectionEndingPosition() {
        return selectionEndingPosition;
    }

    public String getComment() {
        return comment;
    }
}
