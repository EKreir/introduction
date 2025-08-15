# Projet Introduction - Java / Maven / Hibernate / MySQL

## 📌 Description
Ce projet est un exemple d'application Java utilisant **Maven**, **Hibernate/JPA** et **MySQL**.  
Il met en place une configuration simple pour :
- Se connecter à une base de données MySQL
- Mapper une entité `Student` avec JPA
- Persister et récupérer des données avec Hibernate

## 📂 Structure du projet
introduction/
│── pom.xml # Configuration Maven et dépendances
│── src/
│ ├── main/
│ │ ├── java/ # Code source Java
│ │ │ ├── fr/eliess/
│ │ │ │ ├── Main.java
│ │ │ │ ├── basics/ # Gestion élèves (code passé)
│ │ │ │ ├── dao/ # Connexion à la base
│ │ │ │ └── model/ # Entité Student
│ │ └── resources/
│ │ └── META-INF/
│ │ └── persistence.xml
│ └── test/ # Tests unitaires (JUnit)

## ⚙️ Prérequis
- **Java 21**
- **Maven 3.x**
- **MySQL 8.x**
- Un IDE (ex : IntelliJ IDEA)

## 🗄️ Base de données
Créer la base de données avant de lancer l'application :
```sql
CREATE DATABASE jpaexo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'eliess'@'localhost' IDENTIFIED BY 'ton_mot_de_passe';
GRANT ALL PRIVILEGES ON jpaexo.* TO 'eliess'@'localhost';
FLUSH PRIVILEGES;
```

## 🔧 Configuration Hibernate
Le fichier src/main/resources/META-INF/persistence.xml contient les paramètres de connexion :
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/jpaexo?useSSL=false&serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="eliess"/>
<property name="jakarta.persistence.jdbc.password" value="ton_mot_de_passe"/>
<property name="hibernate.hbm2ddl.auto" value="create"/>
<property name="hibernate.show_sql" value="true"/>

## 🚀 Lancer l'application
- Compiler le projet :

```bash
mvn clean compile
```
- Lancer depuis Maven :

```bash
mvn exec:java -Dexec.mainClass="fr.eliess.Main"
```

- Ou directement depuis IntelliJ avec Run → Main.

## 📋 Exemple de sortie
Hibernate: create table students (id bigint not null auto_increment, age integer not null, name varchar(255) not null, primary key (id)) engine=InnoDB
Hibernate: insert into students (age,name) values (?,?)
Hibernate: select s1_0.id,s1_0.age,s1_0.name from students s1_0
Student{id=1, name='Didier', age=19}
