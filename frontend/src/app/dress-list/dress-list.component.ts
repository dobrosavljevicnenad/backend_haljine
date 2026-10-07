import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { InventoryApiService } from '../core/inventory-api.service';
import { CartItem, Dress, NewDress, Order, OrderGroup } from '../core/models';
import { LoginComponent } from '../login/login.component';
import { OrderHistoryComponent } from '../orders/order-history.component';

/**
 * Admin dashboard: catalog management, stock per color, cart checkout and order history.
 */
@Component({
  selector: 'app-dress-list',
  standalone: true,
  imports: [FormsModule, LoginComponent, OrderHistoryComponent],
  templateUrl: './dress-list.component.html',
  styleUrls: ['./dress-list.component.scss'],
})
export class DressListComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(InventoryApiService);

  dresses: Dress[] = [];
  orders: Order[] = [];
  loading = false;

  colorFilter = '';
  nameFilter = '';

  newDress: NewDress = emptyDress();
  imageUploading = false;

  restockColor: Record<string, string> = {};
  restockQuantity: Record<string, number> = {};

  cart: CartItem[] = [];
  selectedDress: Dress | null = null;
  selectedColor = '';
  screenshotUrl = '';
  screenshotUploading = false;

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.reload();
    }
  }

  onLoggedIn(): void {
    this.reload();
  }

  logout(): void {
    this.auth.logout();
    this.dresses = [];
    this.orders = [];
    this.cart = [];
  }

  get visibleDresses(): Dress[] {
    const color = this.colorFilter.trim().toLowerCase();
    const name = this.nameFilter.trim().toLowerCase();
    return this.dresses.filter(dress =>
      (!color || dress.lager.some(stock => stock.boja.toLowerCase().includes(color))) &&
      (!name || dress.naziv.toLowerCase().includes(name)));
  }

  get cartTotal(): number {
    return this.cart.reduce((sum, item) => sum + item.cena * item.kolicina, 0);
  }

  // ---- catalog -------------------------------------------------------------

  addStockRow(): void {
    this.newDress.lager.push({ boja: '', kolicina: 0 });
  }

  uploadDressImage(event: Event): void {
    const file = selectedFile(event);
    if (!file) return;
    this.imageUploading = true;
    this.api.uploadImage(file).subscribe({
      next: ({ url }) => {
        this.newDress.slika = url;
        this.imageUploading = false;
      },
      error: error => {
        this.imageUploading = false;
        this.fail('Upload slike nije uspeo.')(error);
      },
    });
  }

  addDress(): void {
    this.api.createDress(this.newDress).subscribe({
      next: () => {
        this.newDress = emptyDress();
        this.loadDresses();
      },
      error: this.fail('Haljina nije dodata.'),
    });
  }

  restock(dress: Dress): void {
    const color = (this.restockColor[dress._id] ?? '').trim();
    const quantity = Number(this.restockQuantity[dress._id] ?? 0);
    if (!color || quantity <= 0) return;
    this.api.addStock(dress._id, color, quantity).subscribe({
      next: () => {
        this.restockColor[dress._id] = '';
        this.restockQuantity[dress._id] = 0;
        this.loadDresses();
      },
      error: this.fail('Količina nije ažurirana.'),
    });
  }

  removeColor(dress: Dress, color: string): void {
    if (!confirm(`Da li želiš da obrišeš boju "${color}"?`)) return;
    this.api.removeColor(dress._id, color).subscribe({
      next: () => (dress.lager = dress.lager.filter(stock => stock.boja !== color)),
      error: this.fail('Greška pri brisanju boje.'),
    });
  }

  deleteDress(dress: Dress): void {
    if (!confirm('Da li si siguran da želiš da obrišeš ovu haljinu?')) return;
    this.api.deleteDress(dress._id).subscribe({
      next: () => (this.dresses = this.dresses.filter(d => d._id !== dress._id)),
      error: this.fail('Haljina nije obrisana.'),
    });
  }

  // ---- ordering ------------------------------------------------------------

  addToCart(dress: Dress, color: string): void {
    const existing = this.cart.find(item => item._id === dress._id && item.boja === color);
    if (existing) {
      existing.kolicina += 1;
    } else {
      this.cart.push({ _id: dress._id, naziv: dress.naziv, slika: dress.slika, cena: dress.cena, boja: color, kolicina: 1 });
    }
  }

  removeFromCart(index: number): void {
    this.cart.splice(index, 1);
  }

  checkoutCart(): void {
    if (this.cart.length === 0) return;
    this.api.placeCartOrder(this.cart, this.screenshotUrl).subscribe({
      next: () => {
        alert('✅ Uspešno ste poručili više haljina!');
        this.cart = [];
        this.screenshotUrl = '';
        this.reload();
      },
      error: this.fail('Greška pri slanju porudžbine iz korpe.'),
    });
  }

  openOrderDialog(dress: Dress, color: string): void {
    this.selectedDress = dress;
    this.selectedColor = color;
    this.screenshotUrl = '';
  }

  closeOrderDialog(): void {
    this.selectedDress = null;
  }

  placeSingleOrder(): void {
    const dress = this.selectedDress;
    if (!dress) return;
    this.api.placeOrder(dress._id, this.selectedColor, 1, this.screenshotUrl).subscribe({
      next: () => {
        this.closeOrderDialog();
        this.reload();
      },
      error: this.fail('Greška pri naručivanju.'),
    });
  }

  uploadScreenshot(event: Event): void {
    const file = selectedFile(event);
    if (!file) return;
    this.screenshotUploading = true;
    this.api.uploadImage(file).subscribe({
      next: ({ url }) => {
        this.screenshotUrl = url;
        this.screenshotUploading = false;
      },
      error: error => {
        this.screenshotUploading = false;
        this.fail('Greška pri uploadu slike.')(error);
      },
    });
  }

  // ---- order history -------------------------------------------------------

  deleteOrderGroup(group: OrderGroup): void {
    if (!confirm('Da li si siguran da želiš da obrišeš ovu porudžbinu?')) return;
    const request: Observable<unknown> = group.screenshot
      ? this.api.deleteOrderGroup(group.screenshot)
      : forkJoin(group.proizvodi.map(order => this.api.deleteOrder(order._id)));
    request.subscribe({
      next: () => this.loadOrders(),
      error: this.fail('Porudžbina nije obrisana.'),
    });
  }

  deleteOrder(order: Order): void {
    if (!confirm(`Obrisati stavku "${order.naziv} (${order.boja})"?`)) return;
    this.api.deleteOrder(order._id).subscribe({
      next: () => (this.orders = this.orders.filter(o => o._id !== order._id)),
      error: this.fail('Stavka nije obrisana.'),
    });
  }

  // ---- helpers -------------------------------------------------------------

  private reload(): void {
    this.loadDresses();
    this.loadOrders();
  }

  private loadDresses(): void {
    this.loading = true;
    this.api.getDresses().subscribe({
      next: dresses => {
        this.dresses = dresses;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  private loadOrders(): void {
    this.api.getOrders().subscribe({ next: orders => (this.orders = orders) });
  }

  /** Shows the message returned by the API (e.g. "Nema dovoljno na lageru ...") or a fallback text. */
  private fail(fallback: string): (error: unknown) => void {
    return error => {
      const apiMessage = error instanceof HttpErrorResponse ? error.error?.error : null;
      alert(`❌ ${typeof apiMessage === 'string' ? apiMessage : fallback}`);
    };
  }
}

function emptyDress(): NewDress {
  return { naziv: '', slika: '', cena: 0, lager: [{ boja: '', kolicina: 0 }] };
}

function selectedFile(event: Event): File | undefined {
  return (event.target as HTMLInputElement).files?.[0];
}
