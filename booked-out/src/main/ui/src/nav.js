import { Route, Routes, NavLink } from 'react-router-dom';

import Landing from './pages/Landing';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Home from './pages/Home';
import Button from '@mui/material/Button';
import styles from './styles/Login.module.css';

function Nav() {
  return (
    <div className={styles['page-container']}>
      <main>
        <Routes>
          <Route path='/' element={<Landing />} />
          <Route path='/login' element={<Login />} />
          <Route path='/signup' element={<Signup />} />
          <Route path='/home' element={<Home />} />
        </Routes>
      </main>
    </div>
  );
}

export default Nav;
