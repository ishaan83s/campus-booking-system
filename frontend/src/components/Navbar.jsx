import authService from "../services/authService";

function Navbar({ darkMode, toggleTheme, title = "Dashboard" }) {

    const user = authService.getUser();

    const getFirstName = () => {

        if (!user?.fullName) {
            return "Student";
        }

        return user.fullName.split(" ")[0];
    };


    const getInitial = () => {

        if (!user?.fullName) {
            return "S";
        }

        return user.fullName.charAt(0).toUpperCase();
    };


    return (

        <header className="dashboard-navbar">

            {/* =========================================
                LEFT SIDE
            ========================================== */}

            <div className="navbar-left">

                <p className="navbar-breadcrumb">
                    CAMPUS BOOKING
                    <span> / </span>
                    {title}
                </p>

                <h1>
                    {title}
                </h1>

                <p className="navbar-welcome">
                    Welcome back,{" "}
                    <strong>
                        {getFirstName()}
                    </strong>
                    !
                </p>

            </div>


            {/* =========================================
                RIGHT SIDE
            ========================================== */}

            <div className="navbar-right">


                {/* THEME TOGGLE */}

                <button
                    type="button"
                    className={`dashboard-theme-toggle ${
                        darkMode ? "dark-active" : ""
                    }`}
                    onClick={toggleTheme}
                    aria-label="Toggle light and dark mode"
                >

                    <span className="dashboard-sun">
                        ☀
                    </span>

                    <span className="dashboard-toggle-ball"></span>

                    <span className="dashboard-moon">
                        ☾
                    </span>

                </button>


                {/* NOTIFICATION */}

                <button
                    type="button"
                    className="notification-button"
                    aria-label="Notifications"
                >

                    <span>
                        ♢
                    </span>

                    <span className="notification-dot"></span>

                </button>


                {/* USER */}

                <div className="navbar-user">

                    <div className="navbar-user-text">

                        <strong>
                            {user?.fullName || "Student"}
                        </strong>

                        <span>
                            {user?.email || ""}
                        </span>

                    </div>


                    <div className="navbar-avatar">

                        {getInitial()}

                    </div>

                </div>

            </div>

        </header>
    );
}

export default Navbar;