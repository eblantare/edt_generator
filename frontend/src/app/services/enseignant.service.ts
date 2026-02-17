// C:\projets\java\edt-generator\frontend\src\app\services\enseignant.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Enseignant, EnseignantListResponse, Matiere, EnseignantBackendData } from '../models/enseignant.model';
import { MatiereService } from './matiere.service';

@Injectable({
  providedIn: 'root'
})
export class EnseignantService {
  private apiUrl = 'http://localhost:8080/api/enseignants';

  constructor(
    private http: HttpClient,
    private matiereService: MatiereService
  ) { }

  // MÉTHODE DE DEBUG : Tester la réponse du backend
  testBackendResponse(): void {
    this.http.get<any>('http://localhost:8080/api/enseignants').subscribe({
      next: (response) => {
        console.log('🔍 TEST - Réponse brute du backend:', response);
        if (response.content && response.content.length > 0) {
          const firstEnseignant = response.content[0];
          console.log('🔍 Premier enseignant du backend:', firstEnseignant);
          console.log('📚 Ses matières:', {
            matiereDominante: firstEnseignant.matiereDominante,
            matiereDominanteType: typeof firstEnseignant.matiereDominante,
            matiereSecondaire: firstEnseignant.matiereSecondaire,
            matiereSecondaireType: typeof firstEnseignant.matiereSecondaire
          });
          
          // Vérifier si c'est un objet ou une chaîne
          if (firstEnseignant.matiereDominante) {
            console.log('🔎 Détails matière dominante:', {
              estObjet: typeof firstEnseignant.matiereDominante === 'object',
              estString: typeof firstEnseignant.matiereDominante === 'string',
              valeur: firstEnseignant.matiereDominante
            });
          }
        }
      },
      error: (error) => {
        console.error('❌ Erreur test:', error);
      }
    });
  }

  getAllEnseignants(
    page: number = 0,
    size: number = 10,
    search: string = '',
    sortBy: string = 'nom',
    sortDirection: string = 'asc'
  ): Observable<EnseignantListResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (search && search.trim() !== '') {
      params = params.set('search', search);
    }

    console.log('🟡 getAllEnseignants appelé avec params:', { page, size, search, sortBy, sortDirection });

