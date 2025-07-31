package fr.eliess.basics;

public class Eleve {

    private int id; // id unique
    private String nom;
    private int age;

    // constructeur pour la lecture depuis la BDD
    public Eleve(int id, String nom, int age) {

        this.id = id;
        this.nom = nom;
        this.age = age;

    }

    // constructeur utilisé pour l'ajout (id sera généré par la BDD)
    public Eleve(String nom, int age) {

        this.nom = nom;
        this.age = age;

    }

    public int getId() {

        return id;

    }

    public String getNom() {

        return nom;

    }

    public int getAge() {

        return age;

    }

    public void setNom(String nom) {

        this.nom = nom;

    }

    public void setAge(int age) {

        this.age = age;

    }

    @Override
    public String toString() {

        return "Eleve{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", age=" + age +
                '}';

    }
}
