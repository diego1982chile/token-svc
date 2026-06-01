package cl.dsoto.mappers;

import cl.dsoto.entities.UserEntity;
import cl.dsoto.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = RoleMapper.class)
public interface UserMapper {

    User toModel(UserEntity entity);

    UserEntity toEntity(User model);

    List<User> toModelList(List<UserEntity> entities);
}
