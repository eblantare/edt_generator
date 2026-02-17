import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MatiereService, MatiereListResponse } from '../../services/matiere.service';
import { Matiere } from '../../models/enseignant.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-matiere-list',
  templateUrl: './matiere-list.component.html',
  styleUrls: ['./matiere-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ConfirmationModalComponent]
})
export class MatiereListComponent implements OnInit {
  matieres: Matiere[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];

  searchTerm = '';
  sortBy = 'code';
  sortDirection = 'asc';
  isLoading = false;

  // Cache pour éviter les calculs répétitifs
  private validationCache = new Map<string, boolean>();

  // Modal properties
  showDeleteModal = false;
  matiereToDelete: string | null = null;
  deleteModalTitle = 'Confirmation de suppression';
  deleteModalMessage = 'Êtes-vous sûr de vouloir supprimer cette matière ? Cette action est irréversible.';

  // Mapping des cycles pour l'affichage
  private cyclesMap: { [key: string]: string } = {
    'college': 'Collège',
    'lycee': 'Lycée Général',
    'lycee_tech': 'Lycée Technique',
    'lycee_pro': 'Lycée Professionnel',
    'bt': 'Brevet de Technicien'
  };

  constructor(
    private matiereService: MatiereService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    console.log('🔵 MatiereListComponent - Initialisation');
    this.loadMatieres();
  }

  loadMatieres(): void {
    console.log('🟡 MatiereListComponent - Chargement matières:', {
      page: this.currentPage,
      size: this.pageSize,
      search: this.searchTerm,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    });

    this.isLoading = true;
    this.matieres = [];
    this.validationCache.clear(); // Vider le cache à chaque nouveau chargement

    this.matiereService.getMatieresPaginated(
      this.currentPage,
      this.pageSize,
      this.searchTerm,
      this.sortBy,
      this.sortDirection
    ).subscribe({
      next: (response: MatiereListResponse) => {
        console.log('🟢 MatiereListComponent - Données reçues:', {
          count: response.content?.length || 0,
          total: response.totalElements,
          pages: response.totalPages,
          page: response.number,
          size: response.size
        });

        // CORRECTION OPTIMISÉE: Calculer une fois la validité et éviter les IDs temporaires
        this.matieres = (response.content || []).map(matiere => {
          // Si la matière n'a pas d'ID valide
          if (!this.isIdValid(matiere.id)) {
            console.warn(`⚠️ Matière sans ID valide: ${matiere.code}`);

            // CORRECTION: Ne PAS créer d'ID temporaire pour éviter la confusion
            // La matière reste dans la liste mais les boutons seront désactivés
            return matiere;
          }
          return matiere;
        });

        // Vérifier les IDs dans la console (une seule fois)
        console.log('📋 Liste des matières chargées:');
        this.matieres.forEach((matiere, index) => {
          console.log(`📊 Matière ${index + 1}:`, {
            id: matiere.id,
            code: matiere.code,
            nom: matiere.nom,
            cycle: matiere.cycle,
            niveauClasse: matiere.niveauClasse,
            isValid: this.isIdValid(matiere.id)
          });

          // Pré-calculer et mettre en cache la validité
          if (matiere.id) {
            this.validationCache.set(matiere.id, this.isIdValid(matiere.id));
          }
        });

        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.isLoading = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ MatiereListComponent - Erreur lors du chargement:', error);

        this.notificationService.error(
          'Erreur',
          `Impossible de charger la liste des matières: ${error.message}`
        );

        this.matieres = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.isLoading = false;

        this.cdr.detectChanges();
      }
    });
  }

  // CORRECTION: Méthode privée pour valider un ID
  private isIdValid(id: string | undefined): boolean {
    if (!id) return false;
    return id !== 'null' && id !== 'undefined' && !id.includes('temp_');
  }

  // CORRECTION OPTIMISÉE: Méthode pour vérifier si une matière est valide AVEC CACHE
  isValidMatiere(matiere: Matiere): boolean {
    if (!matiere) return false;

    // Si pas d'ID, invalide
    if (!matiere.id) return false;

    // Vérifier le cache d'abord
    if (this.validationCache.has(matiere.id)) {
      return this.validationCache.get(matiere.id)!;
    }

    // Calculer et mettre en cache
    const isValid = this.isIdValid(matiere.id);
    this.validationCache.set(matiere.id, isValid);

    return isValid;
  }

  onSearch(): void {
    console.log('🔍 MatiereListComponent - Recherche:', this.searchTerm);
    this.currentPage = 0;
    this.loadMatieres();
  }

  clearSearch(): void {
    console.log('🗑️ MatiereListComponent - Nettoyage recherche');
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadMatieres();
  }

  onPageChange(page: number): void {
    console.log('📄 MatiereListComponent - Changement page:', page);
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadMatieres();
    }
  }

  goToPage(pageNumber: string): void {
    const page = parseInt(pageNumber, 10) - 1;
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.onPageChange(page);
    }
  }

  onPageSizeChange(): void {
    console.log('📏 MatiereListComponent - Changement taille page:', this.pageSize);
    this.currentPage = 0;
    this.loadMatieres();
  }

  sort(column: string): void {
    console.log('🔀 MatiereListComponent - Tri par:', column);

    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }

    this.currentPage = 0;
    this.loadMatieres();
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
    if (typeof page === 'string') return page;
    return (page + 1).toString();
  }

  confirmDeleteMatiere(id: string): void {
    console.log('🗑️ MatiereListComponent - Confirmation suppression ID:', id);

    // CORRECTION: Vérifier si c'est un ID valide
    if (!this.isIdValid(id)) {
      this.notificationService.error(
        'Erreur',
        'Impossible de supprimer: ID invalide.'
      );
      return;
    }

    this.matiereToDelete = id;
    this.showDeleteModal = true;
  }

  onDeleteConfirmed(): void {
    if (this.matiereToDelete) {
      console.log('✅ MatiereListComponent - Suppression confirmée ID:', this.matiereToDelete);

      this.matiereService.deleteMatiere(this.matiereToDelete).subscribe({
        next: () => {
          this.notificationService.success(
            'Succès',
            'Matière supprimée avec succès'
          );
          this.loadMatieres();
        },
        error: (error) => {
          console.error('❌ MatiereListComponent - Erreur suppression:', error);
          this.notificationService.error(
            'Erreur',
            `Erreur lors de la suppression de la matière: ${error.message}`
          );
        }
      });
    }
    this.showDeleteModal = false;
    this.matiereToDelete = null;
  }

  onDeleteCancelled(): void {
    console.log('❌ MatiereListComponent - Suppression annulée');
    this.showDeleteModal = false;
    this.matiereToDelete = null;
  }

  // NOUVELLE MÉTHODE: Obtenir le nom d'affichage du cycle
  getCycleDisplayName(cycle: string): string {
    return this.cyclesMap[cycle] || cycle;
  }

  // MÉTHODE MODIFIÉE: Classes de badges pour les cycles
  getCycleBadgeClass(cycle: string): string {
    const cycleClasses: { [key: string]: string } = {
      'college': 'bg-primary',
      'lycee': 'bg-success',
      'lycee_pro': 'bg-warning',
      'lycee_tech': 'bg-info',
      'bt': 'bg-purple'
    };
    return cycleClasses[cycle] || 'bg-secondary';
  }
}