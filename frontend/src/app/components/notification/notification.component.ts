import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private autoCloseSubscription?: Subscription;
  private notificationSubscription?: Subscription;

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    // S'abonner aux notifications
    this.notificationSubscription = this.notificationService.getNotifications().subscribe(
      (notifications: Notification[]) => {
        this.notifications = notifications;

        // Démarrer la vérification automatique de fermeture
        this.startAutoCloseCheck();
      }
    );
  }

  ngOnDestroy(): void {
    // Nettoyer les abonnements
    this.autoCloseSubscription?.unsubscribe();
    this.notificationSubscription?.unsubscribe();
  }

  private startAutoCloseCheck(): void {
    // Arrêter l'ancienne vérification si elle existe
    this.autoCloseSubscription?.unsubscribe();

    // Vérifier toutes les 100ms pour des mises à jour fluides
    this.autoCloseSubscription = interval(100).subscribe(() => {
      const now = Date.now();
      const notificationsToRemove: number[] = [];

      this.notifications.forEach(notification => {
        // Fermer automatiquement après 5 secondes (5000ms)
        if (notification.autoClose && now - notification.timestamp > 5000) {
          notificationsToRemove.push(notification.id);
        }
      });

      // Supprimer les notifications expirées
      notificationsToRemove.forEach(id => {
        this.closeNotification(id);
      });
    });
  }

  getNotificationClass(notification: Notification): string {
    const classes: { [key: string]: string } = {
      'success': 'alert-success',
      'error': 'alert-danger',
      'info': 'alert-info',
      'warning': 'alert-warning'
    };
    return `alert ${classes[notification.type]} alert-dismissible fade show`;
  }

  getNotificationIcon(notification: Notification): string {
    const icons: { [key: string]: string } = {
      'success': 'bi-check-circle-fill',
      'error': 'bi-exclamation-circle-fill',
      'info': 'bi-info-circle-fill',
      'warning': 'bi-exclamation-triangle-fill'
    };
    return icons[notification.type] || 'bi-info-circle-fill';
  }

  getProgressWidth(notification: Notification): number {
    if (!notification.autoClose) return 0;

    const elapsed = Date.now() - notification.timestamp;
    const total = 5000; // 5 secondes
    const remaining = Math.max(0, total - elapsed);

    return (remaining / total) * 100;
  }

  closeNotification(id: number): void {
    this.notificationService.removeNotification(id);
  }
}
