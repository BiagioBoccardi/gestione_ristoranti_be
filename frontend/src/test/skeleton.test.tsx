/// <reference types="vitest/globals" />
import { render, screen } from '@testing-library/react';
import { SkeletonCard, SkeletonRow } from '@/components/ui/skeleton';

describe('SkeletonCard', () => {
  it('renderizza un div con classe animate-pulse', () => {
    const { container } = render(<SkeletonCard />);
    const el = container.firstChild as HTMLElement;
    expect(el.className).toContain('animate-pulse');
    expect(el.className).toContain('rounded-xl');
  });

  it('applica className aggiuntiva passata via props', () => {
    const { container } = render(<SkeletonCard className="h-28 bg-stone-100" />);
    const el = container.firstChild as HTMLElement;
    expect(el.className).toContain('h-28');
    expect(el.className).toContain('bg-stone-100');
  });
});

describe('SkeletonRow', () => {
  it('renderizza il numero corretto di celle', () => {
    const { container } = render(
      <table><tbody><SkeletonRow cols={5} /></tbody></table>
    );
    const cells = container.querySelectorAll('td');
    expect(cells).toHaveLength(5);
  });

  it('ogni cella contiene un div animate-pulse', () => {
    const { container } = render(
      <table><tbody><SkeletonRow cols={3} /></tbody></table>
    );
    const pulseEls = container.querySelectorAll('.animate-pulse');
    expect(pulseEls).toHaveLength(3);
  });

  it('applica cellClass personalizzato alle celle', () => {
    const { container } = render(
      <table><tbody><SkeletonRow cols={2} cellClass="custom-cell" /></tbody></table>
    );
    const cells = container.querySelectorAll('td');
    cells.forEach(cell => {
      expect(cell.className).toContain('custom-cell');
    });
  });
});
