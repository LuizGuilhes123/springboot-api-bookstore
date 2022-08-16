package com.fredfonseca.bookstoremanager.users.controller;

import com.fredfonseca.bookstoremanager.users.dto.*;
import com.fredfonseca.bookstoremanager.users.service.AuthenticationService;
import com.fredfonseca.bookstoremanager.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController implements UserControllerDocs{

    private UserService userService;

    private AuthenticationService authenticationService;

    @Autowired
    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDTO create(@RequestBody @Valid UserDTO userToCreateDTO) {
        return userService.create(userToCreateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.delete(id, authenticatedUser);
    }

    @PutMapping("/{id}")
    public MessageDTO update(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @RequestBody @Valid UserDTO userToUpdateDTO) {
        return userService.update(id, authenticatedUser, userToUpdateDTO);
    }

    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.findById(id, authenticatedUser);
    }

    @GetMapping
    public List<UserDTO> findAll() {
        return userService.findAll();
    }

    @PostMapping(value = "/authenticate")
    public JwtResponse createAuthenticationToken(@RequestBody @Valid JwtRequest jwtRequest) {
        return authenticationService.createAuthenticationToken(jwtRequest);
    }
}
