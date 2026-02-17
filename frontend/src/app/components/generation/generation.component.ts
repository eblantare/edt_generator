// C:\projets\java\edt-generator\frontend\src\app\components\generation\generation.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
// Import des services
import { ClasseService } from '../../services/classe.service';
import { EnseignantService } from '../../services/enseignant.service';
import { GenerationService, GenerationOptionsDTO } from '../../services/generation.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-generation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HttpClientModule],
  template: `
    <div class="container mt-4">
      <div class="card">
        <div class="card-header bg-primary text-white">
          <h3 class="mb-0">
            <i class="bi bi-calendar-week me-2"></i>Génération des Emplois du Temps
          </h3>
        </div>

        <div class="card-body">
          <!-- Diagnostic de la base de données -->
          <div class="row mb-4" *ngIf="showDiagnostic">
            <div class="col-12">
              <div class="alert alert-info">
                <h5><i class="bi bi-info-circle me-2"></i>Diagnostic de la base de données</h5>
                <div *ngIf="diagnosticData">
                  <p><strong>Statut:</strong> {{ diagnosticData.statut }}</p>
                  <div class="row">
                    <div class="col-md-3">Classes: {{ diagnosticData.classes || 0 }}</div>
                    <div class="col-md-3">Enseignants: {{ diagnosticData.enseignants || 0 }}</div>
                    <div class="col-md-3">Matières: {{ diagnosticData.matieres || 0 }}</div>
                    <div class="col-md-3">Enseignements: {{ diagnosticData.enseignements || 0 }}</div>
                  </div>
                  <div *ngIf="diagnosticData.enseignements === 0" class="mt-2 alert alert-warning">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    Aucun enseignement trouvé dans la base. Vérifiez la configuration.
                  </div>
                </div>
                <div *ngIf="diagnosticError" class="mt-2 alert alert-danger">
                  {{ diagnosticError }}
                </div>
                <button class="btn btn-sm btn-outline-primary mt-2" (click)="executerDiagnostic()">
                  <i class="bi bi-arrow-clockwise me-1"></i>Rafraîchir le diagnostic
                </button>
              </div>
            </div>
          </div>

          <!-- Année scolaire -->
          <div class="row mb-4">
            <div class="col-md-6">
              <label for="anneeScolaire" class="form-label">Année scolaire</label>
              <div class="input-group">
                <input type="text"
                       id="anneeScolaire"
                       class="form-control"
                       [(ngModel)]="anneeScolaire"
                       placeholder="Ex: 2023-2024">
                <button class="btn btn-outline-secondary" type="button" (click)="utiliserAnneeCourante()">
                  <i class="bi bi-calendar-check me-1"></i>Courante
                </button>
              </div>
              <div *ngIf="!estAnneeScolaireValide()" class="form-text text-danger">
                Format invalide. Utilisez AAAA-AAAA (ex: 2023-2024)
              </div>
            </div>
            <div class="col-md-6">
              <div class="form-check mt-4">
                <input class="form-check-input" type="checkbox" id="showDiagnosticCheck" [(ngModel)]="showDiagnostic">
                <label class="form-check-label" for="showDiagnosticCheck">
                  Afficher le diagnostic de la base
                </label>
              </div>
            </div>
          </div>

          <div class="row">
            <!-- Génération globale -->
            <div class="col-md-6 mb-4">
              <div class="card h-100">
                <div class="card-body text-center">
                  <h5 class="card-title">
                    <i class="bi bi-globe me-2"></i>Génération globale
                  </h5>
                  <p class="card-text">
                    Génère un emploi du temps complet pour toutes les classes et enseignants.
                  </p>
                  <div class="alert alert-warning" *ngIf="diagnosticData?.enseignements === 0">
                    <small><i class="bi bi-exclamation-triangle me-1"></i>Base de données vide</small>
                  </div>
                  <button class="btn btn-primary btn-lg"
                          (click)="genererGlobal()"
                          [disabled]="isGenerating || !estAnneeScolaireValide()">
                    <i class="bi bi-play-circle me-2"></i>
                    {{ isGenerating ? 'Génération en cours...' : 'Lancer la génération' }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Par classe -->
            <div class="col-md-6 mb-4">
              <div class="card h-100">
                <div class="card-body">
                  <h5 class="card-title">
                    <i class="bi bi-mortarboard me-2"></i>Par classe
                  </h5>
                  <p class="card-text">
                    Génère un emploi du temps pour une classe spécifique.
                  </p>
                  <div class="mb-3">
                    <label for="classeId" class="form-label">Sélectionnez une classe</label>
                    <select id="classeId"
                            class="form-select"
                            [(ngModel)]="selectedClasseId">
                      <option value="">Sélectionnez une classe</option>
                      <option *ngFor="let classe of classes"
                              [value]="classe.id">
                        {{ classe.nom }}
                      </option>
                    </select>
                    <div *ngIf="classes.length === 0" class="form-text text-warning">
                      Aucune classe disponible
                    </div>
                  </div>
                  <button class="btn btn-primary"
                          (click)="genererPourClasse()"
                          [disabled]="!selectedClasseId || isGenerating || !estAnneeScolaireValide()">
                    <i class="bi bi-play-circle me-1"></i>
                    Générer pour cette classe
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <!-- Par enseignant -->
            <div class="col-md-6 mb-4">
              <div class="card h-100">
                <div class="card-body">
                  <h5 class="card-title">
                    <i class="bi bi-person me-2"></i>Par enseignant
                  </h5>
                  <p class="card-text">
                    Génère un emploi du temps pour un enseignant spécifique.
                  </p>
                  <div class="mb-3">
                    <label for="enseignantId" class="form-label">Sélectionnez un enseignant</label>
                    <select id="enseignantId"
                            class="form-select"
                            [(ngModel)]="selectedEnseignantId">
                      <option value="">Sélectionnez un enseignant</option>
                      <option *ngFor="let enseignant of enseignants"
                              [value]="enseignant.id">
                        {{ enseignant.nom }} {{ enseignant.prenom }}
                      </option>
                    </select>
                    <div *ngIf="enseignants.length === 0" class="form-text text-warning">
                      Aucun enseignant disponible
                    </div>
                  </div>
                  <button class="btn btn-primary"
                          (click)="genererPourEnseignant()"
                          [disabled]="!selectedEnseignantId || isGenerating || !estAnneeScolaireValide()">
                    <i class="bi bi-play-circle me-1"></i>
                    Générer pour cet enseignant
                  </button>
                </div>
              </div>
            </div>

            <!-- Configuration -->
            <div class="col-md-6 mb-4">
              <div class="card h-100">
                <div class="card-body">
                  <h5 class="card-title">
                    <i class="bi bi-gear me-2"></i>Configuration
                  </h5>
                  <p class="card-text">
                    Configurez les paramètres de génération.
                  </p>
                  <div class="mb-3">
                    <label class="form-label">Options de génération</label>
                    <div class="form-check mb-2">
                      <input class="form-check-input"
                             type="checkbox"
                             id="verifierConflits"
                             [(ngModel)]="options.verifierConflits"
                             checked>
                      <label class="form-check-label" for="verifierConflits">
                        Vérifier les conflits
                      </label>
                    </div>
                    <div class="form-check mb-2">
                      <input class="form-check-input"
                             type="checkbox"
                             id="optimiserRepartition"
                             [(ngModel)]="options.optimiserRepartition"
                             checked>
                      <label class="form-check-label" for="optimiserRepartition">
                        Optimiser la répartition
                      </label>
                    </div>
                    <div class="form-check mb-2">
                      <input class="form-check-input"
                             type="checkbox"
                             id="genererSalles"
                             [(ngModel)]="options.genererSalles">
                      <label class="form-check-label" for="genererSalles">
                        Attribuer automatiquement les salles
                      </label>
                    </div>
                    <div class="form-check mb-2">
                      <input class="form-check-input"
                             type="checkbox"
                             id="respecterContraintesEPS"
                             [(ngModel)]="options.respecterContraintesEPS"
                             checked>
                      <label class="form-check-label" for="respecterContraintesEPS">
                        Respecter contraintes EPS
                      </label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input"
                             type="checkbox"
                             id="placerPauses"
                             [(ngModel)]="options.placerPauses"
                             checked>
                      <label class="form-check-label" for="placerPauses">
                        Placer les pauses automatiquement
                      </label>
                    </div>
                  </div>
                  <button class="btn btn-outline-primary" [routerLink]="['/configuration']">
                    <i class="bi bi-sliders me-1"></i>Paramètres avancés
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Historique des générations -->
          <div class="row mt-4">
            <div class="col-12">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">
                    <i class="bi bi-clock-history me-2"></i>Historique des générations
                  </h5>
                  <button class="btn btn-sm btn-outline-secondary" (click)="loadHistorique()">
                    <i class="bi bi-arrow-clockwise"></i>
                  </button>
                </div>
                <div class="card-body">
                  <div *ngIf="historique.length === 0" class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>Aucune génération effectuée.
                  </div>
                  <div *ngIf="historique.length > 0" class="table-responsive">
                    <table class="table table-hover table-sm">
                      <thead>
                        <tr>
                          <th>Date</th>
                          <th>Type</th>
                          <th>Nom</th>
                          <th>Statut</th>
                          <th>Créneaux</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr *ngFor="let item of historique">
                          <td>{{ formatDate(item.dateGeneration) }}</td>
                          <td>
                            <span class="badge" [ngClass]="getTypeBadgeClass(item.type || detecterType(item.nom))">
                              {{ item.type || detecterType(item.nom) }}
                            </span>
                          </td>
                          <td>{{ item.nom }}</td>
                          <td>
                            <span class="badge" [ngClass]="getStatutBadgeClass(item.statut)">
                              {{ item.statut }}
                            </span>
                          </td>
                          <td>
                            <small *ngIf="item.totalCreneaux">
                              {{ item.creneauxOccupes || 0 }}/{{ item.totalCreneaux }}
                            </small>
                          </td>
                          <td>
                            <div class="btn-group btn-group-sm">
                              <button class="btn btn-outline-primary"
                                      title="Voir"
                                      (click)="voirEmploiDuTemps(item.id)"
                                      [disabled]="item.statut === 'ERREUR'">
                                <i class="bi bi-eye"></i>
                              </button>
                              <button class="btn btn-outline-success"
                                      title="Exporter PDF"
                                      (click)="exportPDFMatriciel(item.id, item.nom)"
                                      [disabled]="item.statut !== 'TERMINE'">
                                <i class="bi bi-file-pdf"></i>
                              </button>
                              <button class="btn btn-outline-info"
                                      title="Exporter Excel"
                                      (click)="exporterExcel(item.id, item.nom)"
                                      [disabled]="item.statut !== 'TERMINE'">
                                <i class="bi bi-file-excel"></i>
                              </button>
                              <!-- NOUVEAU BOUTON SUPPRIMER -->
                              <button class="btn btn-outline-danger"
                                title="Supprimer"
                                (click)="supprimerEmploi(item.id, item.nom)"
                                [disabled]="isDeleting">
                               <i class="bi bi-trash"></i>
                            </button>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class GenerationComponent implements OnInit {
  anneeScolaire = '';
  selectedClasseId = '';
  selectedEnseignantId = '';
  isGenerating = false;
  showDiagnostic = true;

  classes: any[] = [];
  enseignants: any[] = [];
  historique: any[] = [];
  diagnosticData: any = null;
  diagnosticError: string = '';

  options: GenerationOptionsDTO = {
    verifierConflits: true,
    optimiserRepartition: true,
    genererSalles: false,
    respecterContraintesEPS: true,
    placerPauses: true
  };

  constructor(
    private classeService: ClasseService,
    private enseignantService: EnseignantService,
    private generationService: GenerationService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
  this.utiliserAnneeCourante();
  this.loadClasses();
  this.loadEnseignants();

  // Maintenant .subscribe() fonctionne car loadHistorique() retourne un Observable
  this.loadHistorique().subscribe({
    next: () => console.log('Historique chargé'),
    error: (err) => console.error('Erreur chargement historique', err)
  });

  this.executerDiagnostic();
}

  utiliserAnneeCourante() {
    this.anneeScolaire = this.generationService.genererAnneeScolaireCourante();
  }

  estAnneeScolaireValide(): boolean {
    return this.generationService.validerFormatAnneeScolaire(this.anneeScolaire);
  }

  loadClasses() {
    this.classeService.getAllClasses().subscribe({
      next: (data: any) => {
        if (Array.isArray(data)) {
          this.classes = data;
        } else if (data && data.content && Array.isArray(data.content)) {
          this.classes = data.content;
        } else {
          this.classes = [];
        }
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des classes:', error);
        this.classes = [];
        this.notificationService.showWarning('Impossible de charger les classes. Vérifiez la connexion au serveur.');
      }
    });
  }

  loadEnseignants() {
    this.enseignantService.getAllEnseignants().subscribe({
      next: (data: any) => {
        if (Array.isArray(data)) {
          this.enseignants = data;
        } else if (data && data.content && Array.isArray(data.content)) {
          this.enseignants = data.content;
        } else {
          this.enseignants = [];
        }
      },
      error: (error: any) => {
        console.error('Erreur lors du chargement des enseignants:', error);
        this.enseignants = [];
        this.notificationService.showWarning('Impossible de charger les enseignants.');
      }
    });
  }

  loadHistorique(): Observable<any> {  // ← AJOUTER LE TYPE DE RETOUR
  return this.generationService.getHistoriqueGenerations().pipe(
    tap((data: any) => {
      if (Array.isArray(data)) {
        this.historique = data;
      } else {
        this.historique = [];
      }
    }),
    catchError((error) => {
      console.error('Erreur historique:', error);
      this.historique = [];
      return of([]); // ← IMPORTANT : retourner un observable même en erreur
    })
  );
}

  executerDiagnostic() {
    this.generationService.diagnostiquerBaseDeDonnees().subscribe({
      next: (data: any) => {
        this.diagnosticData = data;
        this.diagnosticError = '';

        if (data.statut === 'OK') {
          if (data.enseignements === 0) {
            this.notificationService.showWarning('Base de données vide. Ajoutez des enseignements avant de générer.');
          }
        }
      },
      error: (error: any) => {
        this.diagnosticError = 'Impossible de diagnostiquer la base de données';
        console.error('Erreur diagnostic:', error);
      }
    });
  }

  genererGlobal() {
    if (!this.estAnneeScolaireValide()) {
      this.notificationService.showWarning('Format d\'année scolaire invalide');
      return;
    }

    if (this.diagnosticData?.enseignements === 0) {
      if (!confirm('La base de données semble vide. Voulez-vous quand même continuer ?')) {
        return;
      }
    }

    if (!confirm(`Lancer la génération globale pour ${this.anneeScolaire} ?`)) {
      return;
    }

    this.isGenerating = true;
    this.generationService.genererGlobal(this.anneeScolaire, this.options).subscribe({
      next: (result) => {
        this.notificationService.showSuccess(result.message || 'Génération terminée avec succès !');
        this.loadHistorique();
        this.executerDiagnostic();
      },
      error: (error) => {
        console.error('Erreur lors de la génération globale:', error);
        this.notificationService.showError('Erreur lors de la génération: ' + error.message);
      },
      complete: () => {
        this.isGenerating = false;
      }
    });
  }

  genererPourClasse() {
  // Validation
  if (!this.selectedClasseId) {
    this.notificationService.showWarning('Veuillez sélectionner une classe');
    return;
  }

  const classe = this.classes.find(c => c.id === this.selectedClasseId);

  // Afficher le chargement
  this.isGenerating = true;

  // 1. Générer l'emploi du temps
  this.generationService.genererPourClasse(
    this.selectedClasseId,
    this.anneeScolaire,
    this.options
  ).subscribe({
    next: (result) => {
      // 2. Succès : afficher la notification
      this.notificationService.showSuccess(
        result.message || `Génération pour ${classe.nom} terminée !`
      );

      // 3. RECHARGER l'historique
      this.generationService.getHistoriqueGenerations().subscribe({
        next: (historiqueData) => {
          // 4. Mettre à jour la liste
          if (Array.isArray(historiqueData)) {
            this.historique = historiqueData;
          }
          // 5. Désactiver le chargement SEULEMENT après le rechargement
          this.isGenerating = false;
        },
        error: () => {
          this.isGenerating = false;
        }
      });
    },
    error: (error) => {
      console.error('Erreur génération:', error);
      this.notificationService.showError('Erreur: ' + error.message);
      this.isGenerating = false;
    }
  });
}

  genererPourEnseignant() {
    if (!this.selectedEnseignantId) {
      this.notificationService.showWarning('Veuillez sélectionner un enseignant');
      return;
    }

    if (!this.estAnneeScolaireValide()) {
      this.notificationService.showWarning('Format d\'année scolaire invalide');
      return;
    }

    const enseignant = this.enseignants.find(e => e.id === this.selectedEnseignantId);
    if (!enseignant) {
      this.notificationService.showError('Enseignant non trouvé');
      return;
    }

    if (!confirm(`Générer l'emploi du temps pour ${enseignant.nom} ${enseignant.prenom} (${this.anneeScolaire}) ?`)) {
      return;
    }

    this.isGenerating = true;
    this.generationService.genererPourEnseignant(this.selectedEnseignantId, this.anneeScolaire, this.options).subscribe({
      next: (result) => {
        this.notificationService.showSuccess(result.message || `Génération pour ${enseignant.nom} terminée !`);
        this.loadHistorique();
      },
      error: (error) => {
        console.error('Erreur lors de la génération pour enseignant:', error);
        this.notificationService.showError('Erreur lors de la génération: ' + error.message);
      },
      complete: () => {
        this.isGenerating = false;
      }
    });
  }

  // === MÉTHODES POUR LA VISUALISATION ===
  voirEmploiDuTemps(emploiId: string) {
    this.router.navigate(['/visualisation', emploiId]);
  }

  // REMPLACEZ la méthode exporterPDF existante par celle-ci
  exportPDFMatriciel(emploiId: string, nom: string) {
  // Extraire le nom de la classe depuis le nom de l'emploi du temps
  // Format attendu: "Emploi du temps - 4 ème - 2025-2026"
  let classeNom = "4 ème"; // valeur par défaut

  const match = nom.match(/Emploi du temps - (.+?) - \d{4}-\d{4}/);
  if (match && match[1]) {
    classeNom = match[1].trim();
  }

  this.generationService.exporterPDFMatriciel(emploiId, classeNom).subscribe({
    next: (blob: Blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `EDT_${classeNom.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      this.notificationService.showSuccess('PDF exporté avec succès (format tableau) !');
    },
    error: (error) => {
      console.error('Erreur export PDF matriciel:', error);
      this.notificationService.showError('Erreur lors de l\'export PDF: ' + error.message);
    }
  });
  }

  exporterExcel(emploiId: string, nom: string) {
    this.generationService.exporterExcel(emploiId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${nom.replace(/[^a-z0-9]/gi, '_')}_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        this.notificationService.showSuccess('Excel exporté avec succès !');
      },
      error: (error) => {
        console.error('Erreur export Excel:', error);
        this.notificationService.showError('Erreur lors de l\'export Excel: ' + error.message);
      }
    });
  }

  // === MÉTHODES UTILITAIRES ===
  detecterType(nom: string): string {
    if (!nom) return 'Global';
    const nomLower = nom.toLowerCase();
    if (nomLower.includes('classe')) return 'Classe';
    if (nomLower.includes('enseignant')) return 'Enseignant';
    if (nomLower.includes('global')) return 'Global';
    return 'Global';
  }

  formatDate(date: Date | string): string {
    if (!date) return '';
    try {
      const d = typeof date === 'string' ? new Date(date) : date;
      return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
    } catch (e) {
      return String(date);
    }
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'Global': return 'bg-primary';
      case 'Classe': return 'bg-success';
      case 'Enseignant': return 'bg-info';
      default: return 'bg-secondary';
    }
  }

  getStatutBadgeClass(statut: string): string {
    if (!statut) return 'bg-secondary';

    const statutLower = statut.toLowerCase();
    if (statutLower.includes('termine') || statutLower.includes('terminé') || statutLower.includes('succès')) {
      return 'bg-success';
    } else if (statutLower.includes('cours') || statutLower.includes('en cours')) {
      return 'bg-warning text-dark';
    } else if (statutLower.includes('erreur') || statutLower.includes('échec') || statutLower.includes('echec')) {
      return 'bg-danger';
    } else {
      return 'bg-secondary';
    }
  }

  // Ajoutez cette propriété
  isDeleting = false;

  // Ajoutez cette méthode pour supprimer un emploi du temps
supprimerEmploi(emploiId: string, nomEmploi: string) {
  // Demander confirmation
  if (!confirm(`Supprimer "${nomEmploi}" ?`)) {
    return;
  }

  // Afficher le chargement
  this.isDeleting = true;

  // 1. Supprimer l'emploi
  this.generationService.supprimerEmploiDuTemps(emploiId).subscribe({
    next: () => {
      // 2. Succès : notification
      this.notificationService.showSuccess('Emploi du temps supprimé !');

      // 3. RECHARGER l'historique
      this.generationService.getHistoriqueGenerations().subscribe({
        next: (historiqueData) => {
          // 4. Mettre à jour la liste
          if (Array.isArray(historiqueData)) {
            this.historique = historiqueData;
          }
          // 5. Désactiver le chargement
          this.isDeleting = false;
        },
        error: () => {
          this.isDeleting = false;
        }
      });
    },
    error: (error) => {
      console.error('Erreur suppression:', error);
      this.notificationService.showError('Erreur: ' + error.message);
      this.isDeleting = false;
    }
  });
}
}
