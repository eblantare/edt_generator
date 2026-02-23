// C:\projets\java\edt-generator\frontend\src\app\components\matiere-list\matiere-list.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { MatiereService, MatiereListResponse } from '../../services/matiere.service';
import { Matiere } from '../../models/enseignant.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-matiere-list',
  templateUrl: './matiere-list.component.html',
  styleUrls: ['./matiere-list.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
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
    console.log('🟡 MatiereListComponent - Chargement matières');
    
    this.isLoading = true;
    this.validationCache.clear();

    this.matiereService.getMatieresPaginated(
      this.currentPage,
      this.pageSize,
      this.searchTerm,
      this.sortBy,
      this.sortDirection
    ).subscribe({
      next: (response: MatiereListResponse) => {
        console.log('🟢 MatiereListComponent - Données reçues:', response.content?.length || 0);
        
        this.matieres = response.content || [];
        this.totalElements = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.isLoading = false;
        
        this.matieres.forEach(matiere => {
          if (matiere.id) {
            this.validationCache.set(matiere.id, this.isIdValid(matiere.id));
          }
        });
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ MatiereListComponent - Erreur lors du chargement:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private isIdValid(id: string | undefined): boolean {
    if (!id) return false;
    return id !== 'null' && id !== 'undefined' && !id.includes('temp_');
  }

  isValidMatiere(matiere: Matiere): boolean {
    if (!matiere || !matiere.id) return false;
    if (this.validationCache.has(matiere.id)) {
      return this.validationCache.get(matiere.id)!;
    }
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

  confirmDeleteMatiere(id: string | undefined, nom?: string): void {
    console.log('🗑️ MatiereListComponent - Confirmation suppression ID:', id);
    
    if (!id) {
      alert(`❌ Impossible de supprimer: identifiant manquant.`);
      return;
    }
    
    const matiereNom = nom || 'cette matière';
    
    if (!this.isIdValid(id)) {
      alert(`❌ Impossible de supprimer: identifiant invalide.`);
      return;
    }

    if (confirm(`Êtes-vous sûr de vouloir supprimer la matière "${matiereNom}" ? Cette action est irréversible.`)) {
      this.matiereService.deleteMatiere(id).subscribe({
        next: () => {
          alert(`✅ Matière "${matiereNom}" supprimée avec succès`);
          this.loadMatieres();
        },
        error: (error) => {
          console.error('❌ API - Erreur suppression:', error);
          
          if (error.status === 400) {
            alert(`⚠️ Impossible de supprimer "${matiereNom}" : matière rattachée à des enseignants`);
          } else if (error.status === 404) {
            alert(`❌ Matière "${matiereNom}" non trouvée`);
          } else {
            alert(`❌ Erreur: ${error.message}`);
          }
          
          this.loadMatieres();
        }
      });
    }
  }

  getCycleDisplayName(cycle: string): string {
    return this.cyclesMap[cycle] || cycle;
  }

  getCycleBadgeClass(cycle: string): string {
    const cycleClasses: { [key: string]: string } = {
      'college': 'bg-primary',
      'lycee': 'bg-success',
      'lycee_pro': 'bg-warning',
      'lycee_tech': 'bg-info',
      'bt': 'bg-secondary'
    };
    return cycleClasses[cycle] || 'bg-secondary';
  }
}