    return this.http.get<EnseignantListResponse>(this.apiUrl, { params })
      .pipe(
        switchMap(response => {
          console.log('📦 Réponse brute du backend (liste):', response);
          
          if (response.content && response.content.length > 0) {
            // Charger toutes les matières pour pouvoir les mapper
            return this.matiereService.getAllMatieres().pipe(
              map(allMatieres => {
                console.log('📚 Toutes les matières chargées:', allMatieres.length);
                console.log('🔍 Premières matières:', allMatieres.slice(0, 3));
                
                // TEST : Vérifier le mapping pour le premier enseignant
                if (response.content.length > 0 && allMatieres.length > 0) {
                  const testEnseignant = response.content[0];
                  console.log('🧪 TEST Mapping pour le premier enseignant:', {
                    nom: testEnseignant.nom,
                    matiereDominanteRaw: testEnseignant.matiereDominante,
                    matiereSecondaireRaw: testEnseignant.matiereSecondaire,
                    trouveDominante: allMatieres.find(m => m.id === testEnseignant.matiereDominante),
                    trouveSecondaire: allMatieres.find(m => m.id === testEnseignant.matiereSecondaire)
                  });
                }
                
                // Convertir chaque enseignant brut en enseignant enrichi
                const enseignantsEnrichis = response.content.map(enseignantData => 
                  this.mapBackendToFrontend(enseignantData, allMatieres)
                );
                
                return {
                  ...response,
                  content: enseignantsEnrichis
                };
              })
            );
          }
          return of(response);
        }),
        catchError(this.handleError)
      );
  }

  getEnseignant(id: string): Observable<Enseignant> {
    console.log('🟡 getEnseignant appelé pour ID:', id);
    
    return this.http.get<EnseignantBackendData>(`${this.apiUrl}/${id}`)
      .pipe(
        switchMap(enseignantData => {
          console.log('📦 Enseignant brut du backend (détail):', enseignantData);
          
          // Charger toutes les matières pour le mapping
          return this.matiereService.getAllMatieres().pipe(
            map(allMatieres => {
              console.log('📚 Matières disponibles pour mapping:', allMatieres.length);
              return this.mapBackendToFrontend(enseignantData, allMatieres);
            })
          );
        }),
        catchError(this.handleError)
      );
  }

  // MÉTHODE CRITIQUE : Convertir les données du backend en modèle frontend
  private mapBackendToFrontend(enseignantData: EnseignantBackendData, allMatieres: Matiere[]): Enseignant {
    console.log('🔄 Début mapping pour:', enseignantData.nom);
    console.log('📊 Données brutes reçues:', {
      id: enseignantData.id,
      nom: enseignantData.nom,
      matiereDominante: enseignantData.matiereDominante,
      matiereDominanteType: typeof enseignantData.matiereDominante,
      matiereSecondaire: enseignantData.matiereSecondaire,
      matiereSecondaireType: typeof enseignantData.matiereSecondaire
    });

    console.log('📚 Matières disponibles pour recherche:', allMatieres.map(m => ({ id: m.id, code: m.code, nom: m.nom })));

    const enseignant: Enseignant = {
      id: enseignantData.id,
      nom: enseignantData.nom,
      prenom: enseignantData.prenom,
      matricule: enseignantData.matricule,
      email: enseignantData.email,
      telephone: enseignantData.telephone || '',
      heuresMaxHebdo: enseignantData.heuresMaxHebdo || 0,
      matiereDominante: undefined,
      matiereSecondaire: undefined
    };

    // STRATÉGIE DE RECHERCHE : Essayer toutes les possibilités
    
    // 1. Traitement de la matière dominante
    if (enseignantData.matiereDominante) {
      // Cas 1 : C'est déjà un objet complet
      if (typeof enseignantData.matiereDominante === 'object' && enseignantData.matiereDominante !== null) {
        const matiereObj = enseignantData.matiereDominante as any;
        // S'assurer que l'objet a les bonnes propriétés
        if (matiereObj.id && matiereObj.code && matiereObj.nom) {
          enseignant.matiereDominante = {
            id: matiereObj.id,
            code: matiereObj.code,
            nom: matiereObj.nom,
            cycle: matiereObj.cycle || '',
            niveauClasse: matiereObj.niveauClasse || ''
          };
          console.log('✅ Matière dominante déjà en objet complet:', enseignant.matiereDominante);
        } else {
          console.warn('⚠️ Objet matière dominante incomplet:', matiereObj);
        }
      }
      // Cas 2 : C'est une chaîne (ID)
      else if (typeof enseignantData.matiereDominante === 'string') {
        const matiereId = enseignantData.matiereDominante;
        console.log('🔍 Recherche matière dominante par ID:', matiereId);
        
        const matiereTrouvee = allMatieres.find(m => m.id === matiereId);
        if (matiereTrouvee) {
          enseignant.matiereDominante = matiereTrouvee;
          console.log('✅ Matière dominante trouvée dans la liste:', matiereTrouvee);
        } else {
          console.log('⚠️ Matière dominante NON trouvée pour ID:', matiereId);
          // Créer un placeholder pour le débogage
          enseignant.matiereDominante = {
            id: matiereId,
            code: 'DOM-' + (matiereId.substring(0, Math.min(4, matiereId.length)) || '????'),
            nom: 'Matière à charger (ID: ' + matiereId + ')',
            cycle: 'non spécifié'
          };
        }
      } else {
        console.warn('⚠️ Type inattendu pour matière dominante:', typeof enseignantData.matiereDominante);
      }
    }

    // 2. Traitement de la matière secondaire
    if (enseignantData.matiereSecondaire) {
      // Cas 1 : C'est déjà un objet complet
      if (typeof enseignantData.matiereSecondaire === 'object' && enseignantData.matiereSecondaire !== null) {
        const matiereObj = enseignantData.matiereSecondaire as any;
        // S'assurer que l'objet a les bonnes propriétés
        if (matiereObj.id && matiereObj.code && matiereObj.nom) {
          enseignant.matiereSecondaire = {
            id: matiereObj.id,
            code: matiereObj.code,
            nom: matiereObj.nom,
            cycle: matiereObj.cycle || '',
            niveauClasse: matiereObj.niveauClasse || ''
          };
          console.log('✅ Matière secondaire déjà en objet complet:', enseignant.matiereSecondaire);
        } else {
          console.warn('⚠️ Objet matière secondaire incomplet:', matiereObj);
        }
      }
      // Cas 2 : C'est une chaîne (ID)
      else if (typeof enseignantData.matiereSecondaire === 'string') {
        const matiereId = enseignantData.matiereSecondaire;
        console.log('🔍 Recherche matière secondaire par ID:', matiereId);
        
        const matiereTrouvee = allMatieres.find(m => m.id === matiereId);
        if (matiereTrouvee) {
          enseignant.matiereSecondaire = matiereTrouvee;
          console.log('✅ Matière secondaire trouvée dans la liste:', matiereTrouvee);
        } else {
          console.log('⚠️ Matière secondaire NON trouvée pour ID:', matiereId);
          // Créer un placeholder pour le débogage
          enseignant.matiereSecondaire = {
            id: matiereId,
            code: 'SEC-' + (matiereId.substring(0, Math.min(4, matiereId.length)) || '????'),
            nom: 'Matière à charger (ID: ' + matiereId + ')',
            cycle: 'non spécifié'
          };
        }
      } else {
        console.warn('⚠️ Type inattendu pour matière secondaire:', typeof enseignantData.matiereSecondaire);
      }
    }

    // FALLBACK : Si aucune matière n'a été trouvée
    if (!enseignant.matiereDominante && !enseignant.matiereSecondaire) {
      console.warn('⚠️ Aucune matière trouvée pour cet enseignant');
      // Ne pas créer de placeholders automatiquement, laisser undefined
    }

    console.log('🎯 Enseignant après mapping:', {
      nom: enseignant.nom,
      hasDominante: !!enseignant.matiereDominante,
      dominante: enseignant.matiereDominante,
      hasSecondaire: !!enseignant.matiereSecondaire,
      secondaire: enseignant.matiereSecondaire
    });

    return enseignant;
  }

  createEnseignant(enseignant: any): Observable<Enseignant> {
  // Préparer les données pour le backend
  const backendData: any = {
    nom: enseignant.nom,
    prenom: enseignant.prenom,
    matricule: enseignant.matricule,
    email: enseignant.email,
    telephone: enseignant.telephone,
    heuresMaxHebdo: enseignant.heuresMaxHebdo
  };

  // ⭐⭐ CORRECTION : Envoyer juste l'objet avec id, pas tout l'objet ⭐⭐
  if (enseignant.matiereDominante && enseignant.matiereDominante.id) {
    backendData.matiereDominante = { id: enseignant.matiereDominante.id };
  }

  if (enseignant.matiereSecondaire && enseignant.matiereSecondaire.id) {
    backendData.matiereSecondaire = { id: enseignant.matiereSecondaire.id };
  }

  console.log('📤 Données envoyées au backend:', backendData);
  
  return this.http.post<Enseignant>(this.apiUrl, backendData)
    .pipe(
      catchError(this.handleError)
    );
}

  updateEnseignant(id: string, enseignant: any): Observable<Enseignant> {
  const backendData: any = {
    nom: enseignant.nom,
    prenom: enseignant.prenom,
    matricule: enseignant.matricule,
    email: enseignant.email,
    telephone: enseignant.telephone,
    heuresMaxHebdo: enseignant.heuresMaxHebdo
  };

  // ⭐⭐ CORRECTION : Envoyer juste l'objet avec id ⭐⭐
  if (enseignant.matiereDominante && enseignant.matiereDominante.id) {
    backendData.matiereDominante = { id: enseignant.matiereDominante.id };
  } else {
    backendData.matiereDominante = null;
  }

  if (enseignant.matiereSecondaire && enseignant.matiereSecondaire.id) {
    backendData.matiereSecondaire = { id: enseignant.matiereSecondaire.id };
  } else {
    backendData.matiereSecondaire = null;
  }

  console.log('📤 Données de mise à jour:', backendData);
  
  return this.http.put<Enseignant>(`${this.apiUrl}/${id}`, backendData)
    .pipe(
      catchError(this.handleError)
    );
}

  deleteEnseignant(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getCycleDisplayName(cycleCode: string): string {
    if (!cycleCode) return 'Non spécifié';
    
    const cycleMap: {[key: string]: string} = {
      'college': 'Collège',
      'lycee': 'Lycée Général',
      'lycee_tech': 'Lycée Technique',
      'lycee_pro': 'Lycée Professionnel',
      'bt': 'Brevet de Technicien',
      'primaria': 'P primaire',
      'secundaria': 'Secondaire',
      'superior': 'Supérieur'
    };
    
    return cycleMap[cycleCode.toLowerCase()] || cycleCode;
  }

  getCycleBadgeClass(cycleCode: string): string {
    if (!cycleCode) return 'bg-secondary';
    
    const cycleClassMap: {[key: string]: string} = {
      'college': 'bg-primary',
      'lycee': 'bg-success',
      'lycee_tech': 'bg-info',
      'lycee_pro': 'bg-warning',
      'bt': 'bg-danger',
      'primaria': 'bg-primary',
      'secundaria': 'bg-success',
      'superior': 'bg-info'
    };
    
    return cycleClassMap[cycleCode.toLowerCase()] || 'bg-secondary';
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue';
    
    console.error('❌ Erreur:', error);
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de se connecter au serveur';
          break;
        case 400:
          errorMessage = 'Données invalides';
          break;
        case 404:
          errorMessage = 'Ressource non trouvée';
          break;
        case 409:
          errorMessage = 'Conflit (matricule déjà utilisé)';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur';
          break;
        default:
          errorMessage = `Erreur ${error.status}: ${error.message}`;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }
}