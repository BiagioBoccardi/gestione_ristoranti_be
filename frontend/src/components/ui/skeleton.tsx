interface SkeletonCardProps {
  className?: string;
}

export function SkeletonCard({ className = '' }: SkeletonCardProps) {
  return <div className={`rounded-xl animate-pulse ${className}`} />;
}

export function SkeletonRow({ cols, cellClass = 'px-5 py-4' }: { cols: number; cellClass?: string }) {
  return (
    <tr>
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className={cellClass}>
          <div className="h-4 bg-zinc-800 rounded animate-pulse" />
        </td>
      ))}
    </tr>
  );
}
