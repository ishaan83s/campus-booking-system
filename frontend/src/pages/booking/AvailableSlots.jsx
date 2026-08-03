import { useEffect, useState } from "react";

import Sidebar from "../../components/Sidebar";
import Navbar from "../../components/Navbar";

import professorService from "../../services/professorService";

import "../../styles/dashboard.css";


function AvailableSlots() {

    // =====================================================
    // STATE
    // =====================================================

    const [slots, setSlots] = useState([]);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");

    const [darkMode, setDarkMode] = useState(false);


    // =====================================================
    // LOAD SAVED THEME
    // =====================================================

    useEffect(() => {

        const savedTheme =
            localStorage.getItem("campus-theme");

        if (savedTheme === "dark") {
            setDarkMode(true);
        }

    }, []);


    // =====================================================
    // LOAD AVAILABLE SLOTS
    // =====================================================

    useEffect(() => {

        loadAvailableSlots();

    }, []);


    const loadAvailableSlots = async () => {

        try {

            setLoading(true);

            setError("");

            const data =
                await professorService.getAvailableSlots();

            setSlots(data);

        } catch (err) {

            console.error(
                "Error loading available slots:",
                err
            );

            setError(
                "Unable to load available slots. Please try again."
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // THEME
    // =====================================================

    const toggleTheme = () => {

        const newMode = !darkMode;

        setDarkMode(newMode);

        localStorage.setItem(
            "campus-theme",
            newMode ? "dark" : "light"
        );
    };


    // =====================================================
    // DATE FORMAT
    // =====================================================

    const formatDate = (date) => {

        if (!date) {
            return "";
        }

        const formattedDate =
            new Date(`${date}T00:00:00`);

        return formattedDate.toLocaleDateString(
            "en-IN",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );
    };


    // =====================================================
    // TIME FORMAT
    // =====================================================

    const formatTime = (time) => {

        if (!time) {
            return "";
        }

        const [hours, minutes] =
            time.split(":");

        const date = new Date();

        date.setHours(
            Number(hours),
            Number(minutes),
            0
        );

        return date.toLocaleTimeString(
            "en-IN",
            {
                hour: "2-digit",
                minute: "2-digit",
                hour12: true
            }
        );
    };


    // =====================================================
    // AVAILABLE SEATS
    // =====================================================

    const getAvailableSeats = (slot) => {

        return Math.max(
            0,
            slot.capacity - slot.bookedCount
        );
    };


    // =====================================================
    // BOOK BUTTON
    // =====================================================

    const handleBookSlot = (slot) => {

        /*
         * We have NOT connected the booking API yet.
         *
         * So we deliberately do not create fake bookings
         * from the frontend.
         */

        console.log(
            "Selected slot:",
            slot
        );

        alert(
            `You selected ${slot.professorName}'s slot.\n\n` +
            `Booking functionality will be connected next.`
        );
    };


    // =====================================================
    // PAGE
    // =====================================================

    return (

        <div
            className={`dashboard-page ${
                darkMode
                    ? "dashboard-dark"
                    : "dashboard-light"
            }`}
        >

            <Sidebar />


            <div className="dashboard-main">

                <Navbar
                    darkMode={darkMode}
                    toggleTheme={toggleTheme}
                    title="Available Slots"
                />


                <main className="dashboard-content">


                    {/* =====================================
                        PAGE INTRODUCTION
                    ====================================== */}

                    <section className="slots-page-header">

                        <div>

                            <p className="slots-page-label">
                                OFFICE HOURS
                            </p>

                            <h2>
                                Find your professor.
                                <span>
                                    {" "}Book your time.
                                </span>
                            </h2>

                            <p className="slots-page-description">
                                Browse available office-hour
                                sessions and choose a time that
                                works for you.
                            </p>

                        </div>


                        <button
                            type="button"
                            className="refresh-slots-button"
                            onClick={loadAvailableSlots}
                            disabled={loading}
                        >

                            ↻

                            {loading
                                ? "Refreshing..."
                                : "Refresh Slots"
                            }

                        </button>

                    </section>


                    {/* =====================================
                        SUMMARY
                    ====================================== */}

                    <section className="slots-summary">

                        <div className="slots-summary-icon">
                            ◷
                        </div>

                        <div>

                            <strong>
                                {slots.length}
                            </strong>

                            <span>
                                available{" "}
                                {slots.length === 1
                                    ? "slot"
                                    : "slots"
                                }
                            </span>

                        </div>

                    </section>


                    {/* =====================================
                        LOADING
                    ====================================== */}

                    {loading && (

                        <section className="slots-status-card">

                            <div className="slots-loader"></div>

                            <h3>
                                Finding available slots...
                            </h3>

                            <p>
                                Checking professor office-hour
                                availability.
                            </p>

                        </section>

                    )}


                    {/* =====================================
                        ERROR
                    ====================================== */}

                    {!loading && error && (

                        <section className="slots-status-card">

                            <div className="slots-status-icon">
                                !
                            </div>

                            <h3>
                                Something went wrong
                            </h3>

                            <p>
                                {error}
                            </p>

                            <button
                                type="button"
                                onClick={loadAvailableSlots}
                            >
                                Try Again
                            </button>

                        </section>

                    )}


                    {/* =====================================
                        EMPTY
                    ====================================== */}

                    {!loading &&
                        !error &&
                        slots.length === 0 && (

                            <section className="slots-status-card">

                                <div className="slots-status-icon">
                                    ◷
                                </div>

                                <h3>
                                    No slots available right now
                                </h3>

                                <p>
                                    Professors haven't published any
                                    available office-hour slots yet.
                                    Check again later.
                                </p>

                                <button
                                    type="button"
                                    onClick={loadAvailableSlots}
                                >
                                    Refresh
                                </button>

                            </section>

                        )}


                    {/* =====================================
                        SLOT CARDS
                    ====================================== */}

                    {!loading &&
                        !error &&
                        slots.length > 0 && (

                            <section className="available-slots-grid">

                                {slots.map((slot) => {

                                    const availableSeats =
                                        getAvailableSeats(slot);

                                    return (

                                        <article
                                            className="available-slot-card"
                                            key={slot.id}
                                        >

                                            {/* PROFESSOR */}

                                            <div className="slot-professor">

                                                <div className="slot-professor-avatar">

                                                    {slot.professorName
                                                            ?.charAt(0)
                                                            .toUpperCase() ||
                                                        "P"
                                                    }

                                                </div>


                                                <div className="slot-professor-info">

                                                <span>
                                                    PROFESSOR
                                                </span>

                                                    <h3>
                                                        {
                                                            slot.professorName
                                                        }
                                                    </h3>

                                                    <p>
                                                        {
                                                            slot.department
                                                        }
                                                    </p>

                                                </div>


                                                <div className="slot-status">

                                                    {
                                                        slot.status
                                                    }

                                                </div>

                                            </div>


                                            {/* DIVIDER */}

                                            <div className="slot-divider"></div>


                                            {/* DETAILS */}

                                            <div className="slot-details">


                                                <div className="slot-detail">

                                                <span className="slot-detail-icon">
                                                    ▣
                                                </span>

                                                    <div>

                                                    <span>
                                                        DATE
                                                    </span>

                                                        <strong>
                                                            {
                                                                formatDate(
                                                                    slot.slotDate
                                                                )
                                                            }
                                                        </strong>

                                                    </div>

                                                </div>


                                                <div className="slot-detail">

                                                <span className="slot-detail-icon">
                                                    ◷
                                                </span>

                                                    <div>

                                                    <span>
                                                        TIME
                                                    </span>

                                                        <strong>
                                                            {
                                                                formatTime(
                                                                    slot.startTime
                                                                )
                                                            }
                                                            {" – "}
                                                            {
                                                                formatTime(
                                                                    slot.endTime
                                                                )
                                                            }
                                                        </strong>

                                                    </div>

                                                </div>


                                                <div className="slot-detail">

                                                <span className="slot-detail-icon">
                                                    ⌂
                                                </span>

                                                    <div>

                                                    <span>
                                                        LOCATION
                                                    </span>

                                                        <strong>
                                                            {
                                                                slot.officeLocation ||
                                                                "Campus"
                                                            }
                                                        </strong>

                                                    </div>

                                                </div>

                                            </div>


                                            {/* SEATS */}

                                            <div className="slot-seats">

                                                <div>

                                                <span>
                                                    AVAILABLE SEATS
                                                </span>

                                                    <strong>
                                                        {availableSeats}
                                                        {" / "}
                                                        {slot.capacity}
                                                    </strong>

                                                </div>


                                                <div className="seat-progress">

                                                    <div
                                                        className="seat-progress-fill"
                                                        style={{
                                                            width:
                                                                `${
                                                                    slot.capacity > 0
                                                                        ? (
                                                                        availableSeats /
                                                                        slot.capacity
                                                                    ) * 100
                                                                        : 0
                                                                }%`
                                                        }}
                                                    ></div>

                                                </div>

                                            </div>


                                            {/* BOOK */}

                                            <button
                                                type="button"
                                                className="book-slot-button"
                                                disabled={
                                                    availableSeats <= 0
                                                }
                                                onClick={() =>
                                                    handleBookSlot(slot)
                                                }
                                            >

                                                {availableSeats > 0
                                                    ? "BOOK THIS SLOT"
                                                    : "SLOT FULL"
                                                }

                                                {availableSeats > 0 && (
                                                    <span>
                                                    →
                                                </span>
                                                )}

                                            </button>

                                        </article>

                                    );
                                })}

                            </section>

                        )}

                </main>

            </div>

        </div>
    );
}

export default AvailableSlots;