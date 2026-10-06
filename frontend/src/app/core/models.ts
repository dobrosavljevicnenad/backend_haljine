/**
 * API contract of the inventory backend. Field names follow the JSON of the API
 * (kept compatible with the original Node.js service).
 */
export interface StockItem {
  _id?: string;
  boja: string;
  kolicina: number;
}

export interface Dress {
  _id: string;
  naziv: string;
  slika: string | null;
  cena: number;
  lager: StockItem[];
}

export interface NewDress {
  naziv: string;
  slika: string;
  cena: number;
  lager: StockItem[];
}

export interface Order {
  _id: string;
  haljinaId: string | null;
  naziv: string;
  slika: string | null;
  cena: number;
  boja: string;
  kolicina: number;
  screenshot: string | null;
  vreme: string;
}

export interface CartItem {
  _id: string;
  naziv: string;
  slika: string | null;
  cena: number;
  boja: string;
  kolicina: number;
}

/** Order lines placed together in one checkout (same payment screenshot and time). */
export interface OrderGroup {
  screenshot: string | null;
  vreme: string;
  proizvodi: Order[];
}

export interface MessageResponse {
  message: string;
}
