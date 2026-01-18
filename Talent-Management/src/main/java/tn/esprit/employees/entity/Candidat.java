package tn.esprit.employees.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @NotNull(message = "FileName cannot be null")
    private String fileName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String telephone;

    @Column(unique = true, nullable = false)
    private String posteActuel;

    @NotNull(message = "Lien CV cannot be null")
    @Column(unique = true, nullable = false)
    private String lienCV;

    @Column(unique = true, nullable = false)
    private String lienLinkedin;

    @Column(unique = true, nullable = false)
    private String lienGitHub;


    private Integer anneesExperience;

    private LocalDateTime dateCreation;

    private Boolean consentementDonnees;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private DomainePrincipale domainePrincipale;


    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Competence> competences = new ArrayList<>();


    // --- Méthodes pratiques ---
    public void addCompetence(Competence competence) {
        competences.add(competence);
        competence.setCandidat(this);
    }

    public void removeCompetence(Competence competence) {
        competences.remove(competence);
        competence.setCandidat(null);
    }

    /**
     * Retourne le nom complet pour affichage dans JSON.
     */
    public String getNomComplet() {
        return nom + " " + prenom;
    }
}
