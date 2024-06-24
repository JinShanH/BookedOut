import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

const Home = props => {

  const navigate = useNavigate();

  async function handleLogout() {
    navigate('/');
  }

  return (
    <Stack 
      justifyContent="center"
      alignItems="center"
      spacing={2}
    >
      <h3>Home View</h3>
      <p> This is the home page</p>
      <Button variant="contained" sx={{ mb: 1 }} onClick={() => handleLogout()}>
          Logout
      </Button>
    </Stack>
  );
};

export default Home;