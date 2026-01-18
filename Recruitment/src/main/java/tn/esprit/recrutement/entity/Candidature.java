package tn.esprit.recrutement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "candidatures")
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence vers Talent-Management-Service
    private Long candidatId;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    @JsonIgnore // <--- AJOUTEZ CECI
    private OffreEmploi offre;

    private LocalDateTime datePostulation;

    @Enumerated(EnumType.STRING)
    private EtatCandidature etat; // NOUVELLE, ENTRETIEN_EN_COURS, etc.

    // Dans l'entité Candidature.java
    @OneToMany(mappedBy = "candidature")
    private List<Entretien> entretiens = new ArrayList<> (); // Initialisation pour éviter le null

    @PrePersist
    protected void onCreate() {
        this.datePostulation = LocalDateTime.now();
        this.etat = EtatCandidature.NOUVELLE;
    }
}
