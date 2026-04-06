package com.usermgmt.security;

import com.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: "+username));
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getAllRoles().forEach(r ->
            authorities.add(new SimpleGrantedAuthority("ROLE_"+r.getName().toUpperCase())));
        user.getAllPermissions().forEach(p ->
            authorities.add(new SimpleGrantedAuthority(p.getName())));
        return User.builder()
            .username(user.getUsername()).password(user.getPassword())
            .authorities(authorities).accountLocked(user.isAccountLocked())
            .disabled(!user.isActive()).build();
    }
}