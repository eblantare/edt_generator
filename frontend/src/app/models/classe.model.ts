export interface Classe {
  id?: string;
  nom: string;
  niveau: string;
  filiere: string;
  effectif: number;
}

export interface ClasseListResponse {
  content: Classe[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}