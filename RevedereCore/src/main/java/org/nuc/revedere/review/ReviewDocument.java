package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReviewDocument implements Serializable {
    private static final long serialVersionUID = 8332498755999815354L;
    private final Map<ReviewDocumentSection, String> sections;

    public ReviewDocument() {
        sections = new HashMap<>();
    }

    public void addSection(ReviewDocumentSection sectionName, String sectionText) {
        sections.put(sectionName, sectionText);
    }

    public String getSectionText(ReviewDocumentSection sectionName) {
        return sections.get(sectionName);
    }
}
