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

    private static final Pattern UUID_PATTERN = Pattern.compile("([a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12})", Pattern.CASE_INSENSITIVE);
    private static final long DEFAULT_MAX_WAIT_MS = 60000;
    private static final long DEFAULT_POLL_INTERVAL_MS = 3000;
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final Pattern DEFAULT_OTP_PATTERN = Pattern.compile("\\b\\d{" + DEFAULT_OTP_LENGTH + "}\\b");
    private static final Set<String> DEFAULT_SEARCH_FOLDERS = Set.of("INBOX", "[Gmail]/Spam");

    public static String getToken(String email,
                                  String appPassword,
                                  String emailSubject)
            throws MessagingException, InterruptedException, IOException {
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
                                                      boolean delete)
            throws MessagingException, InterruptedException, IOException {
        validateArguments(
                email,
                appPassword,
                emailSubject,
                folders,
                maxWaitTimeMs,
                intervalTimeMs
        );
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

    public static void validateArguments(String email,
                                         String appPassword,
                                         String emailSubject,
                                         Set<String> folders,
                                         long maxWaitTimeMs,
                                         long intervalTimeMs) {
        if (email == null ||
                email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (appPassword == null ||
                appPassword.isBlank()) {
            throw new IllegalArgumentException("App password cannot be null or blank");
        }
        if (emailSubject == null ||
                emailSubject.isBlank()) {
            throw new IllegalArgumentException("Email subject cannot be null or blank");
        }
        if (folders == null ||
                folders.isEmpty()) {
            throw new IllegalArgumentException("Folders cannot be null or empty");
        }
        for (String folder : folders) {
            if (folder == null ||
                    folder.isBlank()) {
                throw new IllegalArgumentException("Folder name cannot be null or blank");
            }
        }
        if (maxWaitTimeMs < 1) {
            throw new IllegalArgumentException("Max wait time must be greater than 0");
        }
        if (intervalTimeMs < 1) {
            throw new IllegalArgumentException("Interval time must be greater than 0");
        }
    }

    private static String getTextFromMessage(Message message)
            throws MessagingException, IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
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

    private static void validateContent(String content) {
        if (content == null ||
                content.isBlank()) {
            throw new IllegalArgumentException("Email content cannot be null or blank");
        }
    }

    private static String extractUuid(String content) {
        validateContent(content);
        Matcher matcher = UUID_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
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
        validateContent(content);
        Matcher matcher = DEFAULT_OTP_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Otp not found in email content");
    }
}
