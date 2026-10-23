package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.user.ReqCreateUserDTO;
import com.dtn.apply_job.domain.request.user.ReqUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResCreateUserDTO;
import com.dtn.apply_job.domain.response.user.ResLoginDTO;
import com.dtn.apply_job.domain.response.user.ResUpdateUserDTO;
import com.dtn.apply_job.domain.response.user.ResUserDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "savedJobs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "googleRefreshToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toUser(ReqCreateUserDTO reqDTO);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "gender", expression = "java(user.getGender() != null ? user.getGender().toString() : null)")
    @Mapping(target = "isActive", source = "isActive")
    ResUserDTO toResUserDTO(User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "gender", expression = "java(user.getGender() != null ? user.getGender().toString() : null)")
    ResCreateUserDTO toResCreateUserDTO(User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "gender", expression = "java(user.getGender() != null ? user.getGender().toString() : null)")
    @Mapping(target = "isActive", source = "isActive")
    ResUpdateUserDTO toResUpdateUserDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "savedJobs", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromDTO(ReqUpdateUserDTO dto, @MappingTarget User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "isGmailLinked", expression = "java(user.getGoogleRefreshToken() != null)")
    ResLoginDTO.UserLogin toUserLogin(User user);

    ResLoginDTO.UserLogin.CompanyInfo toCompanyInfo(Company company);

    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream().map(r -> r.getName().name()).toList();
    }
}
