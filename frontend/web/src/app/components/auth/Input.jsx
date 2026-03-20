import { forwardRef, useState } from "react";
import { Eye, EyeOff, CheckCircle, AlertCircle } from "lucide-react";

export const Input = forwardRef(
  ({ label, error, success, showPasswordToggle, type, className = "", ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false);
    const inputType = showPasswordToggle && showPassword ? "text" : type;

    return (
      <div className="space-y-1.5">
        {label && (
          <label className="block text-sm font-medium text-foreground">
            {label}
          </label>
        )}
        <div className="relative">
          <input
            ref={ref}
            type={inputType}
            className={`
              w-full h-11 px-4 rounded-xl
              bg-[var(--color-input)] border-2 
              text-sm text-foreground placeholder:text-[var(--color-muted-foreground)]
              transition-all duration-200 ease-out
              ${error 
                ? "border-[var(--color-error)] focus:border-[var(--color-error)] focus:ring-4 focus:ring-[var(--color-error)]/10" 
                : success
                ? "border-[var(--color-success)] focus:border-[var(--color-success)] focus:ring-4 focus:ring-[var(--color-success)]/10"
                : "border-[var(--color-input-border)] focus:border-[var(--color-primary)] focus:ring-4 focus:ring-[var(--color-primary)]/10"
              }
              focus:outline-none
              disabled:bg-[var(--color-muted)] disabled:cursor-not-allowed disabled:opacity-60
              ${showPasswordToggle || success || error ? "pr-11" : ""}
              ${className}
            `}
            {...props}
          />
          
          {/* Status Icons */}
          {success && !error && (
            <CheckCircle className="absolute right-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-[var(--color-success)]" />
          )}
          
          {error && (
            <AlertCircle className="absolute right-3.5 top-1/2 -translate-y-1/2 h-5 w-5 text-[var(--color-error)]" />
          )}
          
          {/* Password Toggle */}
          {showPasswordToggle && !error && !success && (
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[var(--color-muted-foreground)] hover:text-foreground transition-colors"
            >
              {showPassword ? (
                <EyeOff className="h-5 w-5" />
              ) : (
                <Eye className="h-5 w-5" />
              )}
            </button>
          )}
        </div>
        
        {/* Error Message */}
        {error && (
          <p className="text-xs text-[var(--color-error)] flex items-center gap-1">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";
