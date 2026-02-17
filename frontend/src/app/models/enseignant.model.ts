// C:\projets\java\edt-generator\frontend\src\app\models\enseignant.model.ts
export interface Matiere {
  id?: string;
  code: string;
  nom: string;
  cycle: string;           // Changé de "niveau" à "cycle"
  niveauClasse?: string;   // Optionnel : pour préciser le niveau de classe
}

export interface Enseignant {
  id?: string;
  nom: string;
  prenom: string;
  matricule: string;
  email: string;
  telephone: string;
  heuresMaxHebdo: number;
  matiereDominante?: Matiere | null;
  matiereSecondaire?: Matiere | null;
}

// AJOUTEZ CETTE INTERFACE
export interface EnseignantListResponse {
  content: Enseignant[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Gardez aussi MatiereListResponse si vous l'avez déjà
export interface MatiereListResponse {
  content: Matiere[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// AJOUTEZ CETTE INTERFACE POUR LES DONNÉES BRUTES DU BACKEND
export interface EnseignantBackendData {
  id?: string;
  nom: string;
  prenom: string;
  matricule: string;
  email: string;
  telephone: string;
  heuresMaxHebdo: number;
  matiereDominante?: Matiere | string | null; // Peut être objet, string (ID) ou null
  matiereSecondaire?: Matiere | string | null; // Peut être objet, string (ID) ou null
}