package cl.dsoto.repositories;

import cl.dsoto.entities.RoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by root on 13-10-22.
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("SELECT r FROM RoleEntity r where r.rolename = :rolename")
    RoleEntity findByRolename(@Param("rolename") String rolename);

    @Query("SELECT r FROM RoleEntity r order by r.rolename")
    Set<RoleEntity> findAllOrderByName();

}
