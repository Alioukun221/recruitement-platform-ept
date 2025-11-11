package com.ept.sn.cri.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "candidates")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Candidate extends User {

    @Column(length = 255)
    private String address;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Application> applications;


    @Override
    public String getName() {
        return "";
    }
}
