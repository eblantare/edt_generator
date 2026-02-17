// C:\projets\java\edt-generator\frontend\src\app\components\visualisation\visualisation.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { GenerationService } from '../../services/generation.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-visualisation',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterLink],
  template: `
    <div class="container mt-4">
      <!-- Carte principale avec en-tête professionnel -->
      <div class="card shadow-lg border-0 rounded-4">
        <!-- En-tête avec dégradé -->
        <div class="card-header bg-gradient-primary text-white py-3 rounded-top-4"
             style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <h3 class="mb-0 fw-bold">
                <i class="bi bi-calendar-week me-2"></i>{{ emploi?.nom || 'Emploi du temps' }}
              </h3>
              <p class="mb-0 mt-1 opacity-75 small">
                <i class="bi bi-clock me-1"></i> Généré le {{ formatDate(emploi?.dateGeneration) }}
              </p>
            </div>
            <div class="btn-group">
              <button class="btn btn-light btn-sm rounded-pill px-3 mx-1" (click)="exporterPDFMatriciel()">
                <i class="bi bi-file-pdf text-danger me-1"></i>PDF
              </button>
              <button class="btn btn-light btn-sm rounded-pill px-3 mx-1" (click)="exporterExcel()">
                <i class="bi bi-file-excel text-success me-1"></i>Excel
              </button>
              <button class="btn btn-light btn-sm rounded-pill px-3 mx-1" (click)="imprimer()">
                <i class="bi bi-printer text-primary me-1"></i>Imprimer
              </button>
              <button class="btn btn-light btn-sm rounded-pill px-3 mx-1" routerLink="/generation">
                <i class="bi bi-arrow-left me-1"></i>Retour
              </button>
            </div>
          </div>
        </div>

        <div class="card-body p-4">
          <!-- Cartes d'information en haut -->
          <div class="row g-3 mb-4">
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 bg-light rounded-4 h-100">
                <div class="card-body d-flex align-items-center">
                  <div class="rounded-circle bg-primary bg-opacity-10 p-3 me-3">
                    <i class="bi bi-calendar3 text-primary fs-4"></i>
                  </div>
                  <div>
                    <small class="text-muted text-uppercase fw-bold">Année scolaire</small>
                    <h5 class="mb-0 fw-bold">{{ emploi?.anneeScolaire || 'Non spécifiée' }}</h5>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 bg-light rounded-4 h-100">
                <div class="card-body d-flex align-items-center">
                  <div class="rounded-circle bg-success bg-opacity-10 p-3 me-3">
                    <i class="bi bi-tag text-success fs-4"></i>
                  </div>
                  <div>
                    <small class="text-muted text-uppercase fw-bold">Type</small>
                    <h5 class="mb-0">
                      <span class="badge rounded-pill" [ngClass]="getTypeClass(emploi?.type || detecterType(emploi?.nom))">
                        {{ emploi?.type || detecterType(emploi?.nom) }}
                      </span>
                    </h5>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 bg-light rounded-4 h-100">
                <div class="card-body d-flex align-items-center">
                  <div class="rounded-circle bg-warning bg-opacity-10 p-3 me-3">
                    <i class="bi bi-bar-chart text-warning fs-4"></i>
                  </div>
                  <div>
                    <small class="text-muted text-uppercase fw-bold">Statut</small>
                    <h5 class="mb-0">
                      <span class="badge rounded-pill" [ngClass]="getStatutClass(emploi?.statut)">
                        {{ emploi?.statut || 'INCONNU' }}
                      </span>
                    </h5>
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 bg-light rounded-4 h-100">
                <div class="card-body d-flex align-items-center">
                  <div class="rounded-circle bg-info bg-opacity-10 p-3 me-3">
                    <i class="bi bi-people text-info fs-4"></i>
                  </div>
                  <div>
                    <small class="text-muted text-uppercase fw-bold">Classe</small>
                    <h5 class="mb-0 fw-bold">{{ getClassePrincipale() }}</h5>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Zone de statistiques -->
          <div class="row g-3 mb-4" *ngIf="creneauxOccupes.length > 0">
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 text-white rounded-4" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <div class="card-body text-center py-3">
                  <h2 class="fw-bold mb-0">{{ creneauxOccupes.length }}</h2>
                  <small>Cours programmés</small>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 text-white rounded-4" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <div class="card-body text-center py-3">
                  <h2 class="fw-bold mb-0">{{ getMatieresUniques() }}</h2>
                  <small>Matières distinctes</small>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 text-white rounded-4" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <div class="card-body text-center py-3">
                  <h2 class="fw-bold mb-0">{{ getEnseignantsUniques() }}</h2>
                  <small>Enseignants</small>
                </div>
              </div>
            </div>
            <div class="col-md-3 col-sm-6">
              <div class="card border-0 text-white rounded-4" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                <div class="card-body text-center py-3">
                  <h2 class="fw-bold mb-0">{{ getJoursUniques() }}/5</h2>
                  <small>Jours utilisés</small>
                </div>
              </div>
            </div>
          </div>

          <!-- Tableau des créneaux - UNIQUEMENT LES COURS RÉELS -->
          <div class="card border-0 shadow-sm rounded-4">
            <div class="card-header bg-white py-3 rounded-top-4 border-0">
              <h5 class="mb-0 fw-bold">
                <i class="bi bi-table me-2 text-primary"></i>
                Détail des cours
                <span class="badge bg-primary bg-opacity-10 text-primary ms-2">{{ creneauxOccupes.length }} cours</span>
              </h5>
            </div>

            <div class="card-body p-0">
              <div *ngIf="creneauxOccupes.length === 0" class="text-center py-5">
                <i class="bi bi-calendar-x text-muted" style="font-size: 4rem;"></i>
                <h5 class="mt-3 text-muted">Aucun cours programmé</h5>
                <button class="btn btn-outline-primary mt-3 rounded-pill px-4" (click)="chargerCreneaux()">
                  <i class="bi bi-arrow-clockwise me-2"></i>Réessayer
                </button>
              </div>

              <div *ngIf="creneauxOccupes.length > 0" class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="bg-light">
                    <tr>
                      <th class="border-0 rounded-start ps-4">Jour</th>
                      <th class="border-0">Horaire</th>
                      <th class="border-0">Classe</th>
                      <th class="border-0">Matière</th>
                      <th class="border-0 rounded-end pe-4">Enseignant</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let creneau of creneauxOccupes" class="border-bottom">
                      <td class="ps-4">
                        <span class="fw-semibold">{{ creneau.jourSemaine }}</span>
                      </td>
                      <td>
                        <span class="badge bg-light text-dark rounded-pill px-3 py-2">
                          {{ creneau.heureDebut }} - {{ creneau.heureFin }}
                        </span>
                      </td>
                      <td>
                        <span class="fw-semibold">{{ creneau.classeNom || 'Non spécifiée' }}</span>
                      </td>
                      <td>
                        <span class="badge bg-primary bg-opacity-10 text-primary rounded-pill px-3 py-2">
                          {{ creneau.matiereNom }}
                        </span>
                      </td>
                      <td class="pe-4">
                        <div class="d-flex align-items-center">
                          <div class="rounded-circle bg-secondary bg-opacity-10 p-2 me-2">
                            <i class="bi bi-person text-secondary small"></i>
                          </div>
                          <span>{{ creneau.enseignantNom }}</span>
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
  `,
  styles: [`
    .bg-gradient-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    .bg-light-success {
      background-color: rgba(40, 167, 69, 0.05);
    }
    .rounded-4 {
      border-radius: 1rem !important;
    }
    .rounded-top-4 {
      border-top-left-radius: 1rem !important;
      border-top-right-radius: 1rem !important;
    }
    .rounded-bottom-4 {
      border-bottom-left-radius: 1rem !important;
      border-bottom-right-radius: 1rem !important;
    }
  `]
})
export class VisualisationComponent implements OnInit {
  emploiId: string = '';
  emploi: any = null;
  creneaux: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private generationService: GenerationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.emploiId = this.route.snapshot.paramMap.get('id') || '';

