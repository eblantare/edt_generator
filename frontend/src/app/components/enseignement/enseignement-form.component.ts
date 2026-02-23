// C:\projets\java\edt-generator\frontend\src\app\components\enseignement\enseignement-form.component.ts
import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { EnseignementService, EnseignementDTO } from '../../services/enseignement.service';
import { EnseignantService } from '../../services/enseignant.service';
import { MatiereService } from '../../services/matiere.service';
import { ClasseService } from '../../services/classe.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-enseignement-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './enseignement-form.component.html'
})
export class EnseignementFormComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  isEditMode = false;
  isLoading = false;
  allowOtherMatieres = false;
  notifyEnseignant = false;
  validateSchedule = true;
  showClassFilter = false;
  filterByNiveau = false;
  filterByEffectif = true;
  commentaire = '';
  
  // Pour la multi-sélection des classes
  selectedClasses: string[] = [];
  
  enseignement: EnseignementDTO = {
    enseignantId: '',
    matiereId: '',
    classeId: '',
    heuresParSemaine: 4
  };

  enseignants: any[] = [];
  allMatieres: any[] = [];
  classes: any[] = [];
  filteredClasses: any[] = [];
  
  selectedEnseignant: any = null;
  selectedMatiere: any = null;
  selectedClasse: any = null;
  selectedEnseignantHeuresAttribuees = 0;
  
  // Pour les calculs mathématiques dans le template
  Math = Math;
  
  heuresValidation = {
    isValid: false,
    isWarning: false,
    isError: false,
    message: '',
    details: ''
  };

  get enseignementsMatiere(): any[] {
    if (!this.selectedEnseignant) return [];
    
    const matieres: any[] = [];
    
    if (this.selectedEnseignant.matiereDominante) {
      matieres.push(this.selectedEnseignant.matiereDominante);
    }
    
    if (this.selectedEnseignant.matiereSecondaire && 
        this.selectedEnseignant.matiereSecondaire.id !== this.selectedEnseignant.matiereDominante?.id) {
      matieres.push(this.selectedEnseignant.matiereSecondaire);
    }
    
    return matieres;
  }
  
  get otherMatieres(): any[] {
    if (!this.selectedEnseignant) return this.allMatieres;
    
    const enseignementsMatiereIds = this.enseignementsMatiere.map(m => m.id);
    return this.allMatieres.filter(m => !enseignementsMatiereIds.includes(m.id));
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enseignementService: EnseignementService,
    private enseignantService: EnseignantService,
    private matiereService: MatiereService,
    private classeService: ClasseService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    console.log('🔍 EnseignementFormComponent ngOnInit');
    this.loadData();

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEditMode = true;
      this.loadEnseignement(id);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadData() {
    console.log('🔄 Début de loadData()');
    this.isLoading = true;
    this.cdr.detectChanges();
    
    // Compteur de requêtes
    let requestsCompleted = 0;
    const totalRequests = 3;
    
    const completeRequest = () => {
      this.ngZone.run(() => {
        requestsCompleted++;
        console.log(`📊 Requête ${requestsCompleted}/${totalRequests} terminée`);
        
        if (requestsCompleted >= totalRequests) {
          this.isLoading = false;
          console.log('✅ Toutes les requêtes sont terminées - isLoading:', this.isLoading);
          
          // Force la détection de changement
          this.cdr.detectChanges();
          console.log('🔁 Changement détecté manuellement');
          
          // Vérifier les données chargées
          console.log('📋 Données chargées:');
          console.log('  - Enseignants:', this.enseignants.length);
          console.log('  - Matières:', this.allMatieres.length);
          console.log('  - Classes:', this.classes.length);
        }
      });
    };
    
    // Enseignants
    this.enseignantService.getAllEnseignants(0, 100, '', 'nom', 'asc').subscribe({
      next: (data: any) => {
        console.log('✅ Enseignants reçus:', data?.content?.length || data?.length || 0);
        if (data?.content) {
          this.enseignants = data.content.map((e: any) => ({
            ...e,
            heuresMaxHebdo: e.heuresMaxHebdo || 20
          }));
        } else if (Array.isArray(data)) {
          this.enseignants = data.map((e: any) => ({
            ...e,
            heuresMaxHebdo: e.heuresMaxHebdo || 20
          }));
        }
        completeRequest();
      },
      error: (error) => {
        console.error('❌ Erreur enseignants:', error);
        this.enseignants = [];
        completeRequest();
      }
    });
    
    // Matières
    this.matiereService.getAllMatieres().subscribe({
      next: (data: any) => {
        console.log('✅ Matières reçues:', data?.content?.length || data?.length || 0);
        if (data?.content) {
          this.allMatieres = data.content;
        } else if (Array.isArray(data)) {
          this.allMatieres = data;
        }
        completeRequest();
      },
      error: (error) => {
        console.error('❌ Erreur matières:', error);
        this.allMatieres = [];
        completeRequest();
      }
    });
    
    // Classes
    this.classeService.getAllClasses().subscribe({
      next: (data: any) => {
        console.log('✅ Classes reçues:', data?.content?.length || data?.length || 0);
        if (data?.content) {
          this.classes = data.content;
          this.filteredClasses = [...data.content];
        } else if (Array.isArray(data)) {
          this.classes = data;
          this.filteredClasses = [...data];
        }
        completeRequest();
      },
      error: (error) => {
        console.error('❌ Erreur classes:', error);
        this.classes = [];
        this.filteredClasses = [];
        completeRequest();
      }
    });
    
    // Sécurité : forcer isLoading à false après 3 secondes
    setTimeout(() => {
      if (this.isLoading) {
        console.warn('⚠️ Sécurité: Forçage isLoading à false après timeout');
        this.ngZone.run(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        });
      }
    }, 3000);
  }
  
  getSelectedEnseignantHeuresMax(): number {
     return this.selectedEnseignant?.heuresMaxHebdo || 20;
  }

  loadEnseignement(id: string) {
    this.isLoading = true;
    this.enseignementService.getEnseignementById(id).subscribe({
      next: (enseignement: EnseignementDTO) => {
        this.enseignement = enseignement;
        
        // Charger les détails de l'enseignant sélectionné
        if (enseignement.enseignantId) {
          this.enseignantService.getEnseignant(enseignement.enseignantId).subscribe({
            next: (enseignant: any) => {
              this.selectedEnseignant = enseignant;
              this.loadEnseignantHeures();
              this.onEnseignantSelected();
            },
            error: (error) => {
              console.error('Erreur lors du chargement de l\'enseignant:', error);
            }
          });
        }
        
        // Charger la matière sélectionnée
        if (enseignement.matiereId) {
          this.matiereService.getMatiere(enseignement.matiereId).subscribe({
            next: (matiere: any) => {
              this.selectedMatiere = matiere;
            },
            error: (error) => {
              console.error('Erreur lors du chargement de la matière:', error);
            }
          });
        }
        
        // Charger la classe sélectionnée
        if (enseignement.classeId) {
          this.classeService.getClasse(enseignement.classeId).subscribe({
            next: (classe: any) => {
              this.selectedClasse = classe;
              // En mode édition, initialiser la sélection avec cette classe
              this.selectedClasses = [enseignement.classeId];
            },
            error: (error) => {
              console.error('Erreur lors du chargement de la classe:', error);
            }
          });
        }
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement de l\'enseignement:', error);
        this.notificationService.showError('Impossible de charger l\'attribution');
        this.isLoading = false;
        this.router.navigate(['/enseignements']);
      }
    });
  }

  loadEnseignantHeures() {
    if (this.selectedEnseignant) {
      this.enseignementService.getEnseignementsByEnseignant(this.selectedEnseignant.id).subscribe({
        next: (enseignements: EnseignementDTO[]) => {
          this.selectedEnseignantHeuresAttribuees = enseignements
            .filter(e => e.id !== this.enseignement.id) // Exclure l'enseignement actuel en mode édition
            .reduce((total, e) => total + e.heuresParSemaine, 0);
          this.validateHeures();
        },
        error: (error) => {
          console.error('Erreur lors du chargement des heures attribuées:', error);
        }
      });
    }
  }

  onEnseignantSelected() {
    if (this.enseignement.enseignantId) {
      this.selectedEnseignant = this.enseignants.find(e => e.id === this.enseignement.enseignantId);
      this.loadEnseignantHeures();
      
      // Réinitialiser la matière si elle n'est pas dans les spécialités de l'enseignant
      if (this.enseignement.matiereId && this.selectedEnseignant) {
        const isMatiereDeEnseignant = this.isMatiereDeEnseignant(this.enseignement.matiereId);
        if (!isMatiereDeEnseignant && !this.allowOtherMatieres) {
          this.enseignement.matiereId = '';
          this.selectedMatiere = null;
        }
      }
    } else {
      this.selectedEnseignant = null;
      this.selectedEnseignantHeuresAttribuees = 0;
    }
    this.validateHeures();
  }

  onMatiereSelected() {
    if (this.enseignement.matiereId) {
      this.selectedMatiere = this.allMatieres.find(m => m.id === this.enseignement.matiereId);
    } else {
      this.selectedMatiere = null;
    }
  }

  onClasseSelected() {
    // Cette méthode est conservée pour compatibilité
    // La sélection se fait maintenant via toggleClasseSelection
  }

  onAllowOtherMatieresChange() {
    if (!this.allowOtherMatieres && this.enseignement.matiereId) {
      const isMatiereDeEnseignant = this.isMatiereDeEnseignant(this.enseignement.matiereId);
      if (!isMatiereDeEnseignant) {
        this.enseignement.matiereId = '';
        this.selectedMatiere = null;
      }
    }
  }

  toggleClassFilter() {
    this.showClassFilter = !this.showClassFilter;
  }

  applyClassFilter() {
    this.filteredClasses = [...this.classes];
    
    if (this.filterByNiveau) {
      // Filtrer par niveau si nécessaire
      // Cette logique dépend de la structure de vos données
    }
  }

  // Méthodes pour la gestion de la sélection multiple des classes
  isClasseSelected(classeId: string): boolean {
    return this.selectedClasses.includes(classeId);
  }

  toggleClasseSelection(classeId: string): void {
    const index = this.selectedClasses.indexOf(classeId);
    if (index === -1) {
      this.selectedClasses.push(classeId);
    } else {
      this.selectedClasses.splice(index, 1);
    }
    this.onClasseSelected();
    this.validateHeures();
    this.cdr.detectChanges();
  }

  selectAllClasses(): void {
    this.selectedClasses = this.filteredClasses.map(c => c.id);
    this.onClasseSelected();
    this.validateHeures();
    this.cdr.detectChanges();
  }

  removeClasseSelection(classeId: string): void {
    const index = this.selectedClasses.indexOf(classeId);
    if (index !== -1) {
      this.selectedClasses.splice(index, 1);
      this.onClasseSelected();
      this.validateHeures();
      this.cdr.detectChanges();
    }
  }

  getSelectedClassesDetails(): any[] {
    return this.classes.filter(c => this.selectedClasses.includes(c.id));
  }

  getTotalEffectif(): number {
    return this.getSelectedClassesDetails()
      .reduce((total, c) => total + (c.effectif || 0), 0);
  }

  validateHeures() {
    if (!this.selectedEnseignant || !this.enseignement.heuresParSemaine) {
      this.heuresValidation = {
        isValid: false,
        isWarning: false,
        isError: false,
        message: 'Veuillez sélectionner un enseignant et saisir un nombre d\'heures',
        details: ''
      };
      return;
    }

    // Calculer les heures totales en tenant compte du nombre de classes sélectionnées
    const heuresTotal = this.selectedEnseignantHeuresAttribuees + 
                        (this.enseignement.heuresParSemaine * this.selectedClasses.length);
    const heuresDisponibles = this.selectedEnseignant.heuresMaxHebdo;
    const pourcentage = (heuresTotal / heuresDisponibles) * 100;

    if (heuresTotal <= heuresDisponibles) {
      if (pourcentage <= 60) {
        this.heuresValidation = {
          isValid: true,
          isWarning: false,
          isError: false,
          message: 'Disponibilité correcte',
          details: `${heuresTotal}h sur ${heuresDisponibles}h (${Math.round(pourcentage)}%)`
        };
      } else if (pourcentage <= 85) {
        this.heuresValidation = {
          isValid: true,
          isWarning: true,
          isError: false,
          message: 'Attention : occupation élevée',
          details: `${heuresTotal}h sur ${heuresDisponibles}h (${Math.round(pourcentage)}%) - Seuil d\'avertissement`
        };
      } else {
        this.heuresValidation = {
          isValid: false,
          isWarning: true,
          isError: false,
          message: 'Occupation élevée',
          details: `${heuresTotal}h sur ${heuresDisponibles}h (${Math.round(pourcentage)}%) - Proche de la limite`
        };
      }
    } else {
      this.heuresValidation = {
        isValid: false,
        isWarning: false,
        isError: true,
        message: 'Dépassement de la limite d\'heures',
        details: `${heuresTotal}h sur ${heuresDisponibles}h - Dépassement de ${heuresTotal - heuresDisponibles}h`
      };
    }
  }

  getEnseignantDisponibilite(): number {
    if (!this.selectedEnseignant) return 0;
    const disponible = this.selectedEnseignant.heuresMaxHebdo - this.selectedEnseignantHeuresAttribuees;
    return (disponible / this.selectedEnseignant.heuresMaxHebdo) * 100;
  }

  getEnseignantOccupation(): number {
    if (!this.selectedEnseignant) return 0;
    return (this.selectedEnseignantHeuresAttribuees / this.selectedEnseignant.heuresMaxHebdo) * 100;
  }

  isMatiereDominante(matiereId: string): boolean {
    return this.selectedEnseignant?.matiereDominante?.id === matiereId;
  }

  isMatiereSecondaire(matiereId: string): boolean {
    return this.selectedEnseignant?.matiereSecondaire?.id === matiereId;
  }

  isMatiereDeEnseignant(matiereId: string): boolean {
    return this.isMatiereDominante(matiereId) || this.isMatiereSecondaire(matiereId);
  }

  getHeuresProgressClass(): string {
    if (!this.selectedEnseignant || !this.enseignement.heuresParSemaine) return 'bg-secondary';
    
    const heuresTotal = this.selectedEnseignantHeuresAttribuees + 
                        (this.enseignement.heuresParSemaine * this.selectedClasses.length);
    const pourcentage = (heuresTotal / this.selectedEnseignant.heuresMaxHebdo) * 100;
    
    if (pourcentage <= 60) return 'bg-success';
    if (pourcentage <= 85) return 'bg-warning';
    return 'bg-danger';
  }

  getHeuresProgressWidth(): number {
    if (!this.selectedEnseignant || !this.enseignement.heuresParSemaine) return 0;
    
    const heuresTotal = this.selectedEnseignantHeuresAttribuees + 
                        (this.enseignement.heuresParSemaine * this.selectedClasses.length);
    const pourcentage = (heuresTotal / this.selectedEnseignant.heuresMaxHebdo) * 100;
    return Math.min(pourcentage, 100);
  }

  previewAttribution() {
    const classesDetails = this.getSelectedClassesDetails()
      .map(c => `    • ${c.nom} (${c.niveau || 'N/A'} - ${c.effectif || '?'} élèves)`)
      .join('\n');

    const message = `
      📋 APERÇU DÉTAILLÉ DE L'ATTRIBUTION :
      
      👨‍🏫 ENSEIGNANT:
      • Nom: ${this.selectedEnseignant?.nom} ${this.selectedEnseignant?.prenom}
      • Spécialités: ${this.selectedEnseignant?.matiereDominante?.code || 'Aucune'} ${this.selectedEnseignant?.matiereSecondaire?.code ? ', ' + this.selectedEnseignant.matiereSecondaire.code : ''}
      • Heures déjà attribuées: ${this.selectedEnseignantHeuresAttribuees}h
      • Heures maximum: ${this.selectedEnseignant?.heuresMaxHebdo}h
      
      📚 MATIÈRE:
      • Code: ${this.selectedMatiere?.code}
      • Nom: ${this.selectedMatiere?.nom}
      • Statut: ${this.isMatiereDeEnseignant(this.enseignement.matiereId) ? 'Spécialité' : 'Hors spécialité'}
      
      🏫 CLASSES (${this.selectedClasses.length} sélectionnée(s)):
${classesDetails}
      
      ⏰ VOLUME HORAIRE:
      • Heures par classe: ${this.enseignement.heuresParSemaine} heures/semaine
      • Heures totales: ${this.selectedClasses.length * this.enseignement.heuresParSemaine} heures/semaine
      • Total après attribution: ${this.selectedEnseignantHeuresAttribuees + (this.selectedClasses.length * this.enseignement.heuresParSemaine)}h sur ${this.selectedEnseignant?.heuresMaxHebdo}h
      • Validation: ${this.heuresValidation.message}
      
      ${this.commentaire ? `
      💬 COMMENTAIRE:
      ${this.commentaire}
      ` : ''}
      
      ${this.isMatiereDeEnseignant(this.enseignement.matiereId) ? 
        '✅ Cette attribution respecte les spécialités de l\'enseignant' : 
        '⚠️ ATTENTION: Cette matière n\'est pas une spécialité de l\'enseignant'}
    `;
    
    alert(message.replace(/^\s+/gm, ''));
  }

  onSubmit() {
    // Validation finale avant soumission
    this.validateHeures();
    
    // Vérifier qu'au moins une classe est sélectionnée
    if (this.selectedClasses.length === 0) {
      this.notificationService.showError('Erreur', 'Veuillez sélectionner au moins une classe');
      return;
    }

    // Vérifier que tous les champs requis sont remplis
    if (!this.enseignement.enseignantId || !this.enseignement.matiereId || !this.enseignement.heuresParSemaine) {
      this.notificationService.showError('Erreur', 'Veuillez remplir tous les champs obligatoires');
      return;
    }

    // CORRECTION IMPORTANTE : Bloquer la soumission si dépassement
    if (this.heuresValidation.isError) {
      const confirmSubmit = confirm(
        `ATTENTION CRITIQUE : ${this.heuresValidation.message}\n${this.heuresValidation.details}\n\nL'enseignant ne peut pas dépasser sa limite d'heures.\nVoulez-vous quand même continuer ?`
      );
      
      if (!confirmSubmit) {
        return;
      }
    }

    if (this.isEditMode) {
      this.updateEnseignement();
    } else {
      this.createEnseignement();
    }
  }
  
  createEnseignement() {
    this.isLoading = true;
    
    if (!this.enseignement.enseignantId || !this.enseignement.matiereId || this.selectedClasses.length === 0) {
      this.notificationService.showError('Erreur', 'Veuillez sélectionner tous les champs obligatoires');
      this.isLoading = false;
      return;
    }

    // Créer un enseignement POUR CHAQUE CLASSE sélectionnée
    const creations: Promise<any>[] = [];
    
    for (const classeId of this.selectedClasses) {
      const classe = this.classes.find(c => c.id === classeId);
      
      const enseignementToSend: EnseignementDTO = {
        enseignantId: this.enseignement.enseignantId,
        matiereId: this.enseignement.matiereId,
        classeId: classeId,
        heuresParSemaine: this.enseignement.heuresParSemaine,
        
        enseignantNom: this.selectedEnseignant?.nom,
        enseignantPrenom: this.selectedEnseignant?.prenom,
        enseignantMatricule: this.selectedEnseignant?.matricule,
        
        classeNom: classe?.nom,
        classeNiveau: classe?.niveau,
        classeFiliere: classe?.filiere,
        classeEffectif: classe?.effectif,
        
        matiereCode: this.selectedMatiere?.code,
        matiereNom: this.selectedMatiere?.nom,
        matiereCycle: this.selectedMatiere?.cycle,
        
        estMatiereDominante: this.isMatiereDominante(this.enseignement.matiereId),
        statut: 'ACTIF',
        ordrePriorite: 2,
        commentaire: this.commentaire || '',
        heuresAttribuees: 0,
        heuresRestantes: this.enseignement.heuresParSemaine
      };

      // Convertir l'observable en promesse pour gérer plusieurs appels
      creations.push(
        this.enseignementService.createEnseignement(enseignementToSend).toPromise()
      );
    }

    // Attendre que toutes les créations soient terminées
    Promise.all(creations)
      .then(results => {
        console.log('✅ Créations réussies:', results);
        this.notificationService.showSuccess(
          'Attributions créées avec succès!',
          `${results.length} attribution(s) créée(s) pour ${this.selectedEnseignant?.nom}`
        );
        this.isLoading = false;
        this.router.navigate(['/enseignements']);
      })
      .catch(error => {
        console.error('❌ Erreur lors de la création:', error);
        this.notificationService.showError(
          'Erreur',
          error.error?.message || error.message || 'Une erreur est survenue'
        );
        this.isLoading = false;
      });
  }

  // MODIFIÉE POUR GÉRER LA MULTI-SÉLECTION
  updateEnseignement() {
    if (!this.enseignement.id) {
      this.notificationService.showError('Erreur', 'ID de l\'enseignement manquant');
      return;
    }

    this.isLoading = true;

    // Pour la mise à jour, on utilise la première classe sélectionnée
    // (on ne peut pas mettre à jour plusieurs classes à la fois)
    const classeId = this.selectedClasses[0] || this.enseignement.classeId;
    const classe = this.classes.find(c => c.id === classeId);

    const enseignementToSend: EnseignementDTO = {
      id: this.enseignement.id,
      enseignantId: this.enseignement.enseignantId,
      matiereId: this.enseignement.matiereId,
      classeId: classeId,
      heuresParSemaine: this.enseignement.heuresParSemaine,
      
      enseignantNom: this.selectedEnseignant?.nom,
      enseignantPrenom: this.selectedEnseignant?.prenom,
      enseignantMatricule: this.selectedEnseignant?.matricule,
      
      classeNom: classe?.nom,
      classeNiveau: classe?.niveau,
      classeFiliere: classe?.filiere,
      classeEffectif: classe?.effectif,
      
      matiereCode: this.selectedMatiere?.code,
      matiereNom: this.selectedMatiere?.nom,
      matiereCycle: this.selectedMatiere?.cycle,
      
      estMatiereDominante: this.isMatiereDominante(this.enseignement.matiereId),
      statut: 'ACTIF',
      ordrePriorite: 2,
      commentaire: this.commentaire || ''
    };

    this.enseignementService.updateEnseignement(this.enseignement.id, enseignementToSend).subscribe({
      next: (response) => {
        console.log('✅ Mise à jour réussie:', response);
        this.notificationService.showSuccess(
          'Succès',
          'Attribution mise à jour avec succès!'
        );
        this.isLoading = false;
        this.router.navigate(['/enseignements']);
      },
      error: (error) => {
        console.error('Erreur lors de la mise à jour:', error);
        this.notificationService.showError(
          'Erreur',
          error.error?.message || error.message || 'Une erreur est survenue'
        );
        this.isLoading = false;
      }
    });
  }

  confirmDelete() {
    if (!this.enseignement.id) return;
    
    const classesList = this.selectedClasses.length > 0 
      ? this.getSelectedClassesDetails().map(c => c.nom).join(', ')
      : this.selectedClasse?.nom || 'Non spécifiée';
    
    const confirmationMessage = `
      Voulez-vous vraiment supprimer cette attribution ?
      
      Enseignant: ${this.selectedEnseignant?.nom} ${this.selectedEnseignant?.prenom}
      Matière: ${this.selectedMatiere?.nom}
      Classe(s): ${classesList}
      Heures: ${this.enseignement.heuresParSemaine}h/semaine
      
      Cette action est irréversible.
    `;
    
    if (confirm(confirmationMessage.replace(/^\s+/gm, ''))) {
      this.deleteEnseignement();
    }
  }

  deleteEnseignement() {
    if (!this.enseignement.id) return;
    
    this.isLoading = true;
    this.enseignementService.deleteEnseignement(this.enseignement.id).subscribe({
      next: () => {
        this.notificationService.showSuccess(
          'Attribution supprimée avec succès!',
          'L\'attribution a été supprimée définitivement'
        );
        this.isLoading = false;
        this.router.navigate(['/enseignements']);
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        this.notificationService.showError(
          'Erreur lors de la suppression',
          error.error?.message || error.message || 'Une erreur est survenue'
        );
        this.isLoading = false;
      }
    });
  }

  cancel() {
    if (this.enseignement.enseignantId || this.enseignement.matiereId || this.selectedClasses.length > 0) {
      if (confirm('Voulez-vous vraiment annuler ? Les modifications seront perdues.')) {
        this.router.navigate(['/enseignements']);
      }
    } else {
      this.router.navigate(['/enseignements']);
    }
  }
}