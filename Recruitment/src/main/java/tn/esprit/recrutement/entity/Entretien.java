package tn.esprit.recrutement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Entretien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;
    private String intervieweur;

    @Column(length = 1000)
    private String feedbackTechnique;

    private Integer noteGlobale; // Valeur entre 1 et 5 selon votre diagramme

    @ManyToOne
    @JoinColumn(name = "candidature_id")
    @JsonIgnore // <--- AJOUTEZ CECI
    private Candidature candidature;
}