    if (this.emploiId) {
      this.chargerEmploiDuTemps();
      this.chargerCreneaux();
    } else {
      this.notificationService.showError('ID d\'emploi du temps manquant');
    }
  }

  // Propriété calculée : uniquement les créneaux occupés
  get creneauxOccupes(): any[] {
    return this.creneaux.filter(c => !c.estLibre);
  }

  chargerEmploiDuTemps() {
    this.generationService.getEmploiDuTemps(this.emploiId).subscribe({
      next: (data) => {
        this.emploi = data;
      },
      error: (error) => {
        console.error('Erreur chargement emploi:', error);
        this.notificationService.showWarning('Impossible de charger les détails de l\'emploi du temps');
      }
    });
  }

  chargerCreneaux() {
    this.generationService.getCreneauxParEmploi(this.emploiId).subscribe({
      next: (data) => {
        this.creneaux = data;
      },
      error: (error) => {
        console.error('Erreur chargement créneaux:', error);
        this.notificationService.showWarning('Impossible de charger les créneaux horaires');
      }
    });
  }

  exporterPDFMatriciel() {
    if (!this.emploi) return;

    // Extraire le nom de la classe
    let classeNom = this.getClassePrincipale();

    this.generationService.exporterPDFMatriciel(this.emploiId, classeNom).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `EDT_${classeNom}_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.notificationService.showSuccess('PDF exporté avec succès !');
      },
      error: (error) => {
        console.error('Erreur export PDF:', error);
        this.notificationService.showError('Erreur lors de l\'export PDF');
      }
    });
  }

  exporterExcel() {
    this.generationService.exporterExcel(this.emploiId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.emploi?.nom || 'emploi'}_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.notificationService.showSuccess('Excel exporté avec succès !');
      },
      error: (error) => {
        console.error('Erreur export Excel:', error);
        this.notificationService.showError('Erreur lors de l\'export Excel');
      }
    });
  }

  imprimer() {
    window.print();
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

  getClassePrincipale(): string {
    if (this.emploi?.classeNom) {
      return this.emploi.classeNom;
    }

    // Sinon, chercher dans les créneaux
    for (let c of this.creneauxOccupes) {
      if (c.classeNom) {
        return c.classeNom;
      }
    }
    return 'Non spécifiée';
  }

  getMatieresUniques(): number {
    const matieres = new Set(this.creneauxOccupes.map(c => c.matiereNom).filter(Boolean));
    return matieres.size;
  }

  getEnseignantsUniques(): number {
    const enseignants = new Set(this.creneauxOccupes.map(c => c.enseignantNom).filter(Boolean));
    return enseignants.size;
  }

  getJoursUniques(): number {
    const jours = new Set(this.creneauxOccupes.map(c => c.jourSemaine));
    return jours.size;
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

  getStatutClass(statut: string): string {
    const statutLower = statut ? statut.toLowerCase() : '';
    if (statutLower.includes('termine') || statutLower.includes('terminé') || statutLower.includes('succès')) {
      return 'bg-success';
    } else if (statutLower.includes('cours') || statutLower.includes('en cours')) {
      return 'bg-warning text-dark';
    } else if (statutLower.includes('erreur') || statutLower.includes('échec')) {
      return 'bg-danger';
    } else {
      return 'bg-secondary';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'Global': return 'bg-primary';
      case 'Classe': return 'bg-success';
      case 'Enseignant': return 'bg-info';
      default: return 'bg-secondary';
    }
  }
}
