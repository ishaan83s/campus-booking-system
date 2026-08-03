import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../../services/authService";
import "../../styles/auth.css";

function Login() {

    const navigate = useNavigate();

    const [darkMode, setDarkMode] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const [formData, setFormData] = useState({
        email: "",
        password: "",
    });


    // ==========================================
    // LOAD SAVED THEME
    // ==========================================

    useEffect(() => {

        const savedTheme = localStorage.getItem("campus-theme");

        if (savedTheme === "dark") {
            setDarkMode(true);
        }

    }, []);


    // ==========================================
    // LIGHT / DARK MODE
    // ==========================================

    const toggleTheme = () => {

        const newMode = !darkMode;

        setDarkMode(newMode);

        localStorage.setItem(
            "campus-theme",
            newMode ? "dark" : "light"
        );
    };


    // ==========================================
    // HANDLE INPUT
    // ==========================================

    const handleChange = (event) => {

        const { name, value } = event.target;

        setFormData((previousData) => ({
            ...previousData,
            [name]: value,
        }));

        setMessage("");
    };


    // ==========================================
    // LOGIN
    // ==========================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setLoading(true);
        setMessage("");

        try {

            // Send email + password to Spring Boot
            const response = await authService.login(formData);

            console.log("Login response:", response);


            // Make sure backend returned a user
            if (!response.user) {

                setMessage(
                    response.message ||
                    "Login failed. User information was not returned."
                );

                return;
            }


            // Save logged-in user
            authService.saveUser(response.user);


            // Read role returned by backend
            const role = response.user.role;


            // Redirect according to role
            if (role === "STUDENT") {

                navigate("/student/dashboard");

            } else if (role === "PROFESSOR") {

                navigate("/professor/dashboard");

            } else {

                setMessage(
                    "Login successful, but the account role is not recognized."
                );
            }

        } catch (error) {

            console.error("Login error:", error);


            // Try to display backend error
            if (error.response?.data?.message) {

                setMessage(error.response.data.message);

            } else if (typeof error.response?.data === "string") {

                setMessage(error.response.data);

            } else {

                setMessage(
                    "Invalid email or password."
                );
            }

        } finally {

            setLoading(false);
        }
    };


    return (

        <main
            className={`auth-page ${
                darkMode ? "dark" : "light"
            }`}
        >

            {/* BACKGROUND */}

            <div className="auth-background-circle circle-one"></div>
            <div className="auth-background-circle circle-two"></div>


            <section className="auth-container">


                {/* ======================================
                    LEFT SIDE
                ======================================= */}

                <div className="auth-brand-panel">


                    {/* LOGO */}

                    <div className="brand-logo">

                        <div className="book-logo">

                            <span className="book-left"></span>

                            <span className="book-right"></span>

                            <span className="clock-logo">
                                ◷
                            </span>

                        </div>

                    </div>


                    {/* TITLE */}

                    <div className="brand-title">

                        <h1>
                            CAMPUS
                            <span>BOOKING</span>
                        </h1>

                        <div className="title-line"></div>

                    </div>


                    {/* MESSAGE */}

                    <div className="brand-message">

                        <h2>
                            Office hours,{" "}
                            <span>
                                simplified.
                            </span>
                        </h2>

                        <p>
                            Connect with professors, reserve
                            office-hour slots, manage appointments
                            and join waitlists from one place.
                        </p>

                    </div>


                    {/* FEATURES */}

                    <div className="feature-list">


                        <div className="feature-item">

                            <div className="feature-icon">
                                ▣
                            </div>

                            <div>

                                <h3>
                                    Find Available Slots
                                </h3>

                                <p>
                                    Browse professor availability
                                    instantly.
                                </p>

                            </div>

                        </div>


                        <div className="feature-item">

                            <div className="feature-icon">
                                ♟
                            </div>

                            <div>

                                <h3>
                                    Book Appointments
                                </h3>

                                <p>
                                    Reserve your office-hour slot
                                    in seconds.
                                </p>

                            </div>

                        </div>


                        <div className="feature-item">

                            <div className="feature-icon">
                                ♢
                            </div>

                            <div>

                                <h3>
                                    Smart Waitlist
                                </h3>

                                <p>
                                    Automatically move up when a
                                    seat opens.
                                </p>

                            </div>

                        </div>

                    </div>


                    {/* CAMPUS ART */}

                    <div className="campus-art">

                        <div className="campus-building">

                            <div className="building-roof"></div>

                            <div className="building-clock">
                                ◷
                            </div>

                        </div>

                        <div className="hill hill-one"></div>
                        <div className="hill hill-two"></div>

                    </div>

                </div>


                {/* ======================================
                    RIGHT SIDE
                ======================================= */}

                <div className="auth-form-panel">


                    {/* THEME SWITCH */}

                    <div className="theme-switch-container">

                        <button
                            className={`theme-switch ${
                                darkMode ? "active" : ""
                            }`}
                            onClick={toggleTheme}
                            type="button"
                            aria-label="Toggle dark mode"
                        >

                            <span className="sun">
                                ☀
                            </span>

                            <span className="switch-ball"></span>

                            <span className="moon">
                                ☾
                            </span>

                        </button>

                    </div>


                    {/* LOGIN CONTENT */}

                    <div className="login-content">


                        <div className="welcome-label">
                            WELCOME BACK
                        </div>


                        <h2>
                            Sign in to your account
                        </h2>


                        <p className="login-subtitle">
                            Enter your credentials to continue
                        </p>


                        {/* LOGIN FORM */}

                        <form
                            className="login-form"
                            onSubmit={handleSubmit}
                        >


                            {/* EMAIL */}

                            <div className="form-group">

                                <label htmlFor="email">
                                    Email address
                                </label>


                                <div className="input-wrapper">

                                    <span className="input-icon">
                                        ✉
                                    </span>


                                    <input
                                        id="email"
                                        name="email"
                                        type="email"
                                        placeholder="you@campus.edu"
                                        value={formData.email}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                            </div>


                            {/* PASSWORD */}

                            <div className="form-group">


                                <div className="password-heading">

                                    <label htmlFor="password">
                                        Password
                                    </label>


                                    <button
                                        type="button"
                                        className="forgot-password"
                                    >
                                        Forgot password?
                                    </button>

                                </div>


                                <div className="input-wrapper">

                                    <span className="input-icon">
                                        ♙
                                    </span>


                                    <input
                                        id="password"
                                        name="password"
                                        type={
                                            showPassword
                                                ? "text"
                                                : "password"
                                        }
                                        placeholder="Enter your password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                    />


                                    <button
                                        type="button"
                                        className="show-password"
                                        onClick={() =>
                                            setShowPassword(
                                                !showPassword
                                            )
                                        }
                                        aria-label="Show or hide password"
                                    >

                                        {
                                            showPassword
                                                ? "◉"
                                                : "◎"
                                        }

                                    </button>

                                </div>

                            </div>


                            {/* SIGN IN BUTTON */}

                            <button
                                type="submit"
                                className="sign-in-button"
                                disabled={loading}
                            >

                                <span>

                                    {
                                        loading
                                            ? "SIGNING IN..."
                                            : "SIGN IN"
                                    }

                                </span>


                                {!loading && (

                                    <span className="button-arrow">
                                        →
                                    </span>

                                )}

                            </button>

                        </form>


                        {/* LOGIN ERROR */}

                        {message && (

                            <div className="auth-message">

                                {message}

                            </div>

                        )}


                        {/* DIVIDER */}

                        <div className="divider">

                            <span></span>

                            <p>
                                OR
                            </p>

                            <span></span>

                        </div>


                        {/* REGISTER */}

                        <button
                            type="button"
                            className="create-account-button"
                            onClick={() =>
                                navigate("/register")
                            }
                        >

                            <span className="create-icon">
                                ♙+
                            </span>

                            CREATE AN ACCOUNT

                        </button>


                        {/* SECURITY */}

                        <div className="security-message">

                            <span>
                                ♢
                            </span>

                            Secure

                            <b>
                                •
                            </b>

                            Reliable

                            <b>
                                •
                            </b>

                            Built for Campus

                        </div>

                    </div>

                </div>

            </section>

        </main>
    );
}

export default Login;