package org.vimal.utils;

import jakarta.mail.*;
import jakarta.mail.search.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class MailReaderUtility {
    private MailReaderUtility() {
    }

    private static final long DEFAULT_MAX_WAIT_MS = 60000;
    private static final long DEFAULT_POLL_INTERVAL_MS = 3000;
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private static final Pattern DEFAULT_OTP_PATTERN = Pattern.compile("\\b\\d{" + DEFAULT_OTP_LENGTH + "}\\b");
    private static final Set<String> DEFAULT_SEARCH_FOLDERS = Set.of("INBOX", "[Gmail]/Spam");

    public static String getToken(String email,
                                  String appPassword,
                                  String emailSubject) throws MessagingException, InterruptedException, IOException {
        return extractUuid(fetchParticularEmailContent(
                        email,
                        appPassword,
                        emailSubject,
                        DEFAULT_SEARCH_FOLDERS,
                        DEFAULT_MAX_WAIT_MS,
                        DEFAULT_POLL_INTERVAL_MS,
                        true,
                        true
                )
        );
    }

    private static String fetchParticularEmailContent(String email,
                                                      String appPassword,
                                                      String emailSubject,
                                                      Set<String> folders,
                                                      long maxWaitTimeMs,
                                                      long intervalTimeMs,
                                                      boolean seen,
                                                      boolean delete) throws MessagingException, InterruptedException, IOException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.timeout", "30000");
        props.put("mail.imaps.connectiontimeout", "30000");
        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        long searchStartTimeMillis = System.currentTimeMillis();
        Date searchStartTime = new Date(searchStartTimeMillis);
        Folder folder = null;
        try {
            store.connect(
                    email,
                    appPassword
            );
            while ((System.currentTimeMillis() - searchStartTimeMillis) < maxWaitTimeMs) {
                for (String folderName : folders) {
                    try {
                        folder = store.getFolder(folderName);
                        if (!folder.exists()) {
                            continue;
                        }
                        folder.open(Folder.READ_WRITE);
                        AndTerm searchTerm = new AndTerm(new SearchTerm[]{
                                new SubjectTerm(emailSubject),
                                new ReceivedDateTerm(
                                        ComparisonTerm.GE,
                                        searchStartTime
                                ),
                                new RecipientStringTerm(Message.RecipientType.TO, email)
                        });
                        Message[] messages = folder.search(searchTerm);
                        for (Message message : messages) {
                            if (message.getReceivedDate() != null &&
                                    message.getReceivedDate().before(searchStartTime)) {
                                continue;
                            }
                            String content = getTextFromMessage(message);
                            if (seen) {
                                message.setFlag(Flags.Flag.SEEN, true);
                            }
                            if (delete) {
                                message.setFlag(Flags.Flag.DELETED, true);
                                folder.expunge();
                            }
                            return content;
                        }
                    } finally {
                        if (folder != null &&
                                folder.isOpen()) {
                            folder.close(true);
                        }
                    }
                }
                log.info(
                        "No email found with subject '{}' yet, waiting for {} ms before retrying",
                        emailSubject,
                        intervalTimeMs
                );
                Thread.sleep(intervalTimeMs);
            }
            throw new RuntimeException("No email found with subject '" + emailSubject + "' after " + searchStartTime);
        } finally {
            if (store != null &&
                    store.isConnected()) {
                store.close();
            }
        }
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            BodyPart bodyPart;
            for (int i = 0; i < multipart.getCount(); i++) {
                bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                } else if (bodyPart.isMimeType("text/html")) {
                    return Jsoup.parse(bodyPart.getContent()
                                    .toString())
                            .text();
                }
            }
        }
        throw new RuntimeException("Unsupported message type");
    }

    private static String extractUuid(String content) {
        Matcher matcher = UUID_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new RuntimeException("Token not found in email content");
    }

    public static String getOtp(String email,
                                String appPassword,
                                String emailSubject)
            throws MessagingException, InterruptedException, IOException {
        return extractOtp(fetchParticularEmailContent(email,
                        appPassword,
                        emailSubject,
                        DEFAULT_SEARCH_FOLDERS,
                        DEFAULT_MAX_WAIT_MS,
                        DEFAULT_POLL_INTERVAL_MS,
                        true,
                        true
                )
        );
    }

    private static String extractOtp(String content) {
        Matcher matcher = DEFAULT_OTP_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new RuntimeException("Otp not found in email content");
    }
}
