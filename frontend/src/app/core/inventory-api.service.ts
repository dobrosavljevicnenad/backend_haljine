import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CartItem, Dress, MessageResponse, NewDress, Order } from './models';

/**
 * Typed client for the inventory REST API. Components never build URLs themselves.
 */
@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getDresses(): Observable<Dress[]> {
    return this.http.get<Dress[]>(`${this.baseUrl}/haljine`);
  }

  createDress(dress: NewDress): Observable<MessageResponse & { _id: string }> {
    return this.http.post<MessageResponse & { _id: string }>(`${this.baseUrl}/haljine`, dress);
  }

  deleteDress(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/haljine/${id}`);
  }

  addStock(dressId: string, boja: string, kolicina: number): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/dodajKolicinu/${dressId}`, { boja, kolicina });
  }

  removeColor(dressId: string, boja: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/obrisi-boju/${dressId}`, { boja });
  }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/narudzbine`);
  }

  placeOrder(dressId: string, boja: string, kolicina: number, screenshot: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/naruci/${dressId}`, { boja, kolicina, screenshot });
  }

  /** Sends only ids, colors and quantities - prices are always taken from the catalog on the server. */
  placeCartOrder(items: CartItem[], screenshot: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/naruci-vise`, {
      proizvodi: items.map(({ _id, boja, kolicina }) => ({ _id, boja, kolicina })),
      kupac: { screenshot },
    });
  }

  deleteOrder(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/narudzbine/${id}`);
  }

  deleteOrderGroup(screenshot: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/narudzbine/grupa`, { params: { screenshot } });
  }

  uploadImage(file: File): Observable<{ url: string }> {
    const form = new FormData();
    form.append('slika', file);
    return this.http.post<{ url: string }>(`${this.baseUrl}/upload`, form);
  }
}
