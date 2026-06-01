import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset",
  {
    variants: {
      variant: {
        default:     "bg-stone-100 text-stone-700 ring-stone-200",
        success:     "bg-green-50 text-green-700 ring-green-200",
        destructive: "bg-red-50 text-red-600 ring-red-200",
        secondary:   "bg-stone-50 text-stone-500 ring-stone-200",
        warning:     "bg-amber-50 text-amber-700 ring-amber-200",
      },
    },
    defaultVariants: { variant: "default" },
  }
)

function Badge({
  className,
  variant,
  ...props
}: React.ComponentProps<"span"> & VariantProps<typeof badgeVariants>) {
  return (
    <span
      data-slot="badge"
      className={cn(badgeVariants({ variant }), className)}
      {...props}
    />
  )
}

export { Badge, badgeVariants }
