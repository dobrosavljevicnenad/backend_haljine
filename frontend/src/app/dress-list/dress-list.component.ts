import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/auth.service';
import { InventoryApiService } from '../core/inventory-api.service';
import { CartItem, Dress, NewDress, Order } from '../core/models';

@Component({
  selector: 'app-dress-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dress-list.component.html',
  styleUrls: ['./dress-list.component.scss'],
})
export class DressListComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(InventoryApiService);

  haljine: Dress[] = [];
  narudzbine: Order[] = [];
  ucitavanje = false;
  filterBoja = '';
  filterName = '';
  screenshotUploadInProgress = false;
  uploadInProgress = false;
  korpa: CartItem[] = [];
  datumOd = '';
  datumDo = '';

  novaHaljina: NewDress = emptyDress();

  izabranaHaljina: Dress | null = null;
  izabranaBoja = '';
  formaKupca = { screenshot: '' };

  novaBoja: Record<string, string> = {};
  novaKolicina: Record<string, number> = {};

  username = '';
  password = '';

  get token(): string | null {
    return this.auth.token();
  }

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.ucitaj();
      this.ucitajNarudzbine();
    }
  }

  login(): void {
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.username = '';
        this.password = '';
        this.ucitaj();
        this.ucitajNarudzbine();
      },
      error: () => alert('❌ Neispravni podaci'),
    });
  }

  logout(): void {
    this.auth.logout();
    this.haljine = [];
    this.narudzbine = [];
    this.korpa = [];
    this.ucitavanje = false;
  }

  resetujFilter(): void {
    this.datumOd = '';
    this.datumDo = '';
  }

  ucitaj(): void {
    this.ucitavanje = true;
    this.api.getDresses().subscribe({
      next: data => (this.haljine = data),
      error: err => {
        console.error('Greška pri učitavanju haljina:', err);
        this.haljine = [];
        this.ucitavanje = false;
      },
      complete: () => (this.ucitavanje = false),
    });
  }

  ucitajNarudzbine(): void {
    this.api.getOrders().subscribe({
      next: data => (this.narudzbine = data),
      error: err => console.error('Greška pri učitavanju narudžbina:', err),
    });
  }

  dodajHaljinu(): void {
    this.api.createDress(this.novaHaljina).subscribe(() => {
      this.ucitaj();
      this.novaHaljina = emptyDress();
    });
  }

  dodajUKorpu(haljina: Dress, boja: string): void {
    const postojeca = this.korpa.find(item => item._id === haljina._id && item.boja === boja);
    if (postojeca) {
      postojeca.kolicina += 1;
    } else {
      this.korpa.push({ _id: haljina._id, naziv: haljina.naziv, slika: haljina.slika, cena: haljina.cena, boja, kolicina: 1 });
    }
  }

  ukloniIzKorpe(index: number): void {
    this.korpa.splice(index, 1);
  }

  posaljiKorpu(): void {
    if (this.korpa.length === 0) {
      alert('Korpa je prazna.');
      return;
    }
    this.api.placeCartOrder(this.korpa, this.formaKupca.screenshot).subscribe({
      next: () => {
        alert('✅ Uspešno ste poručili više haljina!');
        this.korpa = [];
        this.formaKupca = { screenshot: '' };
        this.screenshotUploadInProgress = false;
        this.ucitaj();
        this.ucitajNarudzbine();
      },
      error: () => alert('❌ Greška pri slanju porudžbine iz korpe.'),
    });
  }

  dodajBojuPolje(): void {
    this.novaHaljina.lager.push({ boja: '', kolicina: 0 });
  }

  uploadSlika(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploadInProgress = true;
    this.api.uploadImage(file).subscribe({
      next: res => {
        this.novaHaljina.slika = res.url;
        this.uploadInProgress = false;
      },
      error: () => (this.uploadInProgress = false),
    });
  }

  dodajIliAzurirajBoju(id: string, boja: string, kolicina: number): void {
    if (!boja || !kolicina) return;
    this.api.addStock(id, boja, kolicina).subscribe(() => {
      const haljina = this.haljine.find(h => h._id === id);
      if (!haljina) return;
      const postojeca = haljina.lager.find(l => l.boja === boja);
      if (postojeca) {
        postojeca.kolicina += kolicina;
      } else {
        haljina.lager.push({ boja, kolicina });
      }
      this.novaBoja[id] = '';
      this.novaKolicina[id] = 0;
    });
  }

  obrisiHaljinu(id: string): void {
    if (!confirm('Da li si siguran da želiš da obrišeš ovu haljinu?')) return;
    this.api.deleteDress(id).subscribe(() => {
      this.haljine = this.haljine.filter(h => h._id !== id);
    });
  }

  otvoriFormuZaNarudzbinu(haljina: Dress, boja: string): void {
    this.izabranaHaljina = haljina;
    this.izabranaBoja = boja;
    this.formaKupca = { screenshot: '' };
  }

  posaljiNarudzbinu(): void {
    const haljina = this.izabranaHaljina;
    if (!haljina) return;
    this.api.placeOrder(haljina._id, this.izabranaBoja, 1, this.formaKupca.screenshot).subscribe({
      next: () => {
        this.izabranaHaljina = null;
        this.ucitaj();
        this.ucitajNarudzbine();
      },
      error: () => alert('❌ Greška pri naručivanju.'),
    });
  }

  uploadScreenshot(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.screenshotUploadInProgress = true;
    this.api.uploadImage(file).subscribe({
      next: res => {
        this.formaKupca.screenshot = res.url;
        this.screenshotUploadInProgress = false;
      },
      error: () => {
        alert('Greška pri uploadu slike.');
        this.screenshotUploadInProgress = false;
      },
    });
  }

  get filteredHaljine(): Dress[] {
    const boja = this.filterBoja.trim().toLowerCase();
    if (!boja) return this.haljine;
    return this.haljine.filter(h => h.lager.some(l => l.boja.toLowerCase().includes(boja)));
  }

  get filterByName(): Dress[] {
    const name = this.filterName.trim().toLowerCase();
    if (!name) return this.filteredHaljine;
    return this.filteredHaljine.filter(h => h.naziv.toLowerCase().includes(name));
  }

  get grupisaneNarudzbine(): { kupac: { screenshot: string | null; vreme: string }; proizvodi: Order[] }[] {
    const grupe: Record<string, { kupac: { screenshot: string | null; vreme: string }; proizvodi: Order[] }> = {};
    const od = this.datumOd ? new Date(this.datumOd) : null;
    const doDatuma = this.datumDo ? new Date(this.datumDo) : null;

    for (const n of this.narudzbine) {
      const vreme = new Date(n.vreme);
      if (od && vreme < od) continue;
      if (doDatuma && vreme > doDatuma) continue;

      const key = `${n.screenshot}|${vreme.toISOString().slice(0, 16)}`;
      grupe[key] ??= { kupac: { screenshot: n.screenshot, vreme: n.vreme }, proizvodi: [] };
      grupe[key].proizvodi.push(n);
    }
    return Object.values(grupe);
  }

  obrisiGrupuNarudzbina(screenshot: string | null): void {
    if (!screenshot || !confirm('Da li si siguran da želiš da obrišeš ovu porudžbinu?')) return;
    this.api.deleteOrderGroup(screenshot).subscribe({
      next: () => this.ucitajNarudzbine(),
      error: () => alert('❌ Nije obrisana grupa porudžbina.'),
    });
  }

  obrisiBoju(haljinaId: string, boja: string): void {
    if (!confirm(`Da li želiš da obrišeš boju "${boja}"?`)) return;
    this.api.removeColor(haljinaId, boja).subscribe({
      next: () => {
        const haljina = this.haljine.find(h => h._id === haljinaId);
        if (haljina) {
          haljina.lager = haljina.lager.filter(b => b.boja !== boja);
        }
      },
      error: () => alert('❌ Greška pri brisanju boje.'),
    });
  }
}

function emptyDress(): NewDress {
  return { naziv: '', slika: '', cena: 0, lager: [{ boja: '', kolicina: 0 }] };
}
