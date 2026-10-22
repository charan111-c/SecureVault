package com.securevault.backend.security;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;

@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    public CustomOAuth2UserService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser =
                delegate.loadUser(userRequest);

        Map<String, Object> attributes =
                oauthUser.getAttributes();

        String email =
                (String) attributes.get("email");

        String name =
                (String) attributes.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "OAuth2 provider did not return an email"
            );
        }

        User user =
                userRepository.findByEmail(email)
                        .orElseGet(() -> {

                            User newUser = new User();

                            newUser.setEmail(email);

                            String username =
                                    name != null && !name.isBlank()
                                            ? name
                                            : email.split("@")[0];

                            // Make username unique
                            String baseUsername = username;
                            int counter = 1;

                            while (userRepository
                                    .existsByUsername(username)) {

                                username =
                                        baseUsername + counter++;
                            }

                            newUser.setUsername(username);

                            /*
                             * OAuth users do not authenticate using
                             * the normal password flow.
                             */
                            newUser.setPasswordHash("");

                            newUser.setMfaEnabled(false);

                            return userRepository.save(newUser);
                        });

        return new DefaultOAuth2User(
                oauthUser.getAuthorities(),
                attributes,
                "email"
        );
    }
}