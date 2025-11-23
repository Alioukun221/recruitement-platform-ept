package com.ept.sn.cri.backend.rh.dto;

import lombok.Data;

@Data
public class SimpleUserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}