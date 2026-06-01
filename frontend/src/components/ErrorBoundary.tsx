import { Component, type ReactNode } from 'react';
import { UtensilsCrossed } from 'lucide-react';

interface Props  { children: ReactNode; }
interface State  { hasError: boolean; }

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  render() {
    if (!this.state.hasError) return this.props.children;

    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-6 bg-zinc-950 text-zinc-100 px-4">
        <div className="w-12 h-12 rounded-xl bg-red-600/20 flex items-center justify-center">
          <UtensilsCrossed className="w-6 h-6 text-red-400" />
        </div>
        <div className="text-center">
          <h1 className="text-xl font-semibold mb-2">Qualcosa è andato storto</h1>
          <p className="text-sm text-zinc-500">Si è verificato un errore imprevisto nell'applicazione.</p>
        </div>
        <button
          onClick={() => window.location.reload()}
          className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-sm font-medium transition-colors"
        >
          Ricarica la pagina
        </button>
      </div>
    );
  }
}
