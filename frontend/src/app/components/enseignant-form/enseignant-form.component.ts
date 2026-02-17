import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EnseignantService } from '../../services/enseignant.service';
import { MatiereService } from '../../services/matiere.service';
import { Enseignant, Matiere } from '../../models/enseignant.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-enseignant-form',
  templateUrl: './enseignant-form.component.html',
  styleUrls: ['./enseignant-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule]
})
export class EnseignantFormComponent implements OnInit {
  enseignantForm: FormGroup;
  isEditMode = false;
  enseignantId?: string;
  matieres: Matiere[] = [];
  isGeneratingMatricule = false;
  isLoading = false;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private enseignantService: EnseignantService,
    private matiereService: MatiereService,
    private cdr: ChangeDetectorRef
  ) {
    this.enseignantForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      prenom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      matricule: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(8)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      telephone: ['', [Validators.pattern('^[0-9+ ]*$'), Validators.maxLength(15)]],
      heuresMaxHebdo: [16, [Validators.required, Validators.min(1), Validators.max(28)]],
      matiereDominanteId: [null],
      matiereSecondaireId: [null]
    });
  }

  ngOnInit(): void {
    this.loadMatieres();

    this.route.params.subscribe(params => {
      const idParam = params['id'];
      if (idParam) {
        this.isEditMode = true;
        this.enseignantId = idParam;
        this.loadEnseignant(idParam);
      }
    });
  }

  loadMatieres(): void {
    this.isLoading = true;
    this.matiereService.getAllMatieres().subscribe({
      next: (data: Matiere[]) => {
        this.matieres = data;
        console.log('📚 Matières chargées:', this.matieres.length);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des matières', error);
        this.isLoading = false;
        this.cdr.detectChanges();
        alert('Erreur: ' + error.message);
      }
    });
  }

  loadEnseignant(id: string): void {
    this.isLoading = true;
    this.enseignantService.getEnseignant(id).subscribe({
      next: (data: Enseignant) => {
        console.log('📦 Enseignant chargé:', data);
        
        this.enseignantForm.patchValue({
          nom: data.nom,
          prenom: data.prenom,
          matricule: data.matricule,
          email: data.email,
          telephone: data.telephone || '',
          heuresMaxHebdo: data.heuresMaxHebdo,
          matiereDominanteId: data.matiereDominante?.id || null,
          matiereSecondaireId: data.matiereSecondaire?.id || null
        });
        
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur lors du chargement', error);
        this.isLoading = false;
        this.cdr.detectChanges();
        this.router.navigate(['/enseignants']);
      }
    });
  }

  generateMatricule(): void {
    this.isGeneratingMatricule = true;
    const nom = this.nom?.value || '';
    const prenom = this.prenom?.value || '';

    if (nom && prenom) {
      const initiales = (nom.charAt(0) + prenom.charAt(0)).toUpperCase();
      const timestamp = Date.now().toString().slice(-4);
      const matricule = `${initiales}${timestamp}`.slice(0, 8);

      this.enseignantForm.patchValue({ matricule });
      this.enseignantForm.get('matricule')?.markAsDirty();
    }
    this.isGeneratingMatricule = false;
  }

  onSubmit(): void {
    if (this.enseignantForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      const formValue = this.enseignantForm.value;

      // ⭐⭐ FORMAT CORRECT pour le backend ⭐⭐
      const enseignantData: any = {
        nom: formValue.nom,
        prenom: formValue.prenom,
        matricule: formValue.matricule,
        email: formValue.email,
        telephone: formValue.telephone,
        heuresMaxHebdo: formValue.heuresMaxHebdo
      };

      // Envoyer seulement les IDs des matières
      if (formValue.matiereDominanteId) {
        enseignantData.matiereDominante = { id: formValue.matiereDominanteId };
      }
      
      if (formValue.matiereSecondaireId) {
        enseignantData.matiereSecondaire = { id: formValue.matiereSecondaireId };
      }

      console.log('📤 Données envoyées au backend:', enseignantData);

      if (this.isEditMode && this.enseignantId) {
        this.enseignantService.updateEnseignant(this.enseignantId, enseignantData).subscribe({
          next: (response) => {
            console.log('✅ Réponse backend (modification):', response);
            alert('Enseignant modifié avec succès');
            this.router.navigate(['/enseignants']);
          },
          error: (error) => {
            console.error('❌ Erreur lors de la mise à jour', error);
            console.error('Détails:', error.error);
            alert('Erreur: ' + error.message);
            this.isSubmitting = false;
            this.cdr.detectChanges();
          }
        });
      } else {
        this.enseignantService.createEnseignant(enseignantData).subscribe({
          next: (response) => {
            console.log('✅ Réponse backend (création):', response);
            alert('Enseignant ajouté avec succès');
            this.router.navigate(['/enseignants']);
          },
          error: (error) => {
            console.error('❌ Erreur lors de la création', error);
            console.error('Détails:', error.error);
            alert('Erreur: ' + error.message);
            this.isSubmitting = false;
            this.cdr.detectChanges();
          }
        });
      }
    } else {
      this.markFormGroupTouched(this.enseignantForm);
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

  get nom() { return this.enseignantForm.get('nom'); }
  get prenom() { return this.enseignantForm.get('prenom'); }
  get matricule() { return this.enseignantForm.get('matricule'); }
  get email() { return this.enseignantForm.get('email'); }
  get telephone() { return this.enseignantForm.get('telephone'); }
  get heuresMaxHebdo() { return this.enseignantForm.get('heuresMaxHebdo'); }
}