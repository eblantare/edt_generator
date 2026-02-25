import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Page de connexion (publique)
  {
    path: 'connexion',
    loadComponent: () => import('./components/connexion/connexion.component')
      .then(m => m.ConnexionComponent)
  },

  // Page d'inscription (publique) - AJOUTÉE
  {
    path: 'inscription',
    loadComponent: () => import('./components/inscription/inscription.component')
      .then(m => m.InscriptionComponent)
  },

  // Enseignants (protégé)
  {
    path: 'enseignants',
    loadComponent: () => import('./components/enseignant-list/enseignant-list.component')
      .then(m => m.EnseignantListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'enseignants/new',
    loadComponent: () => import('./components/enseignant-form/enseignant-form.component')
      .then(m => m.EnseignantFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'enseignants/edit/:id',
    loadComponent: () => import('./components/enseignant-form/enseignant-form.component')
      .then(m => m.EnseignantFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'enseignants/:id',
    loadComponent: () => import('./components/enseignant-detail/enseignant-detail.component')
      .then(m => m.EnseignantDetailComponent),
    canActivate: [AuthGuard]
  },

  // Enseignements (protégé)
  {
    path: 'enseignements',
    loadComponent: () => import('./components/enseignement/enseignement-list.component')
      .then(m => m.EnseignementListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'enseignements/new',
    loadComponent: () => import('./components/enseignement/enseignement-form.component')
      .then(m => m.EnseignementFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'enseignements/edit/:id',
    loadComponent: () => import('./components/enseignement/enseignement-form.component')
      .then(m => m.EnseignementFormComponent),
    canActivate: [AuthGuard]
  },

  // Génération (protégé)
  {
    path: 'generation',
    loadComponent: () => import('./components/generation/generation.component')
      .then(m => m.GenerationComponent),
    canActivate: [AuthGuard]
  },

  // Visualisation (protégé)
  {
    path: 'visualisation/:id',
    loadComponent: () => import('./components/visualisation/visualisation.component')
      .then(m => m.VisualisationComponent),
    canActivate: [AuthGuard]
  },

  // Matières (protégé)
  {
    path: 'matieres',
    loadComponent: () => import('./components/matiere-list/matiere-list.component')
      .then(m => m.MatiereListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'matieres/new',
    loadComponent: () => import('./components/matiere-form/matiere-form.component')
      .then(m => m.MatiereFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'matieres/edit/:id',
    loadComponent: () => import('./components/matiere-form/matiere-form.component')
      .then(m => m.MatiereFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'matieres/:id',
    loadComponent: () => import('./components/matiere-details/matiere-details.component')
      .then(m => m.MatiereDetailsComponent),
    canActivate: [AuthGuard]
  },

  // Classes (protégé)
  {
    path: 'classes',
    loadComponent: () => import('./components/classe-list/classe-list.component')
      .then(m => m.ClasseListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'classes/new',
    loadComponent: () => import('./components/classe-form/classe-form.component')
      .then(m => m.ClasseFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'classes/edit/:id',
    loadComponent: () => import('./components/classe-form/classe-form.component')
      .then(m => m.ClasseFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'classes/:id',
    loadComponent: () => import('./components/classe-details/classe-details.component')
      .then(m => m.ClasseDetailsComponent),
    canActivate: [AuthGuard]
  },

  // Redirections
  { path: '', redirectTo: '/connexion', pathMatch: 'full' },
  { path: '**', redirectTo: '/connexion' }
];
