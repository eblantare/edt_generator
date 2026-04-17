import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EcoleDTO {
  id: string;
  nom: string;
  telephone: string;
  adresse: string;
  logo: string;
  devise: string;
  dre: string;
  iesg: string;
  bp: string;
}

@Injectable({
  providedIn: 'root'
})
export class EcoleService {
  private apiUrl = `${environment.apiUrl}/ecole`;

  constructor(private http: HttpClient) {}

  getEcole(): Observable<EcoleDTO> {
    return this.http.get<EcoleDTO>(this.apiUrl);
  }

  saveEcole(ecole: EcoleDTO): Observable<EcoleDTO> {
    return this.http.post<EcoleDTO>(this.apiUrl, ecole);
  }

  getAllEcoles(): Observable<EcoleDTO[]> {
    return this.http.get<EcoleDTO[]>(`${this.apiUrl}/all`);
  }

  deleteEcole(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}