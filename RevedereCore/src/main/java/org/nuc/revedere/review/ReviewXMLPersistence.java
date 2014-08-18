package org.nuc.revedere.review;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.nuc.revedere.core.User;

public class ReviewXMLPersistence implements ReviewPersistence {
    private static final Logger LOGGER = Logger.getLogger(ReviewXMLPersistence.class);
    private final String reviewPersistencePath;
    private final List<Review> reviews = new ArrayList<>();
    private final List<Review> notClosedReviews = new ArrayList<>();
    private final ReviewDocumentSectionsManager sectionManager;
    private String lastReviewIndex = "!!!!";
    private String firstNotClosedReviewIndex = "~~~~";

    public ReviewXMLPersistence(String reviewPersistencePath, ReviewDocumentSectionsManager sectionManager) {
        this.reviewPersistencePath = reviewPersistencePath;
        this.sectionManager = sectionManager;
        loadReviews();
    }

    @Override
    public void save(Review review) {
        updateLastReviewIndex(review);
        reviews.add(review);
        save();

    }

    @Override
    public void update(Review review) {
        reviews.remove(review);
        updateFirstNotClosedReviewIndex(review);
        save(review);
    }

    @Override
    public List<Review> getReviews() {
        return reviews;
    }
    
    private void updateFirstNotClosedReviewIndex(Review review) {
        if (review.getState().equals(ReviewState.CLOSED)) {
            notClosedReviews.remove(review);
            firstNotClosedReviewIndex = "~~~~";
            for (Review notClosedReview : notClosedReviews) {
                if (notClosedReview.getID().compareTo(firstNotClosedReviewIndex) <0) {
                    firstNotClosedReviewIndex = notClosedReview.getID();
                }
            }
        }
    }

    private void loadReviews() {
        final File reviewsFile = new File(reviewPersistencePath);
        if (!reviewsFile.exists()) {
            save();
            LOGGER.info("Created persistence file");
            return;
        }

        try {
            final Document document = new SAXBuilder().build(reviewsFile);
            final Element rootNode = document.getRootElement();
            for (Element reviewElement : rootNode.getChildren("review")) {
                final Review review = extractReview(reviewElement);
                reviews.add(review);
                if (!review.getState().equals(ReviewState.CLOSED)) {
                    notClosedReviews.add(review);
                }
                updateLastReviewIndex(review);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load reviews " + e);
        }

    }

    private void updateLastReviewIndex(final Review review) {
        if (review.getID().compareTo(lastReviewIndex) > 0) {
            lastReviewIndex = review.getID();
        }
    }

    private Review extractReview(Element reviewElement) {
        final String reviewID = reviewElement.getAttributeValue("id");
        final String sourceUser = reviewElement.getAttributeValue("source");
        final String destinationUser = reviewElement.getAttributeValue("destination");
        final String reviewStateString = reviewElement.getAttributeValue("state");
        final ReviewState reviewState = ReviewState.valueOf(reviewStateString);

        final ReviewDocument reviewDocument = extractReviewDocument(reviewElement);
        final ReviewData reviewData = extractReviewData(reviewElement);

        return new Review(new User(sourceUser), new User(destinationUser), reviewData, reviewDocument, reviewID, reviewState);
    }

    private ReviewDocument extractReviewDocument(final Element reviewElement) {
        final Element reviewDocumentElement = reviewElement.getChild("document");
        final ReviewDocument reviewDocument = new ReviewDocument();
        for (Element section : reviewDocumentElement.getChildren("section")) {
            final String sectionNameString = section.getAttributeValue("name");
            final ReviewDocumentSection documentSection = sectionManager.getSection(sectionNameString);
            final String sectionContent = section.getText();
            reviewDocument.addSection(documentSection, sectionContent);
        }
        return reviewDocument;
    }

    private ReviewData extractReviewData(Element reviewElement) {
        final Element reviewDataElement = reviewElement.getChild("data");
        final List<ReviewFile> reviewFiles = new ArrayList<>();
        for (Element reviewFileElement : reviewDataElement.getChildren("file")) {
            reviewFiles.add(extractReviewFile(reviewFileElement));
        }

        final List<String> folders = new ArrayList<>();
        for (Element reviewFolderElement : reviewDataElement.getChildren("folder")) {
            folders.add(reviewFolderElement.getText());
        }
        return new ReviewData(reviewFiles, folders);
    }

    private ReviewFile extractReviewFile(Element reviewFileElement) {
        final String fileRelativePath = reviewFileElement.getAttributeValue("relativePath");
        final String content = reviewFileElement.getChild("content").getText();
        final ReviewFile reviewFile = new ReviewFile(fileRelativePath, content);
        final Element commentsElement = reviewFileElement.getChild("comments");
        for (Element commentElement : commentsElement.getChildren("comment")) {
            final int offset = Integer.parseInt(commentElement.getAttributeValue("offset"));
            final int length = Integer.parseInt(commentElement.getAttributeValue("length"));
            final String commentContent = commentElement.getText();
            final ReviewComment reviewComment = new ReviewComment(offset, length, commentContent);
            reviewFile.addComment(reviewComment);
        }
        return reviewFile;
    }

    private void save() {
        final File persistenceFile = new File(reviewPersistencePath);
        final Element rootElement = new Element("root");
        final Document document = new Document(rootElement);
        for (Review review : reviews) {
            final Element reviewElement = buildElement(review);
            rootElement.addContent(reviewElement);
        }

        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(document, new FileWriter(persistenceFile));

        } catch (Exception e) {
            LOGGER.error("Failed to save message file", e);
        }

    }

