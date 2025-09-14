package org.vimal.helpers;

import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.vimal.api.AdminCallsUsingGlobalAdminUser.deleteRoles;
import static org.vimal.api.AdminCallsUsingGlobalAdminUser.deleteUsers;
import static org.vimal.constants.Common.*;

public final class CleanUpHelper {
    private CleanUpHelper() {
    }

    public static void cleanUpTestUsers(Object... inputs) {
        Iterator<String> iterator = extractUsernames(inputs).iterator();
        Set<String> batch = new HashSet<>();
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_USER_DELETION_AT_A_TIME) {
                batch.add(iterator.next());
            }
            try {
                deleteUsers(
                        batch,
                        ENABLE,
                        ENABLE
                );
            } catch (Exception ignored) {
            }
        }
    }

    private static Set<String> extractUsernames(Object... inputs) {
        Set<String> result = new HashSet<>();
        for (Object input : inputs) {
            switch (input) {
                case null -> {
                }
                case String str -> result.add(str);
                case UserDto dto -> result.add(dto.getUsername());
                case Iterable<?> iterable -> {
                    for (Object element : iterable) {
                        result.addAll(extractUsernames(element));
                    }
                }
                case Object[] objects -> {
                    for (Object element : objects) {
                        result.addAll(extractUsernames(element));
                    }
                }
                default -> throw new RuntimeException("Unsupported input type: " + input.getClass());
            }
        }
        return result;
    }

    public static void cleanUpTestRoles(Object... inputs) {
        Iterator<String> iterator = extractRolenames(inputs).iterator();
        Set<String> batch = new HashSet<>();
        while (iterator.hasNext()) {
            batch.clear();
            while (iterator.hasNext() &&
                    batch.size() < MAX_BATCH_SIZE_OF_ROLE_DELETION_AT_A_TIME) {
                batch.add(iterator.next());
            }
            try {
                deleteRoles(
                        batch,
                        ENABLE,
                        ENABLE
                );
            } catch (Exception ignored) {
            }
        }
    }

    private static Set<String> extractRolenames(Object... inputs) {
        Set<String> result = new HashSet<>();
        for (Object input : inputs) {
            switch (input) {
                case null -> {
                }
                case String str -> result.add(str);
                case RoleDto dto -> result.add(dto.getRoleName());
                case Iterable<?> iterable -> {
                    for (Object element : iterable) {
                        result.addAll(extractRolenames(element));
                    }
                }
                case Object[] objects -> {
                    for (Object element : objects) {
                        result.addAll(extractRolenames(element));
                    }
                }
                default -> throw new RuntimeException("Unsupported input type: " + input.getClass());
            }
        }
        return result;
    }
}
