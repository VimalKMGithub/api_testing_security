package org.vimal.dtos;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private Set<String> roles;
    private boolean emailVerified;
    private boolean accountLocked;
    private boolean accountEnabled;
    private boolean accountDeleted;
    private String oldUsername;
}
