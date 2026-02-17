// C:\projets\java\edt-generator\frontend\src\app\components\visualisation\visualisation.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { GenerationService } from '../../services/generation.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-visualisation',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  template: `
    <div class="container mt-4">
      <div class="card">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
          <h3 class="mb-0">
            <i class="bi bi-calendar3 me-2"></i>{{ emploi?.nom || 'Visualisation' }}
          </h3>
          <div class="btn-group">
            <button class="btn btn-light btn-sm" (click)="exporterPDF()">
              <i class="bi bi-file-pdf me-1"></i>PDF
            </button>
            <button class="btn btn-light btn-sm" (click)="exporterExcel()">
              <i class="bi bi-file-excel me-1"></i>Excel
            </button>
            <button class="btn btn-light btn-sm" (click)="imprimer()">
              <i class="bi bi-printer me-1"></i>Imprimer
            </button>
            <button class="btn btn-light btn-sm" (click)="retour()">
              <i class="bi bi-arrow-left me-1"></i>Retour
            </button>
          </div>
        </div>

        <div class="card-body">
          <!-- Informations générales -->
          <div class="row mb-4">
            <div class="col-md-3">
              <div class="card bg-light">
                <div class="card-body">
                  <h6 class="card-title">Année scolaire</h6>
                  <p class="card-text">{{ emploi?.anneeScolaire || 'Non spécifié' }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-light">
                <div class="card-body">
                  <h6 class="card-title">Date génération</h6>
                  <p class="card-text">{{ formatDate(emploi?.dateGeneration) }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-light">
                <div class="card-body">
                  <h6 class="card-title">Statut</h6>
                  <span class="badge" [ngClass]="getStatutClass(emploi?.statut)">
                    {{ emploi?.statut || 'INCONNU' }}
                  </span>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card bg-light">
                <div class="card-body">
                  <h6 class="card-title">Type</h6>
                  <span class="badge" [ngClass]="getTypeClass(emploi?.type || detecterType(emploi?.nom))">
                    {{ emploi?.type || detecterType(emploi?.nom) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Tableau des créneaux -->
          <div *ngIf="creneaux.length > 0" class="table-responsive">
            <h5 class="mb-3">Créneaux horaires</h5>
            <table class="table table-bordered table-hover">
              <thead class="table-dark">
                <tr>
                  <th>Jour</th>
                  <th>Heure début</th>
                  <th>Heure fin</th>
                  <th>Classe</th>
                  <th>Matière</th>
                  <th>Enseignant</th>
                  <th>Salle</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let creneau of creneaux">
                  <td>{{ creneau.jourSemaine }}</td>
                  <td>{{ creneau.heureDebut }}</td>
                  <td>{{ creneau.heureFin }}</td>
                  <td>{{ creneau.classeNom || 'Non spécifié' }}</td>
                  <td>{{ creneau.matiereNom || 'Non spécifié' }}</td>
                  <td>{{ creneau.enseignantNom || 'Non spécifié' }}</td>
                  <td>{{ creneau.salle || 'Non attribuée' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div *ngIf="creneaux.length === 0" class="alert alert-warning">
            Aucun créneau trouvé pour cet emploi du temps.
            <button class="btn btn-sm btn-outline-primary ms-2" (click)="chargerCreneaux()">
              <i class="bi bi-arrow-clockwise"></i> Réessayer
            </button>
          </div>

          <!-- Statistiques -->
          <div *ngIf="creneaux.length > 0" class="row mt-4">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <h5 class="mb-0">Statistiques</h5>
                </div>
                <div class="card-body">
                  <div class="row">
                    <div class="col-md-3">
                      <div class="card bg-info text-white">
                        <div class="card-body text-center">
                          <h6 class="card-title">Total créneaux</h6>
                          <p class="card-text display-6">{{ creneaux.length }}</p>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-3">
                      <div class="card bg-success text-white">
                        <div class="card-body text-center">
                          <h6 class="card-title">Créneaux occupés</h6>
                          <p class="card-text display-6">{{ getCreneauxOccupes() }}</p>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-3">
                      <div class="card bg-warning text-white">
                        <div class="card-body text-center">
                          <h6 class="card-title">Jours utilisés</h6>
                          <p class="card-text display-6">{{ getJoursUniques() }}</p>
                        </div>
                      </div>
                    </div>
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

  exporterPDF() {
    this.generationService.exporterPDF(this.emploiId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.emploi?.nom || 'emploi'}_${new Date().toISOString().split('T')[0]}.pdf`;
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

  retour() {
    window.history.back();
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

  getCreneauxOccupes(): number {
    return this.creneaux.filter(c => !c.estLibre).length;
  }

  getJoursUniques(): number {
    const jours = new Set(this.creneaux.map(c => c.jourSemaine));
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
      return 'bg-warning';
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