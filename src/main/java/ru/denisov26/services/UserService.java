package ru.denisov26.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.denisov26.domain.Role;
import ru.denisov26.domain.User;
import ru.denisov26.repositories.UserRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list() {
        return repository.findAll();
    }

    public User findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найдена"));
    }

    public User create(User user) {
        User byEmail = repository.findByEmail(user.getEmail());
        if (Objects.isNull(user.getId()) && byEmail != null) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(Role.USER));
        return repository.save(user);
    }

    @NotNull
    private User encodePassword(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        return user;
    }

    public User update(User user) {
        User userEn = encodePassword(user);
        User userFromDb = findById(user.getId());
        if (userFromDb != null) {
            userEn.setPassword(userFromDb.getPassword());
        }
        return repository.save(userEn);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public void delete(User user) {
        repository.delete(user);
    }

}
