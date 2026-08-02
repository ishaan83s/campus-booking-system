import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../../services/authService";
import "../../styles/auth.css";

function Register() {

    const navigate = useNavigate();

    const [darkMode, setDarkMode] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        password: "",
        role: "STUDENT",
    });


    // Load saved theme
    useEffect(() => {

        const savedTheme = localStorage.getItem("campus-theme");

        if (savedTheme === "dark") {
            setDarkMode(true);
        }

    }, []);


    // Toggle light / dark mode
    const toggleTheme = () => {

        const newMode = !darkMode;

        setDarkMode(newMode);

        localStorage.setItem(
            "campus-theme",
            newMode ? "dark" : "light"
        );
    };


    // Handle form input
    const handleChange = (event) => {

        const { name, value } = event.target;

        setFormData((previousData) => ({
            ...previousData,
            [name]: value,
        }));
    };


    // Select account role
    const selectRole = (role) => {

        setFormData((previousData) => ({
            ...previousData,
            role: role,
        }));
    };


    // Register account
    const handleSubmit = async (event) => {

        event.preventDefault();

        setLoading(true);
        setMessage("");

        try {

            const response = await authService.register(formData);

            console.log("Registration response:", response);

            setMessage("Account created successfully!");

            /*
                Later we can automatically redirect
                after successful registration.
            */

        } catch (error) {

            console.error("Registration error:", error);

            setMessage(
                error?.response?.data?.message ||
                "Registration failed. Please check your information."
            );

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


            <section className="auth-container register-container">


                {/* =====================================
                    LEFT BRAND PANEL
                ====================================== */}

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
                            Your campus,{" "}
                            <span>
                                connected.
                            </span>
                        </h2>

                        <p>
                            Create your account and start managing
                            office-hour appointments from one simple
                            platform.
                        </p>

                    </div>


                    {/* FEATURES */}

                    <div className="feature-list">


                        <div className="feature-item">

                            <div className="feature-icon">
                                ◈
                            </div>

                            <div>

                                <h3>
                                    Students
                                </h3>

                                <p>
                                    Discover professors and reserve
                                    available office-hour slots.
                                </p>

                            </div>

                        </div>


                        <div className="feature-item">

                            <div className="feature-icon">
                                ◉
                            </div>

                            <div>

                                <h3>
                                    Professors
                                </h3>

                                <p>
                                    Create slots and manage your
                                    appointments efficiently.
                                </p>

                            </div>

                        </div>


                        <div className="feature-item">

                            <div className="feature-icon">
                                ◇
                            </div>

                            <div>

                                <h3>
                                    One Platform
                                </h3>

                                <p>
                                    Bookings, schedules and waitlists
                                    all in one place.
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


                {/* =====================================
                    RIGHT REGISTER PANEL
                ====================================== */}

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


                    <div className="login-content register-content">


                        <div className="welcome-label">
                            JOIN CAMPUS BOOKING
                        </div>


                        <h2>
                            Create your account
                        </h2>


                        <p className="login-subtitle">
                            Choose your role and enter your details
                        </p>


                        {/* REGISTER FORM */}

                        <form
                            className="login-form"
                            onSubmit={handleSubmit}
                        >


                            {/* FULL NAME */}

                            <div className="form-group">

                                <label htmlFor="fullName">
                                    Full name
                                </label>

                                <div className="input-wrapper">

                                    <span className="input-icon">
                                        ♙
                                    </span>

                                    <input
                                        id="fullName"
                                        name="fullName"
                                        type="text"
                                        placeholder="Enter your full name"
                                        value={formData.fullName}
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                            </div>


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

                                <label htmlFor="password">
                                    Password
                                </label>

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
                                        placeholder="Create a password"
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


                            {/* ROLE */}

                            <div className="form-group">

                                <label>
                                    I am a
                                </label>


                                <div className="role-selector">


                                    {/* STUDENT */}

                                    <button
                                        type="button"
                                        className={`role-card ${
                                            formData.role === "STUDENT"
                                                ? "selected"
                                                : ""
                                        }`}
                                        onClick={() =>
                                            selectRole("STUDENT")
                                        }
                                    >

                                        <span className="role-icon">
                                            ♙
                                        </span>

                                        <span>

                                            <strong>
                                                Student
                                            </strong>

                                            <small>
                                                Book office hours
                                            </small>

                                        </span>

                                    </button>


                                    {/* PROFESSOR */}

                                    <button
                                        type="button"
                                        className={`role-card ${
                                            formData.role === "PROFESSOR"
                                                ? "selected"
                                                : ""
                                        }`}
                                        onClick={() =>
                                            selectRole("PROFESSOR")
                                        }
                                    >

                                        <span className="role-icon">
                                            ◉
                                        </span>

                                        <span>

                                            <strong>
                                                Professor
                                            </strong>

                                            <small>
                                                Manage office hours
                                            </small>

                                        </span>

                                    </button>

                                </div>

                            </div>


                            {/* CREATE ACCOUNT */}

                            <button
                                type="submit"
                                className="sign-in-button"
                                disabled={loading}
                            >

                                <span>

                                    {
                                        loading
                                            ? "CREATING ACCOUNT..."
                                            : "CREATE ACCOUNT"
                                    }

                                </span>


                                {!loading && (

                                    <span className="button-arrow">
                                        →
                                    </span>

                                )}

                            </button>

                        </form>


                        {/* SERVER MESSAGE */}

                        {message && (

                            <div className="auth-message">

                                {message}

                            </div>

                        )}


                        {/* =================================
                            BACK TO LOGIN
                        ================================== */}

                        <div className="register-login-link">

                            <span>
                                Already have an account?
                            </span>

                            <button
                                type="button"
                                onClick={() =>
                                    navigate("/login")
                                }
                            >
                                SIGN IN
                            </button>

                        </div>

                    </div>

                </div>

            </section>

        </main>
    );
}

export default Register;