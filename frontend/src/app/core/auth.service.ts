import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';

const TOKEN_KEY = 'token';

/**
 * Holds the JWT issued by the backend. The token lives in a signal (for the UI)
 * and in localStorage (to survive page reloads); storage is only touched in the browser,
 * never during server-side rendering.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly tokenState = signal<string | null>(this.readStoredToken());

  readonly token = this.tokenState.asReadonly();
  readonly isLoggedIn = computed(() => this.tokenState() !== null);

  login(username: string, password: string): Observable<void> {
    return this.http
      .post<{ token: string }>(`${environment.apiUrl}/login`, { username, password })
      .pipe(
        tap(({ token }) => this.storeToken(token)),
        map(() => undefined),
      );
  }

  logout(): void {
    this.tokenState.set(null);
    if (this.isBrowser) {
      localStorage.removeItem(TOKEN_KEY);
    }
  }

  private storeToken(token: string): void {
    this.tokenState.set(token);
    if (this.isBrowser) {
      localStorage.setItem(TOKEN_KEY, token);
    }
  }

  private readStoredToken(): string | null {
    return this.isBrowser ? localStorage.getItem(TOKEN_KEY) : null;
  }
}
