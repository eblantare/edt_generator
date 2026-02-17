// C:\projets\java\edt-generator\frontend\src\app\services\enseignant-mapper.service.ts
import { Injectable } from '@angular/core';
import { Enseignant, Matiere } from '../models/enseignant.model';

@Injectable({
  providedIn: 'root'
})
export class EnseignantMapperService {
  
  // Méthode pour mapper les données brutes du backend vers le modèle frontend
  mapBackendToFrontend(enseignantData: any, allMatieres: Matiere[]): Enseignant {
    console.log('🔄 Mapping backend → frontend:', enseignantData);
    
    const result: Enseignant = {
      id: enseignantData.id,
      nom: enseignantData.nom,
      prenom: enseignantData.prenom,
      matricule: enseignantData.matricule,
      email: enseignantData.email,
      telephone: enseignantData.telephone || '',
      heuresMaxHebdo: enseignantData.heuresMaxHebdo || 0
    };

    // Vérifier ce que le backend renvoie réellement
    console.log('🔍 Analyse des matières renvoyées:');
    console.log('   matiereDominante:', enseignantData.matiereDominante);
    console.log('   matiereDominanteId:', enseignantData.matiereDominanteId);
    console.log('   matiereSecondaire:', enseignantData.matiereSecondaire);
    console.log('   matiereSecondaireId:', enseignantData.matiereSecondaireId);

    // Gestion matière dominante
    if (enseignantData.matiereDominante) {
      // Cas 1: Objet complet
      if (typeof enseignantData.matiereDominante === 'object') {
        result.matiereDominante = enseignantData.matiereDominante;
      }
      // Cas 2: Chaîne (ID)
      else if (typeof enseignantData.matiereDominante === 'string') {
        const matiere = allMatieres.find(m => m.id === enseignantData.matiereDominante);
        result.matiereDominante = matiere || this.createMatierePlaceholder(enseignantData.matiereDominante, 'dominante');
      }
    }
    // Si le backend envoie un ID séparé
    else if (enseignantData.matiereDominanteId) {
      const matiere = allMatieres.find(m => m.id === enseignantData.matiereDominanteId);
      result.matiereDominante = matiere || this.createMatierePlaceholder(enseignantData.matiereDominanteId, 'dominante');
    }

    // Gestion matière secondaire
    if (enseignantData.matiereSecondaire) {
      if (typeof enseignantData.matiereSecondaire === 'object') {
        result.matiereSecondaire = enseignantData.matiereSecondaire;
      }
      else if (typeof enseignantData.matiereSecondaire === 'string') {
        const matiere = allMatieres.find(m => m.id === enseignantData.matiereSecondaire);
        result.matiereSecondaire = matiere || this.createMatierePlaceholder(enseignantData.matiereSecondaire, 'secondaire');
      }
    }
    else if (enseignantData.matiereSecondaireId) {
      const matiere = allMatieres.find(m => m.id === enseignantData.matiereSecondaireId);
      result.matiereSecondaire = matiere || this.createMatierePlaceholder(enseignantData.matiereSecondaireId, 'secondaire');
    }

    console.log('✅ Résultat mappé:', result);
    return result;
  }

  private createMatierePlaceholder(id: string, type: string): Matiere {
    return {
      id: id,
      code: `${type === 'dominante' ? 'DOM' : 'SEC'}-${id.substring(0, 4)}`,
      nom: `Matière ${type} (ID: ${id})`,
      cycle: 'non spécifié'
    };
  }
}