package com.ept.sn.cri.backend.entity;

import com.ept.sn.cri.backend.enums.CommissionRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commission_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionMember extends User {

    @ManyToOne
    @JoinColumn(name = "commission_id")
    private Commission commission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionRole commissionRole = CommissionRole.MEMBER;

    @Column(length = 255)
    private String expertiseArea;

    @OneToMany(mappedBy = "commissionMember", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evaluation> evaluationsGiven = new ArrayList<>();

    @Override
    public String getName() {
        return "";
    }
}
