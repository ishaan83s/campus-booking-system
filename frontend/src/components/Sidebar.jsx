import { useLocation, useNavigate } from "react-router-dom";
import authService from "../services/authService";

function Sidebar() {

    const navigate = useNavigate();
    const location = useLocation();

    const user = authService.getUser();

    const isActive = (path) => {
        return location.pathname === path;
    };

    const handleLogout = () => {

        authService.logout();

        navigate("/login");
    };


    return (

        <aside className="dashboard-sidebar">

            {/* =========================================
                BRAND
            ========================================== */}

            <div className="sidebar-brand">

                <div className="sidebar-logo">

                    <div className="sidebar-book">

                        <span className="sidebar-book-left"></span>
                        <span className="sidebar-book-right"></span>

                        <span className="sidebar-clock">
                            ◷
                        </span>

                    </div>

                </div>


                <div className="sidebar-brand-name">

                    <span>
                        CAMPUS
                    </span>

                    <strong>
                        BOOKING
                    </strong>

                </div>

            </div>


            {/* =========================================
                USER
            ========================================== */}

            <div className="sidebar-user">

                <div className="sidebar-avatar">

                    {user?.fullName
                        ? user.fullName.charAt(0).toUpperCase()
                        : "S"
                    }

                </div>


                <div className="sidebar-user-info">

                    <strong>
                        {user?.fullName || "Student"}
                    </strong>

                    <span>
                        Student
                    </span>

                </div>

            </div>


            {/* =========================================
                MENU LABEL
            ========================================== */}

            <p className="sidebar-menu-label">
                MAIN MENU
            </p>


            {/* =========================================
                NAVIGATION
            ========================================== */}

            <nav className="sidebar-navigation">


                {/* DASHBOARD */}

                <button
                    type="button"
                    className={
                        isActive("/student/dashboard")
                            ? "sidebar-nav-item active"
                            : "sidebar-nav-item"
                    }
                    onClick={() =>
                        navigate("/student/dashboard")
                    }
                >

                    <span className="sidebar-nav-icon">
                        ◫
                    </span>

                    <span>
                        Dashboard
                    </span>

                </button>


                {/* AVAILABLE SLOTS */}

                <button
                    type="button"
                    className={
                        isActive("/student/slots")
                            ? "sidebar-nav-item active"
                            : "sidebar-nav-item"
                    }
                    onClick={() =>
                        navigate("/student/slots")
                    }
                >

                    <span className="sidebar-nav-icon">
                        ◷
                    </span>

                    <span>
                        Available Slots
                    </span>

                </button>


                {/* BOOKINGS */}

                <button
                    type="button"
                    className={
                        isActive("/student/bookings")
                            ? "sidebar-nav-item active"
                            : "sidebar-nav-item"
                    }
                    onClick={() =>
                        navigate("/student/bookings")
                    }
                >

                    <span className="sidebar-nav-icon">
                        ▣
                    </span>

                    <span>
                        My Bookings
                    </span>

                </button>


                {/* WAITLIST */}

                <button
                    type="button"
                    className={
                        isActive("/student/waitlist")
                            ? "sidebar-nav-item active"
                            : "sidebar-nav-item"
                    }
                    onClick={() =>
                        navigate("/student/waitlist")
                    }
                >

                    <span className="sidebar-nav-icon">
                        ◇
                    </span>

                    <span>
                        My Waitlist
                    </span>

                </button>


                {/* PROFILE */}

                <button
                    type="button"
                    className={
                        isActive("/student/profile")
                            ? "sidebar-nav-item active"
                            : "sidebar-nav-item"
                    }
                    onClick={() =>
                        navigate("/student/profile")
                    }
                >

                    <span className="sidebar-nav-icon">
                        ♙
                    </span>

                    <span>
                        My Profile
                    </span>

                </button>

            </nav>


            {/* =========================================
                BOTTOM AREA
            ========================================== */}

            <div className="sidebar-bottom">


                <div className="sidebar-help-card">

                    <div className="sidebar-help-icon">
                        ?
                    </div>

                    <div>

                        <strong>
                            Need help?
                        </strong>

                        <p>
                            Campus Booking Support
                        </p>

                    </div>

                </div>


                {/* LOGOUT */}

                <button
                    type="button"
                    className="sidebar-logout"
                    onClick={handleLogout}
                >

                    <span>
                        ↪
                    </span>

                    Logout

                </button>

            </div>

        </aside>
    );
}

export default Sidebar;