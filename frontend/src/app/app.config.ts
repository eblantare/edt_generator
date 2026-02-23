// C:\projets\java\edt-generator\frontend\src\app\app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';

// Importer les intercepteurs (ce sont des constantes/fonctions)
import { authInterceptor } from './interceptors/auth.interceptor';
import { loggingInterceptor } from './interceptors/logging.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        loggingInterceptor,  // Premier intercepteur
        authInterceptor      // Deuxième intercepteur
      ])
    )
  ]
};
