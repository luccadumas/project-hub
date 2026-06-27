import { Box, Paper, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface ContentCardProps {
  title?: string;
  subtitle?: string;
  action?: ReactNode;
  children: ReactNode;
  noPadding?: boolean;
}

export function ContentCard({ title, subtitle, action, children, noPadding }: ContentCardProps) {
  return (
    <Paper sx={{ overflow: 'hidden' }}>
      {(title || action) && (
        <Box
          sx={{
            px: 3,
            py: 2,
            borderBottom: '1px solid',
            borderColor: 'divider',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Box>
            {title && (
              <Typography variant="h6">{title}</Typography>
            )}
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          {action}
        </Box>
      )}
      <Box sx={{ p: noPadding ? 0 : 3 }}>{children}</Box>
    </Paper>
  );
}
