import axios from 'axios';

class UserService {
    createUser(username, email, hashedPassword) {
        return new Promise((resolve, reject) => {
            axios.post('http://localhost:4567/user/new', {
                username: username,
                displayname: username,
                email: email,
                hashedPassword: hashedPassword
            })
            .then(function (response) {
                resolve(response.status);
            })
            .catch(function (error) {
                console.log(error.message);
            });
        })
    }

    loginUser(username, hashedPassword) {
        return new Promise((resolve, reject) => {
            axios.post('http://localhost:4567/user/login', {
                username: username,
                hashedPassword: hashedPassword
            })
            .then(function (response) {
                resolve(response.token);
            })
            .catch(function (error) {
                reject(error.token);
                console.log(error.message);
            });
        })
    }

    logoutUser(token) {
        return new Promise((resolve, reject) => {
            axios.delete('http://localhost:4567/user/delete', {
                token: token
            })
            .then(function (response) {
                resolve(response.status);
            })
            .catch(function (error) {
                reject(error.status);
                console.log(error.message);
            });
        })
    }

    userExists(username, email) {
        return new Promise((resolve, reject) => {
            axios.get(`http://localhost:4567/user/exists?username=${username}&email=${email}`)
            .then(function (response) {
                resolve(response);
            })
            .catch(function (error) {
                console.log(error.message);
            });
        })
    }
}

export default new UserService();
