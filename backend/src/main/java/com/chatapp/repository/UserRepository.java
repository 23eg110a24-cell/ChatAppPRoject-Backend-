package com.chatapp.repository;

import com.chatapp.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select u from User u
            where lower(u.username) like lower(concat('%', :term, '%'))
               or lower(u.displayName) like lower(concat('%', :term, '%'))
            order by u.username asc
            """)
    List<User> search(@Param("term") String term, Pageable pageable);

    @Query("select u from User u where u.id <> :excludeUserId order by u.username asc")
    List<User> findAllExcept(@Param("excludeUserId") Long excludeUserId, Pageable pageable);
}
