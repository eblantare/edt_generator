// C:\projets\java\edt-generator\frontend\src\app\components\notification\notification.component.ts
import { Component, OnInit, OnDestroy, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private autoCloseSubscription?: Subscription;
  private notificationSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.notificationSubscription = this.notificationService.getNotifications().subscribe(
      (notifications: Notification[]) => {
        this.ngZone.run(() => {
          this.notifications = notifications;
          this.startAutoCloseCheck();
        });
      }
    );
  }

  ngOnDestroy(): void {
    this.autoCloseSubscription?.unsubscribe();
    this.notificationSubscription?.unsubscribe();
  }

  private startAutoCloseCheck(): void {
    this.autoCloseSubscription?.unsubscribe();

    // 🔴 SOLUTION: Intervalle plus long (300ms) pour réduire la fréquence
    this.autoCloseSubscription = interval(300).subscribe(() => {
      this.ngZone.run(() => {
        const now = Date.now();
        const notificationsToRemove: number[] = [];

        this.notifications.forEach(notification => {
          if (notification.autoClose && now - notification.timestamp > 5000) {
            notificationsToRemove.push(notification.id);
          }
        });

        if (notificationsToRemove.length > 0) {
          notificationsToRemove.forEach(id => {
            this.notificationService.removeNotification(id);
          });
        } else {
          // 🔴 Forcer la détection uniquement si nécessaire
          this.cdr.detectChanges();
        }
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

  // 🔴 SOLUTION: Arrondir à 1 décimale pour éviter les fluctuations (99.4 → 99.3)
  getProgressWidth(notification: Notification): number {
    if (!notification.autoClose) return 0;

    const elapsed = Date.now() - notification.timestamp;
    const total = 5000;
    const remaining = Math.max(0, total - elapsed);
    // Arrondir pour éviter les changements infimes
    return Math.round((remaining / total) * 100 * 10) / 10;
  }

  closeNotification(id: number): void {
    this.ngZone.run(() => {
      this.notificationService.removeNotification(id);
    });
  }
}
