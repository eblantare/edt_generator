// frontend/src/app/interceptors/logging.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('🌐 Requête HTTP:', req.method, req.url);
  console.log('📦 Body:', req.body);
  console.log('🔍 Params:', req.params.toString());
  
  return next(req).pipe(
    tap({
      next: (event) => {
        console.log('✅ Réponse HTTP reçue pour:', req.url);
      },
      error: (error) => {
        console.error('❌ Erreur HTTP pour:', req.url, error);
      }
    })
  );
};