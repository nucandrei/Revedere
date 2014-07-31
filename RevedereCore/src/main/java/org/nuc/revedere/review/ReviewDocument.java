package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReviewDocument implements Serializable {
    private static final long serialVersionUID = 8332498755999815354L;
    private final Map<ReviewDocumentSection, String> sections;

    public static final ReviewDocumentSection USER_SECTION = new ReviewDocumentSection("User", true, "", 1);
    public static final ReviewDocumentSection PROJECT_NAME_SECTION = new ReviewDocumentSection("Project name", true, "", 1);

    public ReviewDocument() {
        sections = new HashMap<>();
    }

    public void addSection(ReviewDocumentSection sectionName, String sectionText) {
        sections.put(sectionName, sectionText);
    }

    public String getSectionText(ReviewDocumentSection sectionName) {
        return sections.get(sectionName);
    }

    public Set<ReviewDocumentSection> getSections() {
        return sections.keySet();
    }
}
