package cl.dsoto.repositories;


import cl.dsoto.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by root on 13-10-22.
 */
public interface UserRepository extends JpaRepository<User, String> {


    @Query("SELECT u FROM User u where u.username = :username")
    User findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u order by u.username")
    List<User> findAllOrderByName();

    @Modifying
    @Query("delete from User u")
    void removeAll();
}
