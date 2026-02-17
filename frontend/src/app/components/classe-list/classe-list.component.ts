import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ClasseService } from '../../services/classe.service';
import { Classe, ClasseListResponse } from '../../models/classe.model'; // IMPORT MODIFIÉ
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-classe-list',
  templateUrl: './classe-list.component.html',
  styleUrls: ['./classe-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmationModalComponent]
})
export class ClasseListComponent implements OnInit {
  classes: Classe[] = [];
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
  classeToDelete: string | null = null;
  deleteModalTitle = 'Confirmation de suppression';
  deleteModalMessage = 'Êtes-vous sûr de vouloir supprimer cette classe ? Cette action est irréversible.';

  constructor(
    private classeService: ClasseService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('🔵 ClasseListComponent - Initialisation');
    this.loadClasses();
  }

  loadClasses(): void {
    console.log('🟡 ClasseListComponent - Chargement classes:', {
      page: this.currentPage,
      size: this.pageSize,
      search: this.searchTerm,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    });

    this.isLoading = true;
    this.classes = [];

    this.classeService.getClassesPaginated(
      this.currentPage,
      this.pageSize,
      this.searchTerm,
      this.sortBy,
      this.sortDirection
    ).subscribe({
      next: (response: ClasseListResponse) => {
        console.log('🟢 ClasseListComponent - Données reçues:', {
          count: response.content?.length || 0,
          total: response.totalElements,
          pages: response.totalPages,
          page: response.number,
          size: response.size
        });

        this.classes = response.content || [];

        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.isLoading = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ ClasseListComponent - Erreur lors du chargement:', error);

        this.notificationService.error(
          'Erreur',
          `Impossible de charger la liste des classes: ${error.message}`
        );

        this.classes = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.isLoading = false;

        this.cdr.detectChanges();
      }
    });
  }

  onSearch(): void {
    console.log('🔍 ClasseListComponent - Recherche:', this.searchTerm);
    this.currentPage = 0;
    this.loadClasses();
  }

  clearSearch(): void {
    console.log('🗑️ ClasseListComponent - Nettoyage recherche');
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadClasses();
  }

  onPageChange(page: number): void {
    console.log('📄 ClasseListComponent - Changement page:', page);
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadClasses();
    }
  }

  goToPage(pageNumber: string): void {
    const page = parseInt(pageNumber, 10) - 1;
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.onPageChange(page);
    }
  }

  onPageSizeChange(): void {
    console.log('📏 ClasseListComponent - Changement taille page:', this.pageSize);
    this.currentPage = 0;
    this.loadClasses();
  }

  sort(column: string): void {
    console.log('🔀 ClasseListComponent - Tri par:', column);

    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }

    this.currentPage = 0;
    this.loadClasses();
  }

  getSortIcon(column: string): string {
    if (this.sortBy !== column) return 'bi-arrow-down-up';
    return this.sortDirection === 'asc' ? 'bi-arrow-up' : 'bi-arrow-down';
  }

  confirmDeleteClasse(id: string): void {
    console.log('🗑️ ClasseListComponent - Confirmation suppression ID:', id);

    this.classeToDelete = id;
    this.showDeleteModal = true;
  }

  onDeleteConfirmed(): void {
    if (this.classeToDelete) {
      console.log('✅ ClasseListComponent - Suppression confirmée ID:', this.classeToDelete);

      this.classeService.deleteClasse(this.classeToDelete).subscribe({
        next: () => {
          this.notificationService.success(
            'Succès',
            'Classe supprimée avec succès'
          );
          this.loadClasses();
        },
        error: (error) => {
          console.error('❌ ClasseListComponent - Erreur suppression:', error);
          this.notificationService.error(
            'Erreur',
            `Erreur lors de la suppression de la classe: ${error.message}`
          );
        }
      });
    }
    this.showDeleteModal = false;
    this.classeToDelete = null;
  }

  onDeleteCancelled(): void {
    console.log('❌ ClasseListComponent - Suppression annulée');
    this.showDeleteModal = false;
    this.classeToDelete = null;
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
    if (typeof page === 'string') return page;
    return (page + 1).toString();
  }

  getFiliereBadgeClass(filiere: string): string {
    const filiereClasses: { [key: string]: string } = {
      'Générale': 'bg-primary',
      'Technologique': 'bg-success',
      'Professionnelle': 'bg-warning'
    };
    return filiereClasses[filiere] || 'bg-secondary';
  }

  getNiveauBadgeClass(niveau: string): string {
    const niveauClasses: { [key: string]: string } = {
      '6ème': 'bg-info',
      '5ème': 'bg-info',
      '4ème': 'bg-info',
      '3ème': 'bg-info',
      '2nde': 'bg-primary',
      '1ère': 'bg-success',
      'Terminale': 'bg-warning'
    };
    return niveauClasses[niveau] || 'bg-secondary';
  }
}