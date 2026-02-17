// C:\projets\java\edt-generator\frontend\src\app\models\enseignement.model.ts
export interface Enseignement {
  id?: string;
  enseignantId: string;
  enseignantNom?: string;
  enseignantPrenom?: string;
  matiereId: string;
  matiereCode?: string;
  matiereNom?: string;
  classeId: string;
  classeNom?: string;
  heuresParSemaine: number;
}
