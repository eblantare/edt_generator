// C:\projets\java\edt-generator\frontend\src\app\app.component.ts
import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';
import { NotificationComponent } from './components/notification/notification.component';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,  // IMPORTANT: Fournit routerLink, router-outlet
    NotificationComponent  // IMPORTANT: Le composant de notification
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  schemas: []  // Pas besoin de CUSTOM_ELEMENTS_SCHEMA car on importe correctement
})
export class AppComponent implements OnInit {
  title = 'EDT Generator';
  estConnecte = false;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {
    // Surveiller les changements de route pour mettre à jour l'état de connexion
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.estConnecte = this.authService.estConnecte();
    });
  }

  ngOnInit() {
    this.estConnecte = this.authService.estConnecte();
  }

  deconnexion(event: Event) {
    event.preventDefault();
    this.authService.deconnexion();
    this.estConnecte = false;
  }

  getEmailUtilisateur(): string {
    const user = this.authService.getUtilisateurCourant();
    return user ? user.email : 'Utilisateur';
  }
}
