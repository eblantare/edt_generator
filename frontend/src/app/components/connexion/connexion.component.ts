// C:\projets\java\edt-generator\frontend\src\app\components\connexion\connexion.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
              <p class="text-muted">Générateur d'emplois du temps intelligent</p>
            </div>

            <!-- Carte de connexion -->
            <div class="card border-0 shadow-lg rounded-4 overflow-hidden">
              <!-- En-tête avec progression -->
              <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="badge bg-primary rounded-pill px-3 py-2">
                    Étape {{ etape }}/2
                  </span>
                  <small class="text-muted">
                    <i class="bi bi-shield-check me-1"></i>
                    Connexion sécurisée
                  </small>
                </div>
                <div class="progress" style="height: 4px;">
                  <div class="progress-bar bg-primary" 
                       [style.width]="etape === 1 ? '50%' : '100%'"
                       role="progressbar"></div>
                </div>
              </div>

              <div class="card-body p-4">
                <!-- Étape 1: Email -->
                <div *ngIf="etape === 1">
                  <div class="text-center mb-4">
                    <div class="feature-icon bg-primary bg-opacity-10 text-primary rounded-circle d-inline-flex align-items-center justify-content-center mb-3" 
                         style="width: 60px; height: 60px;">
                      <i class="bi bi-envelope fs-2"></i>
                    </div>
                    <h4 class="fw-bold">Bienvenue</h4>
                    <p class="text-muted">Connectez-vous avec votre email professionnel</p>
                  </div>

                  <div class="mb-4">
                    <label for="email" class="form-label fw-semibold">Email</label>
                    <div class="input-group input-group-lg">
                      <span class="input-group-text bg-light border-end-0">
                        <i class="bi bi-envelope text-primary"></i>
                      </span>
                      <input type="email"
                             class="form-control border-start-0 ps-0"
                             id="email"
                             [(ngModel)]="email"
                             placeholder="prenom.nom@ecole.fr"
                             [disabled]="loading"
                             autofocus>
                    </div>
                    <div class="form-text">
                      <i class="bi bi-info-circle me-1"></i>
                      Un code à 7-9 chiffres vous sera envoyé
                    </div>
                  </div>

                  <button class="btn btn-primary w-100 py-3 fw-bold rounded-3"
                          (click)="demanderCode()"
                          [disabled]="!email || loading">
                    <span *ngIf="!loading">
                      <i class="bi bi-send me-2"></i>Envoyer le code
                    </span>
                    <span *ngIf="loading">
                      <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                      Envoi en cours...
                    </span>
                  </button>
                </div>

                <!-- Étape 2: Validation du code -->
                <div *ngIf="etape === 2">
                  <div class="text-center mb-4">
                    <div class="feature-icon bg-success bg-opacity-10 text-success rounded-circle d-inline-flex align-items-center justify-content-center mb-3" 
                         style="width: 60px; height: 60px;">
                      <i class="bi bi-shield-lock fs-2"></i>
                    </div>
                    <h4 class="fw-bold">Vérification</h4>
                    <p class="text-muted">Code envoyé à</p>
                    <div class="bg-light rounded-3 p-2 mb-2">
                      <strong class="text-primary">{{ email }}</strong>
                    </div>
                    <small class="text-muted">
                      <i class="bi bi-clock me-1"></i>
                      Valable 10 minutes
                    </small>
                  </div>

                  <div class="mb-4">
                    <label for="code" class="form-label fw-semibold">Code de validation</label>
                    <div class="code-input-container text-center">
                      <input type="text"
                             class="form-control form-control-lg text-center fw-bold"
                             id="code"
                             [(ngModel)]="code"
                             placeholder="• • • • • • •"
                             maxlength="9"
                             pattern="[0-9]*"
                             inputmode="numeric"
                             [disabled]="loading"
                             autofocus
                             style="font-size: 2rem; letter-spacing: 8px; font-family: monospace;">
                    </div>
                    <div class="d-flex justify-content-between mt-2">
                      <small class="text-muted">
                        <i class="bi bi-123 me-1"></i>
                        7 à 9 chiffres
                      </small>
                      <a href="#" class="text-decoration-none small" (click)="$event.preventDefault(); renvoyerCode()">
                        <i class="bi bi-arrow-repeat me-1"></i>
                        Renvoyer
                      </a>
                    </div>
                  </div>

                  <div class="d-grid gap-3">
                    <button class="btn btn-success py-3 fw-bold rounded-3"
                            (click)="validerCode()"
                            [disabled]="!code || code.length < 7 || loading">
                      <span *ngIf="!loading">
                        <i class="bi bi-check-lg me-2"></i>Valider et continuer
                      </span>
                      <span *ngIf="loading">
                        <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                        Vérification...
                      </span>
                    </button>

                    <button class="btn btn-link text-decoration-none"
                            (click)="retourEtape1()"
                            [disabled]="loading">
                      <i class="bi bi-arrow-left me-2"></i>Modifier l'email
                    </button>
                  </div>
                </div>
              </div>

              <!-- Pied de carte avec liens -->
              <div class="card-footer bg-white border-0 pb-4 px-4 text-center">
                <small class="text-muted">
                  © 2026 EDT Generator. Tous droits réservés.
                </small>
              </div>
            </div>

            <!-- Badges de sécurité -->
            <div class="d-flex justify-content-center gap-3 mt-4">
              <span class="badge bg-light text-dark rounded-pill px-3 py-2">
                <i class="bi bi-shield-check text-success me-1"></i>
                Chiffré
              </span>
              <span class="badge bg-light text-dark rounded-pill px-3 py-2">
                <i class="bi bi-clock-history text-primary me-1"></i>
                Session 10min
              </span>
              <span class="badge bg-light text-dark rounded-pill px-3 py-2">
                <i class="bi bi-envelope-paper text-info me-1"></i>
                Code par email
              </span>
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
      box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
    }
    .feature-icon {
      transition: transform 0.3s;
    }
    .feature-icon:hover {
      transform: scale(1.1);
    }
    .card {
      backdrop-filter: blur(10px);
      background: rgba(255, 255, 255, 0.95);
    }
    .input-group-text {
      background: white;
      border-right: none;
    }
    .form-control:focus {
      box-shadow: none;
      border-color: #dee2e6;
    }
    .input-group:focus-within {
      box-shadow: 0 0 0 0.25rem rgba(102, 126, 234, 0.25);
      border-radius: 0.375rem;
    }
    .code-input-container input {
      transition: all 0.3s;
    }
    .code-input-container input:focus {
      border-color: #28a745;
      box-shadow: 0 0 0 0.25rem rgba(40, 167, 69, 0.25);
    }
    .progress {
      background-color: #e9ecef;
      overflow: hidden;
    }
    .progress-bar {
      transition: width 0.5s ease;
    }
    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      transition: all 0.3s;
    }
    .btn-primary:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
    }
    .btn-success {
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      border: none;
      transition: all 0.3s;
    }
    .btn-success:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(17, 153, 142, 0.4);
    }
  `]
})
export class ConnexionComponent implements OnInit {
  etape = 1;
  email = '';
  code = '';
  utilisateurId = '';
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.authService.estConnecte()) {
      this.router.navigate(['/generation']);
    }
  }

  demanderCode() {
    if (!this.email || !this.email.includes('@')) {
      this.notificationService.showWarning('Veuillez saisir un email valide');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.demanderConnexion(this.email).subscribe({
      next: (result) => {
        this.loading = false;
        
        if (result.utilisateurId) {
          this.utilisateurId = result.utilisateurId;
          this.etape = 2;
          this.cdr.detectChanges();
          this.notificationService.showSuccess('📧 Code envoyé à ' + this.email);
          
          if (!result.success) {
            this.notificationService.showInfo('Code déjà existant - utilisez le dernier code reçu');
          }
        } else {
          this.errorMessage = result.message || 'Email non reconnu';
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        this.loading = false;
        
        if (error.error && error.error.utilisateurId) {
          this.utilisateurId = error.error.utilisateurId;
          this.etape = 2;
          this.cdr.detectChanges();
          this.notificationService.showSuccess('✅ Code disponible - vérifiez votre email');
        } else {
          this.errorMessage = error.message || 'Erreur de connexion au serveur';
          this.cdr.detectChanges();
          this.notificationService.showError(this.errorMessage);
        }
      }
    });
  }

  validerCode() {
    if (!this.code || this.code.length < 7) {
      this.notificationService.showWarning('Le code doit contenir 7 à 9 chiffres');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.validerCode(this.utilisateurId, this.code).subscribe({
      next: (result) => {
        this.loading = false;
        if (result.success) {
          this.notificationService.showSuccess('✨ Connexion réussie !');
          this.router.navigate(['/generation']);
        } else {
          this.errorMessage = result.message || 'Code invalide ou expiré';
          this.cdr.detectChanges();
          this.notificationService.showError(this.errorMessage);
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message || 'Erreur de validation';
        
        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        }
        
        this.cdr.detectChanges();
        this.notificationService.showError(this.errorMessage);
      }
    });
  }

  renvoyerCode() {
    this.notificationService.showInfo('🔄 Nouvelle demande de code...');
    this.demanderCode();
  }

  retourEtape1() {
    this.etape = 1;
    this.code = '';
    this.errorMessage = '';
    this.utilisateurId = '';
    this.cdr.detectChanges();
    this.notificationService.showInfo('Modifiez votre email et recommencez');
  }
}