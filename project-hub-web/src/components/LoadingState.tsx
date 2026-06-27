import { Box, CircularProgress, Typography } from '@mui/material';

export function LoadingState({ label = 'Carregando...' }: { label?: string }) {
  return (
    <Box
      role="status"
      aria-live="polite"
      aria-busy="true"
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        py: 10,
      }}
    >
      <CircularProgress size={36} aria-hidden="true" />
      <Typography color="text.secondary">{label}</Typography>
    </Box>
  );
}
