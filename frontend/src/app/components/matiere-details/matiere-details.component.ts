import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatiereService } from '../../services/matiere.service';
import { NotificationService } from '../../services/notification.service';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { Matiere } from '../../models/enseignant.model';

@Component({
  selector: 'app-matiere-details',
  templateUrl: './matiere-details.component.html',
  styleUrls: ['./matiere-details.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationModalComponent]
})
export class MatiereDetailsComponent implements OnInit {
  matiere: Matiere | null = null;
  isLoading = false;
  errorMessage = '';
  today = new Date();
  showDeleteModal = false;

  // Mapping des cycles pour l'affichage
  private cyclesMap: { [key: string]: string } = {
    'college': 'Collège',
    'lycee': 'Lycée Général',
    'lycee_tech': 'Lycée Technique',
    'lycee_pro': 'Lycée Professionnel',
    'bt': 'Brevet de Technicien'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private matiereService: MatiereService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log('🔍 MatiereDetailsComponent - ngOnInit() - ID:', id);
  
    // DÉTECTION DU PROBLÈME DE ROUTE
    if (id === 'new') {
      console.error('🚨 ROUTE MAL ROUTÉE ! ID = "new"');
      console.log('🔀 Redirection vers /matieres/new');
      this.router.navigate(['/matieres/new']);
      return;
    }
  
    if (id && this.isValidId(id)) {
      console.log('✅ ID valide, chargement...');
      this.loadMatiere(id);
    } else {
      console.error('❌ ID invalide:', id);
      this.errorMessage = 'ID de matière invalide';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private isValidId(id: string): boolean {
    if (!id) return false;
    if (id === 'null') return false;
    if (id === 'undefined') return false;
    if (id.includes('temp_')) return false;
    
    console.log(`🔍 isValidId("${id}") = true`);
    return true;
  }

  loadMatiere(id: string): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.matiere = null;
    this.cdr.detectChanges();

    console.log('🟡 MatiereDetailsComponent - Chargement matière ID:', id);

    this.matiereService.getMatiere(id).subscribe({
      next: (data) => {
        console.log('✅ MatiereDetailsComponent - Matière chargée:', data);
        console.log('📋 Données reçues - Code:', data.code, 'Nom:', data.nom, 'Cycle:', data.cycle);
        this.matiere = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ MatiereDetailsComponent - Erreur:', error);
        this.errorMessage = error.message || 'Erreur lors du chargement de la matière';
        this.matiere = null;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  reloadMatiere(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadMatiere(id);
    }
  }

  confirmDelete(): void {
    this.showDeleteModal = true;
  }

  onDeleteConfirmed(): void {
    if (this.matiere?.id) {
      this.matiereService.deleteMatiere(this.matiere.id).subscribe({
        next: () => {
          this.notificationService.success('Suppression réussie', 'Matière supprimée');
          this.router.navigate(['/matieres']);
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          this.notificationService.error('Erreur', 'Impossible de supprimer la matière');
        }
      });
    }
    this.showDeleteModal = false;
  }

  onDeleteCancelled(): void {
    this.showDeleteModal = false;
  }

  // MÉTHODE MODIFIÉE: Classes de badges pour les cycles
  getCycleBadgeClass(cycle: string): string {
    const classes: { [key: string]: string } = {
      'college': 'bg-primary',
      'lycee': 'bg-success',
      'lycee_pro': 'bg-warning',
      'lycee_tech': 'bg-info',
      'bt': 'bg-purple'
    };
    return classes[cycle] || 'bg-secondary';
  }

  // NOUVELLE MÉTHODE: Obtenir le nom d'affichage du cycle
  getCycleDisplayName(cycle: string): string {
    return this.cyclesMap[cycle] || cycle;
  }

  hasValidId(): boolean {
    if (!this.matiere) return false;
    if (!this.matiere.id) return false;
    return this.isValidId(this.matiere.id);
  }

  getMatiereId(): string {
    return this.matiere?.id || '';
  }

  // Méthode pour afficher le niveau de classe si présent
  hasNiveauClasse(): boolean {
    return !!this.matiere?.niveauClasse;
  }

  getNiveauClasseDisplay(): string {
    return this.matiere?.niveauClasse || 'Non spécifié';
  }
}