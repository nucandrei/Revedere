package org.nuc.revedere.review;

import java.io.Serializable;

public class ReviewDocumentSection implements Serializable {
    private static final long serialVersionUID = 4595996493510102337L;
    private final String sectionName;
    private final boolean isMandatory;
    private final String defaultValue;

    public ReviewDocumentSection(String sectionName, boolean isMandatory, String defaultValue) {
        this.sectionName = sectionName;
        this.isMandatory = isMandatory;
        this.defaultValue = defaultValue;
    }

    public String getSectionName() {
        return sectionName;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ReviewDocumentSection)) {
            return false;
        }
        final ReviewDocumentSection that = (ReviewDocumentSection) object;
        return this.sectionName.equals(that.sectionName);
    }

    @Override
    public int hashCode() {
        return this.sectionName.hashCode();
    }
}
