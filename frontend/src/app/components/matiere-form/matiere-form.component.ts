import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatiereService } from '../../services/matiere.service';
import { Matiere } from '../../models/enseignant.model';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-matiere-form',
  templateUrl: './matiere-form.component.html',
  styleUrls: ['./matiere-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class MatiereFormComponent implements OnInit, OnDestroy {
  matiereForm: FormGroup;
  isEditMode = false;
  matiereId: string = '';
  isLoading = false;
  isSubmitting = false;

  private destroy$ = new Subject<void>();

  // Options pour les cycles collège/lycée
  cyclesOptions = [
    { value: 'college', label: 'Collège' },
    { value: 'lycee', label: 'Lycée Général' },
    { value: 'lycee_tech', label: 'Lycée Technique' },
    { value: 'lycee_pro', label: 'Lycée Professionnel' },
    { value: 'bt', label: 'Brevet de Technicien' }
  ];

  // Options pour les niveaux de classe (optionnel)
  niveauxClasseOptions = [
    '6ème', '5ème', '4ème', '3ème',
    '2nde', '1ère', 'Terminale',
    'CAP 1', 'CAP 2',
    'Bac Pro 1', 'Bac Pro 2', 'Bac Pro 3',
    'BT 1', 'BT 2'
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private matiereService: MatiereService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    console.log('🔨 MatiereFormComponent - Constructeur appelé');
    this.matiereForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(10)]],
      nom: ['', [Validators.required, Validators.maxLength(100)]],
      cycle: ['college', Validators.required],
      niveauClasse: ['']
    });
  }

  ngOnInit(): void {
    console.log('🟡 MatiereFormComponent - ngOnInit()');

    const idParam = this.route.snapshot.paramMap.get('id');
    console.log('🔍 ID depuis snapshot:', idParam);

    if (idParam && idParam !== 'new') {
      console.log('📝 Mode ÉDITION détecté');
      console.log('📝 ID reçu:', idParam);

      this.isEditMode = true;
      this.matiereId = idParam;

      // Validation de l'ID
      if (!idParam || idParam === 'null' || idParam === 'undefined' || idParam.includes('temp_')) {
        console.error('❌ ID INVALIDE détecté! Redirection...');
        this.notificationService.error(
          'Erreur',
          'ID de matière invalide'
        );
        this.router.navigate(['/matieres']);
        return;
      }

      console.log('✅ ID VALIDE, chargement de la matière...');
      this.loadMatiere(idParam);
    } else {
      console.log('📝 Mode CRÉATION détecté');
      this.isEditMode = false;
      this.matiereId = '';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  loadMatiere(id: string): void {
    console.log('📡 MatiereFormComponent - loadMatiere() appelé');
    console.log('📡 ID à charger:', id);

    this.isLoading = true;
    this.cdr.detectChanges();

    this.matiereService.getMatiere(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: Matiere) => {
          console.log('✅ MatiereFormComponent - SUCCÈS: Données reçues du service');
          console.log('📋 Données:', data);

          this.matiereForm.patchValue({
            code: data.code,
            nom: data.nom,
            cycle: data.cycle,
            niveauClasse: data.niveauClasse || ''
          });

          this.isLoading = false;
          this.cdr.detectChanges();
          console.log('✅ Chargement terminé avec succès!');
        },
        error: (error) => {
          console.error('❌ MatiereFormComponent - ERREUR lors du chargement:', error);
          console.error('❌ Status:', error.status);
          console.error('❌ URL:', error.url);

          let errorMessage = 'Erreur lors du chargement de la matière';
          if (error.message.includes('ID de matière invalide')) {
            errorMessage = 'ID de matière invalide. Veuillez sélectionner une matière valide.';
          } else if (error.status === 404) {
            errorMessage = 'Matière non trouvée. Elle a peut-être été supprimée.';
          } else if (error.status === 500) {
            errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
          } else if (error.status === 0) {
            errorMessage = 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré.';
          }

          this.notificationService.error(
            'Erreur',
            errorMessage
          );

          this.isLoading = false;
          this.cdr.detectChanges();

          // Rediriger après 1 seconde
          setTimeout(() => {
            console.log('🔄 Redirection vers /matieres...');
            this.router.navigate(['/matieres']);
          }, 1000);
        }
      });
  }

  onSubmit(): void {
    console.log('📤 MatiereFormComponent - onSubmit() appelé');
    console.log('📋 Formulaire valide?', this.matiereForm.valid);

    if (this.matiereForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.cdr.detectChanges();

      const matiereData: Matiere = this.matiereForm.value;
      
      // Si niveauClasse est vide, on le met à undefined
      if (!matiereData.niveauClasse) {
        matiereData.niveauClasse = undefined;
      }

      console.log('📋 Données à envoyer:', matiereData);
      console.log('📋 Mode:', this.isEditMode ? 'Édition' : 'Création');

      if (this.isEditMode && this.matiereId) {
        console.log('📤 Appel updateMatiere() avec ID:', this.matiereId);

        this.matiereService.updateMatiere(this.matiereId, matiereData)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response) => {
              console.log('✅ Update réussi:', response);
              this.notificationService.success(
                'Succès',
                'Matière modifiée avec succès'
              );
              this.router.navigate(['/matieres']);
            },
            error: (error) => {
              console.error('❌ Erreur update:', error);
              this.notificationService.error(
                'Erreur',
                'Erreur lors de la mise à jour de la matière'
              );
              this.isSubmitting = false;
              this.cdr.detectChanges();
            }
          });
      } else {
        console.log('📤 Appel createMatiere()');

        this.matiereService.createMatiere(matiereData)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (response) => {
              console.log('✅ Création réussie:', response);
              this.notificationService.success(
                'Succès',
                'Matière ajoutée avec succès'
              );
              this.router.navigate(['/matieres']);
            },
            error: (error) => {
              console.error('❌ Erreur création:', error);
              this.notificationService.error(
                'Erreur',
                'Erreur lors de la création de la matière'
              );
              this.isSubmitting = false;
              this.cdr.detectChanges();
            }
          });
      }
    } else {
      console.log('❌ Formulaire invalide ou déjà en soumission');
      this.markFormGroupTouched(this.matiereForm);
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
    console.log('♻️ MatiereFormComponent - ngOnDestroy()');
    this.destroy$.next();
    this.destroy$.complete();
  }

  get code() { return this.matiereForm.get('code'); }
  get nom() { return this.matiereForm.get('nom'); }
  get cycle() { return this.matiereForm.get('cycle'); }
  get niveauClasse() { return this.matiereForm.get('niveauClasse'); }
}