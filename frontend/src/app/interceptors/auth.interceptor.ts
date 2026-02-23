// C:\projets\java\edt-generator\frontend\src\app\interceptors\auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

// C'est une constante, pas une classe
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Ne pas intercepter les requêtes d'authentification
  if (req.url.includes('/auth/')) {
    return next(req);
  }

  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        authService.deconnexion();
        router.navigate(['/connexion']);
      }
      return throwError(() => error);
    })
  );
};
