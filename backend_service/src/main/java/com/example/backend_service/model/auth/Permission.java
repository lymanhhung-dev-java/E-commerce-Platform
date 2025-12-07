package com.example.backend_service.model.auth;

import java.io.Serializable;
import com.example.backend_service.model.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Permission extends AbstractEntity<Integer> implements Serializable  {
   
    @Column(name ="name")
    private String name;
    private String description;
    
}
