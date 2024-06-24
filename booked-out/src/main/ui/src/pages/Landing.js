import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

const Landing = props => {
  const navigate = useNavigate();

  return (
    <Stack 
      justifyContent="center"
      alignItems="center"
      spacing={2}
    >
      <h1>
        Booked Out
      </h1>
      <Button sx={{ mb: 1 }} variant="contained" onClick={() => navigate('/login')}>
          Login
      </Button>
      <Button variant="contained" onClick={() => navigate ('/signup')}>
          Sign Up
      </Button>
    </Stack>
  );
};

export default Landing;