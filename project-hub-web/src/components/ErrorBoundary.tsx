import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Alert, Box, Button, Typography } from '@mui/material';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 3 }}>
          <Box sx={{ maxWidth: 480, textAlign: 'center' }}>
            <Alert severity="error" sx={{ mb: 2 }}>
              Ocorreu um erro inesperado na aplicacao.
            </Alert>
            <Typography color="text.secondary" sx={{ mb: 2 }}>
              Recarregue a pagina para tentar novamente.
            </Typography>
            <Button variant="contained" onClick={() => window.location.reload()}>
              Recarregar
            </Button>
          </Box>
        </Box>
      );
    }

    return this.props.children;
  }
}
