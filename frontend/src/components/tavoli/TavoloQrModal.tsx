import { useEffect, useRef, useState } from 'react';
import { RefreshCw, X, Copy, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { qrService } from '@/services/qrService';
import type { QrCodeInfo } from '@/types/qr';

interface TavoloQrModalProps {
  tavoloId: number;
  numeroTavolo: number;
  onClose: () => void;
}

export default function TavoloQrModal({ tavoloId, numeroTavolo, onClose }: TavoloQrModalProps) {
  const [info, setInfo] = useState<QrCodeInfo | null>(null);
  const [imgSrc, setImgSrc] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [regenerating, setRegenerating] = useState(false);
  const [copied, setCopied] = useState(false);
  const prevBlobUrl = useRef<string | null>(null);

  useEffect(() => {
    load();
    return () => revokePrev();
  }, [tavoloId]);

  function revokePrev() {
    if (prevBlobUrl.current) {
      URL.revokeObjectURL(prevBlobUrl.current);
      prevBlobUrl.current = null;
    }
  }

  async function load() {
    setLoading(true);
    try {
      const [data, blobUrl] = await Promise.all([
        qrService.getQrInfo(tavoloId),
        qrService.getQrImageBlob(tavoloId),
      ]);
      revokePrev();
      prevBlobUrl.current = blobUrl;
      setInfo(data);
      setImgSrc(blobUrl);
    } finally {
      setLoading(false);
    }
  }

  async function handleRigenera() {
    setRegenerating(true);
    try {
      const [data, blobUrl] = await Promise.all([
        qrService.rigeneraQr(tavoloId),
        qrService.getQrImageBlob(tavoloId),
      ]);
      revokePrev();
      prevBlobUrl.current = blobUrl;
      setInfo(data);
      setImgSrc(blobUrl);
    } finally {
      setRegenerating(false);
    }
  }

  function handleCopy() {
    if (!info?.menuUrl) return;
    navigator.clipboard.writeText(info.menuUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 p-6 flex flex-col gap-4"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-stone-800">QR Code — Tavolo {numeroTavolo}</h2>
          <button onClick={onClose} className="text-stone-400 hover:text-stone-700 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex justify-center">
          {loading ? (
            <div className="w-48 h-48 bg-stone-100 rounded-xl animate-pulse" />
          ) : imgSrc ? (
            <img
              src={imgSrc}
              alt={`QR Tavolo ${numeroTavolo}`}
              className="w-48 h-48 rounded-xl border border-stone-200"
              style={{ imageRendering: 'pixelated' }}
            />
          ) : (
            <div className="w-48 h-48 bg-stone-100 rounded-xl flex items-center justify-center text-xs text-stone-400">
              Errore caricamento
            </div>
          )}
        </div>

        {info?.menuUrl && (
          <div className="flex items-center gap-2 bg-stone-50 rounded-lg px-3 py-2 text-xs text-stone-600">
            <span className="flex-1 truncate">{info.menuUrl}</span>
            <button onClick={handleCopy} className="shrink-0 text-stone-400 hover:text-stone-700">
              <Copy className="w-4 h-4" />
            </button>
            {copied && <span className="text-green-600 shrink-0">Copiato!</span>}
          </div>
        )}

        {info?.menuUrl && (
          <a
            href={info.menuUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center justify-center gap-2 text-sm text-stone-500 hover:text-stone-800 transition-colors"
          >
            <ExternalLink className="w-4 h-4" />
            Apri anteprima menu
          </a>
        )}

        <div className="flex gap-3 pt-2">
          <Button
            variant="outline"
            className="flex-1"
            onClick={handleRigenera}
            disabled={regenerating || loading}
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${regenerating ? 'animate-spin' : ''}`} />
            {regenerating ? 'Rigenera...' : 'Rigenera QR'}
          </Button>
          <Button variant="outline" className="flex-1" onClick={onClose}>
            Chiudi
          </Button>
        </div>
      </div>
    </div>
  );
}
