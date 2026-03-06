package ru.gigasigma.blpscrud.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
}
