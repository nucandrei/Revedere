package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.List;

public class ReviewData implements Serializable {
    private static final long serialVersionUID = 5452721936434398370L;
    private final List<ReviewFile> reviewFiles;
    private final List<String> folders;

    public ReviewData(List<ReviewFile> reviewFiles, List<String> folders) {
        this.reviewFiles = reviewFiles;
        this.folders = folders;
    }

    public List<ReviewFile> getReviewFiles() {
        return this.reviewFiles;
    }

    public List<String> getFolders() {
        return this.folders;
    }
}
