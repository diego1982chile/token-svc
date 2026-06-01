package cl.dsoto.mappers;

import cl.dsoto.entities.RoleEntity;
import cl.dsoto.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface RoleMapper {

    Role toModel(RoleEntity entity);

    RoleEntity toEntity(Role model);

    Set<Role> toModelSet(Set<RoleEntity> entities);

    Set<RoleEntity> toEntitySet(Set<Role> models);
}
