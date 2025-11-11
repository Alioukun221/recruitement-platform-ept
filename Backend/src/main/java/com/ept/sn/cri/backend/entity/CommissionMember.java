package com.ept.sn.cri.backend.entity;

import com.ept.sn.cri.backend.enums.CommissionRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commission_members")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commission_id", nullable = false)
    private Commission commission;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionRole role = CommissionRole.MEMBER;

    @Column(length = 255)
    private String expertiseArea;

    @OneToMany(mappedBy = "commissionMember", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evaluation> evaluationsGiven= new ArrayList<>();

}
