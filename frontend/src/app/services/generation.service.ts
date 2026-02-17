// C:\projets\java\edt-generator\frontend\src\app\services\generation.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface GenerationResultDTO {
  success: boolean;
  message: string;
  emploiDuTempsId?: string;
  details?: any;
}

export interface GenerationOptionsDTO {
  verifierConflits: boolean;
  optimiserRepartition: boolean;
  genererSalles: boolean;
  respecterContraintesEPS?: boolean;
  placerPauses?: boolean;
  exporterFormat?: string;
}

export interface GenerationRequestDTO {
  anneeScolaire: string;
  classeId?: string;
  enseignantId?: string;
  options?: GenerationOptionsDTO;
}

export interface EmploiDuTempsDTO {
  id: string;
  nom: string;
  anneeScolaire: string;
  dateGeneration: string;
  statut: string;
  classeId?: string;
  classeNom?: string;
  enseignantId?: string;
  enseignantNom?: string;
  type?: string;
}

export interface CreneauHoraireDTO {
  id: string;
  emploiDuTempsId: string;
  jourSemaine: string;
  heureDebut: string;
  heureFin: string;
  numeroCreneau: number;
  estLibre: boolean;
  salle?: string;
  classeId?: string;
  classeNom?: string;
  enseignantId?: string;
  enseignantNom?: string;
  matiereId?: string;
  matiereNom?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GenerationService {
  private apiUrl = `${environment.apiUrl}/generation`;
  private baseApiUrl = environment.apiUrl;
  // ✅ AJOUT : URL spécifique pour les emplois du temps
  private emploisUrl = `${environment.apiUrl}/emplois-du-temps`;

  constructor(private http: HttpClient) {}

  // === GÉNÉRATION ===
  genererGlobal(anneeScolaire: string, options?: GenerationOptionsDTO): Observable<GenerationResultDTO> {
    const request: GenerationRequestDTO = {
      anneeScolaire,
      options: options || this.getDefaultOptions()
    };
    return this.http.post<GenerationResultDTO>(`${this.apiUrl}/global`, request).pipe(
      catchError(this.handleError('genererGlobal'))
    );
  }

  genererPourClasse(classeId: string, anneeScolaire: string, options?: GenerationOptionsDTO): Observable<GenerationResultDTO> {
    const request: GenerationRequestDTO = {
      anneeScolaire,
      classeId,
      options: options || this.getDefaultOptions()
    };
    return this.http.post<GenerationResultDTO>(`${this.apiUrl}/classe`, request).pipe(
      catchError(this.handleError('genererPourClasse'))
    );
  }

  genererPourEnseignant(enseignantId: string, anneeScolaire: string, options?: GenerationOptionsDTO): Observable<GenerationResultDTO> {
    const request: GenerationRequestDTO = {
      anneeScolaire,
      enseignantId,
      options: options || this.getDefaultOptions()
    };
    return this.http.post<GenerationResultDTO>(`${this.apiUrl}/enseignant`, request).pipe(
      catchError(this.handleError('genererPourEnseignant'))
    );
  }

  // === GESTION DES ANNÉES SCOLAIRES ===
  getAnneesScolaires(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseApiUrl}/annees-scolaires`).pipe(
      catchError(() => {
        console.warn('Utilisation des données locales pour les années scolaires');
        return of(this.genererListeAnneesScolaires());
      })
    );
  }

  getAnneeScolaireCourante(): Observable<string> {
    return this.http.get(`${this.baseApiUrl}/annee-scolaire-courante`, { responseType: 'text' }).pipe(
      catchError(() => {
        console.warn('Utilisation de la date locale pour l\'année scolaire courante');
        return of(this.genererAnneeScolaireCourante());
      })
    );
  }

  // === VISUALISATION DES EMPLOIS ===
  getEmploisGlobal(): Observable<EmploiDuTempsDTO[]> {
    return this.http.get<EmploiDuTempsDTO[]>(`${this.emploisUrl}/emplois/global`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error('Erreur récupération emplois globaux:', error);
        return of([]);
      })
    );
  }

  getEmploisParClasse(classeId: string): Observable<EmploiDuTempsDTO[]> {
    return this.http.get<EmploiDuTempsDTO[]>(`${this.emploisUrl}/emplois/classe/${classeId}`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error(`Erreur récupération emplois pour classe ${classeId}:`, error);
        return of([]);
      })
    );
  }

  getEmploiDuTemps(id: string): Observable<EmploiDuTempsDTO> {
    return this.http.get<EmploiDuTempsDTO>(`${this.emploisUrl}/emplois/${id}`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error(`Erreur récupération emploi ${id}:`, error);
        return of({
          id,
          nom: 'Emploi du temps',
          anneeScolaire: this.genererAnneeScolaireCourante(),
          dateGeneration: new Date().toISOString(),
          statut: 'ERREUR'
        } as EmploiDuTempsDTO);
      })
    );
  }

  getCreneauxParEmploi(emploiId: string): Observable<CreneauHoraireDTO[]> {
    return this.http.get<CreneauHoraireDTO[]>(`${this.emploisUrl}/creneaux/emploi/${emploiId}`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error(`Erreur récupération créneaux pour emploi ${emploiId}:`, error);
        return of([]);
      })
    );
  }

  getCreneauxParJour(emploiId: string, jour: string): Observable<CreneauHoraireDTO[]> {
    return this.http.get<CreneauHoraireDTO[]>(`${this.emploisUrl}/creneaux/jour/${emploiId}/${jour}`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error(`Erreur récupération créneaux pour jour ${jour}:`, error);
        return of([]);
      })
    );
  }

  // === EXPORT ===
  exporterPDF(emploiId: string): Observable<Blob> {
    return this.http.get(`${this.baseApiUrl}/export/pdf/${emploiId}`, {
      responseType: 'blob'
    }).pipe(
      catchError((error) => {
        console.error('Erreur export PDF:', error);
        return throwError(() => new Error('Impossible d\'exporter le PDF'));
      })
    );
  }

  exporterExcel(emploiId: string): Observable<Blob> {
    return this.http.get(`${this.baseApiUrl}/export/excel/${emploiId}`, {
      responseType: 'blob'
    }).pipe(
      catchError((error) => {
        console.error('Erreur export Excel:', error);
        return throwError(() => new Error('Impossible d\'exporter le fichier Excel'));
      })
    );
  }

  exporterPDFMatriciel(emploiId: string, classeNom: string): Observable<Blob> {
    const encodedClasseNom = encodeURIComponent(classeNom);
    return this.http.get(`${this.baseApiUrl}/export/pdf-matriciel/${emploiId}/${encodedClasseNom}`, {
      responseType: 'blob'
    }).pipe(
      catchError((error) => {
        console.error('Erreur export PDF matriciel:', error);
        return throwError(() => new Error('Impossible d\'exporter le PDF au format tableau'));
      })
    );
  }

  // === STATISTIQUES ET HISTORIQUE ===
  getHistoriqueGenerations(): Observable<EmploiDuTempsDTO[]> {
    return this.http.get<EmploiDuTempsDTO[]>(`${this.apiUrl}/historique`).pipe(
      catchError((error) => {
        console.error('Erreur récupération historique:', error);
        return of([]);
      })
    );
  }

  getStatutGeneration(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statut/${id}`).pipe(
      catchError((error) => {
        console.error(`Erreur récupération statut pour ${id}:`, error);
        return of({ statut: 'ERREUR', progression: 0, message: 'Impossible de récupérer le statut' });
      })
    );
  }

  getStatistiquesEmploi(emploiId: string): Observable<any> {
    return this.http.get<any>(`${this.emploisUrl}/statistiques/global/${emploiId}`).pipe( // ✅ CORRIGÉ
      catchError((error) => {
        console.error(`Erreur récupération statistiques pour ${emploiId}:`, error);
        return of({
          totalCours: 0,
          heuresParSemaine: 0,
          message: 'Statistiques non disponibles'
        });
      })
    );
  }

  // === DIAGNOSTIC ===
  diagnostiquerBaseDeDonnees(): Observable<any> {
    return this.http.get<any>(`${this.baseApiUrl}/diagnostic/base-donnees`).pipe(
      catchError((error) => {
        console.error('Erreur diagnostic base de données:', error);
        return of({
          statut: 'ERREUR',
          message: 'Impossible de diagnostiquer la base de données'
        });
      })
    );
  }

  // === SUPPRESSION ===
  supprimerEmploiDuTemps(emploiId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/emploi/${emploiId}`).pipe(
      catchError((error) => {
        console.error('Erreur suppression emploi:', error);
        return throwError(() => new Error('Impossible de supprimer l\'emploi du temps'));
      })
    );
  }

  // === MÉTHODES UTILITAIRES ===
  private getDefaultOptions(): GenerationOptionsDTO {
    return {
      verifierConflits: true,
      optimiserRepartition: true,
      genererSalles: false,
      respecterContraintesEPS: true,
      placerPauses: true
    };
  }

  genererAnneeScolaireCourante(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    if (month >= 8) {
      return `${year}-${year + 1}`;
    } else {
      return `${year - 1}-${year}`;
    }
  }

  private genererListeAnneesScolaires(): string[] {
    const current = this.genererAnneeScolaireCourante();
    const [debut] = current.split('-').map(Number);
    return [
      `${debut-2}-${debut-1}`,
      `${debut-1}-${debut}`,
      current,
      `${debut+1}-${debut+2}`,
      `${debut+2}-${debut+3}`
    ];
  }

  validerFormatAnneeScolaire(annee: string): boolean {
    const regex = /^\d{4}-\d{4}$/;
    if (!regex.test(annee)) return false;
    const [debut, fin] = annee.split('-').map(Number);
    return fin === debut + 1;
  }

  private handleError(operation = 'opération') {
    return (error: any): Observable<any> => {
      console.error(`${operation} a échoué:`, error);
      let errorMessage = 'Une erreur est survenue';
      if (error.error instanceof ErrorEvent) {
        errorMessage = `Erreur: ${error.error.message}`;
      } else if (error.status === 0) {
        errorMessage = 'Serveur inaccessible. Vérifiez que le backend est en cours d\'exécution.';
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      return throwError(() => new Error(errorMessage));
    };
  }
}
