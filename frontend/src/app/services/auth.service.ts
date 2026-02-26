// C:\projets\java\edt-generator\frontend\src\app\services\auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

// Interface pour l'inscription
export interface InscriptionResult {
  success: boolean;
  message: string;
  userId?: string;
}

export interface DemandeConnexion {
  email: string;
}

export interface DemandeConnexionResult {
  success: boolean;
  message: string;
  utilisateurId?: string;
}

export interface ValidationCode {
  utilisateurId: string;
  code: string;
}

export interface ValidationCodeResult {
  success: boolean;
  message: string;
  token?: string;
  utilisateurId?: string;
  email?: string;
  role?: string;
}

export interface UtilisateurInfo {
  id: string;
  email: string;
  role: string;
}

// Interfaces pour la vérification d'email et l'inscription
export interface VerificationEmail {
  email: string;
}

export interface VerificationEmailResult {
  success: boolean;
  message: string;
  existe: boolean;
}

export interface Inscription {
  email: string;
  role: string;
  nom?: string;
  prenom?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenKey = 'auth_token';
  private utilisateurKey = 'utilisateur_info';

  private utilisateurSubject = new BehaviorSubject<UtilisateurInfo | null>(null);
  public utilisateur$ = this.utilisateurSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.chargerUtilisateurStocke();
  }

  // ========== MÉTHODE D'INSCRIPTION (UNE SEULE FOIS) ==========
  inscrire(email: string, role: string = 'CONSULTANT'): Observable<InscriptionResult> {
    const inscription: Inscription = { email, role };
    return this.http.post<InscriptionResult>(`${this.apiUrl}/inscrire`, inscription).pipe(
      tap(result => {
        if (result.success) {
          console.log('✅ Inscription réussie pour:', email);
        }
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Vérifier si un email existe déjà
   */
  verifierEmail(email: string): Observable<VerificationEmailResult> {
    const verification: VerificationEmail = { email };
    return this.http.post<VerificationEmailResult>(`${this.apiUrl}/verifier-email`, verification).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Étape 1: Demander un code de connexion (pour utilisateur existant)
   */
  demanderConnexion(email: string): Observable<DemandeConnexionResult> {
    const demande: DemandeConnexion = { email };

    return this.http.post<DemandeConnexionResult>(`${this.apiUrl}/demander-connexion`, demande).pipe(
      tap(result => {
        if (result.success) {
          console.log('✅ Code envoyé à:', email);
        }
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Étape 2: Valider le code reçu
   */
  validerCode(utilisateurId: string, code: string): Observable<ValidationCodeResult> {
    const validation: ValidationCode = { utilisateurId, code };

    return this.http.post<ValidationCodeResult>(`${this.apiUrl}/valider-code`, validation).pipe(
      tap(result => {
        if (result.success && result.token) {
          this.enregistrerSession(result);
        }
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Enregistrer la session
   */
  private enregistrerSession(result: ValidationCodeResult) {
    if (result.token) {
      localStorage.setItem(this.tokenKey, result.token);

      const utilisateurInfo: UtilisateurInfo = {
        id: result.utilisateurId || '',
        email: result.email || '',
        role: result.role || 'ADMIN'
      };

      localStorage.setItem(this.utilisateurKey, JSON.stringify(utilisateurInfo));
      this.utilisateurSubject.next(utilisateurInfo);
    }
  }

  /**
   * Charger l'utilisateur depuis le stockage
   */
  private chargerUtilisateurStocke() {
    const token = localStorage.getItem(this.tokenKey);
    const utilisateurStr = localStorage.getItem(this.utilisateurKey);

    if (token && utilisateurStr) {
      try {
        const utilisateur = JSON.parse(utilisateurStr);
        this.utilisateurSubject.next(utilisateur);
      } catch (e) {
        this.deconnexion();
      }
    }
  }

  /**
   * Vérifier si l'utilisateur est connecté
   */
  estConnecte(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  /**
   * Obtenir le token
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Obtenir l'utilisateur courant
   */
  getUtilisateurCourant(): UtilisateurInfo | null {
    return this.utilisateurSubject.value;
  }

  /**
   * Déconnexion
   */
  deconnexion() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.utilisateurKey);
    this.utilisateurSubject.next(null);
    this.router.navigate(['/connexion']);
  }

  /**
   * Gestion des erreurs
   */
  private handleError(error: any): Observable<never> {
    console.error('Erreur AuthService:', error);

    let message = 'Une erreur est survenue';
    if (error.error && error.error.message) {
      message = error.error.message;
    } else if (error.status === 0) {
      message = 'Serveur inaccessible. Vérifiez que le backend est démarré.';
    } else if (error.status === 404) {
      message = 'Email non trouvé';
    } else if (error.status === 409) {
      message = 'Cet email est déjà utilisé';
    }

    return throwError(() => new Error(message));
  }
}