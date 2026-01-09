package com.example.backend_service.model.auth;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.example.backend_service.model.AbstractEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role extends AbstractEntity<Integer> implements Serializable {
    @Column(name ="name")
    private String name;
    private String description;
    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
        name = "roles_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
