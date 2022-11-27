package ru.denisov26.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.denisov26.domain.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(
            value = "SELECT * FROM USERS u WHERE u.email like (?)",
            nativeQuery = true)
    User findByEmail(String email);
}
