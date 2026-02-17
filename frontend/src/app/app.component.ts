// C:\projets\java\edt-generator\frontend\src\app\app.component.ts
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NotificationComponent } from './components/notification/notification.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, // Utilisé pour <router-outlet>
    RouterLink,
    RouterLinkActive,
    NotificationComponent // Utilisé pour <app-notification>
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Générateur EDT';
}
