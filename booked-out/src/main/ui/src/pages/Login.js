import React from 'react';
import { useNavigate } from 'react-router-dom';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import OutlinedInput from '@mui/material/OutlinedInput';
import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import FormControl from '@mui/material/FormControl';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import Alert from '@mui/material/Alert';
import Collapse from '@mui/material/Collapse';
import Stack from '@mui/material/Stack';

import UserService from '../services/UserService';

const Login = props => {

  const navigate = useNavigate();
  const sha256 = require('js-sha256').sha256;

  const [values, setValues] = React.useState({
    username: '',
    password: '',
    showPassword: false,
    errorMessage: '',
    alertType: "error",
    showAlert: false,
  });

  const handleChange = (prop) => (event) => {
    setValues({ ...values, [prop]: event.target.value });
  };

  const handleClickShowPassword = () => {
    setValues({
      ...values,
      showPassword: !values.showPassword,
    });
  };

  const handleMouseDownPassword = (event) => {
    event.preventDefault();
  };

  const handleErrorMessage = (props) => {
    setValues({
      ...values,
      errorMessage: props.errorMessage,
      alertType: props.alertType,
      showAlert: true
    })
  }

  function timeout(delay) {
    return new Promise( res => setTimeout(res, delay) );
  }

  function hashPassword() {
    return new Promise((resolve, reject) => {
      resolve(`${sha256(values.password)}`);
    })
  }

  async function handleSubmit() {
    const hPassword = await hashPassword();
    try {
      const token = await UserService.loginUser(values.username, hPassword);
      document.cookie = `token=${token}`;
      console.log("New token: " + token);
      handleErrorMessage({errorMessage:"Login Success",alertType:"success"});
      await timeout(2000);
      navigate('/home');
    } catch {
      console.log("run");
      handleErrorMessage({errorMessage:"Username or password is incorrect",alertType:"error"});
    }
  }

  return (
    <Stack 
      justifyContent="center"
      alignItems="center"
      spacing={2}
    >
      <h1>
        Login  
      </h1>
      <TextField id="outlined-basic" label="Username" variant="outlined" value={values.username} onChange={handleChange('username')} />
      {/* <TextField id="outlined-basic" label="Password" variant="outlined" /> */}
      <FormControl sx={{ width: '27ch' }} variant="outlined">
        <InputLabel htmlFor="outlined-adornment-password">Password</InputLabel>
        <OutlinedInput
          id="outlined-adornment-password"
          type={values.showPassword ? 'text' : 'password'}
          value={values.password}
          onChange={handleChange('password')}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={handleClickShowPassword}
                onMouseDown={handleMouseDownPassword}
                edge="end"
              >
                {values.showPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          label="Password"
          error={values.error}
        />
      </FormControl>
      <Collapse in={values.showAlert}>
        <Alert 
          severity={values.alertType}
        >
          {values.errorMessage}
        </Alert>
      </Collapse>
      <Button variant="contained" sx={{ mb: 1 }} onClick={() => handleSubmit()}>
          Submit
      </Button>
      <Button variant="text" color="secondary" onClick={() => navigate('/signup')}>
          Don't have an account? Sign up.
      </Button>
    </Stack>
  );
};

export default Login;