import api from "./api";

const professorService = {

    // ==========================================
    // GET ALL AVAILABLE SLOTS
    // ==========================================

    getAvailableSlots: async () => {

        const response = await api.get(
            "/professors/slots/available"
        );

        return response.data;
    },


    // ==========================================
    // GET PROFESSOR PROFILE
    // ==========================================

    getProfessor: async (professorId) => {

        const response = await api.get(
            `/professors/${professorId}`
        );

        return response.data;
    },


    // ==========================================
    // GET ONE PROFESSOR'S SLOTS
    // ==========================================

    getProfessorSlots: async (professorId) => {

        const response = await api.get(
            `/professors/${professorId}/slots`
        );

        return response.data;
    },


    // ==========================================
    // CREATE PROFESSOR SLOT
    // ==========================================

    createSlot: async (professorId, slotData) => {

        const response = await api.post(
            `/professors/${professorId}/slots`,
            slotData
        );

        return response.data;
    }

};

export default professorService;