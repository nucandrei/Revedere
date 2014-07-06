package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReviewFile implements Serializable {
    private static final long serialVersionUID = -8992154190093388403L;
    private final String fileContent;
    private final String fileRelativePath;
    private final List<ReviewComment> comments;

    public ReviewFile(String fileRelativePath, String fileContent) {
        this.fileRelativePath = fileRelativePath;
        this.fileContent = fileContent;
        this.comments = new ArrayList<>();
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileRelativePath() {
        return fileRelativePath;
    }

    public void addComment(ReviewComment reviewComment) {
        this.comments.add(reviewComment);
    }

    public List<ReviewComment> getComments() {
        return this.comments;
    }
}
