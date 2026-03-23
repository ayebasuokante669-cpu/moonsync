import { motion } from "motion/react";

export function AuthButton({ 
  variant = "primary", 
  icon: Icon, 
  loading,
  children, 
  className = "",
  disabled,
  ...props 
}) {
  const variants = {
    primary: "bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] shadow-soft hover:shadow-soft-lg",
    secondary: "bg-white border-2 border-[var(--color-border)] text-foreground hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-light)]",
    ghost: "bg-transparent text-[var(--color-muted-foreground)] hover:text-foreground",
  };

  return (
    <motion.button
      whileHover={{ scale: disabled || loading ? 1 : 1.01 }}
      whileTap={{ scale: disabled || loading ? 1 : 0.98 }}
      transition={{ duration: 0.15 }}
      className={`
        relative h-11 px-6 rounded-xl font-medium text-sm
        transition-all duration-200 ease-out
        disabled:opacity-50 disabled:cursor-not-allowed
        flex items-center justify-center gap-2
        ${variants[variant]}
        ${className}
      `}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <>
          <motion.div
            className="h-4 w-4 border-2 border-current border-t-transparent rounded-full"
            animate={{ rotate: 360 }}
            transition={{ duration: 0.8, repeat: Infinity, ease: "linear" }}
          />
          <span>Loading...</span>
        </>
      ) : (
        <>
          {Icon && <Icon className="h-4 w-4" />}
          {children}
        </>
      )}
    </motion.button>
  );
}
