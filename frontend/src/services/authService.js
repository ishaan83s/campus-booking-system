import api from "./api";

const authService = {

    // ============================
    // REGISTER
    // ============================

    register: async (registerData) => {

        const response = await api.post(
            "/auth/register",
            registerData
        );

        return response.data;
    },


    // ============================
    // LOGIN
    // ============================

    login: async (loginData) => {

        const response = await api.post(
            "/auth/login",
            loginData
        );

        return response.data;
    },


    // ============================
    // SAVE LOGGED-IN USER
    // ============================

    saveUser: (user) => {

        localStorage.setItem(
            "campus-user",
            JSON.stringify(user)
        );
    },


    // ============================
    // GET LOGGED-IN USER
    // ============================

    getUser: () => {

        const user = localStorage.getItem("campus-user");

        if (!user) {
            return null;
        }

        return JSON.parse(user);
    },


    // ============================
    // CHECK LOGIN
    // ============================

    isLoggedIn: () => {

        return localStorage.getItem("campus-user") !== null;
    },


    // ============================
    // LOGOUT
    // ============================

    logout: () => {

        localStorage.removeItem("campus-user");
    }

};

export default authService;