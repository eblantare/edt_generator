import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Matiere } from '../models/enseignant.model';
import { environment } from '../../environments/environment';
export interface MatiereListResponse {
  content: Matiere[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class MatiereService {
  private apiUrl = `${environment.apiUrl}/matieres`;

  constructor(private http: HttpClient) { }

  // CORRECTION dans matiere.service.ts
getMatiere(id: string): Observable<Matiere> {
  console.log(`📡 MatiereService - Chargement matière ID: ${id}`);

  // Validation plus robuste de l'ID
  if (!id || id.trim() === '' || id === 'null' || id === 'undefined' || id.includes('temp_')) {
    console.error('❌ ID invalide:', id);
    return throwError(() => new Error(`ID de matière invalide: ${id}`));
  }

  const url = `${this.apiUrl}/${id}`;
  console.log(`🌐 URL appelée: ${url}`);

  return this.http.get<Matiere>(url).pipe(
    catchError(error => {
      console.error('❌ MatiereService - Erreur API:', {
        status: error.status,
        message: error.message,
        url: error.url
      });
      // Retourner une erreur descriptive
      return throwError(() => new Error(`Impossible de charger la matière: ${error.status} ${error.message}`));
    })
  );
}

  // Gardez les autres méthodes inchangées
  getMatieresPaginated(
    page: number = 0,
    size: number = 10,
    search: string = '',
    sortBy: string = 'code',
    sortDirection: string = 'asc'
  ): Observable<MatiereListResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (search && search.trim() !== '') {
      params = params.set('search', search);
    }

    return this.http.get<MatiereListResponse>(`${this.apiUrl}/paginated`, { params });
  }

  getAllMatieres(): Observable<Matiere[]> {
    return this.http.get<Matiere[]>(this.apiUrl);
  }

  createMatiere(matiere: Matiere): Observable<Matiere> {
    return this.http.post<Matiere>(this.apiUrl, matiere);
  }

  updateMatiere(id: string, matiere: Matiere): Observable<Matiere> {
    return this.http.put<Matiere>(`${this.apiUrl}/${id}`, matiere);
  }

  deleteMatiere(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
