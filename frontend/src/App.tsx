import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '@/components/ProtectedRoute';
import { Role } from '@/types/auth';

function PageSpinner() {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="w-10 h-10 border-4 border-stone-300 border-t-stone-700 rounded-full animate-spin" />
    </div>
  );
}

const LoginPage              = lazy(() => import('@/pages/Login'));
const RegisterPage           = lazy(() => import('@/pages/Register'));
const ForgotPasswordPage     = lazy(() => import('@/pages/ForgotPassword'));
const ResetPasswordPage      = lazy(() => import('@/pages/ResetPassword'));
const Dashboard              = lazy(() => import('@/pages/Dashboard'));
const MenuPage               = lazy(() => import('@/pages/Menu'));
const OrdiniPage             = lazy(() => import('@/pages/Ordini'));
const CucinaPage             = lazy(() => import('@/pages/Cucina'));
const PrenotazioniPage       = lazy(() => import('@/pages/Prenotazioni'));
const PrenotazioneOnlinePage = lazy(() => import('@/pages/PrenotazioneOnline'));
const ContoPage              = lazy(() => import('@/pages/Conto'));
const AnalyticsPage          = lazy(() => import('@/pages/Analytics'));
const RicettePage            = lazy(() => import('@/pages/Ricette'));
const StaffPage              = lazy(() => import('@/pages/Staff'));
const AdminPage              = lazy(() => import('@/pages/Admin'));
const Unauthorized           = lazy(() => import('@/pages/Unauthorized'));
const MenuQrPage             = lazy(() => import('@/pages/MenuQr'));

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageSpinner />}>
      <Routes>
        {/* Rotte Pubbliche */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/menu/qr/:token" element={<MenuQrPage />} />
        


        {/* Rotte Protette - Accessibili a chiunque sia loggato */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/menu"
          element={
            <ProtectedRoute>
              <MenuPage />
            </ProtectedRoute>
          }
        />

        {/* Rotte Protette - ADMIN e CAMERIERE */}
        <Route
          path="/ordini"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN, Role.CAMERIERE]}>
              <OrdiniPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ordini/tavolo/:tavoloId"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN, Role.CAMERIERE]}>
              <OrdiniPage />
            </ProtectedRoute>
          }
        />

        {/* Rotta cucina - CUOCO e ADMIN */}
        <Route
          path="/cucina"
          element={
            <ProtectedRoute allowedRoles={[Role.CUOCO, Role.ADMIN]}>
              <CucinaPage />
            </ProtectedRoute>
          }
        />

        {/* Prenotazioni - tutti gli utenti autenticati */}
        <Route
          path="/prenotazioni"
          element={
            <ProtectedRoute>
              <PrenotazioniPage />
            </ProtectedRoute>
          }
        />

        {/* Analytics - solo ADMIN */}
        <Route
          path="/analytics"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN]}>
              <AnalyticsPage />
            </ProtectedRoute>
          }
        />

        {/* Ricette - solo ADMIN */}
        <Route
          path="/ricette"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN]}>
              <RicettePage />
            </ProtectedRoute>
          }
        />

        {/* Staff - solo ADMIN */}
        <Route
          path="/staff"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN]}>
              <StaffPage />
            </ProtectedRoute>
          }
        />

        {/* Conto - ADMIN e CAMERIERE */}
        <Route
          path="/conto/:ordineId"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN, Role.CAMERIERE]}>
              <ContoPage />
            </ProtectedRoute>
          }
        />

        {/* Admin - gestione utenti */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={[Role.ADMIN]}>
              <AdminPage />
            </ProtectedRoute>
          }
        />

        {/* Prenotazione online - CLIENTE */}
        <Route
          path="/prenotazione-online"
          element={
            <ProtectedRoute allowedRoles={[Role.CLIENTE]}>
              <PrenotazioneOnlinePage />
            </ProtectedRoute>
          }
        />

        {/* Fallback per rotte inesistenti */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;