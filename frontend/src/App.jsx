import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import StudentDashboard from "./pages/student/StudentDashboard";
import AvailableSlots from "./pages/booking/AvailableSlots";

import ProfessorDashboard from "./pages/professor/ProfessorDashboard";


function App() {

    return (

        <BrowserRouter>

            <Routes>

                {/* =========================
                    DEFAULT ROUTE
                ========================== */}

                <Route
                    path="/"
                    element={
                        <Navigate
                            to="/login"
                            replace
                        />
                    }
                />


                {/* =========================
                    AUTH ROUTES
                ========================== */}

                <Route
                    path="/login"
                    element={<Login />}
                />


                <Route
                    path="/register"
                    element={<Register />}
                />


                {/* =========================
                    STUDENT ROUTES
                ========================== */}

                <Route
                    path="/student/dashboard"
                    element={<StudentDashboard />}
                />


                <Route
                    path="/student/slots"
                    element={<AvailableSlots />}
                />


                {/* =========================
                    PROFESSOR ROUTES
                ========================== */}

                <Route
                    path="/professor/dashboard"
                    element={<ProfessorDashboard />}
                />


                {/* =========================
                    UNKNOWN ROUTE
                ========================== */}

                <Route
                    path="*"
                    element={
                        <Navigate
                            to="/login"
                            replace
                        />
                    }
                />

            </Routes>

        </BrowserRouter>
    );
}

export default App;