package org.vimal.helpers;

import org.vimal.dtos.RoleDto;
import org.vimal.dtos.UserDto;

import java.util.HashSet;
import java.util.Set;

import static org.vimal.BaseTest.TEST_EMAIL;
import static org.vimal.utils.DateTimeUtility.getCurrentFormattedLocalTimeStamp;
import static org.vimal.utils.RandomStringUtility.generateRandomStringAlphaNumeric;

public final class DtosHelper {
    private DtosHelper() {
    }

    public static UserDto createRandomUserDto() {
        return createRandomUserDto(null);
    }

    private static Set<UserDto> createRandomUserDtos(int count) {
        Set<UserDto> userDtos = new HashSet<>();
        for (int i = 0; i < count; i++) {
            userDtos.add(createRandomUserDto());
        }
        return userDtos;
    }

    public static UserDto createRandomUserDtoWithRandomValidEmail() {
        return createRandomUserDtoWithGivenEmail(validRandomEmail());
    }

    private static UserDto createRandomUserDtoWithGivenEmail(String email) {
        UserDto user = createRandomUserDto(null);
        user.setEmail(email);
        return user;
    }

    public static String validRandomEmail() {
        int atIndex = TEST_EMAIL.indexOf('@');
        return TEST_EMAIL.substring(0, atIndex) + "+" + getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric() + "@" + TEST_EMAIL.substring(atIndex + 1);
    }

    public static UserDto createRandomUserDto(Set<String> roles) {
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        return UserDto.builder()
                .username("AutoTestUser_" + randomString)
                .email("user_" + randomString + "@example.com")
                .password("Password@1_" + randomString)
                .firstName("AutoTestUser")
                .roles(roles)
                .emailVerified(true)
                .accountEnabled(true)
                .build();
    }

    public static RoleDto createRandomRoleDto() {
        return createRandomRoleDto(null);
    }

    public static Set<RoleDto> createRandomRoleDtos(int count) {
        Set<RoleDto> roleDtos = new HashSet<>();
        for (int i = 0; i < count; i++) {
            roleDtos.add(createRandomRoleDto());
        }
        return roleDtos;
    }

    public static RoleDto createRandomRoleDto(Set<String> permissions) {
        String randomString = getCurrentFormattedLocalTimeStamp() + "_" + generateRandomStringAlphaNumeric();
        return RoleDto.builder()
                .roleName("AutoTestRole_" + randomString)
                .description("Auto-generated role for testing purposes")
                .permissions(permissions)
                .build();
    }
}
