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
@ToString(exclude = {"courses", "profile"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY) // <- cascade activé
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    /*
    Pour les relations ManyToMany.
    Crée une table de jointure entre Student et Course.
    name = "student_course" → nom de la table de jointure.
    joinColumns = @JoinColumn(name = "student_id") → colonne qui pointe vers Student.
    inverseJoinColumns = @JoinColumn(name = "course_id") → colonne qui pointe vers Course.
    */

    // initialise la collection pour éviter les NullPointerException

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentProfile profile;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void addCourse(Course course) {
        if (courses.add(course)) {
            course.getStudents().add(this);
        }
    }

}
