import { Dialog } from "@base-ui/react/dialog"
import * as React from "react"

import { cn } from "@/lib/utils"

function DialogContent({
  className,
  children,
}: {
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <Dialog.Portal>
      <Dialog.Backdrop className="fixed inset-0 bg-black/40 z-40" />
      <Dialog.Popup
        className={cn(
          "fixed left-1/2 top-1/2 z-50 -translate-x-1/2 -translate-y-1/2 w-full max-w-lg bg-white rounded-xl shadow-lg p-6 outline-none max-h-[90vh] overflow-y-auto",
          className
        )}
      >
        {children}
      </Dialog.Popup>
    </Dialog.Portal>
  )
}

function DialogHeader({ className, ...props }: React.ComponentProps<"div">) {
  return <div className={cn("mb-5 space-y-1", className)} {...props} />
}

function DialogTitle({ className, ...props }: React.ComponentProps<"h2">) {
  return (
    <h2
      className={cn("text-lg font-medium text-stone-800", className)}
      {...props}
    />
  )
}

function DialogFooter({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("mt-6 flex justify-end gap-2 border-t border-stone-100 pt-4", className)}
      {...props}
    />
  )
}

export { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter }
