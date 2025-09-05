package org.vimal.helpers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class InvalidInputsHelper {
    private InvalidInputsHelper() {
    }

    private static final Set<String> COMMON = Set.of(
            "",       // blank
            " "       // space only
    );
    public static final Set<String> INVALID_USERNAMES = buildInvalidUsernames();
    public static final Set<String> INVALID_PASSWORDS = buildInvalidPasswords();
    public static final Set<String> INVALID_EMAILS = buildInvalidEmails();
    public static final Set<String> INVALID_UUIDS = buildInvalidUuids();
    public static final Set<String> INVALID_OTPS = buildInvalidOtps();
    public static final Set<String> INVALID_NAMES = buildInvalidNames();
    public static final Set<String> INVALID_ROLE_OR_PERMISSION_NAMES = buildInvalidRoleOrPermissionNames();

    private static Set<String> buildInvalidUsernames() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("a");                         // too short
        set.add("a".repeat(101));       // too long
        set.add("ab cd");                     // contains space
        set.add("ab@cd");                     // contains special character
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidPasswords() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("short");                     // too short
        set.add("s".repeat(256));       // too long
        set.add("12345678");                  // only digits
        set.add("12345678!");                 // digits + special, no letters
        set.add("abcdefgh");                  // only lowercase
        set.add("abcdefgh1");                 // lowercase + digits, missing uppercase/special
        set.add("abcdefgh!");                 // lowercase + special, missing uppercase/digits
        set.add("abcdefgh!1");                // lowercase + digits + special, missing uppercase
        set.add("ABCDEFGH");                  // only uppercase
        set.add("ABCDEFGH!");                 // uppercase + special, missing lowercase/digits
        set.add("ABCDEFGH1");                 // uppercase + digits, missing lowercase/special
        set.add("ABCDEFGH!1");                // uppercase + digits + special, missing lowercase
        set.add("!@#$%^&*");                  // only special characters
        set.add("1234ABCD");                  // digits + uppercase, missing lowercase/special
        set.add("abcdABCD");                  // lowercase + uppercase, missing digits/special
        set.add("abcd1234");                  // lowercase + digits, missing uppercase/special
        set.add("ABCD1234");                  // uppercase + digits, missing lowercase/special
        set.add("ABCD!@#$");                  // uppercase + special, missing lowercase/digits
        set.add("abcd!@#$");                  // lowercase + special, missing uppercase/digits
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidEmails() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("plainaddress");                                       // missing @
        set.add("@no-local-part.com");                                 // missing local part
        set.add("Outlook Contact <outlook-contact@domain.com>");       // invalid format
        set.add("no-at.domain.com");                                   // missing @ symbol
        set.add("no-tld@domain");                                      // missing TLD
        set.add("semicolon@domain.com;");                              // trailing semicolon
        set.add("user@.com");                                          // domain starts with dot
        set.add("user@domain..com");                                   // double dot in domain
        set.add("user@-domain.com");                                   // domain starts with hyphen
        set.add("user@domain-.com");                                   // domain ends with hyphen
        set.add("user@domain.c");                                      // TLD too short
        set.add("a".repeat(255) + "@test.com");                  // local part too long
        set.add("a".repeat(65) + "@test.com");                   // local part exceeds 64 chars
        set.add(".abc@test.com");                                      // local part starts with dot
        set.add("ab..cd@test.com");                                    // local part has consecutive dots
        set.add("user@domain.c1");                                     // invalid TLD with digit
        set.add("user@domain." + "a".repeat(191));               // TLD too long
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidUuids() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("123");                                         // too short
        set.add("not-a-uuid");                                  // plain text
        set.add("550e8400e29b41d4a716446655440000");            // missing dashes
        set.add("550e8400-e29b-41d4-a716-44665544");            // incomplete
        set.add("550e8400-e29b-41d4-a716-44665544000");         // wrong length
        set.add("550e8400-e29b-41d4-a716-44665544000000");      // too long
        set.add("zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz");        // invalid hex chars
        set.add("550e8400-e29b-41d4-a716:446655440000");        // invalid separator
        set.add("550e8400-e29b-41d4-a716-44665544000g");        // invalid hex at end
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidOtps() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("12345");          // too short
        set.add("123456789");      // too long
        set.add("abcdef");         // letters only
        set.add("12a456");         // mix of digits and letters
        set.add("12 456");         // contains space
        set.add("12-456");         // contains special character '-'
        set.add("🙂23456");        // contains emoji / non-numeric
        set.add("00000a");         // trailing invalid char
        set.add("１２３４５６");     // full-width unicode digits
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidNames() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("F".repeat(51));    // too long
        set.add("F1");                    // contains digit
        set.add("F!");                    // contains special character
        set.add("F!1");                   // contains special character + digit
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildInvalidRoleOrPermissionNames() {
        Set<String> set = new HashSet<>(COMMON);
        set.add("role name");                // contains space
        set.add("role-name");                // contains hyphen
        set.add("role.name");                // contains dot
        set.add("role@name");                // contains special char '@'
        set.add("role#name");                // contains special char '#'
        set.add("role$name");                // contains special char '$'
        set.add("role%name");                // contains special char '%'
        set.add("role^name");                // contains special char '^'
        set.add("role&name");                // contains special char '&'
        set.add("role*name");                // contains special char '*'
        set.add("role(name)");               // contains parentheses
        set.add("role+name");                // contains plus
        set.add("role=name");                // contains equal
        set.add("role~name");                // contains tilde
        set.add("role/name");                // contains slash
        set.add("role\\name");               // contains backslash
        set.add("role,name");                // contains comma
        set.add("role;name");                // contains semicolon
        set.add("role:name");                // contains colon
        set.add("role!name");                // contains exclamation
        set.add("role?name");                // contains question mark
        set.add("role\"name\"");             // contains double quotes
        set.add("'rolename'");               // wrapped in single quotes
        set.add("role'name");                // contains single quote
        set.add("role🙂name");               // contains emoji / Unicode symbol
        set.add("R".repeat(101));      // too long
        return Collections.unmodifiableSet(set);
    }
}