    private Element buildElement(Review review) {
        final Element reviewElement = new Element("review");
        reviewElement.setAttribute("id", review.getID());
        reviewElement.setAttribute("source", review.getSourceUser().getUsername());
        reviewElement.setAttribute("destination", review.getDestinationUser().getUsername());
        reviewElement.setAttribute("state", review.getState().toString());

        final Element reviewDocument = buildElement(review.getReviewDocument());
        reviewElement.addContent(reviewDocument);
        final Element reviewData = buildElement(review.getData());
        reviewElement.addContent(reviewData);
        return reviewElement;
    }

    private Element buildElement(ReviewDocument reviewDocument) {
        final Element reviewDocumentElement = new Element("document");
        for (ReviewDocumentSection section : reviewDocument.getSections()) {
            final Element sectionElement = new Element("section");
            sectionElement.setAttribute("name", section.getSectionName());
            sectionElement.setText(reviewDocument.getSectionText(section));
            reviewDocumentElement.addContent(sectionElement);
        }
        return reviewDocumentElement;
    }

    private Element buildElement(ReviewData data) {
        final Element reviewDataElement = new Element("data");
        for (ReviewFile reviewFile : data.getReviewFiles()) {
            reviewDataElement.addContent(buildElement(reviewFile));
        }

        for (String folder : data.getFolders()) {
            final Element folderElement = new Element("folder");
            folderElement.setText(folder);
            reviewDataElement.addContent(folderElement);
        }

        return reviewDataElement;
    }

    private Element buildElement(ReviewFile reviewFile) {
        final Element reviewFileElement = new Element("file");
        reviewFileElement.setAttribute("relativePath", reviewFile.getFileRelativePath());

        final Element contentElement = new Element("content");
        contentElement.setText(reviewFile.getFileContent());
        reviewFileElement.addContent(contentElement);

        final Element commentsElement = new Element("comments");
        reviewFileElement.addContent(commentsElement);

        for (ReviewComment reviewComment : reviewFile.getComments()) {
            final Element reviewCommentElement = new Element("comment");
            reviewCommentElement.setAttribute("offset", reviewComment.getSelectionOffset() + "");
            reviewCommentElement.setAttribute("length", reviewComment.getSelectionLength() + "");
            reviewCommentElement.setText(reviewComment.getComment());
        }
        return reviewFileElement;
    }

    @Override
    public String getLastReviewIndex() {
        return lastReviewIndex;
    }
    
    @Override
    public String getFirstNotClosedReviewIndex() {
        return firstNotClosedReviewIndex;
    }
}
