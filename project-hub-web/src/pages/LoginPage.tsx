import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import {
  Alert,
  Box,
  Button,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LoadingState } from '../components/LoadingState';

export function LoginPage() {
  const { login, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isLoading) {
    return <LoadingState label="Verificando sessão..." />;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      await login(username, password);
      navigate('/');
    } catch {
      setError('Usuário ou senha inválidos');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex' }}>
      <Box
        sx={{
          flex: 1,
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          justifyContent: 'center',
          px: 8,
          background: 'linear-gradient(160deg, #0f172a 0%, #1e3a8a 55%, #2563eb 100%)',
          color: '#fff',
        }}
      >
        <Box
          sx={{
            width: 56,
            height: 56,
            borderRadius: 3,
            bgcolor: 'rgba(255,255,255,0.12)',
            display: 'grid',
            placeItems: 'center',
            fontWeight: 800,
            mb: 3,
          }}
        >
          PH
        </Box>
        <Typography variant="h3" sx={{ fontWeight: 800, mb: 2, maxWidth: 480, lineHeight: 1.15 }}>
          Gestão de portfólio com visibilidade executiva
        </Typography>
        <Typography sx={{ color: 'rgba(255,255,255,0.78)', maxWidth: 460, lineHeight: 1.7 }}>
          Acompanhe projetos, riscos, equipes e orçamento em uma plataforma unificada
          para tomada de decisão corporativa.
        </Typography>
      </Box>

      <Box
        sx={{
          flex: 1,
          display: 'grid',
          placeItems: 'center',
          p: 3,
          bgcolor: '#f8fafc',
        }}
      >
        <Paper sx={{ p: 4, width: '100%', maxWidth: 420, border: '1px solid #e2e8f0' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: 2,
                bgcolor: '#eff6ff',
                color: '#2563eb',
                display: 'grid',
                placeItems: 'center',
              }}
            >
              <LockOutlinedIcon />
            </Box>
            <Box>
              <Typography variant="h5">Entrar</Typography>
              <Typography variant="body2" color="text.secondary">
                Acesse sua conta corporativa
              </Typography>
            </Box>
          </Box>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Usuário"
              margin="normal"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
              fullWidth
              label="Senha"
              type="password"
              margin="normal"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              sx={{ mt: 3 }}
              disabled={submitting}
            >
              {submitting ? 'Entrando...' : 'Entrar'}
            </Button>
          </Box>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 3 }}>
            Use as credenciais definidas no arquivo `.env` do projeto.
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
}
