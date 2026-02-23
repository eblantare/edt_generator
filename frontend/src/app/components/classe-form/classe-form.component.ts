// C:\projets\java\edt-generator\frontend\src\app\components\classe-form\classe-form.component.ts
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClasseService } from '../../services/classe.service';
import { Classe } from '../../models/classe.model';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-classe-form',
  templateUrl: './classe-form.component.html',
  styleUrls: ['./classe-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class ClasseFormComponent implements OnInit, OnDestroy {
  classeForm: FormGroup;
  isEditMode = false;
  classeId: string = '';
  isLoading = false;
  isSubmitting = false;

  private destroy$ = new Subject<void>();

  // CONSTANTES
  readonly EFFECTIF_MIN = 1;  // Uniquement le minimum, PAS de maximum
  readonly NOM_MAX_LENGTH = 50;

  niveauxOptions = ['6ème', '5ème', '4ème', '3ème', '2nde', '1ère', 'Terminale'];
  filieresOptions = ['Générale', 'Technologique', 'Professionnelle'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private classeService: ClasseService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    console.log('🔨 ClasseFormComponent - Constructeur appelé');
    
    // Validation: UNIQUEMENT required et min, PAS de max
    this.classeForm = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(this.NOM_MAX_LENGTH)]],
      niveau: ['6ème', Validators.required],
      filiere: ['Générale', Validators.required],
      effectif: [30, [
        Validators.required, 
        Validators.min(this.EFFECTIF_MIN)
        // SUPPRESSION DE LA LIMITE MAXIMUM
      ]]
    });
  }

  ngOnInit(): void {
    console.log('🟡 ClasseFormComponent - ngOnInit()');

    const idParam = this.route.snapshot.paramMap.get('id');
    console.log('🔍 ID depuis snapshot:', idParam);

    if (idParam && idParam !== 'new') {
      console.log('📝 Mode ÉDITION détecté');
      console.log('📝 ID reçu:', idParam);

      this.isEditMode = true;
      this.classeId = idParam;

      // Validation de l'ID
      if (!idParam || idParam === 'null' || idParam === 'undefined' || idParam.includes('temp_')) {
        console.error('❌ ID INVALIDE détecté! Redirection...');
        this.notificationService.error(
          'Erreur',
          'ID de classe invalide'
        );
        this.router.navigate(['/classes']);
        return;
      }

      console.log('✅ ID VALIDE, chargement de la classe...');
      this.loadClasse(idParam);
    } else {
      console.log('📝 Mode CRÉATION détecté');
      this.isEditMode = false;
      this.classeId = '';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  loadClasse(id: string): void {
    console.log('📡 ClasseFormComponent - loadClasse() appelé');
    console.log('📡 ID à charger:', id);

    this.isLoading = true;
    this.cdr.detectChanges();

    this.classeService.getClasse(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Classe) => {
          console.log('✅ ClasseFormComponent - SUCCÈS: Données reçues du service');
          console.log('📋 Données:', data);

          this.classeForm.patchValue({
            nom: data.nom,
            niveau: data.niveau,
            filiere: data.filiere,
            effectif: data.effectif
          });

          this.isLoading = false;
          this.cdr.detectChanges();
          console.log('✅ Chargement terminé avec succès!');
        },
        error: (error) => {
          console.error('❌ ClasseFormComponent - ERREUR lors du chargement:', error);
          
          let errorMessage = 'Erreur lors du chargement de la classe';
          if (error.status === 404) {
            errorMessage = 'Classe non trouvée. Elle a peut-être été supprimée.';
          } else if (error.status === 500) {
            errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
          } else if (error.status === 0) {
            errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré.';
          }

          this.notificationService.error('Erreur', errorMessage);
          this.isLoading = false;
          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/classes']);
          }, 1000);
        }
      });
  }

  onSubmit(): void {
    console.log('📤 ClasseFormComponent - onSubmit() appelé');
    console.log('📋 Formulaire valide?', this.classeForm.valid);

    if (this.classeForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.cdr.detectChanges();

      const classeData: Classe = this.classeForm.value;

      console.log('📋 Données à envoyer:', classeData);
      console.log('📋 Mode:', this.isEditMode ? 'Édition' : 'Création');

      if (this.isEditMode && this.classeId) {
        this.classeService.updateClasse(this.classeId, classeData)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response) => {
              console.log('✅ Update réussi:', response);
              this.notificationService.success('Succès', 'Classe modifiée avec succès');
              this.router.navigate(['/classes']);
            },
            error: (error) => {
              console.error('❌ Erreur update:', error);
              this.notificationService.error('Erreur', 'Erreur lors de la mise à jour de la classe');
              this.isSubmitting = false;
              this.cdr.detectChanges();
            }
          });
      } else {
        this.classeService.createClasse(classeData)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response) => {
              console.log('✅ Création réussie:', response);
              this.notificationService.success('Succès', 'Classe ajoutée avec succès');
              this.router.navigate(['/classes']);
            },
            error: (error) => {
              console.error('❌ Erreur création:', error);
              this.notificationService.error('Erreur', 'Erreur lors de la création de la classe');
              this.isSubmitting = false;
              this.cdr.detectChanges();
            }
          });
      }
    } else {
      console.log('❌ Formulaire invalide ou déjà en soumission');
      this.markFormGroupTouched(this.classeForm);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  ngOnDestroy(): void {
    console.log('♻️ ClasseFormComponent - ngOnDestroy()');
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Getters
  get nom() { return this.classeForm.get('nom'); }
  get niveau() { return this.classeForm.get('niveau'); }
  get filiere() { return this.classeForm.get('filiere'); }
  get effectif() { return this.classeForm.get('effectif'); }
}