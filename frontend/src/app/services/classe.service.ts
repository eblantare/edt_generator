import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Classe, ClasseListResponse } from '../models/classe.model';

@Injectable({
  providedIn: 'root'
})
export class ClasseService {
  private apiUrl = 'http://localhost:8080/api/classes';

  constructor(private http: HttpClient) { }

  getClasse(id: string): Observable<Classe> {
    console.log(`📡 Chargement classe ID: ${id}`);

    if (!id || id === 'null' || id === 'undefined') {
      console.error('❌ ID invalide:', id);
      return throwError(() => new Error('ID invalide'));
    }

    const url = `${this.apiUrl}/${id}`;
    console.log(`🌐 URL appelée: ${url}`);

    return this.http.get<Classe>(url).pipe(
      catchError(error => {
        console.error('❌ Erreur API:', {
          status: error.status,
          message: error.message,
          url: error.url
        });
        return throwError(() => error);
      })
    );
  }

  getClassesPaginated(
    page: number = 0,
    size: number = 10,
    search: string = '',
    sortBy: string = 'nom',
    sortDirection: string = 'asc'
  ): Observable<ClasseListResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (search && search.trim() !== '') {
      params = params.set('search', search);
    }

    return this.http.get<ClasseListResponse>(`${this.apiUrl}/paginated`, { params });
  }

  getAllClasses(): Observable<Classe[]> {
    return this.http.get<Classe[]>(this.apiUrl);
  }

  createClasse(classe: Classe): Observable<Classe> {
    return this.http.post<Classe>(this.apiUrl, classe);
  }

  updateClasse(id: string, classe: Classe): Observable<Classe> {
    return this.http.put<Classe>(`${this.apiUrl}/${id}`, classe);
  }

  deleteClasse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}