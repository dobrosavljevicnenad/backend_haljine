import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Order, OrderGroup } from '../core/models';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe],
  templateUrl: './order-history.component.html',
})
export class OrderHistoryComponent {
  @Input({ required: true }) orders: Order[] = [];
  @Output() readonly deleteGroup = new EventEmitter<OrderGroup>();
  @Output() readonly deleteOrder = new EventEmitter<Order>();

  from = '';
  to = '';

  get groups(): OrderGroup[] {
    return groupOrders(this.orders, this.from, this.to);
  }

  resetFilter(): void {
    this.from = '';
    this.to = '';
  }

  total(group: OrderGroup): number {
    return group.proizvodi.reduce((sum, order) => sum + order.cena * order.kolicina, 0);
  }
}

/**
 * Groups order lines that were placed in one checkout (same payment screenshot, same minute)
 * and applies the date filter. Both dates are interpreted as local days and "to" is inclusive.
 */
export function groupOrders(orders: Order[], from: string, to: string): OrderGroup[] {
  const start = from ? new Date(`${from}T00:00:00`) : null;
  const end = to ? new Date(`${to}T23:59:59.999`) : null;
  const groups = new Map<string, OrderGroup>();

  for (const order of orders) {
    const placedAt = new Date(order.vreme);
    if ((start && placedAt < start) || (end && placedAt > end)) {
      continue;
    }
    const key = `${order.screenshot ?? ''}|${placedAt.toISOString().slice(0, 16)}`;
    let group = groups.get(key);
    if (!group) {
      group = { screenshot: order.screenshot, vreme: order.vreme, proizvodi: [] };
      groups.set(key, group);
    }
    group.proizvodi.push(order);
  }
  return [...groups.values()];
}
