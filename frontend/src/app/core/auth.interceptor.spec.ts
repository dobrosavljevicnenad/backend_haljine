import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let client: HttpClient;
  let http: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.setItem('token', 'jwt-token');
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('adds the bearer token to API calls', () => {
    client.get(`${environment.apiUrl}/haljine`).subscribe();

    const request = http.expectOne(`${environment.apiUrl}/haljine`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('never sends the token to other hosts', () => {
    client.get('https://res.cloudinary.com/demo/image/upload/haljine/a.png').subscribe();

    const request = http.expectOne('https://res.cloudinary.com/demo/image/upload/haljine/a.png');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush('');
  });

  it('logs the user out when the API rejects the token', () => {
    client.get(`${environment.apiUrl}/haljine`).subscribe({ error: () => undefined });

    http.expectOne(`${environment.apiUrl}/haljine`)
      .flush({ error: 'Token nije važeći' }, { status: 401, statusText: 'Unauthorized' });

    expect(auth.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('token')).toBeNull();
  });
});
