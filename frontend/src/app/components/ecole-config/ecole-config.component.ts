import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { EcoleService, EcoleDTO } from '../../services/ecole.service';
import { NotificationService } from '../../services/notification.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-ecole-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container mt-4">
      <div class="row">
        <!-- Formulaire de configuration -->
        <div class="col-md-6">
          <div class="card">
            <div class="card-header bg-primary text-white">
              <h3 class="mb-0">
                <i class="bi bi-building me-2"></i>Configuration de l'établissement
              </h3>
            </div>
            <div class="card-body">
              <form (ngSubmit)="onSubmit()" #ecoleForm="ngForm">
                <!-- Logo avec upload de fichier -->
                <div class="mb-3">
                  <label class="form-label fw-semibold">Logo</label>
                  <div class="input-group">
                    <input type="file"
                           class="form-control"
                           (change)="onFileSelected($event)"
                           accept="image/*"
                           #fileInput
                           style="display: none;">
                    <input type="text"
                           class="form-control"
                           [(ngModel)]="currentEcole.logo"
                           name="logo"
                           placeholder="URL du logo"
                           readonly>
                    <button class="btn btn-outline-primary"
                            type="button"
                            (click)="fileInput.click()">
                      <i class="bi bi-image me-1"></i>Choisir
                    </button>
                    <button class="btn btn-outline-info"
                            type="button"
                            (click)="showLogoPreview = !showLogoPreview"
                            [disabled]="!currentEcole.logo">
                      <i class="bi bi-search"></i>
                    </button>
                  </div>
                  <small class="text-muted">Formats acceptés: JPG, PNG, GIF</small>

                  <!-- Aperçu du logo -->
                  <div *ngIf="showLogoPreview && currentEcole.logo" class="mt-2 p-2 border rounded text-center">
                    <img [src]="currentEcole.logo"
                         alt="Logo aperçu"
                         style="max-height: 100px; max-width: 100%;"
                         (error)="onLogoError()">
                    <button class="btn btn-sm btn-outline-secondary mt-2" (click)="showLogoPreview = false">
                      <i class="bi bi-x"></i> Fermer
                    </button>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label fw-semibold">Nom de l'école</label>
                    <input type="text" class="form-control" [(ngModel)]="currentEcole.nom" name="nom" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label fw-semibold">Téléphone</label>
                    <input type="text" class="form-control" [(ngModel)]="currentEcole.telephone" name="telephone">
                  </div>
                </div>

                <div class="mb-3">
                  <label class="form-label fw-semibold">Adresse</label>
                  <textarea class="form-control" [(ngModel)]="currentEcole.adresse" name="adresse" rows="2"></textarea>
                </div>

                <div class="mb-3">
                  <label class="form-label fw-semibold">Devise</label>
                  <input type="text" class="form-control" [(ngModel)]="currentEcole.devise" name="devise">
                </div>

                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label fw-semibold">DRE</label>
                    <input type="text" class="form-control" [(ngModel)]="currentEcole.dre" name="dre">
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label fw-semibold">IESG</label>
                    <input type="text" class="form-control" [(ngModel)]="currentEcole.iesg" name="iesg">
                  </div>
                </div>

                <div class="mb-3">
                  <label class="form-label fw-semibold">Boîte Postale</label>
                  <input type="text" class="form-control" [(ngModel)]="currentEcole.bp" name="bp">
                </div>

                <div class="d-grid gap-2">
                  <button type="submit" class="btn btn-primary" [disabled]="!ecoleForm.form.valid || uploading">
                    <span *ngIf="!uploading">
                      <i class="bi bi-save me-2"></i>Enregistrer
                    </span>
                    <span *ngIf="uploading">
                      <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                      Upload en cours...
                    </span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- Aperçu de l'entête -->
        <div class="col-md-6">
          <div class="card">
            <div class="card-header bg-info text-white">
              <h5 class="mb-0">
                <i class="bi bi-eye me-2"></i>Aperçu de l'entête de l'emploi du temps
              </h5>
            </div>
            <div class="card-body">
              <div class="entete-emploi-du-temps p-3 border rounded bg-light">
                <!-- Logo et ministère -->
                <div class="row align-items-center mb-3">
                  <div class="col-3 text-center">
                    <img *ngIf="currentEcole.logo" [src]="currentEcole.logo" alt="Logo" class="img-fluid" style="max-height: 80px;">
                    <div *ngIf="!currentEcole.logo" class="bg-secondary text-white d-flex align-items-center justify-content-center"
                         style="width: 80px; height: 80px; margin: 0 auto;">
                      <i class="bi bi-image fs-1"></i>
                    </div>
                  </div>
                  <div class="col-9 text-center">
                    <h5 class="fw-bold mb-1">MINISTERE DE L'EDUCATION NATIONALE</h5>
                    <p class="mb-1">D.R.E: {{ currentEcole.dre || '____________________' }}</p>
                    <p class="mb-1">I.E.S.G: {{ currentEcole.iesg || '____________________' }}</p>
                  </div>
                </div>

                <!-- École et république -->
                <div class="row mb-3">
                  <div class="col-6">
                    <h6 class="fw-bold mb-1">{{ currentEcole.nom || '____________________' }}</h6>
                    <p class="mb-1">Tél: {{ currentEcole.telephone || '____________________' }}</p>
                    <p class="mb-1">BP: {{ currentEcole.bp || '____________________' }}</p>
                  </div>
                  <div class="col-6 text-end">
                    <h6 class="fw-bold">REPUBLIQUE TOGOLAISE</h6>
                    <p class="fst-italic">{{ currentEcole.devise || 'Travail-Liberté-Patrie' }}</p>
                  </div>
                </div>

                <!-- Titre emploi du temps -->
                <div class="text-center mt-3">
                  <h4 class="fw-bold">EMPLOI DU TEMPS - {{ selectedClasse || 'CLASSE' }}</h4>
                  <p>Année scolaire 2025-2026 | Généré le {{ today | date:'dd/MM/yyyy' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Liste des écoles enregistrées -->
      <div class="row mt-4">
        <div class="col-12">
          <div class="card">
            <div class="card-header bg-success text-white">
              <h5 class="mb-0">
                <i class="bi bi-list me-2"></i>Écoles enregistrées
              </h5>
            </div>
            <div class="card-body">
              <div *ngIf="ecoles.length === 0" class="alert alert-info">
                Aucune école enregistrée.
              </div>
              <div class="table-responsive" *ngIf="ecoles.length > 0">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>Logo</th>
                      <th>Nom</th>
                      <th>DRE</th>
                      <th>IESG</th>
                      <th>Téléphone</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let ecole of ecoles">
                      <td>
                        <div class="logo-mini" (click)="previewLogo(ecole)" style="cursor: pointer;">
                          <img *ngIf="ecole.logo" [src]="ecole.logo + '?t=' + getTimestamp()" alt="logo" style="max-height: 40px; max-width: 40px;" (error)="onLogoError()">
                          <i *ngIf="!ecole.logo" class="bi bi-building fs-3"></i>
                        </div>
                      </td>
                      <td>{{ ecole.nom }}</td>
                      <td>{{ ecole.dre }}</td>
                      <td>{{ ecole.iesg }}</td>
                      <td>{{ ecole.telephone }}</td>
                      <td>
                        <div class="btn-group btn-group-sm">
                          <!-- Bouton aperçu de l'entête -->
                          <button class="btn btn-outline-info" title="Aperçu de l'entête" (click)="previewEntete(ecole)">
                            <i class="bi bi-eye"></i>
                          </button>
                          <!-- Bouton modifier -->
                          <button class="btn btn-outline-primary" title="Modifier" (click)="editEcole(ecole)">
                            <i class="bi bi-pencil"></i>
                          </button>
                          <!-- Bouton détail -->
                          <button class="btn btn-outline-secondary" title="Détail" (click)="viewDetails(ecole)">
                            <i class="bi bi-info-circle"></i>
                          </button>
                          <!-- Bouton supprimer -->
                          <button class="btn btn-outline-danger" title="Supprimer" (click)="deleteEcole(ecole)">
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

    <!-- Modal d'aperçu de l'entête -->
    <div class="modal fade" id="previewModal" tabindex="-1" [class.show]="showPreviewModal" [style.display]="showPreviewModal ? 'block' : 'none'">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">
              <i class="bi bi-eye me-2"></i>Aperçu de l'entête
            </h5>
            <button type="button" class="btn-close" (click)="closePreview()"></button>
          </div>
          <div class="modal-body">
            <div class="entete-emploi-du-temps p-3 border rounded">
              <!-- Même structure que l'aperçu -->
              <div class="row align-items-center mb-3">
                <div class="col-3 text-center">
                  <img *ngIf="previewEcole?.logo" [src]="previewEcole?.logo + '?t=' + getTimestamp()" alt="Logo" class="img-fluid" style="max-height: 80px;" (error)="onLogoError()">
                  <div *ngIf="!previewEcole?.logo" class="bg-secondary text-white d-flex align-items-center justify-content-center"
                       style="width: 80px; height: 80px; margin: 0 auto;">
                    <i class="bi bi-image fs-1"></i>
                  </div>
                </div>
                <div class="col-9 text-center">
                  <h5 class="fw-bold mb-1">MINISTERE DE L'EDUCATION NATIONALE</h5>
                  <p class="mb-1">D.R.E: {{ previewEcole?.dre || '____________________' }}</p>
                  <p class="mb-1">I.E.S.G: {{ previewEcole?.iesg || '____________________' }}</p>
                </div>
              </div>
              <div class="row mb-3">
                <div class="col-6">
                  <h6 class="fw-bold mb-1">{{ previewEcole?.nom || '____________________' }}</h6>
                  <p class="mb-1">Tél: {{ previewEcole?.telephone || '____________________' }}</p>
                  <p class="mb-1">BP: {{ previewEcole?.bp || '____________________' }}</p>
                </div>
                <div class="col-6 text-end">
                  <h6 class="fw-bold">REPUBLIQUE TOGOLAISE</h6>
                  <p class="fst-italic">{{ previewEcole?.devise || 'Travail-Liberté-Patrie' }}</p>
                </div>
              </div>
              <div class="text-center mt-3">
                <h4 class="fw-bold">EMPLOI DU TEMPS - {{ selectedClasse || 'CLASSE' }}</h4>
                <p>Année scolaire 2025-2026 | Généré le {{ today | date:'dd/MM/yyyy' }}</p>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" (click)="closePreview()">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" *ngIf="showPreviewModal"></div>

    <!-- Modal de détails -->
    <div class="modal fade" id="detailsModal" tabindex="-1" [class.show]="showDetailsModal" [style.display]="showDetailsModal ? 'block' : 'none'">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">
              <i class="bi bi-info-circle me-2"></i>Détails de l'établissement
            </h5>
            <button type="button" class="btn-close" (click)="closeDetails()"></button>
          </div>
          <div class="modal-body" *ngIf="detailEcole">
            <div class="text-center mb-3" *ngIf="detailEcole.logo">
              <img [src]="detailEcole.logo + '?t=' + getTimestamp()" alt="Logo" style="max-height: 100px;" (error)="onLogoError()">
            </div>
            <table class="table table-bordered">
              <tr>
                <th>Nom</th>
                <td>{{ detailEcole.nom }}</td>
              </tr>
              <tr>
                <th>Téléphone</th>
                <td>{{ detailEcole.telephone }}</td>
              </tr>
              <tr>
                <th>Adresse</th>
                <td>{{ detailEcole.adresse }}</td>
              </tr>
              <tr>
                <th>DRE</th>
                <td>{{ detailEcole.dre }}</td>
              </tr>
              <tr>
                <th>IESG</th>
                <td>{{ detailEcole.iesg }}</td>
              </tr>
              <tr>
                <th>Boîte Postale</th>
                <td>{{ detailEcole.bp }}</td>
              </tr>
              <tr>
                <th>Devise</th>
                <td>{{ detailEcole.devise }}</td>
              </tr>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" (click)="closeDetails()">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" *ngIf="showDetailsModal"></div>
  `,
  styles: [`
    .entete-emploi-du-temps {
      font-family: 'Times New Roman', serif;
      background: white;
      border: 1px solid #dee2e6;
    }
    .logo-mini {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      overflow: hidden;
    }
    .logo-mini:hover {
      opacity: 0.8;
      transform: scale(1.1);
      transition: all 0.2s;
    }
  `]
})
export class EcoleConfigComponent implements OnInit {
  ecoles: EcoleDTO[] = [];
  currentEcole: EcoleDTO = this.getEmptyEcole();
  selectedEcole: EcoleDTO | null = null;
  previewEcole: EcoleDTO | null = null;
  detailEcole: EcoleDTO | null = null;
  showPreviewModal = false;
  showDetailsModal = false;
  showLogoPreview = false;
  uploading = false;
  selectedFile: File | null = null;
  selectedClasse = '6ème A';
  today = new Date();

  constructor(
    private ecoleService: EcoleService,
    private notificationService: NotificationService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadEcoles();
  }

  getEmptyEcole(): EcoleDTO {
    return {
      id: '',
      nom: '',
      telephone: '',
      adresse: '',
      logo: '',
      devise: '',
      dre: '',
      iesg: '',
      bp: ''
    };
  }

  getTimestamp(): number {
    return new Date().getTime();
  }

  loadEcoles() {
    this.ecoleService.getAllEcoles().subscribe({
      next: (data) => {
        this.ecoles = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement écoles:', error);
      }
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    if (this.selectedFile) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        setTimeout(() => {
          this.currentEcole.logo = e.target.result;
          this.cdr.detectChanges();
        });
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onSubmit() {
    if (this.selectedFile) {
      this.uploading = true;
      const formData = new FormData();
      formData.append('file', this.selectedFile);

      this.http.post(`${environment.apiUrl}/upload/logo`, formData).subscribe({
        next: (response: any) => {
          // Ajouter un timestamp pour éviter le cache
          this.currentEcole.logo = response.logoUrl + '?t=' + this.getTimestamp();
          this.uploading = false;
          this.cdr.detectChanges();
          this.saveEcole();
        },
        error: (error) => {
          this.uploading = false;
          this.notificationService.showError('Erreur upload: ' + error.message);
          this.cdr.detectChanges();
        }
      });
    } else {
      this.saveEcole();
    }
  }

  saveEcole() {
    // Nettoyer le timestamp avant d'envoyer au backend
    const ecoleToSave = {
      ...this.currentEcole,
      logo: this.currentEcole.logo ? this.currentEcole.logo.split('?')[0] : this.currentEcole.logo
    };

    this.ecoleService.saveEcole(ecoleToSave).subscribe({
      next: (saved) => {
        this.notificationService.showSuccess('École enregistrée avec succès !');

        // Ajouter un timestamp pour le rechargement
        if (saved.logo) {
          saved.logo = saved.logo + '?t=' + this.getTimestamp();
        }

        this.loadEcoles();
        this.currentEcole = { ...saved };
        this.selectedFile = null;
        this.showLogoPreview = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.notificationService.showError('Erreur: ' + error.message);
        this.cdr.detectChanges();
      }
    });
  }

  editEcole(ecole: EcoleDTO) {
    this.currentEcole = { ...ecole };
    this.selectedFile = null;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.cdr.detectChanges();
  }

  viewDetails(ecole: EcoleDTO) {
    this.detailEcole = ecole;
    this.showDetailsModal = true;
    this.cdr.detectChanges();
  }

  closeDetails() {
    this.showDetailsModal = false;
    this.detailEcole = null;
    this.cdr.detectChanges();
  }

  deleteEcole(ecole: EcoleDTO) {
    if (confirm(`Supprimer l'école "${ecole.nom}" ?`)) {
      this.ecoleService.deleteEcole(ecole.id).subscribe({
        next: () => {
          this.notificationService.showSuccess('École supprimée avec succès !');
          this.loadEcoles();
          if (this.currentEcole.id === ecole.id) {
            this.currentEcole = this.getEmptyEcole();
          }
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.notificationService.showError('Erreur: ' + error.message);
          this.cdr.detectChanges();
        }
      });
    }
  }

  previewLogo(ecole: EcoleDTO) {
    this.previewEcole = ecole;
    this.showLogoPreview = true;
    this.cdr.detectChanges();
  }

  previewEntete(ecole: EcoleDTO) {
    this.previewEcole = ecole;
    this.showPreviewModal = true;
    this.cdr.detectChanges();
  }

  closePreview() {
    this.showPreviewModal = false;
    this.previewEcole = null;
    this.cdr.detectChanges();
  }

  onLogoError() {
    this.notificationService.showWarning('Erreur de chargement du logo');
    this.cdr.detectChanges();
  }
}
