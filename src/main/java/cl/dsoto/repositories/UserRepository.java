package cl.dsoto.repositories;


import cl.dsoto.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by root on 13-10-22.
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {


    @Query("SELECT u FROM UserEntity u where u.username = :username")
    UserEntity findByUsername(@Param("username") String username);

    @Query("SELECT u FROM UserEntity u order by u.username")
    List<UserEntity> findAllOrderByName();

}
