import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);

  @Output() readonly loggedIn = new EventEmitter<void>();

  username = '';
  password = '';
  submitting = false;
  error = '';

  submit(): void {
    if (!this.username.trim() || !this.password) {
      this.error = 'Unesi korisničko ime i lozinku.';
      return;
    }
    this.submitting = true;
    this.error = '';
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.submitting = false;
        this.password = '';
        this.loggedIn.emit();
      },
      error: () => {
        this.submitting = false;
        this.error = 'Neispravno korisničko ime ili lozinka.';
      },
    });
  }
}
