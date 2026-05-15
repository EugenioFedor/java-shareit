package ru.practicum.shareit.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto userDto);
}