import { motion } from "motion/react";

export function AnimatedBackground({ variant }) {
  return (
    <div className="absolute inset-0 overflow-hidden">
      {/* Base gradient */}
      <motion.div
        className="absolute inset-0"
        animate={{
          background: variant === "signin" 
            ? "linear-gradient(135deg, #f6f4ff 0%, #fef3f8 100%)"
            : "linear-gradient(135deg, #fef3f8 0%, #f6f4ff 100%)"
        }}
        transition={{ duration: 0.6, ease: "easeInOut" }}
      />
      
      {/* Animated blob 1 - Top right */}
      <motion.div
        className="absolute w-[600px] h-[600px] rounded-full blur-3xl opacity-40"
        style={{
          background: "radial-gradient(circle, #9b87e8 0%, transparent 70%)",
        }}
        animate={{
          x: variant === "signin" ? "-20%" : "20%",
          y: variant === "signin" ? "-30%" : "-40%",
        }}
        transition={{ duration: 0.8, ease: "easeInOut" }}
        initial={{ x: "-20%", y: "-30%" }}
      />
      
      {/* Animated blob 2 - Bottom left */}
      <motion.div
        className="absolute w-[500px] h-[500px] rounded-full blur-3xl opacity-30"
        style={{
          background: "radial-gradient(circle, #fad4e5 0%, transparent 70%)",
          bottom: "-20%",
          left: "-10%",
        }}
        animate={{
          x: variant === "signin" ? "10%" : "-10%",
          y: variant === "signin" ? "0%" : "10%",
        }}
        transition={{ duration: 0.8, ease: "easeInOut" }}
        initial={{ x: "10%", y: "0%" }}
      />
      
      {/* Subtle floating orbs */}
      <motion.div
        className="absolute w-32 h-32 rounded-full blur-2xl opacity-20"
        style={{
          background: "#9b87e8",
          top: "20%",
          right: "15%",
        }}
        animate={{
          y: [0, -20, 0],
          scale: [1, 1.1, 1],
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />
      
      <motion.div
        className="absolute w-24 h-24 rounded-full blur-2xl opacity-15"
        style={{
          background: "#fad4e5",
          bottom: "30%",
          right: "25%",
        }}
        animate={{
          y: [0, 20, 0],
          scale: [1, 1.15, 1],
        }}
        transition={{
          duration: 8,
          repeat: Infinity,
          ease: "easeInOut",
          delay: 1,
        }}
      />
    </div>
  );
}
