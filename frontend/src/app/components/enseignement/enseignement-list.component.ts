// C:\projets\java\edt-generator\frontend\src\app\components\enseignement\enseignement-list.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { EnseignementService, EnseignementDTO } from '../../services/enseignement.service';
import { NotificationService } from '../../services/notification.service';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-enseignement-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container mt-4">
      <div class="card">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
          <h3 class="mb-0">Attribution des Enseignements</h3>
          <span class="badge bg-light text-dark">Total: {{ totalElements }} attribution(s)</span>
        </div>
        <div class="card-body">
          <!-- Barre de recherche -->
          <div class="row mb-3">
            <div class="col-md-6">
              <div class="input-group">
                <input type="text" class="form-control" placeholder="Rechercher..."
                       [(ngModel)]="searchTerm" (keyup.enter)="search()">
                <button class="btn btn-outline-secondary" (click)="search()">
                  <i class="bi bi-search"></i>
                </button>
                <button class="btn btn-outline-secondary" (click)="clearSearch()">
                  <i class="bi bi-x-circle"></i>
                </button>
              </div>
            </div>
            <div class="col-md-6 text-end">
              <button class="btn btn-success" routerLink="/enseignements/new">
                <i class="bi bi-plus-circle me-1"></i>Nouvelle Attribution
              </button>
            </div>
          </div>

          <!-- Statut de la requête -->
          <div class="alert alert-info" *ngIf="requestStatus">
            <i class="bi bi-info-circle me-2"></i>
            {{ requestStatus }}
          </div>

          <!-- Indicateur de chargement -->
          <div *ngIf="isLoading" class="text-center py-5">
            <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;"></div>
            <p class="mt-3">Chargement des attributions...</p>
            <p class="small text-muted">URL: {{ apiUrl }}</p>
          </div>

          <!-- Tableau -->
          <div class="table-responsive" *ngIf="!isLoading && enseignements.length > 0">
            <table class="table table-hover">
              <thead class="table-light">
                <tr><th>Enseignant</th><th>Matière</th><th>Classe</th><th>Heures</th><th>Actions</th></tr>
              </thead>
              <tbody>
                <tr *ngFor="let ens of enseignements">
                  <td><strong>{{ ens.enseignantNom }} {{ ens.enseignantPrenom }}</strong></td>
                  <td><span class="badge bg-info">{{ ens.matiereCode }}</span> {{ ens.matiereNom }}</td>
                  <td><span class="badge bg-secondary">{{ ens.classeNom }}</span></td>
                  <td><span class="badge bg-primary">{{ ens.heuresParSemaine }}h</span></td>
                  <td>
                    <button class="btn btn-sm btn-outline-primary me-1" [routerLink]="['/enseignements/edit', ens.id]">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" (click)="deleteEnseignement(ens)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div *ngIf="!isLoading && enseignements.length === 0" class="alert alert-warning text-center">
            Aucune attribution trouvée.
            <a routerLink="/enseignements/new" class="alert-link">Créez votre première attribution</a>
          </div>

          <!-- Pagination -->
          <div class="d-flex justify-content-between mt-3" *ngIf="totalPages > 1">
            <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
            <div>
              <button class="btn btn-sm btn-outline-secondary me-1" [disabled]="currentPage === 0" (click)="previousPage()">Précédent</button>
              <button class="btn btn-sm btn-outline-secondary" [disabled]="currentPage === totalPages - 1" (click)="nextPage()">Suivant</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class EnseignementListComponent implements OnInit {
  enseignements: EnseignementDTO[] = [];
  searchTerm = '';
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  isLoading = true;
  errorMessage = '';
  requestStatus = '';
  apiUrl = '';

  constructor(
    private enseignementService: EnseignementService,
    private notificationService: NotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    console.log('🔍 Initialisation');
    // Afficher l'URL de l'API
    this.apiUrl = this.enseignementService['apiUrl'] || 'http://localhost:8080/api';
    this.loadEnseignements();
  }

  loadEnseignements() {
    console.log('🔄 Chargement...');
    this.isLoading = true;
    this.requestStatus = 'Envoi de la requête à ' + this.apiUrl + '/enseignements...';
    this.cdr.detectChanges();

    this.enseignementService.getAllEnseignements(
      this.currentPage,
      this.pageSize,
      this.searchTerm,
      'id',
      'asc'
    ).pipe(
      timeout(10000), // Timeout après 10 secondes
      catchError(error => {
        console.error('❌ Erreur catchée:', error);
        this.requestStatus = `Erreur: ${error.message || error.status || 'Inconnue'}`;
        this.isLoading = false;
        this.cdr.detectChanges();
        return of({ content: [], totalPages: 0, totalElements: 0 });
      })
    ).subscribe({
      next: (response: any) => {
        console.log('✅ Réponse reçue:', response);
        this.requestStatus = `Réponse reçue: ${response?.content?.length || 0} éléments`;

        if (response && response.content) {
          this.enseignements = response.content;
          this.totalPages = response.totalPages || 0;
          this.totalElements = response.totalElements || 0;
        } else if (Array.isArray(response)) {
          this.enseignements = response;
          this.totalPages = 1;
          this.totalElements = response.length;
        } else {
          this.enseignements = [];
          this.totalPages = 0;
          this.totalElements = 0;
        }

        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('✅ Final - enseignements:', this.enseignements.length);
      },
      error: (error) => {
        console.error('❌ Erreur subscribe:', error);
        this.requestStatus = `Erreur subscribe: ${error.message}`;
        this.isLoading = false;
        this.errorMessage = error.message;
        this.cdr.detectChanges();
      }
    });
  }

  search() {
    this.currentPage = 0;
    this.loadEnseignements();
  }

  clearSearch() {
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadEnseignements();
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadEnseignements();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadEnseignements();
    }
  }

  deleteEnseignement(enseignement: EnseignementDTO) {
    if (confirm(`Supprimer l'attribution ?`)) {
      this.enseignementService.deleteEnseignement(enseignement.id!).subscribe({
        next: () => {
          this.notificationService.showSuccess('Attribution supprimée');
          this.loadEnseignements();
        },
        error: (err) => console.error(err)
      });
    }
  }
}
