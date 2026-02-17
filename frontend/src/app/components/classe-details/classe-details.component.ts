// C:\projets\java\edt-generator\frontend\src\app\components\classe-details\classe-details.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClasseService } from '../../services/classe.service';
import { NotificationService } from '../../services/notification.service';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal.component';
import { Classe } from '../../models/classe.model';

@Component({
  selector: 'app-classe-details',
  templateUrl: './classe-details.component.html',
  styleUrls: ['./classe-details.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmationModalComponent]
})
export class ClasseDetailsComponent implements OnInit {
  classe: Classe | null = null;
  isLoading = false;
  errorMessage = '';
  today = new Date();
  showDeleteModal = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private classeService: ClasseService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log('🔍 ClasseDetailsComponent - ngOnInit() - ID:', id);
  
    // DÉTECTION DU PROBLÈME DE ROUTE
    if (id === 'new') {
      console.error('🚨 ROUTE MAL ROUTÉE ! ID = "new"');
      console.log('🔀 Redirection vers /classes/new');
      this.router.navigate(['/classes/new']);
      return;
    }
  
    if (id && this.isValidId(id)) {
      console.log('✅ ID valide, chargement...');
      this.loadClasse(id);
    } else {
      console.error('❌ ID invalide:', id);
      this.errorMessage = 'ID de classe invalide';
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

  loadClasse(id: string): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.classe = null;
    this.cdr.detectChanges();

    console.log('🟡 ClasseDetailsComponent - Chargement classe ID:', id);

    this.classeService.getClasse(id).subscribe({
      next: (data) => {
        console.log('✅ ClasseDetailsComponent - Classe chargée:', data);
        this.classe = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('❌ ClasseDetailsComponent - Erreur:', error);
        this.errorMessage = error.message || 'Erreur lors du chargement de la classe';
        this.classe = null;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  reloadClasse(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClasse(id);
    }
  }

  confirmDelete(): void {
    this.showDeleteModal = true;
  }

  onDeleteConfirmed(): void {
    if (this.classe?.id) {
      this.classeService.deleteClasse(this.classe.id).subscribe({
        next: () => {
          this.notificationService.success('Suppression réussie', 'Classe supprimée');
          this.router.navigate(['/classes']);
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          this.notificationService.error('Erreur', 'Impossible de supprimer la classe');
        }
      });
    }
    this.showDeleteModal = false;
  }

  onDeleteCancelled(): void {
    this.showDeleteModal = false;
  }

  getNiveauBadgeClass(niveau: string): string {
    const classes: { [key: string]: string } = {
      '6ème': 'bg-info',
      '5ème': 'bg-info',
      '4ème': 'bg-info',
      '3ème': 'bg-info',
      '2nde': 'bg-primary',
      '1ère': 'bg-success',
      'Terminale': 'bg-warning'
    };
    return classes[niveau] || 'bg-secondary';
  }

  getFiliereBadgeClass(filiere: string): string {
    const classes: { [key: string]: string } = {
      'Générale': 'bg-primary',
      'Technologique': 'bg-success',
      'Professionnelle': 'bg-warning'
    };
    return classes[filiere] || 'bg-secondary';
  }

  getEffectifPercentage(effectif: number): number {
    const maxEffectif = 50;
    return Math.round((effectif / maxEffectif) * 100);
  }

  hasValidId(): boolean {
    if (!this.classe) return false;
    if (!this.classe.id) return false;
    return this.isValidId(this.classe.id);
  }

  getClasseId(): string {
    return this.classe?.id || '';
  }
}