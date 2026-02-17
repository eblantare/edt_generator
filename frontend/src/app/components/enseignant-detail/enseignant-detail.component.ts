// C:\projets\java\edt-generator\frontend\src\app\components\enseignant-detail\enseignant-detail.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EnseignantService } from '../../services/enseignant.service';
import { Enseignant, Matiere } from '../../models/enseignant.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-enseignant-detail',
  templateUrl: './enseignant-detail.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class EnseignantDetailComponent implements OnInit {
  enseignant: Enseignant | null = null;
  isLoading = false;
  enseignements: any[] = [];
  
  // Exposer l'ID pour le template
  enseignantId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enseignantService: EnseignantService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('🔵 EnseignantDetailComponent - Initialisation');
    this.enseignantId = this.route.snapshot.paramMap.get('id');
    
    // TEST : Appeler la méthode de test pour voir la réponse du backend
    setTimeout(() => {
      this.enseignantService.testBackendResponse();
    }, 1000);
    
    this.loadEnseignant();
  }

  loadEnseignant(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log('🟡 Chargement enseignant ID:', id);
    
    if (id) {
      this.isLoading = true;
      this.cdr.detectChanges(); // Force l'affichage du spinner
      
      this.enseignantService.getEnseignant(id).subscribe({
        next: (data: Enseignant) => {
          console.log('✅ Données BRUTES du backend (détail):', data);
          console.log('🔍 Analyse des matières:');
          console.log('   Matière dominante:', data.matiereDominante);
          console.log('   Matière dominante type:', typeof data.matiereDominante);
          console.log('   Matière secondaire:', data.matiereSecondaire);
          console.log('   Matière secondaire type:', typeof data.matiereSecondaire);
          
          // Ne pas normaliser - laisser le service gérer le mapping
          this.enseignant = data;
          
          console.log('🔄 Données assignées au composant:', this.enseignant);
          
          this.isLoading = false;
          this.cdr.detectChanges(); // Force la mise à jour
          
          this.loadEnseignementsAssocies();
        },
        error: (error) => {
          console.error('❌ Erreur API:', error);
          this.isLoading = false;
          this.notificationService.error(
            'Erreur',
            'Impossible de charger les détails: ' + error.message
          );
          this.cdr.detectChanges();
        }
      });
    }
  }

  // MÉTHODE SIMPLIFIÉE : Ne pas normaliser, laisser le service faire son travail
  private normalizeEnseignantData(data: Enseignant): Enseignant {
    console.log('🔄 Normalisation des données:', data);
    
    // Si les matières sont déjà des objets complets, ne pas les modifier
    return {
      ...data,
      // Assurez-vous que les propriétés existent
      telephone: data.telephone || '',
      heuresMaxHebdo: data.heuresMaxHebdo || 0,
      matiereDominante: data.matiereDominante || undefined,
      matiereSecondaire: data.matiereSecondaire || undefined
    };
  }

  loadEnseignementsAssocies(): void {
    if (this.enseignant?.id) {
      this.enseignements = [
        { id: '1', matiereId: this.enseignant.matiereDominante?.id || '', classeId: 'classe1', heuresParSemaine: 4 },
        { id: '2', matiereId: this.enseignant.matiereSecondaire?.id || '', classeId: 'classe2', heuresParSemaine: 3 }
      ];
    }
  }

  getProgressBarClass(heures: number): string {
    if (heures <= 14) return 'bg-success';
    if (heures <= 21) return 'bg-warning';
    return 'bg-danger';
  }

  getProgressBarWidth(heures: number): number {
    return Math.min((heures / 28) * 100, 100);
  }

  getMatiereName(matiereId: string): string {
    if (!this.enseignant || !matiereId) return 'Non spécifié';
    if (this.enseignant.matiereDominante?.id === matiereId) return this.enseignant.matiereDominante?.code || 'N/A';
    if (this.enseignant.matiereSecondaire?.id === matiereId) return this.enseignant.matiereSecondaire?.code || 'N/A';
    return 'Autre';
  }

  getClasseName(classeId: string): string {
    const classes: {[key: string]: string} = {
      'classe1': '6ème A', 'classe2': '5ème B', 'classe3': '4ème C'
    };
    return classes[classeId] || classeId;
  }

  getMatiereCycle(matiereId: string): string {
    if (!this.enseignant || !matiereId) return '';
    if (this.enseignant.matiereDominante?.id === matiereId) 
      return this.enseignantService.getCycleDisplayName(this.enseignant.matiereDominante?.cycle || '');
    if (this.enseignant.matiereSecondaire?.id === matiereId) 
      return this.enseignantService.getCycleDisplayName(this.enseignant.matiereSecondaire?.cycle || '');
    return '';
  }

  getCycleDisplayName(cycleCode: string): string {
    return this.enseignantService.getCycleDisplayName(cycleCode);
  }

  getCycleBadgeClass(cycleCode: string): string {
    return this.enseignantService.getCycleBadgeClass(cycleCode);
  }

  // Méthode pour obtenir le type d'une variable (pour le débogage)
  getTypeOf(value: any): string {
    if (value === null) return 'null';
    if (value === undefined) return 'undefined';
    return typeof value;
  }
}