import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { InscriptionResult } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-vh-100 d-flex align-items-center bg-light">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-md-6 col-lg-5">
            <!-- Logo / Brand -->
            <div class="text-center mb-4">
              <div class="brand-icon bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                   style="width: 80px; height: 80px;">
                <i class="bi bi-calendar-week fs-1"></i>
              </div>
              <h2 class="fw-bold text-primary">EDT Generator</h2>
              <p class="text-muted">Créez votre compte</p>
            </div>

            <!-- Carte d'inscription -->
            <div class="card border-0 shadow-lg rounded-4 overflow-hidden">
              <div class="card-header bg-success text-white text-center py-3">
                <h4 class="mb-0">
                  <i class="bi bi-person-plus me-2"></i>Nouveau compte
                </h4>
              </div>

              <div class="card-body p-4">
                <form (ngSubmit)="onSubmit()" #inscriptionForm="ngForm">
                  <div class="text-center mb-4">
                    <p class="text-muted" *ngIf="email; else noEmail">
                      L'email <strong>{{ email }}</strong> n'est pas reconnu.<br>
                      Complétez votre inscription pour recevoir un code.
                    </p>
                    <ng-template #noEmail>
                      <p class="text-muted">
                        Veuillez saisir votre email pour créer un compte.
                      </p>
                    </ng-template>
                  </div>

                  <!-- Email -->
                  <div class="mb-3">
                    <label class="form-label fw-semibold">Email</label>
                    <input type="email" 
                           class="form-control" 
                           [(ngModel)]="email" 
                           name="email"
                           [readonly]="!!email" 
                           [disabled]="!!email"
                           placeholder="votre@email.com"
                           required>
                    <small class="text-muted" *ngIf="!email">
                      Saisissez votre email pour créer un compte
                    </small>
                  </div>

                  <!-- Rôle -->
                  <div class="mb-4">
                    <label class="form-label fw-semibold">Rôle <span class="text-danger">*</span></label>
                    <select class="form-select" [(ngModel)]="role" name="role" required>
                      <option value="CONSULTANT">Consultant (lecture seule)</option>
                      <option value="GESTIONNAIRE">Gestionnaire (création/modification)</option>
                      <option value="ADMIN">Administrateur (tous droits)</option>
                    </select>
                    <div class="form-text">Ce rôle peut être modifié plus tard</div>
                  </div>

                  <!-- Message d'erreur -->
                  <div *ngIf="errorMessage" class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    {{ errorMessage }}
                  </div>

                  <!-- Actions -->
                  <div class="d-grid gap-3">
                    <button type="submit" class="btn btn-success py-3 fw-bold rounded-3"
                            [disabled]="!inscriptionForm.form.valid || loading">
                      <span *ngIf="!loading">
                        <i class="bi bi-person-plus me-2"></i>Créer mon compte
                      </span>
                      <span *ngIf="loading">
                        <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                        Création en cours...
                      </span>
                    </button>

                    <button type="button" class="btn btn-link text-decoration-none" routerLink="/connexion">
                      <i class="bi bi-arrow-left me-2"></i>Retour à la connexion
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .min-vh-100 {
      min-height: 100vh;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }
    .brand-icon {
      box-shadow: 0 10px 20px rgba(40, 167, 69, 0.3);
    }
    .card {
      backdrop-filter: blur(10px);
      background: rgba(255, 255, 255, 0.95);
    }
  `]
})
export class InscriptionComponent implements OnInit {
  email = '';
  role = 'CONSULTANT';
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    // Récupérer l'email depuis la navigation
    const navigation = this.router.getCurrentNavigation();
    if (navigation && navigation.extras.state) {
      const state = navigation.extras.state as { [key: string]: any };
      this.email = state['email'] || '';
      console.log('📧 Email reçu pour inscription:', this.email);
    }

    // 🔴 SÉCURITÉ: Forcer loading à false après 5 secondes
    setTimeout(() => {
      if (this.loading) {
        console.warn('⚠️ Timeout - reset loading');
        this.loading = false;
        this.errorMessage = 'Délai d\'attente dépassé. Veuillez réessayer.';
      }
    }, 5000);
  }

  onSubmit() {
    if (!this.email) {
      this.notificationService.showWarning('Veuillez saisir un email');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.inscrire(this.email, this.role).subscribe({
      next: (result: InscriptionResult) => {
        this.loading = false;
        // On utilise userId car le DTO backend utilise userId
        if (result.success && result.userId) {
          this.notificationService.showSuccess('Compte créé avec succès !');
          this.router.navigate(['/connexion'], {
            state: {
              utilisateurId: result.userId, // Conversion pour la navigation
              email: this.email
            }
          });
        } else {
          this.errorMessage = result.message || 'Erreur lors de l\'inscription';
        }
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        console.error('❌ Erreur inscription:', error);
        
        if (error.status === 0) {
          this.errorMessage = 'Serveur inaccessible. Vérifiez que le backend est démarré.';
        } else if (error.status === 409) {
          this.errorMessage = 'Cet email est déjà utilisé.';
        } else if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Données invalides';
        } else {
          this.errorMessage = error.error?.message || error.message || 'Erreur de connexion au serveur';
        }
      }
    });
  }
}