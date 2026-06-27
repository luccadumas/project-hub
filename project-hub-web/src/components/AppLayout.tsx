import AssessmentOutlinedIcon from '@mui/icons-material/AssessmentOutlined';
import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined';
import FolderCopyOutlinedIcon from '@mui/icons-material/FolderCopyOutlined';
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined';
import AddCircleOutlineOutlinedIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import {
  Avatar,
  Box,
  Button,
  Divider,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
} from '@mui/material';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { sidebarWidth } from '../theme/theme';

const navItems = [
  { label: 'Dashboard', path: '/', icon: <DashboardOutlinedIcon /> },
  { label: 'Projetos', path: '/projects', icon: <FolderCopyOutlinedIcon /> },
  { label: 'Relatório', path: '/reports', icon: <AssessmentOutlinedIcon /> },
];

export function AppLayout({ children }: { children: React.ReactNode }) {
  const { username, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path: string) => {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box
        component="aside"
        sx={{
          width: sidebarWidth,
          flexShrink: 0,
          bgcolor: '#0f172a',
          color: '#e2e8f0',
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          borderRight: '1px solid rgba(255,255,255,0.06)',
        }}
      >
        <Box sx={{ px: 3, py: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box
              sx={{
                width: 36,
                height: 36,
                borderRadius: 2,
                background: 'linear-gradient(135deg, #2563eb 0%, #0ea5e9 100%)',
                display: 'grid',
                placeItems: 'center',
                fontWeight: 800,
                color: '#fff',
                fontSize: 14,
              }}
            >
              PH
            </Box>
            <Box>
              <Typography sx={{ fontWeight: 700, color: '#fff', lineHeight: 1.2 }}>
                Project Hub
              </Typography>
              <Typography variant="caption" sx={{ color: '#94a3b8' }}>
                Portfolio Management
              </Typography>
            </Box>
          </Box>
        </Box>

        <List sx={{ px: 1.5, flex: 1 }}>
          {navItems.map((item) => (
            <ListItemButton
              key={item.path}
              component={RouterLink}
              to={item.path}
              selected={isActive(item.path)}
              sx={{
                mb: 0.5,
                borderRadius: 2,
                color: '#cbd5e1',
                '&.Mui-selected': {
                  bgcolor: 'rgba(37, 99, 235, 0.18)',
                  color: '#fff',
                  '& .MuiListItemIcon-root': { color: '#60a5fa' },
                },
                '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: 'inherit' }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} slotProps={{ primary: { sx: { fontWeight: 600 } } }} />
            </ListItemButton>
          ))}

          {isAdmin && (
            <>
              <Divider sx={{ my: 1.5, borderColor: 'rgba(255,255,255,0.08)' }} />
              <ListItemButton
                component={RouterLink}
                to="/projects/new"
                selected={location.pathname === '/projects/new'}
                sx={{
                  borderRadius: 2,
                  color: '#cbd5e1',
                  '&.Mui-selected': {
                    bgcolor: 'rgba(16, 185, 129, 0.16)',
                    color: '#fff',
                  },
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40, color: 'inherit' }}>
                  <AddCircleOutlineOutlinedIcon />
                </ListItemIcon>
                <ListItemText primary="Novo projeto" slotProps={{ primary: { sx: { fontWeight: 600 } } }} />
              </ListItemButton>
            </>
          )}
        </List>

        <Box sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.08)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1.5 }}>
            <Avatar sx={{ width: 36, height: 36, bgcolor: '#2563eb', fontSize: 14 }}>
              {username?.charAt(0).toUpperCase()}
            </Avatar>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" sx={{ color: '#fff', fontWeight: 600 }} noWrap>
                {username}
              </Typography>
              <Typography variant="caption" sx={{ color: '#94a3b8' }}>
                {isAdmin ? 'Administrador' : 'Consulta'}
              </Typography>
            </Box>
          </Box>
          <Button
            fullWidth
            startIcon={<LogoutOutlinedIcon />}
            onClick={() => {
              logout();
              navigate('/login');
            }}
            sx={{
              color: '#cbd5e1',
              justifyContent: 'flex-start',
              '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
            }}
          >
            Sair
          </Button>
        </Box>
      </Box>

      <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        <Box
          sx={{
            display: { xs: 'flex', md: 'none' },
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 2,
            py: 1.5,
            bgcolor: '#0f172a',
            color: '#fff',
          }}
        >
          <Typography sx={{ fontWeight: 700 }}>Project Hub</Typography>
          <Button size="small" color="inherit" onClick={() => navigate('/projects')}>Menu</Button>
        </Box>

        <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, maxWidth: 1400, width: '100%', mx: 'auto' }}>
          {children}
        </Box>
      </Box>
    </Box>
  );
}
