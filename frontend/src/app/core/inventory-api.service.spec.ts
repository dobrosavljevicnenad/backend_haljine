import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';
import { InventoryApiService } from './inventory-api.service';

describe('InventoryApiService', () => {
  let api: InventoryApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(InventoryApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only ids, colors and quantities when checking out the cart', () => {
    api.placeCartOrder(
      [{ _id: 'd1', naziv: 'Lana', slika: null, cena: 1, boja: 'Crvena', kolicina: 2 }],
      'https://img.example.com/uplata.png',
    ).subscribe();

    const request = http.expectOne(`${environment.apiUrl}/naruci-vise`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      proizvodi: [{ _id: 'd1', boja: 'Crvena', kolicina: 2 }],
      kupac: { screenshot: 'https://img.example.com/uplata.png' },
    });
    request.flush({ message: 'OK' });
  });

  it('passes the screenshot URL as a query parameter when deleting a checkout', () => {
    api.deleteOrderGroup('https://img.example.com/uplata.png').subscribe();

    const request = http.expectOne(r => r.url === `${environment.apiUrl}/narudzbine/grupa`);
    expect(request.request.method).toBe('DELETE');
    expect(request.request.params.get('screenshot')).toBe('https://img.example.com/uplata.png');
    request.flush({ message: 'OK' });
  });

  it('uploads images in the multipart field "slika"', () => {
    api.uploadImage(new File(['x'], 'dress.png', { type: 'image/png' }))
      .subscribe(response => expect(response.url).toBe('https://img.example.com/new.png'));

    const request = http.expectOne(`${environment.apiUrl}/upload`);
    expect((request.request.body as FormData).get('slika')).toBeTruthy();
    request.flush({ url: 'https://img.example.com/new.png' });
  });
});
