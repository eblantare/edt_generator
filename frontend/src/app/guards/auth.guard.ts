// C:\projets\java\edt-generator\frontend\src\app\guards\auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    if (this.authService.estConnecte()) {
      return true;
    }

    // Rediriger vers la page de connexion
    this.router.navigate(['/connexion'], {
      queryParams: { returnUrl: state.url }
    });
    return false;
  }
}
