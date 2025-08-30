package fr.eliess.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class StudentProfile {

    private String address;
    private String phone;

    public StudentProfile() {
    }

    public StudentProfile(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }
}
