package org.nuc.revedere.shortmessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.nuc.revedere.core.User;
import org.nuc.revedere.shortmessage.ShortMessage;

public class MessageBoxXMLPersistence implements MessageBoxPersistence {
    private static final Logger LOGGER = Logger.getLogger(MessageBoxXMLPersistence.class);
    private final String messageBoxPath;
    private final Map<String, List<ShortMessage>> unreadMessages = new HashMap<>();
    private final Map<String, List<ShortMessage>> readMessages = new HashMap<>();
    private final Map<String, List<ShortMessage>> sentMessages = new HashMap<>();

    public MessageBoxXMLPersistence(String messageBoxPath) {
        this.messageBoxPath = messageBoxPath;
        loadData();
    }

    public void save(ShortMessage message, String messageBoxName) {
        if (message.getSender().equals(messageBoxName)) {
            sentMessages.get(messageBoxName).add(message);
        } else if (message.isRead()) {
            readMessages.get(messageBoxName).add(message);
        } else {
            unreadMessages.get(messageBoxName).add(message);
        }
        save();
    }

    public void update(ShortMessage message, String messageBoxName) {
        save();

    }

    public List<ShortMessage> getReadMessages(String messageBoxName) {
        List<ShortMessage> readMessagesForMessageBoxName = readMessages.get(messageBoxName);
        if (readMessagesForMessageBoxName == null) {
            readMessagesForMessageBoxName = new ArrayList<>();
            readMessages.put(messageBoxName, readMessagesForMessageBoxName);
            save();
        }
        return readMessagesForMessageBoxName;
    }

    public List<ShortMessage> getUnreadMessages(String messageBoxName) {
        List<ShortMessage> unreadMessagesForMessageBoxName = unreadMessages.get(messageBoxName);
        if (unreadMessagesForMessageBoxName == null) {
            unreadMessagesForMessageBoxName = new ArrayList<>();
            unreadMessages.put(messageBoxName, unreadMessagesForMessageBoxName);
            save();
        }
        return unreadMessagesForMessageBoxName;
    }

    public List<ShortMessage> getSentMessages(String messageBoxName) {
        List<ShortMessage> sentMessagesForMessageBoxName = readMessages.get(messageBoxName);
        if (sentMessagesForMessageBoxName == null) {
            sentMessagesForMessageBoxName = new ArrayList<>();
            sentMessages.put(messageBoxName, sentMessagesForMessageBoxName);
            save();
        }
        return sentMessagesForMessageBoxName;
    }

    public void clear(String msgBoxName) {
        unreadMessages.put(msgBoxName, new ArrayList<ShortMessage>());
        readMessages.put(msgBoxName, new ArrayList<ShortMessage>());
        sentMessages.put(msgBoxName, new ArrayList<ShortMessage>());
        save();
    }

    private void loadData() {
        File messagesFile = new File(messageBoxPath);
        try {
            final Document document = new SAXBuilder().build(messagesFile);
            final Element rootNode = document.getRootElement();
            for (Element messageBox : rootNode.getChildren("messagebox")) {
                final String messageboxName = messageBox.getAttributeValue("name");
                unreadMessages.put(messageboxName, new ArrayList<ShortMessage>());
                readMessages.put(messageboxName, new ArrayList<ShortMessage>());
                sentMessages.put(messageboxName, new ArrayList<ShortMessage>());

                for (Element message : messageBox.getChildren("message")) {
                    final String type = message.getAttributeValue("type");
                    final int timestamp = Integer.parseInt(message.getAttributeValue("timestamp"));
                    final String content = message.getChildText("content");
                    final String from = message.getChildText("from");
                    final String to = message.getChildText("to");
                    final User fromUser = new User(from);
                    final User toUser = new User(to);
                    final ShortMessage shortMessage = new ShortMessage(fromUser, toUser, content, timestamp);
                    if ("read".equals(type)) {
                        shortMessage.markAsRead();
                    }
                    if (shortMessage.getSender().getUsername().equals(messageboxName)) {
                        sentMessages.get(messageboxName).add(shortMessage);
                    } else if (shortMessage.isRead()) {
                        readMessages.get(messageboxName).add(shortMessage);
                    } else {
                        unreadMessages.get(messageboxName).add(shortMessage);
                    }
                }

                final int unread = unreadMessages.get(messageboxName).size();
                final int read = readMessages.get(messageboxName).size();
                final int sent = sentMessages.get(messageboxName).size();

                LOGGER.info(String.format("Messagebox %s loaded : Unread(%s), Read(%s), Sent(%s)", messageboxName, unread, read, sent));
            }
            LOGGER.info("Finished loading message persistence");
        } catch (JDOMException | IOException exception) {
            LOGGER.error("Error while loading the messages file", exception);
        }
    }

    private void save() {
        final Set<String> messageBoxes = new HashSet<>();
        messageBoxes.addAll(unreadMessages.keySet());
        messageBoxes.addAll(readMessages.keySet());
        messageBoxes.addAll(sentMessages.keySet());

        final File messagesFile = new File(messageBoxPath);
        final Element rootElement = new Element("root");
        final Document document = new Document(rootElement);
        for (String messageBoxName : messageBoxes) {
            final Element messageBoxElement = new Element("messagebox");
            rootElement.addContent(messageBoxElement);
            messageBoxElement.setAttribute("name", messageBoxName);
            if (readMessages.get(messageBoxName) != null) {
                for (ShortMessage readMessage : readMessages.get(messageBoxName)) {
                    messageBoxElement.addContent(generateElement(readMessage, "read"));
                }
            }

            if (unreadMessages.get(messageBoxName) != null) {
                for (ShortMessage unreadMessage : unreadMessages.get(messageBoxName)) {
                    messageBoxElement.addContent(generateElement(unreadMessage, "unread"));
                }
            }

            if (sentMessages.get(messageBoxName) != null) {
                for (ShortMessage sentMessage : sentMessages.get(messageBoxName)) {
                    messageBoxElement.addContent(generateElement(sentMessage, "sent"));
                }
            }
        }

        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(document, new FileWriter(messagesFile));
        } catch (Exception e) {
            LOGGER.error("Could not save message file", e);
        }
    }

    private Element generateElement(ShortMessage shortMessage, String type) {
        final Element message = new Element("message");
        message.setAttribute("type", type);
        message.setAttribute("timestamp", shortMessage.getTimestamp() + "");

        final Element content = new Element("content");
        content.setText(shortMessage.getContent());
        message.addContent(content);

        final Element fromElement = new Element("from");
        fromElement.setText(shortMessage.getSender().getUsername());
        message.addContent(fromElement);

        final Element toElement = new Element("to");
        toElement.setText(shortMessage.getReceiver().getUsername());
        message.addContent(toElement);
        return message;
    }

}
