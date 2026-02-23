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

  // 🔴 VERSION CORRIGÉE: Plus de setTimeout dans addNotification
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
      
      // 🔴 SUPPRIMÉ: Le setTimeout est retiré d'ici
      // La fermeture automatique sera gérée par le composant
    });
    
    return newId;
  }

  // Méthode pour supprimer une notification
  removeNotification(id: number): void {
    this.ngZone.run(() => {
      const currentNotifications = this.notificationsSubject.value;
      const updatedNotifications = currentNotifications.filter(
        notification => notification.id !== id
      );
      this.notificationsSubject.next(updatedNotifications);
    });
  }

  // 🔴 NOUVELLE MÉTHODE: Supprimer une notification après un délai
  autoRemove(id: number, delay: number = 5000): void {
    setTimeout(() => {
      this.removeNotification(id);
    }, delay);
  }

  // Méthodes principales - SANS autoClose
  success(title: string, message: string): number {
    return this.addNotification({
      type: 'success',
      title,
      message,
      autoClose: true
    });
  }

  error(title: string, message: string): number {
    return this.addNotification({
      type: 'error',
      title,
      message,
      autoClose: false
    });
  }

  info(title: string, message: string): number {
    return this.addNotification({
      type: 'info',
      title,
      message,
      autoClose: true
    });
  }

  warning(title: string, message: string): number {
    return this.addNotification({
      type: 'warning',
      title,
      message,
      autoClose: true
    });
  }

  // Méthodes de compatibilité (alias)
  showSuccess(message: string, title: string = 'Succès'): number {
    return this.success(title, message);
  }

  showError(message: string, title: string = 'Erreur'): number {
    return this.error(title, message);
  }

  showInfo(message: string, title: string = 'Information'): number {
    return this.info(title, message);
  }

  showWarning(message: string, title: string = 'Attention'): number {
    return this.warning(title, message);
  }

  // Méthode pour vider toutes les notifications
  clearAll(): void {
    this.ngZone.run(() => {
      this.notificationsSubject.next([]);
    });
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
    return [...notifications]
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