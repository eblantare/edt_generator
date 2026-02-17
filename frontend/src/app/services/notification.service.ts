// C:\projets\java\edt-generator\frontend\src\app\services\notification.service.ts
import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
  id: number;
  type: 'success' | 'error' | 'info' | 'warning';
  title: string;
  message: string;
  autoClose?: boolean;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private currentId = 0;

  constructor(private ngZone: NgZone) { }

  // Méthode pour obtenir les notifications en tant qu'Observable
  getNotifications(): Observable<Notification[]> {
    return this.notificationsSubject.asObservable();
  }

  // Méthode pour ajouter une notification (corrigée avec NgZone)
  addNotification(notification: Omit<Notification, 'id' | 'timestamp'>): number {
    let newId = 0;
    
    this.ngZone.run(() => {
      const newNotification: Notification = {
        ...notification,
        id: ++this.currentId,
        timestamp: Date.now()
      };

      const currentNotifications = this.notificationsSubject.value;
      this.notificationsSubject.next([...currentNotifications, newNotification]);
      newId = newNotification.id;
      
      // Auto-remove si configuré
      if (notification.autoClose !== false) {
        setTimeout(() => {
          this.removeNotification(newNotification.id);
        }, 5000); // Ferme automatiquement après 5 secondes
      }
    });
    
    return newId;
  }

  // Méthode pour supprimer une notification (corrigée avec NgZone)
  removeNotification(id: number): void {
    this.ngZone.run(() => {
      const currentNotifications = this.notificationsSubject.value;
      const updatedNotifications = currentNotifications.filter(
        notification => notification.id !== id
      );
      this.notificationsSubject.next(updatedNotifications);
    });
  }

  // Méthodes principales
  success(title: string, message: string, autoClose: boolean = true): number {
    return this.addNotification({
      type: 'success',
      title,
      message,
      autoClose
    });
  }

  error(title: string, message: string, autoClose: boolean = false): number {
    return this.addNotification({
      type: 'error',
      title,
      message,
      autoClose
    });
  }

  info(title: string, message: string, autoClose: boolean = true): number {
    return this.addNotification({
      type: 'info',
      title,
      message,
      autoClose
    });
  }

  warning(title: string, message: string, autoClose: boolean = true): number {
    return this.addNotification({
      type: 'warning',
      title,
      message,
      autoClose
    });
  }

  // Méthodes de compatibilité (alias)
  showSuccess(message: string, title: string = 'Succès'): number {
    return this.success(title, message, true);
  }

  showError(message: string, title: string = 'Erreur'): number {
    return this.error(title, message, false);
  }

  showInfo(message: string, title: string = 'Information'): number {
    return this.info(title, message, true);
  }

  showWarning(message: string, title: string = 'Attention'): number {
    return this.warning(title, message, true);
  }

  // Méthode pour vider toutes les notifications (corrigée avec NgZone)
  clearAll(): void {
    this.ngZone.run(() => {
      this.notificationsSubject.next([]);
    });
  }

  // Méthode utilitaire pour fermer une notification après un délai
  autoRemove(id: number, delay: number = 5000): void {
    setTimeout(() => {
      this.removeNotification(id);
    }, delay);
  }

  // Méthode pour mettre à jour une notification
  updateNotification(id: number, updates: Partial<Omit<Notification, 'id' | 'timestamp'>>): void {
    this.ngZone.run(() => {
      const currentNotifications = this.notificationsSubject.value;
      const updatedNotifications = currentNotifications.map(notification => {
        if (notification.id === id) {
          return { ...notification, ...updates };
        }
        return notification;
      });
      this.notificationsSubject.next(updatedNotifications);
    });
  }

  // Méthode pour compter les notifications par type
  getNotificationCounts(): { success: number, error: number, info: number, warning: number } {
    const notifications = this.notificationsSubject.value;
    return {
      success: notifications.filter(n => n.type === 'success').length,
      error: notifications.filter(n => n.type === 'error').length,
      info: notifications.filter(n => n.type === 'info').length,
      warning: notifications.filter(n => n.type === 'warning').length
    };
  }

  // Méthode pour obtenir les dernières notifications
  getRecentNotifications(limit: number = 5): Notification[] {
    const notifications = this.notificationsSubject.value;
    return notifications
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, limit);
  }

  // Méthode pour vérifier s'il y a des notifications d'erreur
  hasErrors(): boolean {
    return this.notificationsSubject.value.some(n => n.type === 'error');
  }

  // Méthode pour supprimer toutes les notifications d'un type spécifique
  clearByType(type: Notification['type']): void {
    this.ngZone.run(() => {
      const currentNotifications = this.notificationsSubject.value;
      const updatedNotifications = currentNotifications.filter(
        notification => notification.type !== type
      );
      this.notificationsSubject.next(updatedNotifications);
    });
  }
}