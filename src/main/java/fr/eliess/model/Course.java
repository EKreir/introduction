package fr.eliess.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "students")
/* génère automatiquement une méthode toString() pour la classe
exclude = "students" : n'inclut pas les champs students dans le toString()
pour éviter les boucles infinies dans les relations bidirectionnelles (Student <--> Course)
*/
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "courses") // indique que l'autre côté de la relation possède la vraie clé étrangère
    private Set<Student> students = new HashSet<>();
    // Ici, Student a courses et Course a students. Le champ students dans Course est le propriétaire de la relation.

    @ManyToOne
    @JoinColumn(name = "teacher_id") // foreign key DB
    private Teacher teacher;

    public Course(String title) {
        this.title = title;
    }

    /*
    public void addStudent(Student student) {
        if (students.add(student)) {
            student.getCourses().add(this);
        }
    }
*/
}
