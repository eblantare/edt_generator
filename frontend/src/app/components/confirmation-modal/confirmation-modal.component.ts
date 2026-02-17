import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ConfirmationModalComponent {
  @Input() title: string = 'Confirmation';
  @Input() message: string = 'Êtes-vous sûr de vouloir effectuer cette action ?';
  @Input() confirmButtonText: string = 'Oui';
  @Input() cancelButtonText: string = 'Non';
  @Input() confirmButtonClass: string = 'btn-danger';
  @Input() showModal: boolean = false;
  
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();

  onConfirm(): void {
    this.confirmed.emit();
    this.closeModal();
  }

  onCancel(): void {
    this.cancelled.emit();
    this.closeModal();
  }

  closeModal(): void {
    this.showModal = false;
    this.modalClosed.emit();
  }
}