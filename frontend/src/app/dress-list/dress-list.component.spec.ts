import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { authInterceptor } from '../core/auth.interceptor';
import { DressListComponent } from './dress-list.component';

describe('DressListComponent', () => {
  let http: HttpTestingController;

  function render() {
    const fixture = TestBed.createComponent(DressListComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [DressListComponent],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('shows the login form when the user is not logged in', () => {
    const fixture = render();
    expect(fixture.nativeElement.querySelector('app-login')).toBeTruthy();
  });

  it('loads the catalog and orders for a logged-in user', () => {
    localStorage.setItem('token', 'jwt-token');
    const fixture = render();

    http.expectOne(`${environment.apiUrl}/haljine`).flush([
      { _id: 'd1', naziv: 'Lana', slika: null, cena: 4500, lager: [{ boja: 'Crvena', kolicina: 3 }] },
    ]);
    http.expectOne(`${environment.apiUrl}/narudzbine`).flush([]);
    fixture.detectChanges();

    const text: string = fixture.nativeElement.textContent;
    expect(text).toContain('Lana');
    expect(text).toContain('Crvena');
  });
});
