package com.ept.sn.cri.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hr_users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RH extends User {

    @Column(length = 255)
    private String department;

    @Column(length = 255)
    private String positionTitle;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<JobOffer> jobOffers = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Commission> commissions =new ArrayList<>();

    @Override
    public String getName() {
        return "";
    }
}
