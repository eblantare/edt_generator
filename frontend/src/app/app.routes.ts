// C:\projets\java\edt-generator\frontend\src\app\app.routes.ts
import { Routes } from '@angular/router';
import { EnseignantListComponent } from './components/enseignant-list/enseignant-list.component';
import { EnseignantFormComponent } from './components/enseignant-form/enseignant-form.component';
import { EnseignantDetailComponent } from './components/enseignant-detail/enseignant-detail.component';
import { MatiereListComponent } from './components/matiere-list/matiere-list.component';
import { MatiereFormComponent } from './components/matiere-form/matiere-form.component';
import { MatiereDetailsComponent } from './components/matiere-details/matiere-details.component';
import { ClasseListComponent } from './components/classe-list/classe-list.component';
import { ClasseFormComponent } from './components/classe-form/classe-form.component';
import { ClasseDetailsComponent } from './components/classe-details/classe-details.component';
import { EnseignementListComponent } from './components/enseignement/enseignement-list.component';
import { EnseignementFormComponent } from './components/enseignement/enseignement-form.component';
import { GenerationComponent } from './components/generation/generation.component';
import { VisualisationComponent } from './components/visualisation/visualisation.component';  // AJOUTEZ CETTE LIGNE

export const routes: Routes = [
  { path: '', redirectTo: '/generation', pathMatch: 'full' },  // Redirige vers /generation

  // Enseignants
  { path: 'enseignants', component: EnseignantListComponent },
  { path: 'enseignants/new', component: EnseignantFormComponent },
  { path: 'enseignants/edit/:id', component: EnseignantFormComponent },
  { path: 'enseignants/:id', component: EnseignantDetailComponent },

  // Enseignements
  { path: 'enseignements', component: EnseignementListComponent },
  { path: 'enseignements/new', component: EnseignementFormComponent },
  { path: 'enseignements/edit/:id', component: EnseignementFormComponent },

  // Génération
  { path: 'generation', component: GenerationComponent },

  // Visualisation des emplois du temps
  { path: 'visualisation/:id', component: VisualisationComponent },  // AJOUTEZ CETTE LIGNE

  // Matières
  {
    path: 'matieres',
    children: [
      { path: '', component: MatiereListComponent },
      { path: 'new', component: MatiereFormComponent },
      { path: 'edit/:id', component: MatiereFormComponent },
      { path: ':id', component: MatiereDetailsComponent }
    ]
  },

  // Classes
  {
    path: 'classes',
    children: [
      { path: '', component: ClasseListComponent },
      { path: 'new', component: ClasseFormComponent },
      { path: 'edit/:id', component: ClasseFormComponent },
      { path: ':id', component: ClasseDetailsComponent }
    ]
  },

  // Fallback
  { path: '**', redirectTo: '/generation' }
];