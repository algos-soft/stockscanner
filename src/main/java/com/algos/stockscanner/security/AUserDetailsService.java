package com.algos.stockscanner.security;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Implements the {@link UserDetailsService}.
 * <p>
 * This implementation searches for {@link Utente} entities by the e-mail address
 * supplied in the login screen.
 */
@Service
@Primary
public class AUserDetailsService implements UserDetailsService {


    public PasswordEncoder passwordEncoder;



    @Autowired
    public AUserDetailsService() {
        this.passwordEncoder = new BCryptPasswordEncoder();

    }// end of Spring constructor


    /**
     * Recovers the {@link Utente} from the database using the e-mail address supplied
     * in the login screen. If the user is found, returns a
     * {@link User}.
     *
     * @param username User's e-mail address
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String passwordHash = "";
        Collection<? extends GrantedAuthority> authorities= null;

        if (true) {
            passwordHash = passwordEncoder.encode("password");
            return new User(username, passwordHash, authorities);
        }
        else {
            throw new UsernameNotFoundException(username + " non valido");
        }

    }// end of method


}// end of class