import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Sidebar from "../../components/Sidebar";
import Navbar from "../../components/Navbar";
import authService from "../../services/authService";

import "../../styles/dashboard.css";


function StudentDashboard() {

    const navigate = useNavigate();

    const user = authService.getUser();

    const [darkMode, setDarkMode] = useState(false);


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
    // THEME TOGGLE
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
    // FIRST NAME
    // ==========================================

    const getFirstName = () => {

        if (!user?.fullName) {
            return "Student";
        }

        return user.fullName.split(" ")[0];
    };


    return (

        <div
            className={`dashboard-page ${
                darkMode ? "dashboard-dark" : "dashboard-light"
            }`}
        >

            {/* =====================================
                SIDEBAR
            ====================================== */}

            <Sidebar />


            {/* =====================================
                MAIN AREA
            ====================================== */}

            <div className="dashboard-main">


                {/* NAVBAR */}

                <Navbar
                    darkMode={darkMode}
                    toggleTheme={toggleTheme}
                    title="Dashboard"
                />


                {/* =================================
                    DASHBOARD CONTENT
                ================================== */}

                <main className="dashboard-content">


                    {/* WELCOME BANNER */}

                    <section className="welcome-banner">

                        <div className="welcome-banner-content">

                            <p className="welcome-small">
                                STUDENT PORTAL
                            </p>

                            <h2>
                                Ready to learn,{" "}
                                <span>
                                    {getFirstName()}?
                                </span>
                            </h2>

                            <p className="welcome-description">
                                Find professor office hours,
                                manage your appointments and keep
                                track of your waitlists from one
                                place.
                            </p>


                            <button
                                type="button"
                                className="find-slot-button"
                                onClick={() =>
                                    navigate("/student/slots")
                                }
                            >
                                Find Available Slots

                                <span>
                                    →
                                </span>
                            </button>

                        </div>


                        <div className="welcome-decoration">

                            <div className="welcome-book">
                                ▣
                            </div>

                            <div className="welcome-circle circle-a"></div>

                            <div className="welcome-circle circle-b"></div>

                        </div>

                    </section>


                    {/* =================================
                        STATISTICS
                    ================================== */}

                    <section className="dashboard-stats">


                        {/* UPCOMING */}

                        <article className="stat-card">

                            <div className="stat-icon orange-icon">
                                ◷
                            </div>

                            <div className="stat-information">

                                <span>
                                    UPCOMING
                                </span>

                                <strong>
                                    0
                                </strong>

                                <p>
                                    Appointments
                                </p>

                            </div>

                        </article>


                        {/* AVAILABLE */}

                        <article className="stat-card">

                            <div className="stat-icon yellow-icon">
                                ▣
                            </div>

                            <div className="stat-information">

                                <span>
                                    AVAILABLE
                                </span>

                                <strong>
                                    0
                                </strong>

                                <p>
                                    Office-hour slots
                                </p>

                            </div>

                        </article>


                        {/* WAITLIST */}

                        <article className="stat-card">

                            <div className="stat-icon waitlist-icon">
                                ◇
                            </div>

                            <div className="stat-information">

                                <span>
                                    WAITLIST
                                </span>

                                <strong>
                                    0
                                </strong>

                                <p>
                                    Active waitlists
                                </p>

                            </div>

                        </article>

                    </section>


                    {/* =================================
                        LOWER GRID
                    ================================== */}

                    <section className="dashboard-grid">


                        {/* =============================
                            UPCOMING APPOINTMENT
                        ============================== */}

                        <article className="dashboard-panel upcoming-panel">

                            <div className="panel-heading">

                                <div>

                                    <span className="panel-label">
                                        SCHEDULE
                                    </span>

                                    <h3>
                                        Upcoming Appointment
                                    </h3>

                                </div>


                                <button
                                    type="button"
                                    className="text-button"
                                    onClick={() =>
                                        navigate("/student/bookings")
                                    }
                                >
                                    View all →
                                </button>

                            </div>


                            {/* EMPTY STATE FOR NOW */}

                            <div className="empty-dashboard-state">

                                <div className="empty-state-icon">
                                    ◷
                                </div>

                                <h4>
                                    No upcoming appointments
                                </h4>

                                <p>
                                    When you book an office-hour
                                    slot, your next appointment
                                    will appear here.
                                </p>

                                <button
                                    type="button"
                                    onClick={() =>
                                        navigate("/student/slots")
                                    }
                                >
                                    Browse Slots
                                </button>

                            </div>

                        </article>


                        {/* =============================
                            QUICK ACTIONS
                        ============================== */}

                        <article className="dashboard-panel quick-actions-panel">

                            <div className="panel-heading">

                                <div>

                                    <span className="panel-label">
                                        SHORTCUTS
                                    </span>

                                    <h3>
                                        Quick Actions
                                    </h3>

                                </div>

                            </div>


                            <div className="quick-actions">


                                <button
                                    type="button"
                                    className="quick-action"
                                    onClick={() =>
                                        navigate("/student/slots")
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ◷
                                    </span>

                                    <div>

                                        <strong>
                                            Find a Professor
                                        </strong>

                                        <p>
                                            Browse available
                                            office hours
                                        </p>

                                    </div>

                                    <span className="quick-arrow">
                                        →
                                    </span>

                                </button>


                                <button
                                    type="button"
                                    className="quick-action"
                                    onClick={() =>
                                        navigate("/student/bookings")
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ▣
                                    </span>

                                    <div>

                                        <strong>
                                            My Bookings
                                        </strong>

                                        <p>
                                            Manage your
                                            appointments
                                        </p>

                                    </div>

                                    <span className="quick-arrow">
                                        →
                                    </span>

                                </button>


                                <button
                                    type="button"
                                    className="quick-action"
                                    onClick={() =>
                                        navigate("/student/waitlist")
                                    }
                                >

                                    <span className="quick-action-icon">
                                        ◇
                                    </span>

                                    <div>

                                        <strong>
                                            Check Waitlist
                                        </strong>

                                        <p>
                                            View your current
                                            positions
                                        </p>

                                    </div>

                                    <span className="quick-arrow">
                                        →
                                    </span>

                                </button>

                            </div>

                        </article>


                        {/* =============================
                            AVAILABLE SLOTS
                        ============================== */}

                        <article className="dashboard-panel available-panel">

                            <div className="panel-heading">

                                <div>

                                    <span className="panel-label">
                                        DISCOVER
                                    </span>

                                    <h3>
                                        Available Slots
                                    </h3>

                                </div>


                                <button
                                    type="button"
                                    className="text-button"
                                    onClick={() =>
                                        navigate("/student/slots")
                                    }
                                >
                                    Browse all →
                                </button>

                            </div>


                            <div className="empty-dashboard-state small">

                                <div className="empty-state-icon">
                                    ▣
                                </div>

                                <h4>
                                    Find your next office hour
                                </h4>

                                <p>
                                    Available professor slots will
                                    appear here once we connect the
                                    dashboard to the booking API.
                                </p>

                            </div>

                        </article>


                        {/* =============================
                            WAITLIST
                        ============================== */}

                        <article className="dashboard-panel waitlist-panel">

                            <div className="panel-heading">

                                <div>

                                    <span className="panel-label">
                                        WAITLIST
                                    </span>

                                    <h3>
                                        Your Waitlists
                                    </h3>

                                </div>


                                <button
                                    type="button"
                                    className="text-button"
                                    onClick={() =>
                                        navigate("/student/waitlist")
                                    }
                                >
                                    View all →
                                </button>

                            </div>


                            <div className="empty-dashboard-state small">

                                <div className="empty-state-icon">
                                    ◇
                                </div>

                                <h4>
                                    You're not waiting
                                </h4>

                                <p>
                                    If a slot is full, you can join
                                    its waitlist and track your
                                    position here.
                                </p>

                            </div>

                        </article>

                    </section>

                </main>

            </div>

        </div>
    );
}

export default StudentDashboard;