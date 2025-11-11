package com.ept.sn.cri.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RhRequest {
    @NotEmpty(message = "First name is mandatory")
    private String firstName;
    @NotEmpty(message = "First name is mandatory")
    private String lastName;
    @NotBlank(message = "password is mandatory")
    @Size(min=8,message = "Password should be 8 characters long minium")
    private String password;
    @Email(message = "Email is not formatted")
    private String email;
    private String phoneNumber;
    private String positionTitle;
    private String department;

}
