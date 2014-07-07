package org.nuc.revedere.review;

import java.io.Serializable;

public class ReviewComment implements Serializable {
    private static final long serialVersionUID = -2586774114797140410L;

    private final int selectionOffset;
    private final int selectionLength;
    private final String comment;

    public ReviewComment(int offset, int length, String comment) {
        this.selectionOffset = offset;
        this.selectionLength = length;
        this.comment = comment;
    }

    public int getSelectionOffset() {
        return selectionOffset;
    }

    public int getSelectionLength() {
        return selectionLength;
    }

    public String getComment() {
        return comment;
    }
}
