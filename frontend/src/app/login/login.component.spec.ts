import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('stores the token and notifies the parent after a successful login', () => {
    const component = fixture.componentInstance;
    let loggedIn = false;
    component.loggedIn.subscribe(() => (loggedIn = true));

    component.username = 'admin';
    component.password = 'secret';
    component.submit();
    http.expectOne(`${environment.apiUrl}/login`).flush({ token: 'jwt-token' });

    expect(loggedIn).toBeTrue();
    expect(localStorage.getItem('token')).toBe('jwt-token');
  });

  it('shows an error message for wrong credentials', () => {
    const component = fixture.componentInstance;
    component.username = 'admin';
    component.password = 'wrong';
    component.submit();
    http.expectOne(`${environment.apiUrl}/login`)
      .flush({ error: 'Neispravno' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Neispravno');
  });

  it('validates empty input without calling the API', () => {
    fixture.componentInstance.submit();
    expect(fixture.componentInstance.error).toContain('Unesi');
  });
});
