// C:\projets\java\edt-generator\frontend\src\app\services\enseignement.service.ts
// REMPLACEZ TOUT LE FICHIER PAR :

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EnseignementDTO {
  id?: string;
  enseignantId: string;
  enseignantMatricule?: string;
  enseignantNom?: string;
  enseignantPrenom?: string;
  classeId: string;
  classeNom?: string;
  classeNiveau?: string;
  classeFiliere?: string;
  classeEffectif?: number;
  matiereId: string;
  matiereCode?: string;
  matiereNom?: string;
  matiereCycle?: string;
  heuresParSemaine: number;
  heuresAttribuees?: number;
  heuresRestantes?: number;
  estMatiereDominante?: boolean;
  statut?: string;
  commentaire?: string;
  ordrePriorite?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class EnseignementService {
  private apiUrl = `${environment.apiUrl}/enseignements`;

  constructor(private http: HttpClient) {}

  getAllEnseignements(
    page: number = 0,
    size: number = 10,
    search: string = '',
    sortBy: string = 'id',
    sortDirection: string = 'asc'
  ): Observable<PageResponse<EnseignementDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PageResponse<EnseignementDTO>>(this.apiUrl, { params });
  }

  getEnseignementById(id: string): Observable<EnseignementDTO> {
    return this.http.get<EnseignementDTO>(`${this.apiUrl}/${id}`);
  }

  createEnseignement(enseignement: EnseignementDTO): Observable<EnseignementDTO> {
    console.log('🚀 Envoi POST avec données:', enseignement);
    return this.http.post<EnseignementDTO>(this.apiUrl, enseignement, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  updateEnseignement(id: string, enseignement: EnseignementDTO): Observable<EnseignementDTO> {
    return this.http.put<EnseignementDTO>(`${this.apiUrl}/${id}`, enseignement, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  deleteEnseignement(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getEnseignementsByEnseignant(enseignantId: string): Observable<EnseignementDTO[]> {
    return this.http.get<EnseignementDTO[]>(`${this.apiUrl}/enseignant/${enseignantId}`);
  }

  getEnseignementsByClasse(classeId: string): Observable<EnseignementDTO[]> {
    return this.http.get<EnseignementDTO[]>(`${this.apiUrl}/classe/${classeId}`);
  }

  getEnseignementsByMatiere(matiereId: string): Observable<EnseignementDTO[]> {
    return this.http.get<EnseignementDTO[]>(`${this.apiUrl}/matiere/${matiereId}`);
  }
}