import { Order } from '../core/models';
import { groupOrders } from './order-history.component';

function order(id: string, vreme: string, screenshot: string | null): Order {
  return { _id: id, haljinaId: 'd1', naziv: 'Lana', slika: null, cena: 4500, boja: 'Crvena', kolicina: 1, screenshot, vreme };
}

describe('groupOrders', () => {
  it('groups the lines of one checkout', () => {
    const groups = groupOrders([
      order('1', '2026-10-10T10:15:05Z', 'https://img.example.com/a.png'),
      order('2', '2026-10-10T10:15:06Z', 'https://img.example.com/a.png'),
      order('3', '2026-10-10T11:00:00Z', 'https://img.example.com/b.png'),
    ], '', '');

    expect(groups.length).toBe(2);
    expect(groups[0].proizvodi.map(o => o._id)).toEqual(['1', '2']);
  });

  it('treats the "to" date as an inclusive local day', () => {
    const lateEvening = new Date(2026, 9, 10, 23, 30).toISOString();
    expect(groupOrders([order('1', lateEvening, null)], '2026-10-10', '2026-10-10').length).toBe(1);
  });

  it('excludes orders placed before the "from" date', () => {
    const dayBefore = new Date(2026, 9, 9, 23, 59).toISOString();
    expect(groupOrders([order('1', dayBefore, null)], '2026-10-10', '').length).toBe(0);
  });
});
