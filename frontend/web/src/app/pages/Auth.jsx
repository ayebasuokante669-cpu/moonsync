import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { useNavigate } from "react-router";
import { Input } from "../components/auth/Input";
import { AuthButton } from "../components/auth/AuthButton";
import { AnimatedBackground } from "../components/auth/AnimatedBackground";
import logoImg from "../../assets/moonsync-logo.png";

export function Auth() {
  const [step, setStep] = useState("login");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  // Form states
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  
  // Validation states
  const [emailError, setEmailError] = useState("");
  
  const validateEmail = (value) => {
    if (!value) {
      setEmailError("Email is required");
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
      setEmailError("Please enter a valid email address");
      return false;
    }
    setEmailError("");
    return true;
  };
  
  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    if (!validateEmail(email)) return;
    
    setLoading(true);
    // Simulate API login
    setTimeout(() => {
      setLoading(false);
      setStep("welcome");
      
      // Auto redirect to dashboard after welcome
      setTimeout(() => {
        navigate("/");
      }, 2000);
    }, 1200);
  };

  return (
    <div className="relative min-h-screen w-full flex items-center justify-center p-6 overflow-hidden">
      {/* Animated Background */}
      <AnimatedBackground variant="signin" />
      
      {/* Auth Card Container */}
      <motion.div
        className="relative w-full max-w-md"
        transition={{ duration: 0.4, ease: "easeInOut" }}
      >
        <AnimatePresence mode="wait">
          {step === "login" && (
            <motion.div
              key="auth"
              className="relative bg-[var(--color-card)]/80 backdrop-blur-xl rounded-2xl shadow-xl border border-white/40 p-8 overflow-hidden"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.4 }}
            >
              {/* Subtle inner glow */}
              <div className="absolute inset-0 bg-gradient-to-br from-[var(--color-primary-light)]/20 via-transparent to-[var(--color-secondary)]/20 pointer-events-none" />
              
              <div className="relative z-10">
                {/* Logo & Header */}
                <motion.div
                  className="text-center mb-8"
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3 }}
                >
                  <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl mb-4 overflow-hidden">
                    <img src={logoImg} alt="MoonSync" className="w-12 h-12 object-contain" />
                  </div>
                  
                  <h1 className="text-2xl font-semibold text-foreground mb-2">
                    Welcome back
                  </h1>
                  
                  <p className="text-sm text-[var(--color-muted-foreground)]">
                    Sign in to access the MoonSync Admin Portal
                  </p>
                </motion.div>
                
                {/* Form */}
                <motion.form
                  onSubmit={handleAuthSubmit}
                  className="space-y-5"
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.3 }}
                >
                  {/* Email */}
                  <Input
                    label="Email Address"
                    type="email"
                    placeholder="admin@moonsync.com"
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      if (emailError) validateEmail(e.target.value);
                    }}
                    onBlur={(e) => validateEmail(e.target.value)}
                    error={emailError}
                    required
                  />
                  
                  {/* Password */}
                  <Input
                    label="Password"
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    showPasswordToggle
                    required
                  />
                  
                  {/* Submit Button */}
                  <AuthButton
                    type="submit"
                    variant="primary"
                    loading={loading}
                    className="w-full mt-6"
                  >
                    Sign In
                  </AuthButton>
                </motion.form>
              </div>
            </motion.div>
          )}

          {/* Welcome Step */}
          {step === "welcome" && (
            <motion.div
              key="welcome"
              className="relative bg-[var(--color-card)]/80 backdrop-blur-xl rounded-2xl shadow-xl border border-white/40 p-12 text-center overflow-hidden"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, ease: "easeOut" }}
            >
              <div className="absolute inset-0 bg-gradient-to-br from-[var(--color-primary-light)]/30 via-transparent to-[var(--color-secondary)]/30 pointer-events-none" />
              
              <div className="relative z-10">
                <motion.div
                  initial={{ scale: 0, opacity: 0 }}
                  animate={{ scale: 1, opacity: 1 }}
                  transition={{ type: "spring", bounce: 0.5, delay: 0.2 }}
                  className="inline-flex items-center justify-center w-24 h-24 rounded-3xl bg-[var(--color-card)] shadow-lg border border-[var(--color-border)] mb-8 overflow-hidden"
                >
                  <img src={logoImg} alt="MoonSync" className="w-16 h-16 object-contain" />
                </motion.div>
                
                <motion.h2
                  className="text-3xl font-bold bg-gradient-to-br from-[var(--color-primary)] to-purple-600 bg-clip-text text-transparent mb-4"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4 }}
                >
                  Welcome to MoonSync
                </motion.h2>
                
                <motion.p
                  className="text-[var(--color-muted-foreground)] text-lg"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.6 }}
                >
                  Preparing your dashboard...
                </motion.p>
                
                <motion.div
                  className="mt-8 flex justify-center gap-2"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.8 }}
                >
                  {[0, 1, 2].map((i) => (
                    <motion.div
                      key={i}
                      className="w-2.5 h-2.5 rounded-full bg-[var(--color-primary)]/60"
                      animate={{
                        scale: [1, 1.5, 1],
                        opacity: [0.5, 1, 0.5],
                      }}
                      transition={{
                        duration: 1,
                        repeat: Infinity,
                        delay: i * 0.2,
                        ease: "easeInOut",
                      }}
                    />
                  ))}
                </motion.div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
}
