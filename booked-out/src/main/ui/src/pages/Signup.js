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

const Signup = props => {
  
  const navigate = useNavigate();
  const sha256 = require('js-sha256').sha256;

  const [values, setValues] = React.useState({
    email: '',
    username: '',
    password: '',
    showPassword: false,
    repeatPassword: '',
    showRepeatPassword: false,
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
  
  const handleClickShowRepeatPassword = () => {
    setValues({
      ...values,
      showRepeatPassword: !values.showRepeatPassword,
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
  
  function checkPasswordMatch() {
    return values.password === values.repeatPassword;
  }

  function checkPasswordCorrectLength() {
    return values.password.length >= 8;
  }

  function checkPasswordHasLowercase() {
    for (let i = 0; i < values.password.length; i++) {
      if (values.password.charAt(i).toLowerCase() === values.password.charAt(i) && values.password.charAt(i).toUpperCase() !== values.password.charAt(i)) {
        return true;
      }
    }
    return false;
  }

  function checkPasswordHasUppercase() {
    for (let i = 0; i < values.password.length; i++) {
      if (values.password.charAt(i).toUpperCase() === values.password.charAt(i) && values.password.charAt(i).toLowerCase() !== values.password.charAt(i)) {
        return true;
      }
    }
    return false;
  }

  function checkPasswordHasNumber() {
    return /\d/.test(values.password);
  }

  function checkPasswordHasSpecialChar() {
    const specialChars = /[`!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
    return specialChars.test(values.password);
  }

  function validPassword() {
    let errorMessage = "";
    if (!checkPasswordMatch()) {
      errorMessage = "Passwords do not match";
    } else if (!checkPasswordCorrectLength()) {
      errorMessage = "Password is too short";
    } else if (!checkPasswordHasLowercase()) {
      errorMessage = "Password requires a lowercase letter";
    } else if (!checkPasswordHasUppercase()) {
      errorMessage = "Password requires an uppercase letter";
    } else if (!checkPasswordHasNumber()) {
      errorMessage = "Password requires a number";
    } else if (!checkPasswordHasSpecialChar()) {
      errorMessage = "Password requires a special character";
    } else {
      return true;
    }
    handleErrorMessage({errorMessage:errorMessage,alertType:"error"})
    return false;
  }

  async function validUsername() {
    const response = await UserService.userExists(values.username.replace(/["]+/g, ''), null);
    console.log("Username exists: " + response.data.usernameInUse);
    if (!response.data.usernameInUse) {
      return true;
    }
    handleErrorMessage({errorMessage:"Username already exists",alertType:"error"});
    return false;
  }

  async function validEmail() {
    const response = await UserService.userExists(null, values.email.replace(/["]+/g, ''));
    console.log("Email exists: " + response.data.emailInUse);
    if (!response.data.emailInUse) {
      return true;
    }
    handleErrorMessage({errorMessage:"Email already exists",alertType:"error"});
    return false;
  }
  
  function validInput() {
    if (validPassword() && validUsername() && validEmail()) {
      return true;
    }
    return false;
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
    if (validInput()) {
      // check if username exists
      // check if email exists
      //then try - get rid of else, won't run
      const hPassword = await hashPassword();
      const response = await UserService.createUser(values.username, values.email, hPassword);
      console.log("Status: " + response);
      if (response === 200) {
        handleErrorMessage({errorMessage:"Sign Up Success",alertType:"success"});
        await timeout(2000);
        navigate('/home');
      }
    }
  }
  
  return (
    <Stack 
      justifyContent="center"
      alignItems="center"
      spacing={2}
    >
      <h1>
        Sign Up  
      </h1>
      <TextField id="outlined-basic" label="Email" variant="outlined" value={values.email} onChange={handleChange('email')} />
      <TextField id="outlined-basic" label="Username" variant="outlined" value={values.username} onChange={handleChange('username')} />
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
        />
      </FormControl>
      <FormControl sx={{ width: '27ch' }} variant="outlined">
        <InputLabel htmlFor="outlined-adornment-password">Repeat Password</InputLabel>
        <OutlinedInput
          id="outlined-adornment-password"
          type={values.showRepeatPassword ? 'text' : 'password'}
          value={values.repeatPassword}
          onChange={handleChange('repeatPassword')}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle repeatPassword visibility"
                onClick={handleClickShowRepeatPassword}
                onMouseDown={handleMouseDownPassword}
                edge="end"
              >
                {values.showRepeatPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          label="Repeat Password"
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
      <Button variant="text" color="secondary" onClick={() => navigate('/login')}>
          Already have an account? Log in.
      </Button>
    </Stack>
  );
};

export default Signup;