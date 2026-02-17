// C:\projets\java\edt-generator\frontend\src\app\components\enseignement\enseignement-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { EnseignementService, EnseignementDTO } from '../../services/enseignement.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-enseignement-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  providers: [EnseignementService, NotificationService],
  template: `
    <div class="container mt-4">
      <div class="card">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
          <h3 class="mb-0">
            <i class="bi bi-link-45deg me-2"></i>Attribution des Enseignements
          </h3>
          <span class="badge bg-light text-dark">
            Total: {{ totalElements }} attribution(s)
          </span>
        </div>

        <div class="card-body">
          <!-- Barre de recherche et bouton d'ajout -->
          <div class="row mb-3">
            <div class="col-md-6">
              <div class="input-group">
                <input type="text" class="form-control" placeholder="Rechercher enseignant, matière ou classe..."
                       [(ngModel)]="searchTerm" (keyup.enter)="search()">
                <button class="btn btn-outline-secondary" type="button" (click)="search()">
                  <i class="bi bi-search"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" (click)="clearSearch()">
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

          <!-- Tableau des enseignements -->
          <div class="table-responsive" *ngIf="enseignements.length > 0; else noData">
            <table class="table table-hover">
              <thead class="table-light">
                <tr>
                  <th>Enseignant</th>
                  <th>Matière</th>
                  <th>Classe</th>
                  <th>Heures/Semaine</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let enseignement of enseignements">
                  <td>
                    <strong>{{ enseignement.enseignantNom }} {{ enseignement.enseignantPrenom }}</strong>
                  </td>
                  <td>
                    <span class="badge bg-info me-1">{{ enseignement.matiereCode }}</span>
                    {{ enseignement.matiereNom }}
                  </td>
                  <td>
                    <span class="badge bg-secondary">{{ enseignement.classeNom }}</span>
                  </td>
                  <td>
                    <span class="badge bg-primary">{{ enseignement.heuresParSemaine }}h</span>
                  </td>
                  <td>
                    <div class="btn-group btn-group-sm">
                      <button class="btn btn-outline-primary"
                              [routerLink]="['/enseignements/edit', enseignement.id]"
                              title="Modifier">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-outline-danger"
                              (click)="deleteEnseignement(enseignement)"
                              title="Supprimer">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <ng-template #noData>
            <div class="alert alert-info text-center">
              <i class="bi bi-info-circle me-2"></i>
              Aucune attribution trouvée.
              <a routerLink="/enseignements/new" class="alert-link">Créez votre première attribution</a>
            </div>
          </ng-template>

          <!-- Pagination -->
          <div class="d-flex justify-content-between align-items-center mt-3" *ngIf="totalPages > 1">
            <div>
              <span class="text-muted">
                Page {{ currentPage + 1 }} sur {{ totalPages }}
                ({{ totalElements }} enseignements)
              </span>
            </div>
            <nav>
              <ul class="pagination mb-0">
                <li class="page-item" [class.disabled]="currentPage === 0">
                  <button class="page-link" (click)="previousPage()">
                    <i class="bi bi-chevron-left"></i> Précédent
                  </button>
                </li>
                <li class="page-item" [class.disabled]="currentPage === totalPages - 1">
                  <button class="page-link" (click)="nextPage()">
                    Suivant <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
              </ul>
            </nav>
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
  isLoading = false;

  constructor(
    private enseignementService: EnseignementService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadEnseignements();
  }

  loadEnseignements() {
    this.isLoading = true;
    this.enseignementService.getAllEnseignements(
      this.currentPage,
      this.pageSize,
      this.searchTerm,
      'id',
      'asc'
    ).subscribe({
      next: (page) => {
        this.enseignements = page.content;
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des enseignements:', error);
        this.notificationService.showError('Erreur lors du chargement des enseignements');
        this.isLoading = false;
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

  editEnseignement(enseignement: EnseignementDTO) {
    this.router.navigate(['/enseignements/edit', enseignement.id]);
  }

  deleteEnseignement(enseignement: EnseignementDTO) {
    if (confirm(`Supprimer l'attribution pour ${enseignement.enseignantNom} ?`)) {
      this.enseignementService.deleteEnseignement(enseignement.id!).subscribe({
        next: () => {
          this.notificationService.showSuccess('Attribution supprimée avec succès');
          this.loadEnseignements();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          this.notificationService.showError('Erreur lors de la suppression');
        }
      });
    }
  }
}
