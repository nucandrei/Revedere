package org.nuc.revedere.review;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReviewDocumentSectionsManager {
    private final Map<String, ReviewDocumentSection> sections;

    public ReviewDocumentSectionsManager(Set<ReviewDocumentSection> sections) {
        this.sections = new HashMap<>();
        for (ReviewDocumentSection section : sections) {
            this.sections.put(section.getSectionName(), section);
        }
    }

    public ReviewDocumentSection getSection(String sectionName) {
        return sections.get(sectionName);
    }

}
