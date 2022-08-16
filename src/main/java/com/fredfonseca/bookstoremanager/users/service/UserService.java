package com.fredfonseca.bookstoremanager.users.service;

import com.fredfonseca.bookstoremanager.rentals.repository.RentalRepository;
import com.fredfonseca.bookstoremanager.users.dto.AuthenticatedUser;
import com.fredfonseca.bookstoremanager.users.dto.MessageDTO;
import com.fredfonseca.bookstoremanager.users.dto.UserDTO;
import com.fredfonseca.bookstoremanager.users.entity.Users;
import com.fredfonseca.bookstoremanager.users.exception.*;
import com.fredfonseca.bookstoremanager.users.mapper.UserMapper;
import com.fredfonseca.bookstoremanager.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fredfonseca.bookstoremanager.users.utils.MessageDTOUtils.creationMessage;
import static com.fredfonseca.bookstoremanager.users.utils.MessageDTOUtils.updatedMessage;

@Service
public class UserService {

    private final static UserMapper userMapper = UserMapper.INSTANCE;

    private UserRepository userRepository;

    private RentalRepository rentalRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RentalRepository rentalRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MessageDTO create(UserDTO userToCreateDTO) {
        verifyIfEmailExists(userToCreateDTO.getEmail());
        verifyIfUsernameExists(userToCreateDTO.getUsername());

        Users userToCreate = userMapper.toModel(userToCreateDTO);
        userToCreate.setPassword(passwordEncoder.encode(userToCreate.getPassword()));

        Users createdUser = userRepository.save(userToCreate);
        return creationMessage(createdUser);
    }

    public MessageDTO update(Long id, AuthenticatedUser authenticatedUser, UserDTO userToUpdateDTO) {
        Users foundAuthenticatedUser = verifyAndGetUserIfExists(authenticatedUser.getUsername());
        Users foundUser = verifyAndGetIfExists(id);

        checkChangeStatusPermission(userToUpdateDTO, foundAuthenticatedUser, foundUser);

        Users userToUpdate = userMapper.toModel(userToUpdateDTO);
        userToUpdate.setId(foundUser.getId());
        userToUpdate.setPassword(passwordEncoder.encode(userToUpdate.getPassword()));
        Users updatedUser = userRepository.save(userToUpdate);
        return updatedMessage(updatedUser);
    }

    public void delete(Long id, AuthenticatedUser authenticatedUser) {
        Users foundAuthenticatedUser = verifyAndGetUserIfExists(authenticatedUser.getUsername());
        Users userToDelete = verifyAndGetIfExists(id);

        checkDeleteStatusPermission(foundAuthenticatedUser, userToDelete);

        if(rentalRepository.findByUsers(userToDelete).isPresent())
            throw new DeleteDeniedException();
        userRepository.deleteById(id);
    }

    public UserDTO findById(Long id, AuthenticatedUser authenticatedUser) {
        Users foundUser = verifyAndGetIfExists(id);
        return userMapper.toDTO(foundUser);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Users verifyAndGetIfExists(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private boolean isAdmin(Users foundAuthenticatedUser) {
        return foundAuthenticatedUser.getRole().toString().equals("ADMIN");
    }

    private void checkChangeStatusPermission(UserDTO userToUpdateDTO, Users authenticatedUser, Users foundUser) {
        if(isAdmin(authenticatedUser)) return;
        if(!authenticatedUser.equals(foundUser))
            throw new InvalidCredentialsChange(foundUser.getName());
        if(!passwordEncoder.matches(userToUpdateDTO.getPassword(), foundUser.getPassword()))
            throw new UserCredentialsChangeNotAllowed();
        validateCredentialsChange(foundUser.getEmail(), foundUser.getUsername(),
                userToUpdateDTO.getEmail(), userToUpdateDTO.getUsername());
    }

    private void checkDeleteStatusPermission(Users foundAuthenticatedUser, Users userToDelete) {
        if(isAdmin(foundAuthenticatedUser)) return;
        if(!foundAuthenticatedUser.equals(userToDelete))
            throw new DeleteDeniedException(userToDelete.getName());
    }

    private void validateCredentialsChange(String currEmail, String currUsername, String newEmail, String newUsername) {
        if(!currEmail.equals(newEmail) && !currUsername.equals(newUsername))
            throw new InvalidCredentialsChange();

        if(!currEmail.equals(newEmail)) verifyIfEmailExists(newEmail);

        if(!currUsername.equals(newUsername)) verifyIfUsernameExists(newUsername);
    }

    private void verifyIfEmailExists(String email) {
        Optional<Users> foundUser = userRepository.findByEmail(email);
        if (foundUser.isPresent()) throw new UserEmailAlreadyExistsException(email);
    }

    private void verifyIfUsernameExists(String username) {
        Optional<Users> foundUser = userRepository.findByUsername(username);
        if (foundUser.isPresent()) throw new UsernameAlreadyExistsException(username);
    }

    public Users verifyAndGetUserIfExists(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
