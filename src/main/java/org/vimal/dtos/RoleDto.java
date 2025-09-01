package org.vimal.dtos;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private String roleName;
    private String description;
    private Set<String> permissions;
}
