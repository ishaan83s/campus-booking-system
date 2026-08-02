import authService from "../../services/authService";

function ProfessorDashboard() {

    const user = authService.getUser();

    return (
        <div>
            <h1>Professor Dashboard</h1>

            <h2>
                Welcome, {user?.fullName || "Professor"}
            </h2>

            <p>
                Email: {user?.email}
            </p>

            <p>
                Role: {user?.role}
            </p>
        </div>
    );
}

export default ProfessorDashboard;