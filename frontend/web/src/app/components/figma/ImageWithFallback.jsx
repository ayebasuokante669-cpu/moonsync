import { forwardRef } from "react";

export const ImageWithFallback = forwardRef(({ src, alt, fallbackSrc, className, ...props }, ref) => {
  const handleError = (e) => {
    if (fallbackSrc) {
      e.target.src = fallbackSrc;
    }
  };

  return (
    <img
      ref={ref}
      src={src}
      alt={alt}
      onError={handleError}
      className={className}
      {...props}
    />
  );
});

ImageWithFallback.displayName = "ImageWithFallback";
