// C:\projets\java\edt-generator\frontend\src\app\components\enseignant-list\enseignant-list.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { EnseignantService } from '../../services/enseignant.service';
import { Enseignant, EnseignantListResponse } from '../../models/enseignant.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-enseignant-list',
  templateUrl: './enseignant-list.component.html',
  styleUrls: ['./enseignant-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmationModalComponent]
})
export class EnseignantListComponent implements OnInit {
  enseignants: Enseignant[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];

  searchTerm = '';
  sortBy = 'nom';
  sortDirection = 'asc';
  isLoading = false;

  // Modal properties
  showDeleteModal = false;
  enseignantToDelete: string | null = null;
  deleteModalTitle = 'Confirmation de suppression';
  deleteModalMessage = 'Êtes-vous sûr de vouloir supprimer cet enseignant ? Cette action est irréversible.';

  constructor(
    private enseignantService: EnseignantService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('🔵 EnseignantListComponent - Initialisation');
    this.loadEnseignants();
  }

  // Dans enseignant-list.component.ts, ajoutez ceci à loadEnseignants :
loadEnseignants(): void {
  console.log('🟡 Début chargement');
  
  this.isLoading = true;
  this.enseignants = [];

  this.enseignantService.getAllEnseignants(
    this.currentPage,
    this.pageSize,
    this.searchTerm,
    this.sortBy,
    this.sortDirection
  ).subscribe({
    next: (response: EnseignantListResponse) => {
      console.log('🟢 Données reçues avec succès');
      console.log('📋 Structure complète de la réponse:', response);
      
      this.enseignants = response.content || [];
      this.totalElements = response.totalElements || 0;
      this.totalPages = response.totalPages || 0;

      // Vérifier les matières du premier enseignant
      if (this.enseignants.length > 0) {
        const first = this.enseignants[0];
        console.log('🔍 Premier enseignant:', first);
        console.log('📚 Ses matières:', {
          dominante: first.matiereDominante,
          secondaire: first.matiereSecondaire
        });
      }

      this.isLoading = false;
      this.cdr.detectChanges();
    },
    error: (error) => {
      console.error('❌ Erreur:', error);
      this.handleError(error);
    }
  });
}

  private handleError(error: any): void {
    this.enseignants = [];
    this.totalElements = 0;
    this.totalPages = 0;
    this.isLoading = false;
    
    this.cdr.detectChanges();

    this.notificationService.error(
      'Erreur de chargement',
      'Impossible de charger la liste des enseignants: ' + error.message
    );
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadEnseignants();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadEnseignants();
  }

  onPageChange(page: any): void {
    const pageNumber = Number(page);
    if (!isNaN(pageNumber) &&
        pageNumber >= 0 &&
        pageNumber < this.totalPages &&
        pageNumber !== this.currentPage) {
      this.currentPage = pageNumber;
      this.loadEnseignants();
    }
  }

  goToPage(pageNumber: string): void {
    const page = parseInt(pageNumber, 10) - 1;
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.onPageChange(page);
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadEnseignants();
  }

  sort(column: string): void {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.loadEnseignants();
  }

  getSortIcon(column: string): string {
    if (this.sortBy !== column) return 'bi-arrow-down-up';
    return this.sortDirection === 'asc' ? 'bi-arrow-up' : 'bi-arrow-down';
  }

  getPaginationPages(): (number | string)[] {
    const pages: (number | string)[] = [];
    const maxVisiblePages = 5;

    if (this.totalPages <= maxVisiblePages) {
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      const startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
      const endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);

      if (startPage > 0) {
        pages.push(0);
        if (startPage > 1) {
          pages.push('...');
        }
      }

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      if (endPage < this.totalPages - 1) {
        if (endPage < this.totalPages - 2) {
          pages.push('...');
        }
        pages.push(this.totalPages - 1);
      }
    }

    return pages;
  }

  getDisplayStart(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getDisplayEnd(): number {
    const end = (this.currentPage + 1) * this.pageSize;
    return Math.min(end, this.totalElements);
  }

  getPageDisplayNumber(page: string | number): string {
    if (typeof page === 'string') {
      return page;
    }
    return (page + 1).toString();
  }

  confirmDeleteEnseignant(id: string): void {
    this.enseignantToDelete = id;
    this.showDeleteModal = true;
  }

  onDeleteConfirmed(): void {
    if (this.enseignantToDelete) {
      this.enseignantService.deleteEnseignant(this.enseignantToDelete).subscribe({
        next: () => {
          this.notificationService.success(
            'Suppression réussie',
            'L\'enseignant a été supprimé avec succès'
          );
          this.loadEnseignants();
        },
        error: (error) => {
          console.error('❌ Erreur lors de la suppression', error);
          this.notificationService.error(
            'Erreur de suppression',
            'Impossible de supprimer l\'enseignant: ' + error.message
          );
        }
      });
    }
    this.showDeleteModal = false;
    this.enseignantToDelete = null;
  }

  onDeleteCancelled(): void {
    this.showDeleteModal = false;
    this.enseignantToDelete = null;
  }

  getProgressBarClass(heures: number): string {
    if (heures <= 14) return 'bg-success';
    if (heures <= 21) return 'bg-warning';
    return 'bg-danger';
  }

  getProgressBarWidth(heures: number): number {
    const maxHours = 28;
    return Math.min((heures / maxHours) * 100, 100);
  }

  getCycleDisplayName(cycleCode: string): string {
    return this.enseignantService.getCycleDisplayName(cycleCode);
  }

  getCycleBadgeClass(cycleCode: string): string {
    return this.enseignantService.getCycleBadgeClass(cycleCode);
  }
}