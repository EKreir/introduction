package fr.eliess.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String phone;

    // Relation bidirectionnelle avec Student
    @OneToOne(mappedBy = "profile")
    private Student student;

    public StudentProfile() { }

    public StudentProfile(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }
}
