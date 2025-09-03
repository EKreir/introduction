package fr.eliess.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "student")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String phone;

    // Relation bidirectionnelle avec Student
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;


    public StudentProfile(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }

    /*

    Ici pas besoin de fetch explicite, car StudentProfile -> Student est côté propriétaire,
    et par défaut c’est EAGER.
    Mais on pourrait aussi mettre en LAZY si on veut éviter des surprises.

    */

}
